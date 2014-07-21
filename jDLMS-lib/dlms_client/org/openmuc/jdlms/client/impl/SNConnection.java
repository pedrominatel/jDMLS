/*
 * Copyright 2012-13 Fraunhofer ISE
 *
 * This file is part of jDLMS.
 * For more information visit http://www.openmuc.org
 *
 * jDLMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * jDLMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with jDLMS.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.openmuc.jdlms.client.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.openmuc.asn1.cosem.COSEMpdu;
import org.openmuc.asn1.cosem.Conformance;
import org.openmuc.asn1.cosem.Cosem_Attribute_Descriptor;
import org.openmuc.asn1.cosem.Cosem_Date_Time;
import org.openmuc.asn1.cosem.Cosem_Object_Instance_Id;
import org.openmuc.asn1.cosem.EVENT_NOTIFICATION_Request;
import org.openmuc.asn1.cosem.InformationReportRequest;
import org.openmuc.asn1.cosem.Integer16;
import org.openmuc.asn1.cosem.Integer8;
import org.openmuc.asn1.cosem.ReadRequest;
import org.openmuc.asn1.cosem.ReadResponse;
import org.openmuc.asn1.cosem.UnconfirmedWriteRequest;
import org.openmuc.asn1.cosem.Unsigned16;
import org.openmuc.asn1.cosem.Variable_Access_Specification;
import org.openmuc.asn1.cosem.WriteRequest;
import org.openmuc.asn1.cosem.WriteResponse;
import org.openmuc.jasn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.client.AccessResultCode;
import org.openmuc.jdlms.client.Data;
import org.openmuc.jdlms.client.EventNotification;
import org.openmuc.jdlms.client.GetRequest;
import org.openmuc.jdlms.client.GetResult;
import org.openmuc.jdlms.client.HlsSecretProcessor;
import org.openmuc.jdlms.client.IAttributeRequest;
import org.openmuc.jdlms.client.MethodRequest;
import org.openmuc.jdlms.client.MethodResult;
import org.openmuc.jdlms.client.MethodResultCode;
import org.openmuc.jdlms.client.ObisCode;
import org.openmuc.jdlms.client.SetRequest;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.cosem.context.ApplicationContext;
import org.openmuc.jdlms.client.cosem.context.MechanismName;
import org.openmuc.jdlms.util.ConformanceHelper;
import org.openmuc.jdlms.util.QueueHelper;

/**
 * Variant of the connection class using unencrypted messages with short name referencing to communicate with the remote
 * smart meter
 * 
 * @author Karsten Mueller-Bier
 */
public class SNConnection extends Connection {

	private class ObjectInfo {
		private final int baseName;
		private final int classId;
		private final int version;

		public ObjectInfo(int baseName, int classId, int version) {
			this.baseName = baseName;
			this.classId = classId;
			this.version = version;
		}

		public int getBaseName() {
			return baseName;
		}

		public int getClassId() {
			return classId;
		}

		public int getVersion() {
			return version;
		}
	}

	// Allow read/write
	// Allow unconfirmed write
	// Allow information report
	// Allow multiple references
	// Allow parameterized access
	/**
	 * Bit field containing all operations this client can perform
	 */
	private static Conformance PROPOSED_CONFORMANCE = new Conformance(new byte[] { (byte) 0x1C, (byte) 0x03,
			(byte) 0x20 }, 24);

	private static long DEFAULT_TIMEOUT = 30000;

	/**
	 * Short name referring to the list of all accessible Cosem Objects on the smart meter
	 */
	private static Integer16 ASSOCIATION_OBJECT_LIST = new Integer16((short) 0xFA08);

	private final Map<String, ObjectInfo> lnMapping = new LinkedHashMap<String, ObjectInfo>();
	private volatile boolean isMapInitialized = false;

	private final BlockingQueue<ReadResponse> readResponseQueue = new ArrayBlockingQueue<ReadResponse>(3);
	private final BlockingQueue<WriteResponse> writeResponseQueue = new ArrayBlockingQueue<WriteResponse>(3);

	public SNConnection(boolean confirmedMode, MechanismName authName, ILowerLayer<Object> lowerLayer,
			ConnectModule connectModule) {
		super(confirmedMode, authName, ApplicationContext.SHORT_NAME_NO_CIPHERING, lowerLayer, connectModule);
	}

	@Override
	public void connect(long timeout) throws IOException {
		connect(timeout, null, null);
	}

	@Override
	public void connect(long timeout, byte[] secret) throws IOException {
		connect(timeout, secret, null);
	}

	public void connect(byte[] secret, HlsSecretProcessor processor) throws IOException {
		connect(0, secret, processor);
	}

	/**
	 * Establishes connection using the normal methods and checks if the remote smart meter supports any of the proposed
	 * functions
	 */
	@Override
	public void connect(long timeout, byte[] secret, HlsSecretProcessor processor) throws IOException {
		establishConnection(timeout, secret, processor);
		// If the first byte of the Conformance bit string is 0, then neither
		// read nor write are allowed, a sign that this smart meter cannot
		// communicate with LN referencing.
		if (getNegotiatedFeatures().bitString[0] == 0) {
			disconnect(true);
			throw new IOException("Wrong referencing method. Remote smart meter +" + "can't use SN referencing");
		}
	}

	@Override
	public List<GetResult> get(long timeout, boolean highPriority, GetRequest... params) throws IOException {
		List<Variable_Access_Specification> shortNames = getVariableList(timeout, Arrays.asList(params));

		ReadRequest request = new ReadRequest();
		for (Variable_Access_Specification name : shortNames) {
			request.add(name);
		}

		COSEMpdu pdu = new COSEMpdu();
		pdu.setreadRequest(request);
		send(pdu);

		ReadResponse response = null;
		try {
			response = QueueHelper.waitPoll(readResponseQueue, timeout);
			if (response == null) {
				receiveTimedOut(pdu);
				throw new IOException("Device not responding");
			}
		} catch (InterruptedException e) {
			receiveTimedOut(pdu);
			//TODO LoggingHelper.logStackTrace(e, logger);
			throw new IOException("Interrupted while waiting for incoming response");
		}

		List<GetResult> result = new LinkedList<GetResult>();
		for (ReadResponse.SubChoice data : response.list()) {
			GetResult resultItem;

			if (data.getChoiceIndex() == ReadResponse.SubChoice.Choices.DATA) {
				Data dat = DataConverter.toApi(data.data);
				resultItem = new GetResult(dat);
			}
			else {
				resultItem = new GetResult(AccessResultCode.fromValue((int) data.data_access_error.getValue()));
			}

			result.add(resultItem);
		}

		return result;
	}

	@Override
	public List<AccessResultCode> set(long timeout, boolean highPriority, SetRequest... params) throws IOException {
		List<Variable_Access_Specification> shortNames = getVariableList(timeout, Arrays.asList(params));

		List<AccessResultCode> result = null;

		if (isConfirmedMode()) {
			WriteRequest request = new WriteRequest();
			request.list_of_data = new WriteRequest.SubSeqOf_list_of_data();
			request.variable_access_specification = new WriteRequest.SubSeqOf_variable_access_specification();
			for (int i = 0; i < params.length; i++) {
				request.variable_access_specification.add(shortNames.get(i));
				request.list_of_data.add(DataConverter.toPdu(params[i].data()));
			}

			COSEMpdu pdu = new COSEMpdu();
			pdu.setwriteRequest(request);
			send(pdu);

			WriteResponse response = null;
			try {
				response = QueueHelper.waitPoll(writeResponseQueue, timeout);
			} catch (InterruptedException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
				throw new IOException("Interrupted while waiting for incoming response");
			}

			result = new LinkedList<AccessResultCode>();
			for (WriteResponse.SubChoice data : response.list()) {
				AccessResultCode item;
				if (data.getChoiceIndex() == WriteResponse.SubChoice.Choices.SUCCESS) {
					item = AccessResultCode.SUCCESS;
				}
				else {
					item = AccessResultCode.fromValue((int) data.data_access_error.getValue());
				}
				result.add(item);
			}
		}
		else {
			UnconfirmedWriteRequest request = new UnconfirmedWriteRequest();
			for (int i = 0; i < params.length; i++) {
				request.variable_access_specification.add(shortNames.get(i));
				request.list_of_data.add(DataConverter.toPdu(params[i].data()));
			}

			COSEMpdu pdu = new COSEMpdu();
			pdu.setunconfirmedWriteRequest(request);
			send(pdu);
		}

		return result;
	}

	@Override
	public List<MethodResult> action(long timeout, boolean highPriority, MethodRequest... params) throws IOException {
		if (params.length > 1 && ConformanceHelper.isMultipleReferenceAllowed(getNegotiatedFeatures())) {
			throw new IllegalArgumentException("Connection does not allow calling multiple methods in one call");
		}

		if (isMapInitialized == false) {
			initializeLnMap(DEFAULT_TIMEOUT);
		}

		List<MethodResult> result = null;

		if (isConfirmedMode()) {
			ReadRequest methodsWithReturn = new ReadRequest();
			WriteRequest methodsWithoutReturn = new WriteRequest();

			// This list is used to undo the following optimization after all
			// methods have been invoked
			List<Boolean> returnList = new ArrayList<Boolean>(params.length);

			// To optimize network load, split requested methods
			// into methods with return value and methods without
			for (MethodRequest param : params) {
				Variable_Access_Specification access;

				ObjectInfo objectInfo = lnMapping.get(param.getObisCode());
				InterfaceClass classInfo = InterfaceClassList.getClassInfo(objectInfo.classId, objectInfo.version);

				access = new Variable_Access_Specification();
				Integer16 variableName = new Integer16(classInfo.getFirstOffset() + 8 * (param.getMethodId() - 1));

				returnList.add(classInfo.hasReturnType(param.getMethodId()));

				if (classInfo.hasReturnType(param.getMethodId())) {

					if (param.data() == null) {
						access.setvariable_name(variableName);
					}
					else {
						Variable_Access_Specification.SubSeq_parameterized_access accessParam = new Variable_Access_Specification.SubSeq_parameterized_access(
								variableName, new Integer8(0), DataConverter.toPdu(param.data()));
						access.setparameterized_access(accessParam);
					}

					methodsWithReturn.add(access);
				}
				else {
					access.setvariable_name(variableName);
					methodsWithoutReturn.variable_access_specification.add(access);
					methodsWithoutReturn.list_of_data.add(DataConverter.toPdu(param.data()));
				}
			}

			COSEMpdu pdu = new COSEMpdu();
			WriteResponse responseWithoutReturn = null;
			ReadResponse responseWithReturn = null;

			// If there are methods with return value requested, send all of
			// them now
			if (methodsWithoutReturn.variable_access_specification.size() > 0) {
				pdu.setwriteRequest(methodsWithoutReturn);
				send(pdu);

				try {
					responseWithoutReturn = QueueHelper.waitPoll(writeResponseQueue, timeout);
				} catch (InterruptedException e) {
					//TODO LoggingHelper.logStackTrace(e, logger);
					throw new IOException("Interrupted while waiting for incoming response");
				}
			}

			// If there are methods without return value requested, send all of
			// them now
			if (methodsWithReturn.size() > 0) {
				pdu.setreadRequest(methodsWithReturn);
				send(pdu);

				try {
					responseWithReturn = QueueHelper.waitPoll(readResponseQueue, timeout);
				} catch (InterruptedException e) {
					//TODO LoggingHelper.logStackTrace(e, logger);
					throw new IOException("Interrupted while waiting for incoming response");
				}
			}

			result = new ArrayList<MethodResult>(params.length);
			int responseWithReturnIndex = 0;
			int responseWithoutReturnIndex = 0;

			// Undo earlier split into methods with and without return value, so
			// the order of result items matches the order of called methods
			for (Boolean withReturn : returnList) {
				MethodResultCode resultCode = null;
				Data returnValue = null;
				if (withReturn) {
					if (responseWithReturn.get(responseWithReturnIndex).getChoiceIndex() == ReadResponse.SubChoice.Choices.DATA) {
						returnValue = DataConverter.toApi(responseWithReturn.get(responseWithReturnIndex).data);
						resultCode = MethodResultCode.SUCCESS;
					}
					else {
						resultCode = MethodResultCode
								.fromValue((int) responseWithReturn.get(responseWithReturnIndex).data_access_error
										.getValue());
					}
					responseWithReturnIndex++;
				}
				else {
					if (responseWithoutReturn.get(responseWithoutReturnIndex).getChoiceIndex() == WriteResponse.SubChoice.Choices.DATA_ACCESS_ERROR) {
						resultCode = MethodResultCode.fromValue((int) responseWithoutReturn
								.get(responseWithoutReturnIndex).data_access_error.getValue());
					}
					else {
						resultCode = MethodResultCode.SUCCESS;
					}
					responseWithoutReturnIndex++;
				}

				result.add(new MethodResult(resultCode, returnValue));
			}
		}
		else {
			// Unconfirmed connection mode
			UnconfirmedWriteRequest request = new UnconfirmedWriteRequest();
			for (MethodRequest param : params) {
				Variable_Access_Specification access;

				ObjectInfo objectInfo = lnMapping.get(param.getObisCode());
				InterfaceClass classInfo = InterfaceClassList.getClassInfo(objectInfo.classId, objectInfo.version);

				Integer16 variableName = new Integer16(classInfo.getFirstOffset() + 8 * (param.getMethodId() - 1));

				access = new Variable_Access_Specification();
				access.setvariable_name(variableName);
				request.variable_access_specification.add(access);
				request.list_of_data.add(DataConverter.toPdu(param.data()));
			}

			COSEMpdu pdu = new COSEMpdu();
			pdu.setunconfirmedWriteRequest(request);
			send(pdu);
		}

		return result;
	}

	@Override
	public void processPdu(COSEMpdu pdu) {
		try {
			if (pdu.getChoiceIndex() == COSEMpdu.Choices.READRESPONSE) {
				readResponseQueue.put(pdu.readResponse);
			}
			else if (pdu.getChoiceIndex() == COSEMpdu.Choices.WRITERESPONSE) {
				writeResponseQueue.put(pdu.writeResponse);
			}
			else if (pdu.getChoiceIndex() == COSEMpdu.Choices.INFORMATIONREPORTREQUEST) {
				if (getEventListener() != null) {
					List<EVENT_NOTIFICATION_Request> eventList = transformEventPdu(pdu.informationReportRequest);
					for (EVENT_NOTIFICATION_Request event : eventList) {
						EventNotification notification = DataConverter.toApi(event);
						getEventListener().eventReceived(notification);
					}
				}
			}
		} catch (InterruptedException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		}
	}

	@Override
	protected Conformance getProposedConformance() {
		return PROPOSED_CONFORMANCE;
	}

	private List<Variable_Access_Specification> getVariableList(long timeout, List<? extends IAttributeRequest> params)
			throws IOException {
		if (params == null || params.size() == 0) {
			throw new IllegalArgumentException("No parameter provided");
		}
		if (isConnected() == false) {
			throw new IOException("Connection closed. Call connect first");
		}
		if (params.size() > 1 && ConformanceHelper.isMultipleReferenceAllowed(getNegotiatedFeatures()) == false) {
			throw new IllegalArgumentException("Connection does not allow access to multiple parameters in one call");
		}

		List<Variable_Access_Specification> result = new ArrayList<Variable_Access_Specification>(params.size());

		for (IAttributeRequest param : params) {
			String obisCode = param.getObisCode();
			if (lnMapping.containsKey(obisCode) == false) {
				if (isMapInitialized) {
					throw new InvalidParameterException("Object " + obisCode + " unknown to smart meter");
				}

				try {
					ObjectInfo objectInfo = getVariableInfo(timeout, param);
					lnMapping.put(obisCode, objectInfo);
				} catch (IOException e) {
					initializeLnMap(DEFAULT_TIMEOUT);
				}
			}

			Variable_Access_Specification accessSpec = new Variable_Access_Specification();
			ObjectInfo info = lnMapping.get(obisCode);

			Integer16 variableName = new Integer16(info.getBaseName() + 8 * (param.getAttributeId() - 1));
			if (param.getAccessSelection() == null) {
				accessSpec.setvariable_name(variableName);
			}
			else {
				if (ConformanceHelper.isParameterizedAccessAllowed(getNegotiatedFeatures()) == false) {
					throw new IllegalArgumentException("Connection doesn't allow access selection");
				}
				accessSpec.setparameterized_access(new Variable_Access_Specification.SubSeq_parameterized_access(
						variableName, new Integer8(param.getAccessSelection().getSelector()), DataConverter.toPdu(param
								.getAccessSelection().getParameter())));
			}

			result.add(accessSpec);
		}

		return result;
	}

	private ObjectInfo getVariableInfo(long timeout, IAttributeRequest param) throws IOException {
		if (ConformanceHelper.isParameterizedAccessAllowed(getNegotiatedFeatures()) == false) {
			throw new IOException("Connection does not allow parametrerized actions");
		}

		ReadRequest request = new ReadRequest();
		Variable_Access_Specification getBaseName = new Variable_Access_Specification();
		org.openmuc.asn1.cosem.Data filter = new org.openmuc.asn1.cosem.Data();
		filter.setstructure(new org.openmuc.asn1.cosem.Data.SubSeqOf_structure());
		filter.structure.add(new org.openmuc.asn1.cosem.Data());
		filter.structure.add(new org.openmuc.asn1.cosem.Data());
		filter.structure.get(0).setlong_unsigned(new Unsigned16(param.getClassId()));
		filter.structure.get(1).setoctet_string(
				new AxdrOctetString(new Cosem_Object_Instance_Id(param.getObisCode()).getValue()));
		Variable_Access_Specification.SubSeq_parameterized_access parametrizedAccess = new Variable_Access_Specification.SubSeq_parameterized_access(
				ASSOCIATION_OBJECT_LIST, new Integer8(2), filter);
		getBaseName.setparameterized_access(parametrizedAccess);
		request.add(getBaseName);

		COSEMpdu pdu = new COSEMpdu();
		pdu.setreadRequest(request);
		send(pdu);

		ReadResponse response = null;
		try {
			response = QueueHelper.waitPoll(readResponseQueue, timeout);
		} catch (InterruptedException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
			throw new IOException("Interrupted while waiting for incoming response");
		}

		if (response.get(0).getChoiceIndex() == ReadResponse.SubChoice.Choices.DATA_ACCESS_ERROR) {
			throw new IOException();
		}

		ObjectInfo result = new ObjectInfo(
				(int) response.get(0).data.array.get(0).structure.get(0).long_integer.getValue(),
				(int) response.get(0).data.array.get(0).structure.get(1).long_unsigned.getValue(),
				(int) response.get(0).data.array.get(0).structure.get(2).unsigned.getValue());
		return result;
	}

	private void initializeLnMap(long timeout) throws IOException {
		if (isMapInitialized == false) {
			synchronized (lnMapping) {
				if (isMapInitialized == false) {
					ReadRequest request = new ReadRequest();
					Variable_Access_Specification getObjectList = new Variable_Access_Specification();
					getObjectList.setvariable_name(ASSOCIATION_OBJECT_LIST);
					request.add(getObjectList);

					COSEMpdu pdu = new COSEMpdu();
					pdu.setreadRequest(request);
					send(pdu);

					ReadResponse response = null;
					try {
						response = QueueHelper.waitPoll(readResponseQueue, timeout);
					} catch (InterruptedException e) {
						//TODO LoggingHelper.logStackTrace(e, logger);
						throw new IOException("Interrupted while waiting for incoming response");
					}

					if (response.get(0).getChoiceIndex() == ReadResponse.SubChoice.Choices.DATA_ACCESS_ERROR) {
						throw new IOException("Error on receiving object list");
					}

					for (org.openmuc.asn1.cosem.Data object : response.get(0).data.array.list()) {
						String key = convertObjectId(object.structure.get(3).octet_string.getValue());
						ObjectInfo value = new ObjectInfo((int) object.structure.get(0).long_integer.getValue(),
								(int) object.structure.get(1).long_unsigned.getValue(),
								(int) object.structure.get(2).unsigned.getValue());
						lnMapping.put(key, value);
					}

					isMapInitialized = true;
				}
			}
		}
		return;
	}

	private String convertObjectId(byte[] objectId) {
		if (objectId.length != 6) {
			throw new IllegalArgumentException("ObjectId has wrong number of bytes. Should be 6, was "
					+ objectId.length);
		}
		StringBuilder sb = new StringBuilder();

		for (byte b : objectId) {
			String hex = Integer.toHexString(b % 0xff);
			if (hex.length() == 1) {
				sb.append("0");
			}
			sb.append(hex);
		}

		return sb.toString();
	}

	private List<EVENT_NOTIFICATION_Request> transformEventPdu(InformationReportRequest event) throws IOException {
		List<EVENT_NOTIFICATION_Request> result = new ArrayList<EVENT_NOTIFICATION_Request>(
				event.variable_access_specification.size());

		Cosem_Date_Time convertedTime = null;
		ByteArrayOutputStream oStream = new ByteArrayOutputStream(12);
		if (event.current_time.isUsed()) {
			InputStream iStream = new ByteArrayInputStream(event.current_time.getValue().getValue());
			int buffer = iStream.read() - 0x30;

			// Converting year
			buffer = buffer * 10 + iStream.read() - 0x30;
			buffer = buffer * 10 + iStream.read() - 0x30;
			buffer = buffer * 10 + iStream.read() - 0x30;
			oStream.write((byte) ((buffer >> 8) & 0xFF));
			oStream.write((byte) (buffer & 0xFF));

			// Month
			buffer = iStream.read();
			buffer = buffer * 10 + iStream.read();
			oStream.write((byte) (buffer & 0xFF));

			// Day
			buffer = iStream.read();
			buffer = buffer * 10 + iStream.read();
			oStream.write((byte) (buffer & 0xFF));

			// Day of week not specified
			oStream.write((byte) 0xFF);

			// Hour
			buffer = iStream.read();
			buffer = buffer * 10 + iStream.read();
			oStream.write((byte) (buffer & 0xFF));

			// Minute
			buffer = iStream.read();
			buffer = buffer * 10 + iStream.read();
			oStream.write((byte) (buffer & 0xFF));

			// Second
			buffer = iStream.read();
			buffer = buffer * 10 + iStream.read();
			oStream.write((byte) (buffer & 0xFF));

			// Milliseconds specified
			oStream.write((byte) 0xFF);

			// Deviation specified
			oStream.write((byte) 0x80);
			oStream.write((byte) 0x00);

			// Status no error
			oStream.write((byte) 0x00);

			convertedTime = new Cosem_Date_Time(oStream.toByteArray());
		}

		Iterator<Variable_Access_Specification> eventIter;
		Iterator<org.openmuc.asn1.cosem.Data> dataIter;

		outer: for (eventIter = event.variable_access_specification.iterator(), dataIter = event.list_of_data
				.iterator(); eventIter.hasNext() && dataIter.hasNext();) {
			Variable_Access_Specification eventInfo = eventIter.next();
			org.openmuc.asn1.cosem.Data eventData = dataIter.next();
			for (String shortNameKey : lnMapping.keySet()) {
				ObjectInfo shortNameInfo = lnMapping.get(shortNameKey);
				if (InterfaceClassList.getClassInfo(shortNameInfo.getClassId(), shortNameInfo.getVersion()).isInRange(
						(int) eventInfo.variable_name.getValue(), shortNameInfo.baseName)) {
					Cosem_Attribute_Descriptor logicalNameInfo = new Cosem_Attribute_Descriptor(new Unsigned16(
							shortNameInfo.classId), new Cosem_Object_Instance_Id(shortNameKey), new Integer8(
							(eventInfo.variable_name.getValue() - shortNameInfo.baseName) / 8 + 1));

					EVENT_NOTIFICATION_Request listItem = new EVENT_NOTIFICATION_Request();
					listItem.cosem_attribute_descriptor = logicalNameInfo;
					listItem.attribute_value = eventData;
					if (event.current_time.isUsed()) {
						listItem.time.setValue(convertedTime);
					}
					result.add(listItem);

					continue outer;
				}
			}
		}

		return result;
	}

	@Override
	public byte[] hlsAuthentication(byte[] processedChallenge, long timeout) throws IOException {
		Data param = new Data();
		param.setOctetString(processedChallenge);

		MethodRequest authenticate = new MethodRequest(12, new ObisCode(0, 0, 40, 0, 0, 255), 8, param);

		List<MethodResult> result = action(timeout, false, authenticate);

		if (result.get(0).isSuccess()) {
			return result.get(0).getResultData().getByteArray();
		}
		else {
			return null;
		}
	}
}

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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.asn1.cosem.ACTION_Request;
import org.openmuc.asn1.cosem.ACTION_Response;
import org.openmuc.asn1.cosem.Action_Request_Next_Pblock;
import org.openmuc.asn1.cosem.Action_Request_Normal;
import org.openmuc.asn1.cosem.Action_Request_With_First_Pblock;
import org.openmuc.asn1.cosem.Action_Request_With_List;
import org.openmuc.asn1.cosem.Action_Request_With_List_And_First_Pblock;
import org.openmuc.asn1.cosem.Action_Request_With_Pblock;
import org.openmuc.asn1.cosem.Action_Response_With_Optional_Data;
import org.openmuc.asn1.cosem.COSEMpdu;
import org.openmuc.asn1.cosem.Conformance;
import org.openmuc.asn1.cosem.Cosem_Attribute_Descriptor;
import org.openmuc.asn1.cosem.Cosem_Attribute_Descriptor_With_Selection;
import org.openmuc.asn1.cosem.Cosem_Method_Descriptor;
import org.openmuc.asn1.cosem.Cosem_Object_Instance_Id;
import org.openmuc.asn1.cosem.DataBlock_SA;
import org.openmuc.asn1.cosem.GET_Request;
import org.openmuc.asn1.cosem.GET_Response;
import org.openmuc.asn1.cosem.Get_Data_Result;
import org.openmuc.asn1.cosem.Get_Request_Next;
import org.openmuc.asn1.cosem.Get_Request_Normal;
import org.openmuc.asn1.cosem.Get_Request_With_List;
import org.openmuc.asn1.cosem.Integer8;
import org.openmuc.asn1.cosem.Invoke_Id_And_Priority;
import org.openmuc.asn1.cosem.SET_Request;
import org.openmuc.asn1.cosem.SET_Response;
import org.openmuc.asn1.cosem.Selective_Access_Descriptor;
import org.openmuc.asn1.cosem.Set_Request_Normal;
import org.openmuc.asn1.cosem.Set_Request_With_Datablock;
import org.openmuc.asn1.cosem.Set_Request_With_First_Datablock;
import org.openmuc.asn1.cosem.Set_Request_With_List;
import org.openmuc.asn1.cosem.Set_Request_With_List_And_First_Datablock;
import org.openmuc.asn1.cosem.Unsigned16;
import org.openmuc.asn1.cosem.Unsigned32;
import org.openmuc.asn1.cosem.Unsigned8;
import org.openmuc.jasn1.axdr.AxdrByteArrayOutputStream;
import org.openmuc.jasn1.axdr.AxdrType;
import org.openmuc.jasn1.axdr.NullOutputStream;
import org.openmuc.jasn1.axdr.types.AxdrBoolean;
import org.openmuc.jasn1.axdr.types.AxdrEnum;
import org.openmuc.jasn1.axdr.types.AxdrOctetString;
import org.openmuc.jdlms.client.AccessResultCode;
import org.openmuc.jdlms.client.Data;
import org.openmuc.jdlms.client.EventNotification;
import org.openmuc.jdlms.client.GetRequest;
import org.openmuc.jdlms.client.GetResult;
import org.openmuc.jdlms.client.HlsSecretProcessor;
import org.openmuc.jdlms.client.MethodRequest;
import org.openmuc.jdlms.client.MethodResult;
import org.openmuc.jdlms.client.MethodResultCode;
import org.openmuc.jdlms.client.ObisCode;
import org.openmuc.jdlms.client.SelectiveAccessDescription;
import org.openmuc.jdlms.client.SetRequest;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.cosem.context.ApplicationContext;
import org.openmuc.jdlms.client.cosem.context.MechanismName;
import org.openmuc.jdlms.client.cosem.context.PduHelper;
import org.openmuc.jdlms.util.ConformanceHelper;

/**
 * Variant of the connection class using unencrypted messages with logical name referencing to communicate with the
 * remote smart meter
 * 
 * @author Karsten Mueller-Bier
 */
public class LNConnection extends Connection {

	private static long DEFAULT_TIMEOUT = 30000;

	// Allow GET/SET/ACTION/EVENT
	// Allow selective access
	// Allow priority
	// Allow multiple references
	// Allow block transfer
	// Allow attribute 0 GET/SET
	/**
	 * Bit field containing all operations this client can perform
	 */
	private static Conformance PROPOSED_CONFORMANCE = new Conformance(new byte[] { (byte) 0x00, (byte) 0xBC,
			(byte) 0x3F }, 24);

	private final ResponseQueue<GET_Response> getResponseQueue = new ResponseQueue<GET_Response>();
	private final ResponseQueue<SET_Response> setResponseQueue = new ResponseQueue<SET_Response>();
	private final ResponseQueue<ACTION_Response> actionResponseQueue = new ResponseQueue<ACTION_Response>();

	public LNConnection(boolean confirmedMode, MechanismName authName, ILowerLayer<Object> lowerLayer,
			ConnectModule connectModule) {
		super(confirmedMode, authName, ApplicationContext.LOGICAL_NAME_NO_CIPHERING, lowerLayer, connectModule);
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
		// If the last byte of the Conformance bit string is 0, then neither
		// get, set nor action are allowed, a sign that this smart meter cannot
		// communicate with LN referencing.
		if ((getNegotiatedFeatures().bitString[2] & 0x1F) == 0) {
			disconnect(true);
			throw new IOException("Wrong referencing method. Remote smart meter +" + "can't use LN referencing");
		}
	}

	@Override
	public List<GetResult> get(long timeout, boolean highPriority, GetRequest... params) throws IOException {
		if (isConnected() == false) {
			throw new IOException("Connection closed. Call connect first");
		}
		if (timeout == 0) {
			timeout = DEFAULT_TIMEOUT;
		}

		Invoke_Id_And_Priority id = getInvokeIdAndPriority(highPriority);
		final int invokeId = (id.getValues()[0] & 0xF0) >>> 4;
		COSEMpdu pdu = createGetPdu(id, params);
		send(pdu);

		GET_Response response;
		try {
			response = getResponseQueue.poll(invokeId, timeout);
			if (response == null) {
				receiveTimedOut(pdu);
				throw new IOException("Device is not responding to GET");
			}
		} catch (InterruptedException e) {
			receiveTimedOut(pdu);
			//TODO LoggingHelper.logStackTrace(e, logger);
			throw new IOException("Interrupted while waiting for incoming response");
		}

		List<GetResult> result = new ArrayList<GetResult>(params.length);
		if (response.getChoiceIndex() == GET_Response.Choices.GET_RESPONSE_NORMAL) {
			GetResult res = DataConverter.toApi(response.get_response_normal.result);
			result.add(res);
		}
		else if (response.getChoiceIndex() == GET_Response.Choices.GET_RESPONSE_WITH_DATABLOCK) {
			GET_Request getRequest = new GET_Request();
			ByteArrayOutputStream datablocks = new ByteArrayOutputStream();
			Get_Request_Next nextBlock = new Get_Request_Next();
			nextBlock.invoke_id_and_priority = response.get_response_with_datablock.invoke_id_and_priority;
			while (response.get_response_with_datablock.result.last_block.getValue() == false) {
				datablocks.write(response.get_response_with_datablock.result.result.raw_data.getValue());

				nextBlock.block_number = response.get_response_with_datablock.result.block_number;
				getRequest.setget_request_next(nextBlock);
				pdu.setget_request(getRequest);
				send(pdu);

				try {
					response = getResponseQueue.poll(invokeId, timeout);
					if (response == null) {
						// Send PDU with wrong block number to indicate the device that the block transfer is
						// aborted.
						// This is the well defined behavior to abort a block transfer as in IEC 62056-53 section
						// 7.4.1.8.2
						receiveTimedOut(pdu);
						send(pdu);
						throw new IOException("Device not responding");
					}
				} catch (InterruptedException e) {
					receiveTimedOut(pdu);
					//TODO LoggingHelper.logStackTrace(e, logger);
					throw new IOException("Interrupted while waiting for incoming response");
				}
			}
			datablocks.write(response.get_response_with_datablock.result.result.raw_data.getValue());
			InputStream dataByteStream = new ByteArrayInputStream(datablocks.toByteArray());
			while (dataByteStream.available() > 0) {
				org.openmuc.asn1.cosem.Data resultPduData = new org.openmuc.asn1.cosem.Data();
				resultPduData.decode(dataByteStream);
				Get_Data_Result getResult = new Get_Data_Result();
				getResult.setdata(resultPduData);
				GetResult res = DataConverter.toApi(getResult);
				result.add(res);
			}
		}
		else if (response.getChoiceIndex() == GET_Response.Choices.GET_RESPONSE_WITH_LIST) {
			for (Get_Data_Result resultPdu : response.get_response_with_list.result.list()) {
				GetResult res = DataConverter.toApi(resultPdu);
				result.add(res);
			}
		}
		else {
			throw new UnsupportedOperationException("Unknown response type");
		}

		return result;
	}

	@Override
	public List<AccessResultCode> set(long timeout, boolean highPriority, SetRequest... params) throws IOException {
		if (isConnected() == false) {
			throw new IOException("Connection closed. Call connect first");
		}

		Invoke_Id_And_Priority id = getInvokeIdAndPriority(highPriority);
		int invokeId = getInvokeId(id);
		List<COSEMpdu> pdus = createSetPdu(id, params);
		send(pdus.remove(0));

		List<AccessResultCode> result = null;
		if (isConfirmedMode()) {
			SET_Response response = new SET_Response();
			try {
				response = setResponseQueue.poll(invokeId, timeout);
			} catch (InterruptedException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
				throw new IOException("Interrupted while waiting for incoming response");
			}

			while (response.getChoiceIndex() == SET_Response.Choices.SET_RESPONSE_DATABLOCK) {
				send(pdus.remove(0));
				try {
					response = setResponseQueue.poll(invokeId, timeout);
				} catch (InterruptedException e) {
					//TODO LoggingHelper.logStackTrace(e, logger);
					throw new IOException("Interrupted while waiting for incoming response");
				}
			}

			result = new ArrayList<AccessResultCode>(params.length);
			if (response.getChoiceIndex() == SET_Response.Choices.SET_RESPONSE_NORMAL) {
				result.add(AccessResultCode.fromValue((int) response.set_response_normal.result.getValue()));
			}
			else if (response.getChoiceIndex() == SET_Response.Choices.SET_RESPONSE_WITH_LIST) {
				for (AxdrEnum res : response.set_response_with_list.result.list()) {
					result.add(AccessResultCode.fromValue((int) res.getValue()));
				}
			}
			else if (response.getChoiceIndex() == SET_Response.Choices.SET_RESPONSE_LAST_DATABLOCK) {
				result.add(AccessResultCode.fromValue((int) response.set_response_normal.result.getValue()));
			}
			else if (response.getChoiceIndex() == SET_Response.Choices.SET_RESPONSE_LAST_DATABLOCK_WITH_LIST) {
				for (AxdrEnum res : response.set_response_last_datablock_with_list.result.list()) {
					result.add(AccessResultCode.fromValue((int) res.getValue()));
				}
			}
			else {
				throw new UnsupportedOperationException("Unknown response type");
			}
		}

		return result;
	}

	@Override
	public List<MethodResult> action(long timeout, boolean highPriority, MethodRequest... params) throws IOException {
		if (isConnected() == false) {
			throw new IOException("Connection closed. Call connect first");
		}

		Invoke_Id_And_Priority id = getInvokeIdAndPriority(highPriority);
		int invokeId = getInvokeId(id);
		List<COSEMpdu> pdus = createActionPdu(id, params);
		send(pdus.remove(0));

		List<MethodResult> result = null;
		if (isConfirmedMode()) {
			ACTION_Response response = new ACTION_Response();
			try {
				response = actionResponseQueue.poll(invokeId, timeout);
			} catch (InterruptedException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
				throw new IOException("Interrupted while waiting for incoming response");
			}

			while (response.getChoiceIndex() == ACTION_Response.Choices.ACTION_RESPONSE_NEXT_PBLOCK) {
				send(pdus.remove(0));

				try {
					response = actionResponseQueue.poll(invokeId, timeout);
				} catch (InterruptedException e) {
					//TODO LoggingHelper.logStackTrace(e, logger);
					throw new IOException("Interrupted while waiting for incoming response");
				}
			}

			result = new ArrayList<MethodResult>(params.length);
			if (response.getChoiceIndex() == ACTION_Response.Choices.ACTION_RESPONSE_NORMAL) {
				Action_Response_With_Optional_Data resp = response.action_response_normal.single_response;
				Data resultData = null;
				if (resp.return_parameters.isUsed()) {
					resultData = DataConverter.toApi(resp.return_parameters.getValue().data);
				}
				result.add(new MethodResult(MethodResultCode.fromValue((int) resp.result.getValue()), resultData));
			}
			else if (response.getChoiceIndex() == ACTION_Response.Choices.ACTION_RESPONSE_WITH_LIST) {
				for (Action_Response_With_Optional_Data resp : response.action_response_with_list.list_of_responses
						.list()) {
					Data resultData = null;
					if (resp.return_parameters.isUsed()) {
						resultData = DataConverter.toApi(resp.return_parameters.getValue().data);
					}
					result.add(new MethodResult(MethodResultCode.fromValue((int) resp.result.getValue()), resultData));
				}
			}
			else if (response.getChoiceIndex() == ACTION_Response.Choices.ACTION_RESPONSE_WITH_PBLOCK) {
				ByteArrayOutputStream datablocks = new ByteArrayOutputStream();
				COSEMpdu pdu = new COSEMpdu();
				ACTION_Request request = new ACTION_Request();
				Action_Request_Next_Pblock nextBlock = new Action_Request_Next_Pblock();
				nextBlock.invoke_id_and_priority = response.action_response_with_pblock.invoke_id_and_priority;
				while (response.action_response_with_pblock.pblock.last_block.getValue() == false) {
					datablocks.write(response.action_response_with_pblock.pblock.raw_data.getValue());

					nextBlock.block_number = response.action_response_with_pblock.pblock.block_number;
					request.setaction_request_next_pblock(nextBlock);
					pdu.setaction_request(request);
					send(pdu);

					try {
						response = actionResponseQueue.poll(invokeId, timeout);
					} catch (InterruptedException e) {
						//TODO LoggingHelper.logStackTrace(e, logger);
						throw new IOException("Interrupted while waiting for incoming response");
					}
				}
				datablocks.write(response.action_response_with_pblock.pblock.raw_data.getValue());
				InputStream dataByteStream = new ByteArrayInputStream(datablocks.toByteArray());
				while (dataByteStream.available() > 0) {
					Get_Data_Result dataResult = new Get_Data_Result();
					dataResult.decode(dataByteStream);
					// If remote Method call returns a pdu that must be
					// segmented into datablocks, we can assume that the call
					// was successful.
					Data resultData = DataConverter.toApi(dataResult.data);
					result.add(new MethodResult(MethodResultCode.SUCCESS, resultData));
				}
			}
			else {
				throw new UnsupportedOperationException("Unknown response type");
			}
		}

		return result;
	}

	@Override
	public void processPdu(COSEMpdu pdu) {
		try {
			if (pdu.getChoiceIndex() == COSEMpdu.Choices.GET_RESPONSE) {
				getResponseQueue.put(PduHelper.getInvokeId(pdu.get_response), pdu.get_response);
			}
			else if (pdu.getChoiceIndex() == COSEMpdu.Choices.SET_RESPONSE) {
				setResponseQueue.put(PduHelper.getInvokeId(pdu.set_response), pdu.set_response);
			}
			else if (pdu.getChoiceIndex() == COSEMpdu.Choices.ACTION_RESPONSE) {
				actionResponseQueue.put(PduHelper.getInvokeId(pdu.action_response), pdu.action_response);
			}
			else if (pdu.getChoiceIndex() == COSEMpdu.Choices.EVENT_NOTIFICATION_REQUEST) {
				if (getEventListener() != null) {
					EventNotification notification = DataConverter.toApi(pdu.event_notification_request);
					getEventListener().eventReceived(notification);
				}
			}
		} catch (InterruptedException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		}
	}

	@Override
	protected Conformance getProposedConformance() {
		return PROPOSED_CONFORMANCE;
	}

	@Override
	public byte[] hlsAuthentication(byte[] processedChallenge, long timeout) throws IOException {
		Data param = new Data();
		param.setOctetString(processedChallenge);

		MethodRequest authenticate = new MethodRequest(15, new ObisCode(0, 0, 40, 0, 0, 255), 1, param);

		List<MethodResult> result = action(timeout, false, authenticate);

		if (result.get(0).isSuccess()) {
			return result.get(0).getResultData().getByteArray();
		}
		else {
			return null;
		}
	}

	/**
	 * Creates a PDU to read all attributes listed in params
	 * 
	 * @param id
	 *            InvokeID of this operation
	 * @param params
	 *            All attributes that shall be read
	 * @return A PDU used to read all attributes
	 */
	private COSEMpdu createGetPdu(Invoke_Id_And_Priority id, GetRequest... params) {
		if (params == null || params.length == 0) {
			throw new IllegalArgumentException("No parameter provided for get");
		}
		if (ConformanceHelper.isAttribute0GetAllowed(getNegotiatedFeatures()) == false) {
			for (GetRequest param : params) {
				if (param.getAttributeId() == 0) {
					throw new IllegalArgumentException("No Attribute 0 on get allowed");
				}
			}
		}
		if (ConformanceHelper.isSelectiveAccessAllowed(getProposedConformance()) == false) {
			for (GetRequest param : params) {
				if (param.getAccessSelection() != null) {
					throw new IllegalArgumentException("Selective Access not supported on this connection");
				}
			}
		}

		GET_Request getRequest = new GET_Request();
		if (params.length == 1) {
			Get_Request_Normal requestNormal = new Get_Request_Normal();
			requestNormal.invoke_id_and_priority = id;
			requestNormal.cosem_attribute_descriptor = new Cosem_Attribute_Descriptor(new Unsigned16(
					params[0].getClassId()), new Cosem_Object_Instance_Id(params[0].getObisCode()), new Integer8(
					params[0].getAttributeId()));
			SelectiveAccessDescription accessSelection = params[0].getAccessSelection();
			if (accessSelection != null) {
				requestNormal.access_selection.setValue(new Selective_Access_Descriptor(new Unsigned8(accessSelection
						.getSelector()), DataConverter.toPdu(accessSelection.getParameter())));
			}

			getRequest.setget_request_normal(requestNormal);
		}
		else {
			Get_Request_With_List requestList = new Get_Request_With_List();
			requestList.invoke_id_and_priority = id;
			requestList.attribute_descriptor_list = new Get_Request_With_List.SubSeqOf_attribute_descriptor_list();
			for (GetRequest p : params) {
				Selective_Access_Descriptor access = null;
				SelectiveAccessDescription accessSelection = p.getAccessSelection();
				if (accessSelection != null) {
					access = new Selective_Access_Descriptor(new Unsigned8(accessSelection.getSelector()),
							DataConverter.toPdu(accessSelection.getParameter()));
				}
				requestList.attribute_descriptor_list.add(new Cosem_Attribute_Descriptor_With_Selection(
						new Cosem_Attribute_Descriptor(new Unsigned16(p.getClassId()), new Cosem_Object_Instance_Id(p
								.getObisCode()), new Integer8(p.getAttributeId())), access));
			}

			getRequest.setget_request_with_list(requestList);
		}

		COSEMpdu pdu = new COSEMpdu();
		pdu.setget_request(getRequest);

		return pdu;
	}

	/**
	 * Calculates the size of a PDU in bytes
	 * 
	 * @param pdu
	 * @return
	 */
	private int getPduSize(AxdrType pdu) {
		try {
			return pdu.encode(new NullOutputStream());
		} catch (IOException e) {
			return 0;
		}
	}

	/**
	 * Creates all PDUs needed to set all attributes on the smart meter given by params
	 * 
	 * @param id
	 *            InvokeID of this operation
	 * @param params
	 *            Attributes that shall be changed
	 * @return List of all PDUs needed to send in order
	 * @throws IOException
	 */
	private List<COSEMpdu> createSetPdu(Invoke_Id_And_Priority id, SetRequest... params) throws IOException {
		if (params == null || params.length == 0) {
			throw new IllegalArgumentException("No parameter provided for set");
		}
		if (ConformanceHelper.isAttribute0SetAllowed(getNegotiatedFeatures()) == false) {
			for (SetRequest param : params) {
				if (param.getAttributeId() == 0) {
					throw new IllegalArgumentException("No Attribute 0 on set allowed");
				}
			}
		}

		List<COSEMpdu> result = new LinkedList<COSEMpdu>();

		ByteBuffer dataBuffer = null;
		SET_Request request = new SET_Request();
		COSEMpdu pdu = null;

		if (params.length == 1) {
			Set_Request_Normal requestNormal = new Set_Request_Normal();
			requestNormal.invoke_id_and_priority = id;
			requestNormal.cosem_attribute_descriptor = new Cosem_Attribute_Descriptor(new Unsigned16(
					params[0].getClassId()), new Cosem_Object_Instance_Id(params[0].getObisCode()), new Integer8(
					params[0].getAttributeId()));
			requestNormal.value = DataConverter.toPdu(params[0].data());
			SelectiveAccessDescription accessSelection = params[0].getAccessSelection();
			if (accessSelection != null) {
				requestNormal.access_selection.setValue(new Selective_Access_Descriptor(new Unsigned8(accessSelection
						.getSelector()), DataConverter.toPdu(accessSelection.getParameter())));
			}
			request.setset_request_normal(requestNormal);
		}
		else {
			Set_Request_With_List requestList = new Set_Request_With_List();
			requestList.invoke_id_and_priority = id;
			requestList.attribute_descriptor_list = new Set_Request_With_List.SubSeqOf_attribute_descriptor_list();
			requestList.value_list = new Set_Request_With_List.SubSeqOf_value_list();
			for (SetRequest p : params) {
				Selective_Access_Descriptor access = null;
				SelectiveAccessDescription accessSelection = p.getAccessSelection();
				if (accessSelection != null) {
					access = new Selective_Access_Descriptor(new Unsigned8(accessSelection.getSelector()),
							DataConverter.toPdu(accessSelection.getParameter()));
				}
				Cosem_Attribute_Descriptor desc = new Cosem_Attribute_Descriptor(new Unsigned16(p.getClassId()),
						new Cosem_Object_Instance_Id(p.getObisCode()), new Integer8(p.getAttributeId()));
				requestList.attribute_descriptor_list.add(new Cosem_Attribute_Descriptor_With_Selection(desc, access));
				requestList.value_list.add(DataConverter.toPdu(p.data()));
			}
			request.setset_request_with_list(requestList);
		}

		if (getPduSize(request) < getMaxSendPduSize()) {
			pdu = new COSEMpdu();
			pdu.setset_request(request);
			result.add(pdu);
		}
		else {
			AxdrByteArrayOutputStream os = new AxdrByteArrayOutputStream(getMaxSendPduSize() * 2, true);

			if (params.length == 1) {
				request.set_request_normal.value.encode(os);
				dataBuffer = ByteBuffer.wrap(os.getArray());

				Set_Request_With_First_Datablock requestFirstBlock = new Set_Request_With_First_Datablock();
				requestFirstBlock.invoke_id_and_priority = id;
				requestFirstBlock.cosem_attribute_descriptor = request.set_request_normal.cosem_attribute_descriptor;
				requestFirstBlock.access_selection = request.set_request_normal.access_selection;
				requestFirstBlock.datablock = new DataBlock_SA(new AxdrBoolean(false), new Unsigned32(0),
						new AxdrOctetString(0));

				os = new AxdrByteArrayOutputStream(getMaxSendPduSize() - 2);
				int length = requestFirstBlock.encode(os);
				byte[] firstDataChunk = new byte[getMaxSendPduSize() - 2 - length];
				dataBuffer.get(firstDataChunk, 0, firstDataChunk.length);
				requestFirstBlock.datablock.raw_data = new AxdrOctetString(firstDataChunk);

				request.setset_request_with_first_datablock(requestFirstBlock);
				pdu = new COSEMpdu();
				pdu.setset_request(request);
				result.add(pdu);
			}
			else {
				for (int i = request.set_request_with_list.value_list.size() - 1; i >= 0; i--) {
					request.set_request_with_list.value_list.get(i).encode(os);
				}
				dataBuffer = ByteBuffer.wrap(os.getArray());

				Set_Request_With_List_And_First_Datablock requestListFirstBlock = new Set_Request_With_List_And_First_Datablock();
				requestListFirstBlock.invoke_id_and_priority = id;
				requestListFirstBlock.attribute_descriptor_list = new Set_Request_With_List_And_First_Datablock.SubSeqOf_attribute_descriptor_list();
				requestListFirstBlock.datablock = new DataBlock_SA(new AxdrBoolean(false), new Unsigned32(1),
						new AxdrOctetString(0));

				for (Cosem_Attribute_Descriptor_With_Selection desc : request.set_request_with_list.attribute_descriptor_list
						.list()) {
					requestListFirstBlock.attribute_descriptor_list.add(desc);
				}

				os = new AxdrByteArrayOutputStream(getMaxSendPduSize() - 2);
				int length = requestListFirstBlock.encode(os);
				byte[] firstDataChunk = new byte[getMaxSendPduSize() - 2 - length];
				dataBuffer.get(firstDataChunk, 0, firstDataChunk.length);
				requestListFirstBlock.datablock.raw_data = new AxdrOctetString(firstDataChunk);

				request.setset_request_with_list_and_first_datablock(requestListFirstBlock);
				pdu = new COSEMpdu();
				pdu.setset_request(request);
				result.add(pdu);
			}

			int blockNr = 1;
			while (dataBuffer.hasRemaining()) {
				blockNr++;
				int blockLength = Math.min(getMaxSendPduSize() - 9, dataBuffer.remaining());
				byte[] dataBlock = new byte[blockLength];
				dataBuffer.get(dataBlock, 0, dataBlock.length);

				Set_Request_With_Datablock requestBlock = new Set_Request_With_Datablock();
				requestBlock.invoke_id_and_priority = id;
				requestBlock.datablock = new DataBlock_SA(new AxdrBoolean(dataBuffer.remaining() == 0), new Unsigned32(
						blockNr), new AxdrOctetString(dataBlock));

				request.setset_request_with_datablock(requestBlock);
				pdu = new COSEMpdu();
				pdu.setset_request(request);
				result.add(pdu);
			}
		}

		return result;
	}

	/**
	 * Creates all outgoing PDUs that are needed to call the remote methods given in params
	 * 
	 * @param id
	 *            InvokeID of this operation
	 * @param params
	 *            Methods to call on smart meter
	 * @return List of all outgoing PDUs in order
	 * @throws IOException
	 */
	private List<COSEMpdu> createActionPdu(Invoke_Id_And_Priority id, MethodRequest... params) throws IOException {
		if (params == null || params.length == 0) {
			throw new IllegalArgumentException("No parameter provided for set");
		}
		for (MethodRequest param : params) {
			if (param.getMethodId() == 0) {
				throw new IllegalArgumentException("MethodID 0 not allowed on action");
			}
		}

		List<COSEMpdu> result = new LinkedList<COSEMpdu>();

		ByteBuffer dataBuffer = null;
		ACTION_Request request = new ACTION_Request();
		COSEMpdu pdu = null;

		if (params.length == 1) {
			Action_Request_Normal requestNormal = new Action_Request_Normal();
			requestNormal.invoke_id_and_priority = id;
			requestNormal.cosem_method_descriptor = new Cosem_Method_Descriptor(new Unsigned16(params[0].getClassId()),
					new Cosem_Object_Instance_Id(params[0].getObisCode()), new Integer8(params[0].getMethodId()));
			requestNormal.method_invocation_parameters.setValue(DataConverter.toPdu(params[0].data()));

			request.setaction_request_normal(requestNormal);
		}
		else {
			Action_Request_With_List requestList = new Action_Request_With_List();
			requestList.invoke_id_and_priority = id;
			requestList.cosem_method_descriptor_list = new Action_Request_With_List.SubSeqOf_cosem_method_descriptor_list();
			requestList.method_invocation_parameters = new Action_Request_With_List.SubSeqOf_method_invocation_parameters();
			for (MethodRequest param : params) {
				Cosem_Method_Descriptor desc = new Cosem_Method_Descriptor(new Unsigned16(param.getClassId()),
						new Cosem_Object_Instance_Id(param.getObisCode()), new Integer8(param.getMethodId()));
				requestList.cosem_method_descriptor_list.add(desc);
				requestList.method_invocation_parameters.add(DataConverter.toPdu(param.data()));
			}
			request.setaction_request_with_list(requestList);
		}

		if (getPduSize(request) < getMaxSendPduSize()) {
			pdu = new COSEMpdu();
			pdu.setaction_request(request);
			result.add(pdu);
		}
		else {
			// PDU is too large to send in one chunk to the meter
			// use of several Datablocks instead
			AxdrByteArrayOutputStream os = new AxdrByteArrayOutputStream(getMaxSendPduSize() * 2, true);

			if (params.length == 1) {
				request.action_request_normal.method_invocation_parameters.encode(os);
				dataBuffer = ByteBuffer.wrap(os.getArray());

				Action_Request_With_First_Pblock requestFirstBlock = new Action_Request_With_First_Pblock();
				requestFirstBlock.invoke_id_and_priority = id;
				requestFirstBlock.cosem_method_descriptor = request.action_request_normal.cosem_method_descriptor;

				os = new AxdrByteArrayOutputStream(getMaxSendPduSize() - 2);
				int length = requestFirstBlock.encode(os);
				byte[] firstDataChunk = new byte[getMaxSendPduSize() - 2 - length];
				dataBuffer.get(firstDataChunk, 0, firstDataChunk.length);
				requestFirstBlock.pblock = new DataBlock_SA(new AxdrBoolean(false), new Unsigned32(0),
						new AxdrOctetString(firstDataChunk));

				request.setaction_request_with_first_pblock(requestFirstBlock);
				pdu = new COSEMpdu();
				pdu.setaction_request(request);
				result.add(pdu);
			}
			else {
				request.action_request_with_list.method_invocation_parameters.encode(os);
				dataBuffer = ByteBuffer.wrap(os.getArray());

				Action_Request_With_List_And_First_Pblock requestListFirstBlock = new Action_Request_With_List_And_First_Pblock();
				requestListFirstBlock.invoke_id_and_priority = id;
				requestListFirstBlock.cosem_method_descriptor_list = new Action_Request_With_List_And_First_Pblock.SubSeqOf_cosem_method_descriptor_list();

				for (Cosem_Method_Descriptor desc : request.action_request_with_list.cosem_method_descriptor_list
						.list()) {
					requestListFirstBlock.cosem_method_descriptor_list.add(desc);
				}

				os = new AxdrByteArrayOutputStream(getMaxSendPduSize() - 2);
				int length = requestListFirstBlock.encode(os);
				byte[] firstDataChunk = new byte[getMaxSendPduSize() - 2 - length];
				dataBuffer.get(firstDataChunk, 0, firstDataChunk.length);
				requestListFirstBlock.pblock = new DataBlock_SA(new AxdrBoolean(false), new Unsigned32(0),
						new AxdrOctetString(firstDataChunk));

				request.setaction_request_with_list_and_first_pblock(requestListFirstBlock);
			}

			int blockNr = 1;
			while (dataBuffer.hasRemaining()) {
				int blockLength = Math.min(getMaxSendPduSize() - 8, dataBuffer.remaining());
				byte[] dataBlock = new byte[blockLength];
				dataBuffer.get(dataBlock, 0, dataBlock.length);

				Action_Request_With_Pblock requestBlock = new Action_Request_With_Pblock();
				requestBlock.pBlock = new DataBlock_SA(new AxdrBoolean(dataBuffer.remaining() == 0), new Unsigned32(
						blockNr), new AxdrOctetString(dataBlock));

				request.setaction_request_with_pblock(requestBlock);
				pdu = new COSEMpdu();
				pdu.setaction_request(request);
				result.add(pdu);
			}
		}

		return result;
	}
}

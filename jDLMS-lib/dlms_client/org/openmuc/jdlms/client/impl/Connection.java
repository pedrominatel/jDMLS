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
import java.io.IOException;
import java.util.List;

import org.openmuc.asn1.cosem.COSEMpdu;
import org.openmuc.asn1.cosem.Conformance;
import org.openmuc.asn1.cosem.InitiateRequest;
import org.openmuc.asn1.cosem.InitiateResponse;
import org.openmuc.asn1.cosem.Invoke_Id_And_Priority;
import org.openmuc.asn1.cosem.Unsigned16;
import org.openmuc.asn1.cosem.Unsigned8;
import org.openmuc.jasn1.axdr.AxdrByteArrayOutputStream;
import org.openmuc.jasn1.axdr.types.AxdrBoolean;
import org.openmuc.jdlms.client.AccessResultCode;
import org.openmuc.jdlms.client.GetRequest;
import org.openmuc.jdlms.client.GetResult;
import org.openmuc.jdlms.client.HlsSecretProcessor;
import org.openmuc.jdlms.client.IClientConnection;
import org.openmuc.jdlms.client.IEventListener;
import org.openmuc.jdlms.client.MethodRequest;
import org.openmuc.jdlms.client.MethodResult;
import org.openmuc.jdlms.client.SetRequest;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.communication.IUpperLayer;
import org.openmuc.jdlms.client.cosem.context.ApplicationContext;
import org.openmuc.jdlms.client.cosem.context.MechanismName;

/**
 * Base class used by all application level DLMS/Cosem connections
 * 
 * @author Karsten Mueller-Bier
 */
public abstract class Connection implements IClientConnection, IUpperLayer, IAssociation {

	private IEventListener eventListener;

	private final boolean confirmedMode;
	private final MechanismName authName;
	private final ApplicationContext appContext;

	private final DisconnectModule disconnectModule = new DisconnectModule();
	private final ConnectModule connectModule;
	private ILowerLayer<Object> lowerLayer;
	private Conformance negotiatedFeatures;
	private int maxSendPduSize;

	private int timeoutCounter = 0;

	private int invokeId = 0;

	private boolean connected;

	protected Connection(boolean confirmedMode, MechanismName authName, ApplicationContext appContext,
			ILowerLayer<Object> lowerLayer, ConnectModule connectModule) {
		this.confirmedMode = confirmedMode;
		this.authName = authName;
		this.appContext = appContext;
		this.lowerLayer = lowerLayer;
		this.connectModule = connectModule;
		connected = false;
	}

	/**
	 * @return true if smart meter shall respond to messages
	 */
	@Override
	public boolean isConfirmedMode() {
		return confirmedMode;
	}

	/**
	 * @return The conformance block received from the smart meter while connecting
	 */
	protected Conformance getNegotiatedFeatures() {
		return negotiatedFeatures;
	}

	/**
	 * @return Maximum size of a single message the smart meter can handle
	 */
	protected int getMaxSendPduSize() {
		return maxSendPduSize;
	}

	/**
	 * Creates a InvokeId and Priority Byte with an incrementing Id
	 * 
	 * @param highPriority
	 *            true if this message shall have high priority
	 * @return The generated InvokeId Byte
	 */
	protected Invoke_Id_And_Priority getInvokeIdAndPriority(boolean highPriority) {
		Invoke_Id_And_Priority result = new Invoke_Id_And_Priority();

		result.getValues()[0] = (byte) (invokeId << 4);
		if (isConfirmedMode()) {
			result.getValues()[0] |= 0x02;
		}
		if (highPriority) {
			result.getValues()[0] |= 0x01;
		}

		invokeId = (invokeId + 1) % 16;
		return result;
	}

	protected int getInvokeId(Invoke_Id_And_Priority frame) {
		return (frame.getValues()[0] >> 4) & 0x0F;
	}

	/**
	 * Sends a message to the smart meter
	 * 
	 * @param pdu
	 *            Message to be sent
	 * @throws IOException
	 */
	protected void send(COSEMpdu pdu) throws IOException {
		AxdrByteArrayOutputStream oStream = new AxdrByteArrayOutputStream(1000);
		pdu.encode(oStream);
		lowerLayer.send(oStream.getArray());
	}

	/**
	 * Starts the connection with the smart meter.
	 * 
	 * @param secret
	 *            Pre-shared password to access smart meter. Can be null
	 * @return Answer message from smart meter
	 * @throws IOException
	 */
	protected InitiateResponse establishConnection(long timeout, byte[] secret, HlsSecretProcessor processor)
			throws IOException {
		if (connected == false) {
			lowerLayer.connect(timeout);

			try {
				InitiateResponse xdlmsResponse = connectModule.establishConnection(this, timeout, secret, processor);
				negotiatedFeatures = xdlmsResponse.negotiated_conformance;
				maxSendPduSize = (int) xdlmsResponse.server_max_receive_pdu_size.getValue();

				connected = true;
				return xdlmsResponse;
			} catch (IllegalArgumentException ex) {
				// An IllegalArgumentException is thrown if authentication
				// failed. Close connection and rethrow as IOException
				disconnect(true);
				throw new IOException("Authentication error", ex);
			}
		}
		return null;
	}

	private void discardPDU(COSEMpdu pdu) {
		AxdrByteArrayOutputStream oStream = new AxdrByteArrayOutputStream(1000);
		try {
			pdu.encode(oStream);
		} catch (IOException e) {
			// ignore
		}
		lowerLayer.discardMessage(oStream.getArray());
	}

	protected void receiveTimedOut(COSEMpdu pdu) {
		discardPDU(pdu);
		timeoutCounter++;
		if (timeoutCounter == 3) {
			disconnect(false);
		}
	}

	@Override
	public void disconnect(boolean sendDisconnectMessage) {
		if (isConnected()) {
			try {
				lowerLayer.removeReceivingListener(this);
				if (sendDisconnectMessage) {
					disconnectModule.gracefulDisconnect(this);
				}
				connected = false;
				lowerLayer.disconnect();
			} catch (IOException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
			}
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public List<GetResult> get(long timeout, GetRequest... params) throws IOException {
		return get(timeout, false, params);
	}

	@Override
	public List<AccessResultCode> set(long timeout, SetRequest... params) throws IOException {
		return set(timeout, false, params);
	}

	@Override
	public List<MethodResult> action(long timeout, MethodRequest... params) throws IOException {
		return action(timeout, false, params);
	}

	@Override
	public void disconnect() {
		disconnect(true);
	}

	@Override
	public void dataReceived(byte[] data) {
		COSEMpdu pdu = new COSEMpdu();
		try {
			pdu.decode(new ByteArrayInputStream(data));

			processPdu(pdu);
			timeoutCounter = 0;
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		}
	}

	@Override
	public void remoteDisconnect() {
		disconnect(false);
	}

	@Override
	public void registerEventListener(IEventListener listener) {
		if (eventListener == null) {
			eventListener = listener;
		}
	}

	@Override
	public void removeEventListener(IEventListener listener) {
		if (eventListener == listener) {
			eventListener = null;
		}
	}

	protected IEventListener getEventListener() {
		return eventListener;
	}

	@Override
	public ApplicationContext getAppContext() {
		return appContext;
	}

	@Override
	public MechanismName getAuthName() {
		return authName;
	}

	@Override
	public InitiateRequest getXDlmsRequest() {
		// TODO Change 1st argument to allow encryption
		return new InitiateRequest(null, new AxdrBoolean(confirmedMode), null, new Unsigned8(6),
				getProposedConformance(), new Unsigned16(0xFFFF));
	}

	@Override
	public ILowerLayer<Object> moveLowerLayer() {
		lowerLayer.removeReceivingListener(this);
		return lowerLayer;
	}

	@Override
	public void setLowerLayer(ILowerLayer<Object> lowerLayer) {
		this.lowerLayer = lowerLayer;
		this.lowerLayer.registerReceivingListener(null, this);
	}

	protected abstract Conformance getProposedConformance();

	protected abstract void processPdu(COSEMpdu pdu);
}

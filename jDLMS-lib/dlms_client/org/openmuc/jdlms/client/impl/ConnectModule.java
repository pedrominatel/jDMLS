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
import java.security.SecureRandom;
import java.util.Arrays;

import org.openmuc.asn1.cosem.COSEMpdu;
import org.openmuc.asn1.cosem.InitiateRequest;
import org.openmuc.asn1.cosem.InitiateResponse;
import org.openmuc.asn1.cosem.Integer16;
import org.openmuc.asn1.cosem.Unsigned16;
import org.openmuc.asn1.iso.acse.AARE_apdu;
import org.openmuc.asn1.iso.acse.AARQ_apdu;
import org.openmuc.asn1.iso.acse.Association_information;
import org.openmuc.asn1.iso.acse.Authentication_value;
import org.openmuc.jasn1.axdr.AxdrByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerBitString;
import org.openmuc.jasn1.ber.types.BerOctetString;
import org.openmuc.jasn1.ber.types.string.BerGraphicString;
import org.openmuc.jdlms.client.HlsSecretProcessor;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.communication.IUpperLayer;
import org.openmuc.jdlms.client.cosem.context.MechanismName;

/**
 * Small module shared by all ClientConnections that realizes the complete connection sequence between client and smart
 * meter, authentication included
 * 
 * Only a single connection can be tried to established (read as the act of connecting to the device) at a time. If
 * multiple connections are opened parallel (e.g. at start up) this could be a bottleneck. It is recommended, that the
 * connections are opened sequentially, as that will happen anyway.
 * 
 * @author Karsten Mueller-Bier
 */
public class ConnectModule implements IUpperLayer {

	private final SecureRandom rand = new SecureRandom();

	private final Object waitForReceiveLock = new Object();
	private AARE_apdu response = null;
	private Thread waitingThread = null;

	/**
	 * Tries to open a DLMS/Cosem connection to the remote host
	 * 
	 * @param association
	 *            Reference to the connection that tries to connect at the moment
	 * @param secret
	 *            Pre-shared password to access the smart meter
	 * @return Answer packet from the smart meter
	 * @throws IOException
	 *             Attempting to connect failed
	 */
	public synchronized InitiateResponse establishConnection(IAssociation association, long timeout, byte[] secret,
			HlsSecretProcessor processor) throws IOException {
		response = null;
		waitingThread = null;

		ILowerLayer<Object> lowerLayer = null;
		try {
			lowerLayer = association.moveLowerLayer();
			lowerLayer.registerReceivingListener(null, this);

			MechanismName authName = association.getAuthName();

			byte[] clientToServer = null;
			if (usingHls(authName)) {
				clientToServer = generateRandomSequence();

				if (authName == MechanismName.HIGH_MD5) {
					processor = new HlsProcessorMd5();
				}
				else if (authName == MechanismName.HIGH_SHA1) {
					processor = new HlsProcessorSha1();
				}
			}

			lowerLayer.send(createAarq(association, secret, clientToServer));

			COSEMpdu xdlmsResponse = new COSEMpdu();
			if (association.isConfirmedMode()) {
				synchronized (waitForReceiveLock) {
					try {
						if (response == null) {
							waitingThread = Thread.currentThread();
							waitForReceiveLock.wait(timeout);
							if (response == null) {
								throw new IOException("Device does not respond to DLMS connect");
							}
						}
					} catch (InterruptedException e) {
						throw new IOException("Attempt to connect was interrupted");
					}
				}
				AARE_apdu aare = response;
				if (aare.result.val != 0) {
					lowerLayer.disconnect();
					long errorCode;
					if (aare.result_source_diagnostic.acse_service_user != null) {
						errorCode = aare.result_source_diagnostic.acse_service_user.val;
					}
					else {
						errorCode = aare.result_source_diagnostic.acse_service_provider.val;
					}
					throw new IOException("Error on establishing connection. Error code: " + errorCode);
				}

				xdlmsResponse.decode(new ByteArrayInputStream(aare.user_information.axdr_frame.octetString));

				// Step 3 and 4 of HLS
				if (usingHls(authName)) {
					byte[] serverToClient = aare.responding_authentication_value.charstring.octetString;

					byte[] processedChallenge = processor.process(secret, serverToClient);
					byte[] remoteResponse = association.hlsAuthentication(processedChallenge, timeout);

					if (remoteResponse == null) {
						throw new IllegalArgumentException("Authentication failed");
					}

					processedChallenge = processor.process(secret, clientToServer);
					if (Arrays.equals(remoteResponse, processedChallenge) == false) {
						throw new IllegalArgumentException("Server wasn't able to authenticate itself");
					}
				}

			}
			else {
				InitiateRequest xdlmsInitiate = association.getXDlmsRequest();
				xdlmsResponse.setinitiateResponse(new InitiateResponse(xdlmsInitiate.proposed_quality_of_service
						.getValue(), xdlmsInitiate.proposed_dlms_version_number, xdlmsInitiate.proposed_conformance,
						new Unsigned16(0x0100), new Integer16((short) 0xFA00)));
			}

			return xdlmsResponse.initiateResponse;

		} finally {
			if (lowerLayer != null) {
				lowerLayer.removeReceivingListener(this);
				association.setLowerLayer(lowerLayer);
			}
		}
	}

	@Override
	public void dataReceived(byte[] data) {
		AARE_apdu aare = new AARE_apdu();
		COSEMpdu cpdu = new COSEMpdu();
		synchronized (waitForReceiveLock) {
			try {
				aare.decode(new ByteArrayInputStream(data), true);
				response = aare;
				waitForReceiveLock.notify();
			} catch (IOException e) {
				try {
					cpdu.decode(new ByteArrayInputStream(data));
				} catch (IOException e1) {
					//TODO Log LoggingHelper.logStackTrace(e1, logger);
				}
			}
		}
	}

	@Override
	public void remoteDisconnect() {
		waitingThread.interrupt();
	}

	private byte[] generateRandomSequence() {
		// Random challenge must have a range of 8 to 64 bytes
		int resultLength = rand.nextInt(57) + 8;
		byte[] result = new byte[resultLength];

		for (int i = 0; i < resultLength; i++) {
			byte[] resultByte = new byte[1];

			// Only allow printable characters
			do {
				rand.nextBytes(resultByte);
			} while (resultByte[0] >= 0 && resultByte[0] <= 31);

			result[i] = resultByte[0];
		}

		return result;
	}

	private byte[] createAarq(IAssociation association, byte[] secret, byte[] clientToServer) throws IOException {
		AxdrByteArrayOutputStream oStream = new AxdrByteArrayOutputStream(1000);

		COSEMpdu pdu = new COSEMpdu();
		pdu.setinitiateRequest(association.getXDlmsRequest());
		pdu.encode(oStream);

		AARQ_apdu aarq = new AARQ_apdu();
		aarq.application_context_name = association.getAppContext();

		MechanismName authName = association.getAuthName();
		if (authName != MechanismName.LOWEST) {
			aarq.mechanism_name = authName;
		}
		aarq.user_information = new Association_information(new BerOctetString(oStream.getArray()));

		if (authName == MechanismName.LOWEST) {
			// nothing additional to do
		}
		else if (authName == MechanismName.LOW) {
			aarq.mechanism_name = authName;
			aarq.sender_acse_requirements = new BerBitString(new byte[] { (byte) 0x80 }, 2);
			aarq.calling_authentication_value = new Authentication_value(new BerGraphicString(secret), null);
		}
		else if (authName == MechanismName.HIGH_MD5) {
			aarq.mechanism_name = authName;
			aarq.sender_acse_requirements = new BerBitString(new byte[] { (byte) 0x80 }, 2);
			aarq.calling_authentication_value = new Authentication_value(new BerGraphicString(clientToServer), null);
		}
		else if (authName == MechanismName.HIGH_SHA1) {
			aarq.mechanism_name = authName;
			aarq.sender_acse_requirements = new BerBitString(new byte[] { (byte) 0x80 }, 2);
			aarq.calling_authentication_value = new Authentication_value(new BerGraphicString(clientToServer), null);
		}
		else if (authName == MechanismName.HIGH_MANUFACTURER) {
			aarq.mechanism_name = authName;
			aarq.sender_acse_requirements = new BerBitString(new byte[] { (byte) 0x80 }, 2);
			aarq.calling_authentication_value = new Authentication_value(new BerGraphicString(clientToServer), null);
		}
		else {
			throw new UnsupportedOperationException("Authentication mechanism unknown");
		}

		oStream = new AxdrByteArrayOutputStream(oStream.buffer, oStream.buffer.length - 1);
		aarq.encode(oStream, true);

		return oStream.getArray();
	}

	private boolean usingHls(MechanismName authName) {
		return authName == MechanismName.HIGH_MANUFACTURER || authName == MechanismName.HIGH_MD5
				|| authName == MechanismName.HIGH_SHA1;
	}
}

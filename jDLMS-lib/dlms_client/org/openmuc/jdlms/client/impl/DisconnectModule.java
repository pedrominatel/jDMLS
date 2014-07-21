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

import org.openmuc.asn1.iso.acse.RLRE_apdu;
import org.openmuc.asn1.iso.acse.RLRQ_apdu;
import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.communication.IUpperLayer;

/**
 * Helper object that provides all Connection types with the same disconnection algorithm
 * 
 * @author Karsten Mueller-Bier
 */
public class DisconnectModule implements IUpperLayer {

	private final Object waitForResponseLock = new Object();
	private Thread waitingThread;
	private RLRE_apdu response = null;

	/**
	 * Creates a ReleaseRequest packet, sends it to the smart meter, and disconnects the sub layer after an
	 * acknowledgment has been received
	 * 
	 * @param association
	 *            The connection that shall be closed
	 */
	public synchronized void gracefulDisconnect(IAssociation association) {
		response = null;
		waitingThread = null;

		ILowerLayer<Object> lowerLayer = null;
		try {
			lowerLayer = association.moveLowerLayer();
			lowerLayer.registerReceivingListener(null, this);

			BerByteArrayOutputStream oStream = new BerByteArrayOutputStream(50);

			RLRQ_apdu rlrq = new RLRQ_apdu();
			rlrq.reason = new BerInteger(0);
			try {
				rlrq.encode(oStream, true);
				lowerLayer.send(oStream.getArray());
			} catch (IOException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
			}

			synchronized (waitForResponseLock) {
				try {
					while (response == null) {
						waitingThread = Thread.currentThread();
						waitForResponseLock.wait();
					}
				} catch (InterruptedException e) {
				}
			}
		} finally {
			if (lowerLayer != null) {
				lowerLayer.removeReceivingListener(this);
				association.setLowerLayer(lowerLayer);
			}
		}
	}

	@Override
	public void dataReceived(byte[] data) {
		RLRE_apdu rlre = new RLRE_apdu();
		try {
			rlre.decode(new ByteArrayInputStream(data), true);
			response = rlre;
			synchronized (waitForResponseLock) {
				waitForResponseLock.notify();
			}
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		}
	}

	@Override
	public void remoteDisconnect() {
		waitingThread.interrupt();
	}
}

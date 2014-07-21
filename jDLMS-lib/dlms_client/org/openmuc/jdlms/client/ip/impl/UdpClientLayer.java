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
package org.openmuc.jdlms.client.ip.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.TooManyListenersException;

import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.communication.IUpperLayer;
import org.openmuc.jdlms.client.ip.common.ConnectionIdentifier;
import org.openmuc.jdlms.client.ip.common.IUdpLayer;
import org.openmuc.jdlms.client.ip.common.Wpdu;

/**
 * Communication layer to wrap CosemPDUs inside a WPDU and send it out using an UDP connection
 * 
 * @author Karsten Mueller-Bier
 */
public class UdpClientLayer implements ILowerLayer<Object>, IUpperLayer {

	private IUpperLayer upperLayer;
	private final IUdpLayer lowerLayer;

	private final ConnectionIdentifier identifier;

	private final Wpdu pdu = new Wpdu();

	public UdpClientLayer(IUdpLayer lowerLayer, int clientWPort, int serverWPort, InetSocketAddress remoteAddress) {
		this.lowerLayer = lowerLayer;
		identifier = new ConnectionIdentifier(clientWPort, serverWPort, remoteAddress);
	}

	@Override
	public void dataReceived(byte[] data) {
		try {
			pdu.decode(new ByteArrayInputStream(data));

			if (upperLayer != null) {
				upperLayer.dataReceived(pdu.getData());
			}
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		}
	}

	@Override
	public void remoteDisconnect() {
		lowerLayer.removeUdpListener(identifier);
		if (upperLayer != null) {
			upperLayer.remoteDisconnect();
		}
	}

	@Override
	public void connect(long timeout) throws IOException {
		try {
			lowerLayer.registerUdpListener(identifier, this);
		} catch (TooManyListenersException e) {
			throw new IOException("WPort already used locally: " + identifier.getLocalWPort(), e);
		}
	}

	@Override
	public void send(byte[] data) throws IOException {
		Wpdu pdu = new Wpdu();
		pdu.setDestinationWPort(identifier.getRemoteWPort());
		pdu.setSourceWPort(identifier.getLocalWPort());
		pdu.setData(data);

		lowerLayer.sendOverUdp(pdu.encode(), identifier.getRemoteAddress());
	}

	@Override
	public void disconnect() throws IOException {
		lowerLayer.removeUdpListener(identifier);
	}

	@Override
	public void registerReceivingListener(Object key, IUpperLayer listener) throws IllegalArgumentException {
		if (upperLayer != null) {
			throw new IllegalArgumentException("Upper layer already registered");
		}
		upperLayer = listener;
	}

	@Override
	public void removeReceivingListener(IUpperLayer listener) {
		if (listener.equals(upperLayer)) {
			upperLayer = null;
		}
	}

	@Override
	public void discardMessage(byte[] data) {
		// Messages are not buffered if UDP is used
	}
}

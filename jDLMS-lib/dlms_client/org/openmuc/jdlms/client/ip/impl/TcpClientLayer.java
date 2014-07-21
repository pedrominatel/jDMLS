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
import org.openmuc.jdlms.client.ip.common.ITcpLayer;
import org.openmuc.jdlms.client.ip.common.Wpdu;

/**
 * Communication layer to wrap CosemPDUs inside a WPDU and send it out using a TCP connection
 * 
 * @author Karsten Mueller-Bier
 */
public class TcpClientLayer implements IUpperLayer, ILowerLayer<Object> {


	private IUpperLayer upperLayer;
	private final ITcpLayer lowerLayer;

	private final ConnectionIdentifier identifier;

	private final Wpdu pdu = new Wpdu();

	public TcpClientLayer(ITcpLayer lowerLayer, int clientWPort, int serverWPort, InetSocketAddress remoteAddress) {
		identifier = new ConnectionIdentifier(clientWPort, serverWPort, remoteAddress);
		this.lowerLayer = lowerLayer;
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
		if (upperLayer != null) {
			upperLayer.remoteDisconnect();
		}
	}

	@Override
	public void connect(long timeout) throws IOException {
		lowerLayer.connect();
		try {
			lowerLayer.registerListener(identifier, this);
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

		lowerLayer.send(pdu.encode());
	}

	@Override
	public void disconnect() throws IOException {
		lowerLayer.disconnect();
		lowerLayer.removeListener(identifier);
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
		// no messages are buffered on this layer. The underlying TCP layer is responsible for buffering
	}
}

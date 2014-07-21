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
package org.openmuc.jdlms.client.ip.common;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

import org.openmuc.jdlms.client.communication.IUpperLayer;

/**
 * Class to handle all outgoing and incoming UDP datagrams
 * 
 * @author Karsten Mueller-Bier
 */
public class UdpLayer implements IUdpLayer, ISelectableChannel {

	private final InetSocketAddress receivingPort;

	private final ServerSocketChannel tcpListeningServer;
	private final DatagramChannel udpServer;

	private final ByteBuffer buffer = ByteBuffer.allocate(0xFFFF);
	private final WpduHeader wpduHeader = new WpduHeader();

	private final Map<ConnectionIdentifier, IUpperLayer> listeners = new HashMap<ConnectionIdentifier, IUpperLayer>();

	public UdpLayer(int port) throws IOException {
		receivingPort = new InetSocketAddress(port);
		tcpListeningServer = ServerSocketChannel.open();
		udpServer = DatagramChannel.open();

		tcpListeningServer.socket().bind(receivingPort);
		udpServer.socket().bind(receivingPort);
	}

	@Override
	public void registerUdpListener(ConnectionIdentifier key, IUpperLayer listener) throws TooManyListenersException {
		if (listeners.containsKey(key)) {
			throw new TooManyListenersException("Client WPort already registered");
		}

		listeners.put(key, listener);
		startListening();
	}

	@Override
	public void removeUdpListener(ConnectionIdentifier key) {
		listeners.remove(key);
	}

	@Override
	public void sendOverUdp(byte[] data, SocketAddress destination) throws IOException {
		//TODO LoggingHelper.logBytes(data, data.length, "Sending", logger);
		udpServer.send(ByteBuffer.wrap(data), destination);
	}

	/**
	 * Starts this connection to listen for incoming datagrams
	 */
	public void startListening() {
		SelectController.getInstance().registerChannel(this);
	}

	/**
	 * @return The port used for UDP communication
	 */
	public int getPort() {
		return receivingPort.getPort();
	}

	@Override
	public void registerSelector(Selector selector) throws IOException {
		udpServer.configureBlocking(false);
		udpServer.register(selector, SelectionKey.OP_READ);
	}

	@Override
	public boolean isRightKey(SelectionKey key) {
		return key.channel() == udpServer;
	}

	@Override
	public void processSelection(SelectionKey key) throws IOException {
		SocketAddress remoteHost = udpServer.receive(buffer);

		//TODO LoggingHelper.logBytes(buffer.array(), buffer.position(), "Received", logger);
		try {
			buffer.flip();
			wpduHeader.decode(buffer);

			if (wpduHeader.getVersion() != 1) {
				// WPDU head has wrong version. Probably received no WPDU at
				// all, or a newer version of IEC 62056-47:2007 has been
				// released
				buffer.clear();
				return;
			}

			ConnectionIdentifier hashKey = new ConnectionIdentifier(wpduHeader.getDestinationWPort(),
					wpduHeader.getSourceWPort(), remoteHost);

			IUpperLayer receiver = listeners.get(hashKey);

			byte[] wpdu = new byte[wpduHeader.length + 8];
			System.arraycopy(buffer.array(), 0, wpdu, 0, wpdu.length);
			receiver.dataReceived(wpdu);

			buffer.position(buffer.position() + wpduHeader.length);

			if (buffer.hasRemaining()) {
				// discard any excess bytes
				buffer.clear();
			}
		} catch (IndexOutOfBoundsException e) {
			// received not as much bytes as said in header
			// discard bytes
			buffer.clear();
		}
	}
}

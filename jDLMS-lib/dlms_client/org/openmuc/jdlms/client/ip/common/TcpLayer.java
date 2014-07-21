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
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

import org.openmuc.jdlms.client.communication.IUpperLayer;

/**
 * Class to handle all outgoing and incoming TCP packets over one TCP connection
 * 
 * @author Karsten Mueller-Bier
 */
public class TcpLayer implements ITcpLayer, ISelectableChannel {


	private final SocketAddress destination;
	private SocketChannel client;

	private boolean channelClosed = false;

	ByteBuffer buffer = ByteBuffer.allocate(0xFFFF);
	WpduHeader wpdu = new WpduHeader();

	private final Map<ConnectionIdentifier, IUpperLayer> listeners = new HashMap<ConnectionIdentifier, IUpperLayer>();
	private int connectedUpperLayers = 0;

	public TcpLayer(SocketChannel client, SocketAddress destination) {
		this.client = client;
		this.destination = destination;
	}

	@Override
	public void connect() throws IOException {
		if (connectedUpperLayers == 0) {
			synchronized (client) {
				if (connectedUpperLayers == 0) {
					if (channelClosed) {
						client = SocketChannel.open();
						channelClosed = false;
					}

					client.connect(destination);
					SelectController.getInstance().registerChannel(this);
				}
			}
		}
		else {
			connectedUpperLayers++;
		}
	}

	@Override
	public void disconnect() throws IOException {
		connectedUpperLayers--;
		if (connectedUpperLayers == 0) {
			synchronized (client) {
				client.close();
				SelectController.getInstance().removeChannel(this);
			}
		}
	}

	@Override
	public void send(byte[] data) throws IOException {
		//TODO LoggingHelper.logBytes(data, data.length, "Sending", logger);
		client.write(ByteBuffer.wrap(data));
	}

	@Override
	public void registerListener(ConnectionIdentifier key, IUpperLayer listener) throws TooManyListenersException {
		if (listeners.containsKey(key)) {
			throw new TooManyListenersException("Client WPort already registered");
		}

		listeners.put(key, listener);
	}

	@Override
	public void removeListener(ConnectionIdentifier key) {
		listeners.remove(key);
	}

	private void connectionLost() {
		synchronized (client) {
			for (IUpperLayer listener : listeners.values()) {
				listener.remoteDisconnect();
			}

			listeners.clear();
			connectedUpperLayers = 0;
			try {
				channelClosed = true;
				client.close();
			} catch (IOException e) {
			}

		}
	}

	@Override
	public void registerSelector(Selector selector) throws IOException {
		client.configureBlocking(false);
		client.register(selector, SelectionKey.OP_READ);
	}

	@Override
	public boolean isRightKey(SelectionKey key) {
		return key.channel() == client;
	}

	@Override
	public void processSelection(SelectionKey key) throws IOException {
		try {
			int bytesRead = client.read(buffer);
			while (Thread.currentThread().isInterrupted() == false && bytesRead != -1) {
				if (bytesRead != 0) {
					//TODO LoggingHelper.logBytes(buffer.array(), buffer.position(), "Received", logger);
					if (buffer.position() >= 8) {
						buffer.flip();
						wpdu.decode(buffer);
						if (buffer.remaining() >= wpdu.length) {
							ConnectionIdentifier upperLayerKey = new ConnectionIdentifier(wpdu.getDestinationWPort(),
									wpdu.getSourceWPort(), destination);
							listeners.get(upperLayerKey).dataReceived(buffer.array());

							// If bytes are not parsed, assume that they
							// belong to a new pdu. Remove the parsed pdu
							// out of the buffer
							buffer.position(buffer.position() + wpdu.length + 8);
							buffer.compact();
						}
						else {
							// Not all bytes for this pdu have been received
							// wait for remaining part of pdu
							buffer.position(buffer.limit());
						}
						buffer.limit(buffer.capacity());
					}
				}
				else if (client.socket().getInputStream().available() > 0) {
					// Zero bytes have been read, indicating that the buffer
					// is full
					ByteBuffer newbuf = ByteBuffer.allocate(buffer.capacity() * 2);
					newbuf.put(buffer.array());
					buffer = newbuf;
				}

				bytesRead = client.read(buffer);
			}
		} catch (IOException e) {
			connectionLost();
		}
	}
}

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
package org.openmuc.jdlms.client.ip;

import java.net.InetSocketAddress;

import org.openmuc.jdlms.client.ClientConnectionSettings;

/**
 * Subclass of {@link ClientConnectionSettings} to create connections using UDP as sub layer.
 * 
 * After creation, the local port is set to 4059 per default
 * 
 * @author Karsten Mueller-Bier
 */
public class UdpClientConnectionSettings extends ClientConnectionSettings<UdpClientConnectionSettings> {
	private int clientWPort;
	private int serverWPort;
	private InetSocketAddress serverAddress;
	private int localPort = 4059;

	/**
	 * @param serverAddress
	 *            IP address and port of the smart meter
	 * @param serverWPort
	 *            Address of the logical device inside the smart meter
	 * @param clientWPort
	 *            Logical address of the client (used for identification)
	 * @param referencing
	 *            The object referencing method used on the remote station
	 */
	public UdpClientConnectionSettings(InetSocketAddress serverAddress, int serverWPort, int clientWPort,
			ReferencingMethod referencing) {
		super(referencing);
		setClientWPort(clientWPort);
		setServerWPort(serverWPort);
		this.serverAddress = serverAddress;
	}

	public int getClientWPort() {
		return clientWPort;
	}

	/**
	 * Sets the wrapper port for the local end point of the connection
	 * 
	 * @param wport
	 *            Wrapper Port. Must be between 0 and 65536, both exclusive
	 */
	public final UdpClientConnectionSettings setClientWPort(int wport) {
		if (wport < 0 || wport > 0xFFFF) {
			throw new IllegalArgumentException("WPort out of range (0, 65536)");
		}
		clientWPort = wport;

		return this;
	}

	public int getServerWPort() {
		return serverWPort;
	}

	/**
	 * Sets the wrapper port for the remote end point of the connection
	 * 
	 * @param wport
	 *            Wrapper Port. Must be between 0 and 65536, both exclusive
	 */
	public final UdpClientConnectionSettings setServerWPort(int wport) {
		if (wport < 0 || wport > 0xFFFF) {
			throw new IllegalArgumentException("WPort out of range (0, 65536)");
		}
		serverWPort = wport;

		return this;
	}

	public InetSocketAddress getServerAddress() {
		return serverAddress;
	}

	/**
	 * Sets the IP/Port of the remote end point of the connection
	 * 
	 * @param address
	 *            IP address and port of the remote end point
	 */
	public UdpClientConnectionSettings setServerAddress(InetSocketAddress address) {
		serverAddress = address;

		return this;
	}

	public int getLocalPort() {
		return localPort;
	}

	/**
	 * Sets the port of the local end point. Use this method only, if you have to change from the default port (4059)
	 * 
	 * Note that, once a TCP connection has been created, all connections must have the same local TCP port set
	 * 
	 * @param port
	 *            The local port
	 */
	public UdpClientConnectionSettings setLocalPort(int port) {
		if (port < 0 || port > 0xFFFF) {
			throw new IllegalArgumentException("Port out of range (0, 65536)");
		}
		localPort = port;

		return this;
	}

	@Override
	public int hashCode() {
		return (clientWPort << 16 | serverWPort) ^ serverAddress.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof UdpClientConnectionSettings) {
			UdpClientConnectionSettings other = (UdpClientConnectionSettings) o;

			return super.equals(o) && clientWPort == other.clientWPort && serverWPort == other.serverWPort
					&& serverAddress.equals(other.serverAddress);
		}

		return false;
	}

	@Override
	public boolean isFullyParametrized() {
		return super.isFullyParametrized() && clientWPort > 0 && clientWPort < 0xFFFF && serverWPort > 0
				&& serverWPort < 0xFFFF && serverAddress != null;
	}

}

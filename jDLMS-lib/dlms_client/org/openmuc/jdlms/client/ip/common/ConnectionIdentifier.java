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

import java.net.SocketAddress;

/**
 * Immutable class containing all data necessary to differentiate 2 tcp/udp connections from each other
 * 
 * @author Karsten Mueller-Bier
 */
public class ConnectionIdentifier {
	private final int localWPort;
	private final int remoteWPort;
	private final SocketAddress remoteAddress;

	public ConnectionIdentifier(int local, int remote, SocketAddress address) {
		if (local < 0 || local > 0xFFFF || remote < 0 || remote > 0xFFFF) {
			throw new IllegalArgumentException();
		}

		localWPort = local;
		remoteWPort = remote;
		remoteAddress = address;
	}

	public int getLocalWPort() {
		return localWPort;
	}

	public int getRemoteWPort() {
		return remoteWPort;
	}

	public SocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ConnectionIdentifier) {
			ConnectionIdentifier other = (ConnectionIdentifier) obj;

			return localWPort == other.localWPort && remoteWPort == other.remoteWPort
					&& remoteAddress.equals(other.remoteAddress);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return (localWPort << 16 | remoteWPort) ^ remoteAddress.hashCode();
	}
}

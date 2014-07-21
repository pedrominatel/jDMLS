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

/**
 * Factory class creating and buffering an instance of {@link UdpLayer}
 * 
 * @author Karsten Mueller-Bier
 */
public class UdpLayerFactory {

	private static volatile UdpLayer instance;

	/**
	 * Creates a new instance of {@link UdpLayer} or retrieves the formerly created instance
	 * 
	 * @param port
	 *            UDP port to listen to datagrams
	 * @throws IOException
	 *             Error on creating the socket, or if a different port than the first time has been used
	 */
	public UdpLayer build(int port) throws IOException {
		if (instance == null) {
			synchronized (this) {
				if (instance == null) {
					instance = new UdpLayer(port);
				}
			}
		}
		if (instance.getPort() != port) {
			throw new IOException("IPCommunication already established on another port. Requested port: " + port
					+ ". Actual: " + instance.getPort());
		}

		return instance;
	}
}

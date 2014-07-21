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
package org.openmuc.jdlms.client.hdlc.common;

import org.openmuc.jdlms.client.hdlc.HdlcAddress;

/**
 * Bundle of client and server address that uniquely identifies an Hdlc Client connection.
 * 
 * @author Karsten Mueller-Bier
 */
public class HdlcAddressPair {
	public final HdlcAddress client;
	public final HdlcAddress server;

	public HdlcAddressPair(HdlcAddress client, HdlcAddress server) {
		this.client = client;
		this.server = server;
	}

	@Override
	public int hashCode() {
		int hashClient = client != null ? client.getLowerAddress() + client.getUpperAddress() : 0;
		int hashServer = server != null ? server.getLowerAddress() + server.getUpperAddress() : 0;

		return (hashClient + hashServer) * hashServer + hashClient;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HdlcAddressPair) {
			HdlcAddressPair other = (HdlcAddressPair) o;
			if (client.equals(other.client) && server.equals(other.server)) {
				return true;
			}

			if (client.equals(other.client) && HdlcAddress.isCalling(other.server)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return client + ":" + server;
	}
}

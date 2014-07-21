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
package org.openmuc.jdlms.client.hdlc;

import org.openmuc.jdlms.client.ClientConnectionSettings;

/**
 * Subclass of {@link ClientConnectionSettings} to create connections using HDLC as sub layer
 * 
 * @author Karsten Mueller-Bier
 */
public class HdlcClientConnectionSettings extends ClientConnectionSettings<HdlcClientConnectionSettings> {

	private HdlcAddress clientAddress = null;
	private HdlcAddress serverAddress = null;
	private String btAddress = null;
	private boolean useHandshake = true;

	/**
	 * @param portName
	 *            Name of the physical port used to connect with the smart meter (e.g. ttyUSB0 or COM4)
	 * @param client
	 *            HdlcAddress describing the logical address of the client
	 * @param server
	 *            HdlcAddress describing the full address of the smart meter
	 * @param referencing
	 *            The object referencing method used on the remote station
	 */
	public HdlcClientConnectionSettings(String btAddress, HdlcAddress client, HdlcAddress server,
			ReferencingMethod referencing) {
		super(referencing);
		this.btAddress = btAddress;
		clientAddress = client;
		serverAddress = server;
	}

	public HdlcAddress getClientAddress() {
		return clientAddress;
	}

	public HdlcAddress getServerAddress() {
		return serverAddress;
	}

	public String getBtAddress() {
		return btAddress;
	}

	public boolean doesUseHandshake() {
		return useHandshake;
	}

	public HdlcClientConnectionSettings setClientAddress(HdlcAddress value) {
		clientAddress = value;
		return this;
	}

	public HdlcClientConnectionSettings setServerAddress(HdlcAddress value) {
		serverAddress = value;
		return this;
	}

	public HdlcClientConnectionSettings setBtAddress(String value) {
		btAddress = value;
		return this;
	}

	public HdlcClientConnectionSettings setUseHandshake(boolean useHandshake) {
		this.useHandshake = useHandshake;
		return this;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HdlcClientConnectionSettings) {
			HdlcClientConnectionSettings other = (HdlcClientConnectionSettings) o;
			return super.equals(o) && clientAddress.equals(other.clientAddress)
					&& serverAddress.equals(other.serverAddress) && btAddress.equals(other.btAddress);
		}
		return false;
	}

	@Override
	public boolean isFullyParametrized() {
		return super.isFullyParametrized() && clientAddress != null && serverAddress != null && btAddress != null;
	}
}

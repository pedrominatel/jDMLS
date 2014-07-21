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
package org.openmuc.jdlms.client.hdlc.impl;

import java.io.IOException;

import org.openmuc.jdlms.client.hdlc.states.Disconnected;

/**
 * Strategy pattern representing the different states of a {@link HdlcClientLayer}.
 * 
 * @author Karsten Mueller-Bier
 */
public abstract class HdlcClientLayerState {
	public static HdlcClientLayerState beginningState() {
		return Disconnected.instance;
	}

	public abstract void connect(HdlcClientLayer wrapper, long timeout) throws IOException;

	public abstract void send(HdlcClientLayer wrapper, byte[] data, boolean isSegmented) throws IOException;

	public abstract void disconnect(HdlcClientLayer wrapper) throws IOException;

	public abstract void dataReceived(HdlcClientLayer wrapper, byte[] data);

	public abstract void remoteDisconnect(HdlcClientLayer wrapper);
}

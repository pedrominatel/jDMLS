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
package org.openmuc.jdlms.client.communication;

/**
 * This interface provides callback methods that lower layer connection objects use to send data and disconnection
 * messages to upper layer connection objects.
 * 
 * If an object wants to receive data from a lower layer connection, it has to implement this interface
 * 
 * @author Karsten Mueller-Bier
 */
public interface IUpperLayer {
	/**
	 * Callback method, indicating that data has been received from the remote end point. If the lower layer adds frame
	 * header or footer around the sent data, these headers have to be already stripped at this point
	 * 
	 * @param data
	 *            The received data as raw byte array.
	 */
	void dataReceived(byte[] data);

	/**
	 * Callback method, indicating that the remote end point closed the connection. This method is provided to allow the
	 * upper layers to gracefully close the connection and clean up.
	 */
	void remoteDisconnect();
}

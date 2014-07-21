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
package org.openmuc.jdlms.client.hdlc.physical;

/**
 * Callback Interface used by IPhysicalConnection
 * 
 * @author Karsten Mueller-Bier
 */
public interface IPhysicalConnectionListener {
	/**
	 * This method is called if data has been received by the serial interface. The given byte array is the raw buffer
	 * of the serial interface. It is recommended to buffer this array as soon as possible in this method before
	 * proceeding with further actions.
	 * 
	 * @param data
	 *            The received bytes
	 * @param length
	 *            Length of the bytes
	 */
	public void dataReceived(byte[] data, int length);
}

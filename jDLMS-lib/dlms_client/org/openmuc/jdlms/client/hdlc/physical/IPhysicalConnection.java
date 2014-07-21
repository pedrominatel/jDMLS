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

import java.io.IOException;
import java.util.TooManyListenersException;

/**
 * Interface abstracting all access to and from the serial interface.
 * 
 * @author Karsten Mueller-Bier
 */
public interface IPhysicalConnection {
	/**
	 * Sends the given bytes over the serial interface
	 * 
	 * @param data
	 *            Bytes to send
	 * @throws IOException
	 */
	public void send(byte[] data) throws IOException;

	/**
	 * Closes the serial interface, freeing the resource for other programs
	 */
	public void close();

	/**
	 * Changes the connection parameter of the serial interface
	 * 
	 * @param baud
	 *            Baud rate to communicate
	 * @param databits
	 *            Number of Data bits (Range 7-8)
	 * @param stopbits
	 *            Number of Stop bits (Range 0-2)
	 * @param parity
	 *            Parity Bit (Range 0-2)
	 * @throws UnsupportedCommOperationException
	 */
	//public void setBtParams(int baud, int databits, int stopbits, int parity);

	/**
	 * Registers an upper layer as Listener for incoming data
	 * 
	 * @param listener
	 *            The listener to register
	 * @throws TooManyListenersException
	 *             If a listener has already been registered
	 */
	public void registerListener(IPhysicalConnectionListener listener) throws TooManyListenersException;

	/**
	 * Remove the actual listener from this serial interface
	 */
	public void removeListener();

	public boolean isClosed();
}

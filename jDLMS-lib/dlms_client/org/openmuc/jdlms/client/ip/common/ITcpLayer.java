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
import java.util.TooManyListenersException;

import org.openmuc.jdlms.client.communication.IUpperLayer;

/**
 * Interface used for communicating over an TCP connection
 * 
 * @author Karsten Mueller-Bier
 */
public interface ITcpLayer {

	/**
	 * Establishes an connection to the remote smart meter
	 * 
	 * @throws IOException
	 */
	public abstract void connect() throws IOException;

	/**
	 * Closes the connection to the remote smart meter
	 * 
	 * @throws IOException
	 */
	public abstract void disconnect() throws IOException;

	/**
	 * Sends data to the remote smart meter
	 * 
	 * Prior to sending data, the connection needs to be successfully established using {@link ITcpLayer#connect()}
	 * 
	 * @param data
	 *            Data to send
	 * @throws IOException
	 */
	public abstract void send(byte[] data) throws IOException;

	/**
	 * Register the given IReceivingListener as upper layer client, able to receive WPDUs from this connection.
	 * 
	 * This method has to be called on the connecting step, before any CosemPDUs are sent over the particular upper
	 * layer.
	 * 
	 * @param key
	 *            Key to identify this listener
	 * @param listener
	 *            The upper layer object to register
	 * @throws TooManyListenersException
	 *             If another listener is already registered with the given key
	 */
	public abstract void registerListener(ConnectionIdentifier key, IUpperLayer listener)
			throws TooManyListenersException;

	/**
	 * Removes the IReceivingListener behind the given key from this connection. Further WPDUs with its WPort as
	 * destination are discarded.
	 * 
	 * Use this method to 'close' the connection to the particular remote server.
	 * 
	 * @param key
	 *            Key of the listener to remove
	 */
	public abstract void removeListener(ConnectionIdentifier key);

}

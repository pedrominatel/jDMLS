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

import java.io.IOException;

/**
 * This interface is to be implemented by lower layer connection objects to provide a consistent calling API for its
 * using upper layer connection object.
 * 
 * This way, the lower layer connection object can be switched (e.g. from HDLC to IP) without changing the code of the
 * upper layer connection
 * 
 * Each instance of a class implementing this interface is a remote connection to another end point. If a user wants to
 * remotely read 2 Smart Meters simultaneously, 2 objects implementing the {@code ITransportLayer} interface are needed.
 * 
 * @author Karsten Mueller-Bier
 */
public interface ILowerLayer<E> {
	/**
	 * Connects to the defined remote end point. Note that the destination end point is fixed upon creation, if you want
	 * to connect to another end point, you have to create another object implementing this Interface
	 * 
	 * @throws IOException
	 */
	void connect(long timeout) throws IOException;

	/**
	 * Sends the passed data to the remote client.
	 * 
	 * @param data
	 *            Data to be sent
	 * @throws IOException
	 */
	void send(byte[] data) throws IOException;

	/**
	 * Gracefully close the connection to the remote end point. It is up to the lower layer connection implementing this
	 * interface if the remote end point shall be sent a disconnection message or not
	 * 
	 * @throws IOException
	 */
	void disconnect() throws IOException;

	/**
	 * Registers an upper layer connection object to the lower layer connection object. This method should be called
	 * before the first call of the connect method to ensure receiving of all incoming data Depending of the connection,
	 * several higher layer connections can be mapped to the same lower layer connection (e.g. multiple logical devices
	 * connected through the same physical device), so an additional address field has to be provided to differentiate
	 * between the upper layer connections
	 * 
	 * @param key
	 *            Key to uniquely identify the listener. May be null depending on the implementing ITransportLayer
	 *            object (e.g. a connection that only allows one upper layer at a time)
	 * @param listener
	 *            The upper layer connection to be registered
	 * @throws IllegalArgumentException
	 *             If address is already in use
	 */
	void registerReceivingListener(E key, IUpperLayer listener) throws IllegalArgumentException;

	/**
	 * Unregisters an upper layer connection object. This method should be called after the last call of the disconnect
	 * method
	 * 
	 * @param listener
	 *            The upper layer connection to be unregistered
	 */
	void removeReceivingListener(IUpperLayer listener);

	/**
	 * Discards the message with the given data bytes if the underlying layer buffers messages. This method is called if
	 * an upper layer times out and the request is aborted
	 * 
	 * @param data
	 *            Data of the to discarded message
	 */
	void discardMessage(byte[] data);
}

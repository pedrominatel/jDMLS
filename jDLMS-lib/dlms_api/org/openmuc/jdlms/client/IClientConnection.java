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
package org.openmuc.jdlms.client;

import java.io.IOException;
import java.util.List;

/**
 * This interface is the central user API to communicate over DLMS/Cosem. Each instance of this interface is a single
 * connection to another smart meter.
 * 
 * @author Karsten Mueller-Bier
 */
public interface IClientConnection {

	/**
	 * Connects to the remote smart meter specified at the construction of the communication object. This method returns
	 * after the connection has been successfully established and authentication is completed
	 * 
	 * Use this method only if {@link ClientConnectionSettings.Authentication#LOWEST} Authentication is used
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the connection is aborted
	 * @throws IOException
	 */
	public void connect(long timeout) throws IOException;

	/**
	 * Connects to the remote smart meter specified at the construction of the communication object. This method returns
	 * after the connection has been successfully established and authentication is completed
	 * 
	 * Use this method if {@link ClientConnectionSettings.Authentication#LOW},
	 * {@link ClientConnectionSettings.Authentication#HIGH_MD5} or
	 * {@link ClientConnectionSettings.Authentication#HIGH_SHA1} Authentication is used.
	 * 
	 * Passing null as second argument results in the same behavior as calling {@link IClientConnection#connect(long)}
	 * directly.
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the connection is aborted
	 * @param secret
	 *            Password to access smart meter, null if no password needed
	 * @throws IOException
	 */
	public void connect(long timeout, byte[] secret) throws IOException;

	/**
	 * Connects to the remote smart meter specified at the construction of the communication object. This method returns
	 * after the connection has been successfully established and authentication is completed
	 * 
	 * Use this method only if {@link ClientConnectionSettings.Authentication#HIGH_MANUFACTORER} Authentication is used.
	 * 
	 * Passing null as third argument results in the same behavior as calling
	 * {@link IClientConnection#connect(long, byte[])} directly.
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the connection is aborted
	 * @param secret
	 *            Password to access the remote smart meter
	 * @param processor
	 *            Object used to process the received server to client challenge combined with the afore-mentioned
	 *            password
	 * @throws IOException
	 */
	public void connect(long timeout, byte[] secret, HlsSecretProcessor processor) throws IOException;

	/**
	 * Convenience method to call {@code disconnect(true)}
	 * 
	 * @see #disconnect(boolean)
	 */
	public void disconnect();

	/**
	 * Disconnects connection to remote smart meter
	 * 
	 * @param sendDisconnectMessage
	 *            If a message to release the connection shall be sent to the remote client. This parameter must be true
	 *            on connectionless lower layer protocols (e.g. UDP) or if you want to give the remote end point a
	 *            chance to gracefully close the connection
	 */
	public void disconnect(boolean sendDisconnectMessage);

	/**
	 * Convenience method to call {@code get(timeout, false, params)}
	 * 
	 * @see #get(long, boolean, GetRequest...)
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the request is aborted
	 * @param params
	 *            Varargs of specifiers which attributes to send (See {@link GetRequest})
	 * @return List of results from the smart meter in the same order as the requests
	 * @throws IOException
	 */
	public List<GetResult> get(long timeout, GetRequest... params) throws IOException;

	/**
	 * Requests the remote smart meter to send the values of one or several attributes
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the request is aborted
	 * @param highPriority
	 *            Sends this request with high priority, if supported
	 * @param params
	 *            Varargs of specifiers which attributes to send (See {@link GetRequest})
	 * @return List of results from the smart meter in the same order as the requests
	 * @throws IOException
	 */
	public List<GetResult> get(long timeout, boolean highPriority, GetRequest... params) throws IOException;

	/**
	 * Convenience method to call {@code set(timeout, false, params)}
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the request is aborted
	 * @param params
	 *            Varargs of specifier which attributes to set to which values (See {@link SetRequest})
	 * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
	 *         to false on creation of this object. A true value indicates that this particular value has been
	 *         successfully set
	 * @throws IOException
	 */
	public List<AccessResultCode> set(long timeout, SetRequest... params) throws IOException;

	/**
	 * Requests the remote smart meter to set one or several attributes to the committed values
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the request is aborted
	 * @param params
	 *            Varargs of specifier which attributes to set to which values (See {@link SetRequest})
	 * @param highPriority
	 *            Sends this request with high priority, if supported
	 * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
	 *         to false on creation of this object. A true value indicates that this particular value has been
	 *         successfully set
	 * @throws IOException
	 */
	public List<AccessResultCode> set(long timeout, boolean highPriority, SetRequest... params) throws IOException;

	/**
	 * Convenience method to call {@code action(timeout, false, params)}
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the request is aborted
	 * @param params
	 *            List of specifier which methods to be called and, if needed, what parameters to call (See
	 *            {@link MethodRequest}
	 * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
	 *         to false on creation of this object
	 */
	public List<MethodResult> action(long timeout, MethodRequest... params) throws IOException;

	/**
	 * Requests the remote smart meter to call one or several methods with or without committed parameters
	 * 
	 * @param timeout
	 *            Amount of milliseconds waited before the request is aborted
	 * @param params
	 *            List of specifier which methods to be called and, if needed, what parameters to call (See
	 *            {@link MethodRequest}
	 * @param highPriority
	 *            Sends this request with high priority, if supported
	 * @return List of results from the smart meter in the same order as the requests or null if confirmed has been set
	 *         to false on creation of this object
	 */
	public List<MethodResult> action(long timeout, boolean highPriority, MethodRequest... params) throws IOException;

	/**
	 * Register an EventListener on this connection. All future events coming from the remote smart meter will be
	 * forwarded to that EventListener. If another EventListener is already registered, this method may throw a runtime
	 * exception.
	 * 
	 * @param listener
	 *            The EventListener to register
	 */
	public void registerEventListener(IEventListener listener);

	/**
	 * Remove an EventListener from this connection. Call this method if you don't want to receive future events coming
	 * from the remote smart meter
	 * 
	 * @param listener
	 *            The EventListener to remove
	 */
	public void removeEventListener(IEventListener listener);

	/**
	 * Checks if the connection to the remote smart meter is established
	 * 
	 * @return true if the connection is active
	 */
	public boolean isConnected();
}

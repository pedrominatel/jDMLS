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

import java.util.ServiceLoader;

/**
 * Basic settings all DLMS connections must at least have.
 * <p>
 * This class is never used directly, always instantiate one of the known subclasses.
 * </p>
 * <p>
 * After creation, the following options are set as default:
 * <ul>
 * <li>{@link ConfirmedMode} = {@link ConfirmedMode#CONFIRMED Confirmed}</li>
 * <li>{@link Authentication} = {@link Authentication#LOWEST Lowest}</li>
 * </ul>
 * </p>
 * 
 * @author Karsten Mueller-Bier
 */
public abstract class ClientConnectionSettings<E extends ClientConnectionSettings<E>> {

	private static IClientConnectionFactory factory;
	private static final Object lock = new Object();

	/**
	 * Entry function to get a {@link IClientConnectionFactory} instance.
	 * 
	 * @return {@link IClientConnectionFactory} instance to create {@link IClientConnection} objects
	 */
	public static IClientConnectionFactory getFactory() {
		if (factory == null) {
			synchronized (lock) {
				if (factory == null) {
					factory = ServiceLoader.load(IClientConnectionFactory.class).iterator().next();
				}
			}
		}
		return factory;
	}

	/**
	 * Used to set if the smart meter shall answer to each message sent
	 * 
	 * @author Karsten Mueller-Bier
	 */
	public static enum ConfirmedMode {
		/**
		 * Smart meter shall acknowledge each packet and sent results of the computed message
		 */
		CONFIRMED,
		/**
		 * Smart meter shall neither acknowledge received packets nor sent results of computed messages.
		 */
		UNCONFIRMED
	}

	/**
	 * Used to determine which sort of name referencing the remote smart meter uses.
	 * 
	 * @author Karsten Mueller-Bier
	 */
	public static enum ReferencingMethod {
		/**
		 * Use logical name referencing on this connection
		 */
		LN,
		/**
		 * Use short name referencing on this connection
		 */
		SN
	}

	/**
	 * Used to set the level of authentication on establishing the connection to the smart meter
	 * 
	 * @author Karsten Mueller-Bier
	 */
	public static enum Authentication {
		/**
		 * No authentication used. Parameter secret on {@link IClientConnection#connect(long, byte[])} will be ignored
		 */
		LOWEST,
		/**
		 * Authentication of the client by sending a shared password as secret
		 */
		LOW,
		/**
		 * Authentication of both client and smart meter by manufacturer specific method
		 */
		HIGH_MANUFACTORER,
		/**
		 * Authentication of both client and smart meter using MD5 and a pre shared secret password
		 */
		HIGH_MD5,
		/**
		 * Authentication of both client and smart meter using SHA-1 and a pre shared secret password
		 */
		HIGH_SHA1
	}

	private ConfirmedMode confirmedMode;
	private ReferencingMethod referencingMethod;
	private Authentication authentication;

	protected ClientConnectionSettings(ReferencingMethod referencing) {
		this.confirmedMode = ConfirmedMode.CONFIRMED;
		this.authentication = Authentication.LOWEST;
		this.referencingMethod = referencing;
	}

	/**
	 * @return {@link ConfirmedMode} that will be used
	 */
	public ConfirmedMode getConfirmedMode() {
		return confirmedMode;
	}

	/**
	 * @return {@link ReferencingMethod} that will be used
	 */
	public ReferencingMethod getReferencingMethod() {
		return referencingMethod;
	}

	/**
	 * @return {@link Authentication} that will be used
	 */
	public Authentication getAuthentication() {
		return authentication;
	}

	public E setConfirmedMode(ConfirmedMode value) {
		confirmedMode = value;
		return self();
	}

	public E setReferencingMethod(ReferencingMethod value) {
		referencingMethod = value;
		return self();
	}

	public E setAuthentication(Authentication value) {
		authentication = value;
		return self();
	}

	/**
	 * Helper method to check if all necessary parameters are set.
	 * 
	 * @return true if a IClientConnection object can be created by passing this settings into a factory
	 */
	public boolean isFullyParametrized() {
		return confirmedMode != null && referencingMethod != null && authentication != null;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ClientConnectionSettings) {
			ClientConnectionSettings<?> other = (ClientConnectionSettings<?>) o;
			return this.confirmedMode == other.confirmedMode && this.referencingMethod == other.referencingMethod
					&& this.authentication == other.authentication;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.confirmedMode.hashCode() + this.referencingMethod.hashCode() + this.authentication.hashCode();
	}

	/**
	 * Helper method used internally
	 * 
	 * @return The actual object, cast to the actual object type
	 */
	@SuppressWarnings("unchecked")
	private E self() {
		return (E) this;
	}
}

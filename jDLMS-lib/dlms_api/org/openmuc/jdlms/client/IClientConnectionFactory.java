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

/**
 * Interface of a factory for IClientConnection objects.
 * 
 * Using Java, you can create this factory using {@code new ClientConnectionFactory()} or by calling
 * {@link ClientConnectionSettings#getFactory()}
 * 
 * @author Karsten Mueller-Bier
 */
public interface IClientConnectionFactory {
	/**
	 * Creates a new connection using the given settings or returns a pooled connection, if a connection with exactly
	 * this settings has already been created prior to this call
	 * 
	 * @param settings
	 *            Connection parameters to use
	 * @return The created connection object that has the given settings
	 * @throws IOException
	 *             Creation of underlying layers was unsuccessful
	 */
	IClientConnection createClientConnection(@SuppressWarnings("rawtypes") ClientConnectionSettings settings)
			throws IOException;
}

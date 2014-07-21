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
package org.openmuc.jdlms.client.impl;

import java.io.IOException;

import org.openmuc.jdlms.client.ClientConnectionSettings;
import org.openmuc.jdlms.client.communication.ILowerLayer;

/**
 * Interface for sub layer factories used by {@link ClientConnectionFactory}
 * 
 * @author Karsten Mueller-Bier
 */
public interface ILowerLayerFactory {
	/**
	 * Creates a sub Layer connection using the given settings
	 * 
	 * @param settings
	 *            Settings this sub layer shall use
	 * @return The created sub Layer
	 * @throws IOException
	 *             Creation failed
	 */
	ILowerLayer<Object> build(ClientConnectionSettings<?> settings) throws IOException;

	/**
	 * Checks if this ITransportLayerFactory instance accepts the given class in it's build method
	 * 
	 * @param clazz
	 *            ClientConnectionSettings class to check
	 * @return true of this instance can use the given class in its build method
	 */
	boolean accepts(Class<?> clazz);
}

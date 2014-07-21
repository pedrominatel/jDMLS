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

/**
 * Factory class to create a PhysicalConnection object using RXTX serial
 * communication library
 * 
 * @author Karsten Mueller-Bier
 * 
 */
public class PhysicalConnectionFactory {

	/**
	 * Tries to acquire the port named in the constructor
	 * 
	 * @throws NoSuchPortException
	 * @throws PortInUseException
	 * @throws IOException
	 * @throws UnsupportedCommOperationException
	 */
	public IPhysicalConnection acquireSerialPort(String portName)
			throws IOException {

		return null;
	}
}

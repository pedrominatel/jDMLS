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

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * This class is used to parse the Header of a WPDU from the received ByteBuffer
 * 
 * @author Karsten Mueller-Bier
 */
public class WpduHeader {

	int version;
	int sourceWPort;
	int destinationWPort;
	int length;

	/**
	 * @return The version field of the last successfully parsed WPDU
	 */
	public int getVersion() {
		return version;
	}

	/**
	 * @return The source WPort field of the last successfully parsed WPDU
	 */
	public int getSourceWPort() {
		return sourceWPort;
	}

	/**
	 * @return The destination WPort field of the last successfully parsed WPDU
	 */
	public int getDestinationWPort() {
		return destinationWPort;
	}

	/**
	 * @return The length field of the last successfully parsed WPDU
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @return The length of an WPDU header, fixed at 8 bytes
	 */
	public int getHeaderLength() {
		return 8;
	}

	/**
	 * Try to decode a WPDU header from the last received bytes
	 * 
	 * @param data
	 *            ByteBuffer holding the received data
	 */
	public void decode(ByteBuffer data) {
		if (data.remaining() < getHeaderLength()) {
			throw new IllegalArgumentException("Size too small for complete WPDU header");
		}

		ShortBuffer buffer = data.asReadOnlyBuffer().asShortBuffer();
		version = buffer.get() & 0xFFFF;
		sourceWPort = buffer.get() & 0xFFFF;
		destinationWPort = buffer.get() & 0xFFFF;
		length = buffer.get() & 0xFFFF;
	}
}

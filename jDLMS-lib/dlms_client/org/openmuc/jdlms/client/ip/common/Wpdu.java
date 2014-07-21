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
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * Class representing a WPDU send and received between client and smart meter
 * 
 * @author Karsten Mueller-Bier
 */
public class Wpdu {
	private int sourceWPort = -1;
	private int destinationWPort = -1;
	private int length = 0;

	private byte[] data = null;

	public Wpdu() {
	}

	/**
	 * @return Source WPort of this WPDU
	 */
	public int getSourceWPort() {
		return sourceWPort;
	}

	/**
	 * Sets the source WPort of this WPDU
	 * 
	 * @param newValue
	 *            New source WPort. Has to be between 0 and 65536, both excluding
	 */
	public void setSourceWPort(int newValue) {
		if (newValue < 0 && newValue > 0xFFFF) {
			throw new IllegalArgumentException("Source WPort out of range [0, 65535]");
		}
		sourceWPort = newValue;
	}

	/**
	 * @return Destination WPort of this WPDU
	 */
	public int getDestinationWPort() {
		return destinationWPort;
	}

	/**
	 * Sets the destination WPort of this WPDU
	 * 
	 * @param newValue
	 *            New destination WPort. Has to be between 0 and 65536, both excluding
	 */
	public void setDestinationWPort(int newValue) {
		if (newValue < 0 && newValue > 0xFFFF) {
			throw new IllegalArgumentException("Destination WPort out of range [0, 65535]");
		}
		destinationWPort = newValue;
	}

	/**
	 * @return Length of the data field of this WPDU in bytes
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @return Byte array of the data of this WPDU
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * Sets the data field of this WPDU. Also updates the length field, after the data has been successfully set.
	 * 
	 * @param newValue
	 *            Data to transmit with this WPDU
	 */
	public void setData(byte[] newValue) {
		if (newValue == null) {
			throw new IllegalArgumentException("Data is null");
		}
		if (newValue.length > 0xFFFF) {
			throw new IllegalArgumentException("Length of data out of range [0, 65535]");
		}

		data = newValue;
		length = data.length;
	}

	/**
	 * Encodes this WPDU to a byte array
	 * 
	 * @return The encoded byte array
	 * @throws IOException
	 */
	public byte[] encode() throws IOException {
		if (sourceWPort == -1 || destinationWPort == -1) {
			throw new IOException("Wpdu not initialized. WPort not set");
		}

		ByteBuffer result = ByteBuffer.allocate(length + 8);
		result.putShort((short) 1);
		result.putShort((short) (sourceWPort & 0xFFFF));
		result.putShort((short) (destinationWPort & 0xFFFF));
		result.putShort((short) (length & 0xFFFF));

		result.put(data);

		return result.array();
	}

	/**
	 * Decodes a WPDU from the received data stream
	 * 
	 * @param iStream
	 *            InputStream containing the encoded WPDU
	 * @throws IOException
	 */
	public void decode(InputStream iStream) throws IOException {
		int high = iStream.read();
		int low = iStream.read();

		int version = high << 8 | low;
		if (version != 1) {
			throw new IOException("Wpdu Header version unknown: " + version);
		}

		high = iStream.read();
		low = iStream.read();
		sourceWPort = high << 8 | low;
		if (sourceWPort < 0) {
			throw new IOException("Decoded source WPort has negative value");
		}

		high = iStream.read();
		low = iStream.read();
		destinationWPort = high << 8 | low;
		if (destinationWPort < 0) {
			throw new IOException("Decoded destination WPort has negative value");
		}

		high = iStream.read();
		low = iStream.read();
		length = high << 8 | low;
		if (length < 0) {
			throw new IOException("Decoded length has negative value");
		}

		data = new byte[length];
		iStream.read(data);
	}
}

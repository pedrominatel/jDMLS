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
package org.openmuc.jdlms.client.hdlc.common;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * This class represents optional parameter that are negotiated during the connection phase between client and server on
 * the HDLC layer.
 * 
 * For more information, see IEC 62056-46 section 6.4.4.4.3.2 and ISO 13239 section 5.5.3.2.2
 * 
 * @author Karsten Mueller-Bier
 */
public class HdlcParameterNegotiation {
	public static final int MIN_INFORMATION_LENGTH = 128;
	public static final int MAX_INFORMATION_LENGTH = 2030;
	public static final int MIN_WINDOW_SIZE = 1;
	public static final int MAX_WINDOW_SIZE = 7;

	private static final int FORMAT_IDENTIFIER = 0x81;
	private static final int HDLC_PARAM_IDENTIFIER = 0x80;
	private static final int USER_PARAM_IDENTIFIER = 0xF0;

	private int transmitInformationLength;
	private int receiveInformationLength;
	private int transmitWindowSize;
	private int receiveWindowSize;

	public HdlcParameterNegotiation() {
		transmitInformationLength = receiveInformationLength = MIN_INFORMATION_LENGTH;
		transmitWindowSize = receiveWindowSize = MIN_WINDOW_SIZE;
	}

	public int getTransmitInformationLength() {
		return transmitInformationLength;
	}

	public int getReceiveInformationLength() {
		return receiveInformationLength;
	}

	public int getTransmitWindowSize() {
		return transmitWindowSize;
	}

	public int getReceiveWindowSize() {
		return receiveWindowSize;
	}

	public void setTransmitInformationLength(int value) {
		int newVal = Math.max(value, MIN_INFORMATION_LENGTH);
		newVal = Math.min(newVal, MAX_INFORMATION_LENGTH);
		transmitInformationLength = newVal;
		;
	}

	public void setReceiveInformationLength(int value) {
		int newVal = Math.max(value, MIN_INFORMATION_LENGTH);
		newVal = Math.min(newVal, MAX_INFORMATION_LENGTH);
		receiveInformationLength = newVal;
	}

	public void setTransmitWindowSize(int value) {
		int newVal = Math.max(value, MIN_WINDOW_SIZE);
		newVal = Math.min(newVal, MAX_WINDOW_SIZE);
		transmitWindowSize = newVal;
	}

	public void setReceiveWindowSize(int value) {
		int newVal = Math.max(value, MIN_WINDOW_SIZE);
		newVal = Math.min(newVal, MAX_WINDOW_SIZE);
		receiveWindowSize = newVal;
	}

	/**
	 * Decodes a byte array containing HDLC parameters into this HdlcParameterNegotiation object.
	 * 
	 * Note: User defined parameters will be ignored
	 * 
	 * @param iStream
	 * @throws IOException
	 * @throws FrameInvalidException
	 */
	public void decode(InputStream iStream) throws IOException, FrameInvalidException {
		int byteRead, length;

		byteRead = iStream.read();
		if (byteRead != FORMAT_IDENTIFIER) {
			throw new FrameInvalidException("Information field is no Hdlc parameter negotiation");
		}

		byteRead = iStream.read();
		while (byteRead != -1) {
			if (byteRead == USER_PARAM_IDENTIFIER) {
				length = iStream.read();
				iStream.skip(length);
			}
			else if (byteRead == HDLC_PARAM_IDENTIFIER) {
				int numOfRemainingBytes = iStream.read();
				while (numOfRemainingBytes > 0) {
					int paramIdent = 0;
					int paramLength = 0;

					paramIdent = iStream.read();
					paramLength = iStream.read();

					if (paramIdent == 0x05) {
						transmitInformationLength = readData(iStream, paramLength);
					}
					else if (paramIdent == 0x06) {
						receiveInformationLength = readData(iStream, paramLength);
					}
					else if (paramIdent == 0x07) {
						transmitWindowSize = readData(iStream, paramLength);
					}
					else if (paramIdent == 0x08) {
						receiveWindowSize = readData(iStream, paramLength);
					}
					else {
						throw new FrameInvalidException("Hdlc parameter unknown");
					}

					numOfRemainingBytes -= (2 + paramLength);
				}
			}

			byteRead = iStream.read();
		}
	}

	/**
	 * Encodes the HdlcParameterNegotiation object into a byte sequence with the smallest number of bytes possible
	 * 
	 * @return The encoded object
	 */
	public byte[] encode() {
		byte[] result = null;
		ByteBuffer buffer = ByteBuffer.allocate(20);

		if (transmitInformationLength != MIN_INFORMATION_LENGTH) {
			buffer.put((byte) 0x05);
			if (transmitInformationLength > 255) {
				buffer.put((byte) 2);
				buffer.putShort((short) transmitInformationLength);
			}
			else {
				buffer.put((byte) 1);
				buffer.put((byte) transmitInformationLength);
			}
		}
		if (receiveInformationLength != MIN_INFORMATION_LENGTH) {
			buffer.put((byte) 0x06);
			if (receiveInformationLength > 255) {
				buffer.put((byte) 2);
				buffer.putShort((short) receiveInformationLength);
			}
			else {
				buffer.put((byte) 1);
				buffer.put((byte) receiveInformationLength);
			}
		}
		if (transmitWindowSize != MIN_WINDOW_SIZE) {
			buffer.put((byte) 0x07);
			buffer.put((byte) 4);
			buffer.putInt(transmitWindowSize);
		}
		if (receiveWindowSize != MIN_WINDOW_SIZE) {
			buffer.put((byte) 0x08);
			buffer.put((byte) 4);
			buffer.putInt(receiveWindowSize);
		}

		int size = buffer.position();

		if (size != 0) {
			buffer.rewind();

			result = new byte[size + 3];
			result[0] = (byte) FORMAT_IDENTIFIER;
			result[1] = (byte) HDLC_PARAM_IDENTIFIER;
			result[2] = (byte) size;
			buffer.get(result, 3, size);
		}

		return result;
	}

	private int readData(InputStream iStream, final int length) throws IOException {
		int result = 0;

		for (int i = 0; i < length; i++) {
			result = (result << 8) | iStream.read();
		}

		return result;
	}
}

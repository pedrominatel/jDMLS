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
package org.openmuc.jdlms.client.hdlc;

import java.io.IOException;
import java.io.InputStream;

/**
 * Represents an immutable HdlcAddress consisting of an upper address (logical device) and a lower address (physical
 * device).
 * 
 * The size of an HdlcAddress can be either 1, 2 or 4 bytes. Note that for client addresses, only 1 Byte long
 * HdlcAddresses are allowed
 * 
 * @author Karsten Mueller-Bier
 */
public class HdlcAddress {
	private int byteLength = 0;
	private int upperAddress = 0;
	private int lowerAddress = 0;

	/**
	 * Creates an HdlcAddress of 1 byte with logical address 0 (NO_STATION address)
	 */
	public HdlcAddress() {
		this(0, 0, 1);
	}

	/**
	 * Creates an HdlcAddress of 1 byte. An HDLC client may only have an address of 1 byte
	 * 
	 * @param upperAddress
	 *            Logical address
	 */
	public HdlcAddress(int upperAddress) {
		this(upperAddress, 0, 1);
	}

	/**
	 * Creates an HdlcAddress of defined length. Beware, that any length other than 1, 2 and 4 will throw an Exception
	 * on invoking {@link HdlcAddress#encode()} on that object
	 * 
	 * @param upperAddress
	 *            Upper part of the address (Logical Device)
	 * @param lowerAddress
	 *            Lower part of the address (Physical Device)
	 * @param length
	 *            Length of the HdlcAddress in bytes
	 */
	public HdlcAddress(int upperAddress, int lowerAddress, int length) {
		this.upperAddress = upperAddress;
		this.lowerAddress = lowerAddress;
		byteLength = length;
	}

	public int getUpperAddress() {
		return upperAddress;
	}

	public int getLowerAddress() {
		return lowerAddress;
	}

	public int getByteSize() {
		return byteLength;
	}

	/**
	 * Encodes the HDLC address into a byte array
	 * 
	 * @return the encoded address
	 * @throws IllegalArgumentException
	 *             If this HdlcAddress object is invalid
	 */
	public byte[] encode() {
		if (isValidAddress() == false) {
			throw new IllegalArgumentException("HdlcAddress has a invalid bytelength");
		}

		int upperLength = (byteLength + 1) / 2;
		int lowerLength = byteLength / 2;

		byte[] result = new byte[byteLength];

		for (int i = 0; i < upperLength; i++) {
			int shift = 7 * (upperLength - i - 1);
			result[i] = (byte) ((upperAddress & (0x7F << shift)) >> (shift) << 1);
		}
		for (int i = 0; i < lowerLength; i++) {
			int shift = 7 * (upperLength - i - 1);
			result[upperLength + i] = (byte) ((lowerAddress & (0x7F << shift)) >> (shift) << 1);
		}
		// Setting stop bit
		result[byteLength - 1] |= 1;

		return result;
	}

	/**
	 * Reads an HDLC address from the InputStream and generates an HdlcAddress object.
	 * 
	 * @param iStream
	 * @return The generated HdlcAddress
	 * @throws IOException
	 */
	public static HdlcAddress decode(InputStream iStream) throws IOException {
		int buffer = 0;
		int length = 0;
		int read = 0;

		while ((read & 0x01) == 0) {
			read = iStream.read();
			buffer = (buffer << 8) | read;
			length++;
		}

		byte[] code = new byte[length];
		for (int i = length - 1; i >= 0; i--) {
			code[i] = (byte) (buffer & 0xFF);
			buffer >>>= 8;
		}
		return decode(code);
	}

	/**
	 * Generates an HDLC address from a byte array.
	 * 
	 * WARNING: No sanity check is made to make sure if the generated address is in fact valid. It is assumed that all
	 * bytes inside the array belong to one HDLC address
	 * 
	 * @param code
	 * @return HdlcAddress decoded from the byte array
	 */
	private static HdlcAddress decode(byte[] code) {
		int lower = 0, upper = 0;
		int upperLength = (code.length + 1) / 2;
		int lowerLength = code.length / 2;

		for (int i = 0; i < upperLength; i++) {
			upper = (upper << 7) | (code[i] >> 1);
		}
		for (int i = 0; i < lowerLength; i++) {
			lower = (lower << 7) | (code[upperLength + i] >> 1);
		}

		return new HdlcAddress(upper, lower, code.length);
	}

	/**
	 * Checks if the HdlcAddress object represents a valid HDLC address according to IEC 62056-46 6.4.2.2
	 * 
	 * If this method returns false, encode() will throw an {@link IllegalArgumentException}
	 * 
	 * @return true if the HDLC address is valid
	 */
	public boolean isValidAddress() {
		// According to IEC 62056-46, addresses with a byteLength, that are
		// neither 1, 2 or 4, are illegal
		if (byteLength != 1 && byteLength != 2 && byteLength != 4) {
			return false;
		}

		int upperLength = (byteLength + 1) / 2;
		int lowerLength = byteLength / 2;

		if (upperAddress >= Math.pow(2, 7 * upperLength) || lowerAddress >= Math.pow(2, 7 * lowerLength)
				|| upperAddress < 0 || lowerAddress < 0) {
			return false;
		}

		return true;
	}

	/**
	 * Generates a String containing the hexadecimal representation of this HdlcAddress, including leading zeroes until
	 * {@code byteLength} bytes are written.
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int upperAddressNumbers = ((byteLength + 1) / 2) * 2;
		int lowerAddressNumbers = ((byteLength) / 2) * 2;
		String hex = Integer.toHexString(upperAddress);
		for (int i = hex.length(); i < upperAddressNumbers; i++) {
			sb.append("0");
		}
		sb.append(hex);

		if (lowerAddressNumbers > 0) {
			hex = Integer.toHexString(lowerAddress);
			for (int i = hex.length(); i < lowerAddressNumbers; i++) {
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * Checks if the HdlcAddress is a reserved broadcast address Reserved broadcast addresses may never be the source of
	 * a message
	 * 
	 * @param addr
	 *            HdlcAddress to check
	 * @return true if the address is a broadcast address
	 */
	public static boolean isAllStation(HdlcAddress addr) {
		if (addr.byteLength == 1 || addr.byteLength == 2) {
			return addr.upperAddress == ReservedAddresses.SERVER_UPPER_ALL_STATIONS_1BYTE;
		}
		else if (addr.byteLength == 4) {
			return addr.upperAddress == ReservedAddresses.SERVER_UPPER_ALL_STATIONS_2BYTE;
		}
		return false;
	}

	/**
	 * Checks if the HdlcAddress is a reserved no station address Reserved no station addresses may never be the source
	 * of a message.
	 * 
	 * @param addr
	 *            HdlcAddress to check
	 * @return true if the address is a no station address
	 */
	public static boolean isNoStation(HdlcAddress addr) {
		return addr.upperAddress == ReservedAddresses.NO_STATION && addr.lowerAddress == ReservedAddresses.NO_STATION;
	}

	/**
	 * Checks if the HdlcAddress is a reserved calling station address Reserved calling station addresses may only be
	 * sent from the server to send an event to the client
	 * 
	 * @param addr
	 *            HdlcAddress to check
	 * @return true if the address is a calling station address
	 */
	public static boolean isCalling(HdlcAddress addr) {
		if (addr.byteLength == 2) {
			return addr.lowerAddress == ReservedAddresses.SERVER_LOWER_CALLING_1BYTE;
		}
		else if (addr.byteLength == 4) {
			return addr.lowerAddress == ReservedAddresses.SERVER_LOWER_CALLING_2BYTE;
		}
		return false;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof HdlcAddress) {
			HdlcAddress other = (HdlcAddress) o;

			return byteLength == other.byteLength && upperAddress == other.upperAddress
					&& lowerAddress == other.lowerAddress;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return upperAddress << 16 | lowerAddress;
	}

	/**
	 * HdlcAddresses with special meanings.
	 * 
	 * @author Karsten Müller-Bier
	 */
	public static class ReservedAddresses {
		/**
		 * Guaranteed to be received by no one
		 */
		public static final int NO_STATION = 0x00;

		/**
		 * Guaranteed to be received by no client
		 */
		public static final HdlcAddress CLIENT_NO_STATION = new HdlcAddress();
		/**
		 * Identifies client as management process.
		 * <p>
		 * Not supported by all remote stations
		 * </p>
		 */
		public static final HdlcAddress CLIENT_MANAGEMENT_PROCESS = new HdlcAddress(0x01);
		/**
		 * Identifies client as public client.
		 * <p>
		 * No password is needed to access remote station with public client. On the other hand public clients have the
		 * fewest rights.
		 * </p>
		 */
		public static final HdlcAddress CLIENT_PUBLIC_CLIENT = new HdlcAddress(0x10);
		/**
		 * Client address used by remote stations to send a broadcast message.
		 */
		public static final HdlcAddress CLIENT_ALL_STATION = new HdlcAddress(0x7F);

		/**
		 * Logical address of the management logical device. This logical device should always be accessible.
		 */
		public static final int SERVER_UPPER_MANAGEMENT_LOGICAL_DEVICE = 0x01;
		/**
		 * Logical address to send a message to all logical devices of a remote station. One byte version
		 */
		public static final int SERVER_UPPER_ALL_STATIONS_1BYTE = 0x7F;
		/**
		 * Logical address to send a message to all logical devices of a remote station. Two byte version
		 */
		public static final int SERVER_UPPER_ALL_STATIONS_2BYTE = 0x3FFF;

		/**
		 * Physical address used by remote stations as source for event messages. One byte version
		 */
		public static final int SERVER_LOWER_CALLING_1BYTE = 0x7E;
		/**
		 * Physical address used by remote stations as source for event messages. Two byte version
		 */
		public static final int SERVER_LOWER_CALLING_2BYTE = 0x3FFE;
	}
}

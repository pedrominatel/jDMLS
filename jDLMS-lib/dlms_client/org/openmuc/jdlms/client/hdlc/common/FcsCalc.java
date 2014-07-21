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

/**
 * This class is used to compute a Frame Check Sequence according to IEC 62056-46 annex A.
 * 
 * Because a HDLC frame can have up to 2 FCS fields, the actual FCS value can be read between each computed byte (as the
 * first FCS ,including all data already read, is automatically part of the 2nd FCS).
 * 
 * To compute the FCS value of several different messages, the method {@code reset()} has to be called at the beginning
 * of each subsequent message
 * 
 * @author Karsten Mueller-Bier
 */
public class FcsCalc {

	private static final short[] fcsTable = new short[256];

	private static final short INITIALFCS = (short) 0xFFFF;
	private static final short GOODFCS = (short) 0xF0B8;

	private static final int KEY = 0x8408; // Bit-reversed 1021

	/**
	 * Static initializer, generates the fcsTable from the KEY value when this class is loaded first
	 */
	static {
		int b, v, i;

		for (b = 0;;) {
			v = b;
			for (i = 8; i != 0; i--) {
				if ((v & 1) == 1) {
					v = (v >> 1) ^ KEY;
				}
				else {
					v = v >> 1;
				}
			}

			fcsTable[b] = (short) (v & 0xffff);

			if (++b == 256) {
				break;
			}
		}
	}

	private short fcsValue = INITIALFCS;

	/**
	 * Updates the FCS value by computing the next byte
	 * 
	 * @param data
	 *            The byte to compute
	 */
	public void update(byte data) {
		fcsValue = (short) (((fcsValue & 0xFFFF) >>> 8) ^ fcsTable[(fcsValue ^ data) & 0xFF]);
	}

	/**
	 * Updates the FCS value by computing all bytes in an array
	 * 
	 * @param data
	 *            Array of data to compute
	 * @see FcsCalc#update(byte)
	 */
	public void update(byte[] data) {
		update(data, data.length);
	}

	/**
	 * Updates the FCS value by computing length number of bytes from an array, beginning with the first
	 * 
	 * @param data
	 *            Array of data to compute
	 * @param length
	 *            Number of bytes to compute
	 * @see FcsCalc#update(byte)
	 */
	public void update(byte[] data, int length) {
		for (int i = 0; i < length; i++) {
			update(data[i]);
		}
	}

	/**
	 * Generates a byte array from the current FCS value.
	 * 
	 * The bytes of that array can be inserted into a message in order to add a valid FCS checksum to the computed
	 * message
	 * 
	 * @return the generated checksum
	 */
	public byte[] getFcsBytes() {
		int invFcs = (fcsValue ^ 0xFFFF);
		byte[] result = new byte[2];

		result[0] = (byte) (invFcs & 0xFF);
		result[1] = (byte) ((invFcs & 0xFF00) >>> 8);

		return result;
	}

	/**
	 * Checks if computed message has a valid checksum.
	 * 
	 * The message must be computed according the document it described, that is in most cases from the beginning of the
	 * message up to and including the FCS value itself
	 * 
	 * @return true if the message if valid
	 */
	public boolean checkData() {
		return GOODFCS == fcsValue;
	}

	/**
	 * Resets the computed FCS value back to it's initial value.
	 * 
	 * This method has to be invoked each time a new message is generated or checked
	 */
	public void reset() {
		fcsValue = INITIALFCS;
	}
}

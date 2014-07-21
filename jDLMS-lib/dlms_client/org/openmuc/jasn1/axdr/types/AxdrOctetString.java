/*
 * Copyright Fraunhofer ISE, 2012
 * Author(s): Karsten Mueller-Bier
 *    
 * This file is part of jASN1.
 * For more information visit http://www.openmuc.org
 * 
 * jASN1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jASN1 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with jASN1.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.openmuc.jasn1.axdr.types;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.openmuc.jasn1.axdr.AxdrByteArrayOutputStream;
import org.openmuc.jasn1.axdr.AxdrLength;
import org.openmuc.jasn1.axdr.AxdrType;

public class AxdrOctetString implements AxdrType {

	private byte[] octetString = new byte[0];

	private int length = 0;

	public AxdrOctetString() {
	}

	public AxdrOctetString(int length) {
		this.length = length;
		octetString = new byte[length];
	}

	public AxdrOctetString(byte[] octetString) {
		if (octetString != null) {
			this.octetString = octetString;
		}
	}

	public AxdrOctetString(int length, byte[] octetString) {
		if (length != 0 && length != octetString.length) {
			throw new IllegalArgumentException("octetString of wrong size");
		}

		this.length = length;
		this.octetString = octetString;
	}

	@Override
	public int encode(AxdrByteArrayOutputStream axdrOStream) throws IOException {

		int codeLength = 0;
		axdrOStream.write(octetString);
		codeLength = octetString.length;

		if (length == 0) {
			AxdrLength length = new AxdrLength(octetString.length);
			codeLength += length.encode(axdrOStream);
		}

		return codeLength;
	}

	@Override
	public int decode(InputStream iStream) throws IOException {

		int codeLength = 0;
		int length = this.length;

		if (length == 0) {
			AxdrLength l = new AxdrLength();
			codeLength += l.decode(iStream);

			length = l.getValue();
			octetString = new byte[length];
		}

		if (length != 0) {
			if (iStream.read(octetString, 0, length) < length) {
				throw new IOException("Error Decoding AxdrOctetString");
			}
			codeLength += length;
		}

		return codeLength;

	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof AxdrOctetString) {
			AxdrOctetString other = (AxdrOctetString) o;
			if (Arrays.equals(other.octetString, octetString) && other.length == length) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (byte b : octetString) {
			hash += b;
		}
		return hash ^ length;
	}

	@Override
	public String toString() {
		return new String(octetString);
	}

	public byte[] getValue() {
		return octetString;
	}

	/**
	 * Converts a String representing a hexadecimal byte string into a byte array
	 * 
	 * E.g. The String "33FF7A" will be converted into the equivalent of new byte[]{0x33, 0xFF, 0x7A}
	 * 
	 * @param value
	 *            String to convert
	 * @return converted byte array
	 */
	protected static byte[] getBytesFromString(String value) {
		byte[] result = new byte[value.length() / 2];
		int index = 0;

		while (index < result.length) {
			result[index] = (byte) Short.parseShort(value.substring(index * 2, index * 2 + 2), 16);
			index++;
		}

		return result;
	}
}

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

public class AxdrBitString implements AxdrType {

	private byte[] bitString;
	private int maxBits = 0;

	public AxdrBitString() {
	}

	public AxdrBitString(byte[] bitString) {
		setValues(bitString);
	}

	public AxdrBitString(byte[] bitString, int maxBits) {
		this.maxBits = maxBits;
		setValues(bitString);
	}

	@Override
	public int encode(AxdrByteArrayOutputStream axdrOStream) throws IOException {

		int codeLength = bitString.length;

		for (int i = (bitString.length - 1); i >= 0; i--) {
			axdrOStream.write(bitString[i]);
		}

		if (maxBits == 0) {
			AxdrLength length = new AxdrLength(codeLength * 8);
			codeLength += length.encode(axdrOStream);
		}

		return codeLength;
	}

	@Override
	public int decode(InputStream iStream) throws IOException {
		int codeLength = maxBits / 8;
		int length = codeLength;

		if (codeLength == 0) {
			AxdrLength l = new AxdrLength();
			codeLength += l.decode(iStream);

			length = l.getValue();
			codeLength += length;
		}

		length = length % 8 == 0 ? length / 8 : length / 8 + 1;

		bitString = new byte[length];
		if (iStream.read(bitString, 0, length) < length) {
			throw new IOException("Error Decoding AxdrBitString");
		}

		return codeLength;
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof AxdrBitString) {
			AxdrBitString other = (AxdrBitString) o;
			if (Arrays.equals(other.bitString, bitString) && other.maxBits == maxBits) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int hashCode() {
		int hash = 0;
		for (byte b : bitString) {
			hash += b;
		}
		return hash ^ maxBits;
	}

	public byte[] getValues() {
		return bitString;
	}

	public int getMaxLength() {
		return maxBits;
	}

	public void setValues(byte[] bitString) {
		if (maxBits != 0) {
			if ((maxBits <= (((bitString.length - 1) * 8) + 1)) || (maxBits > (bitString.length * 8))) {
				throw new IllegalArgumentException("BitString size out of bounds");
			}
		}

		this.bitString = bitString;
	}
}

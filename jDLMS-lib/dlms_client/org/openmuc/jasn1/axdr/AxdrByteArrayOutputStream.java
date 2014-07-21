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
package org.openmuc.jasn1.axdr;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;

public class AxdrByteArrayOutputStream extends BerByteArrayOutputStream {
	/**
	 * Creates a <code>AxdrByteArrayOutputStream</code> with a byte array of size <code>bufferSize</code>. The buffer
	 * will not be resized automatically. Use {@link #AxdrByteArrayOutputStream(int, boolean)} instead if you want the
	 * buffer to be dynamically resized.
	 * 
	 * @param bufferSize
	 *            the size of the underlying buffer
	 */
	public AxdrByteArrayOutputStream(int bufferSize) {
		super(bufferSize);
	}

	public AxdrByteArrayOutputStream(int bufferSize, boolean automaticResize) {
		super(bufferSize, automaticResize);
	}

	public AxdrByteArrayOutputStream(byte[] buffer, int startingIndex) {
		super(buffer, startingIndex);
	}

	public AxdrByteArrayOutputStream(byte[] buffer, int startingIndex, boolean automaticResize) {
		super(buffer, startingIndex, automaticResize);
	}
}

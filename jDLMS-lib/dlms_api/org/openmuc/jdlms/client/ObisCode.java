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
package org.openmuc.jdlms.client;

/**
 * Represents the address of a remote object according to IEC 62056-61. An instance of ObisCode is immutable.
 * 
 * @author Karsten Müller-Bier
 */
public class ObisCode {

	private final String code;

	/**
	 * Constructor
	 * 
	 * @param a
	 *            First byte of the address
	 * @param b
	 *            Second byte of the address
	 * @param c
	 *            Third byte of the address
	 * @param d
	 *            Fourth byte of the address
	 * @param e
	 *            Fifth byte of the address
	 * @param f
	 *            Final byte of the address
	 * @throws IllegalArgumentException
	 *             If one of the bytes is out of range [0, 255]
	 */
	public ObisCode(int a, int b, int c, int d, int e, int f) {
		checkLength(a);
		checkLength(b);
		checkLength(c);
		checkLength(d);
		checkLength(e);
		checkLength(f);

		StringBuilder sb = new StringBuilder(12);
		sb.append(String.format("%02x", a));
		sb.append(String.format("%02x", b));
		sb.append(String.format("%02x", c));
		sb.append(String.format("%02x", d));
		sb.append(String.format("%02x", e));
		sb.append(String.format("%02x", f));
		code = sb.toString();
	}

	public String getHexCode() {
		return code;
	}

	private void checkLength(int number) {
		if (number < 0 || number > 255) {
			throw new IllegalArgumentException(number + " is out of range [0, 255]");
		}
	}
}

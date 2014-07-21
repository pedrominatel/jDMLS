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
package org.openmuc.jdlms.client.impl;

import java.util.Arrays;
import java.util.List;

/**
 * Information about a single COSEM interface class needed by SNConnection
 * 
 * @author Karsten Mueller-Bier
 */
public class InterfaceClass {
	private List<Integer> methodsWithReturnType = null;
	private int firstOffset = 0;
	private int lastMethodIndex = 0;

	public InterfaceClass(int firstOffset, int lastMethodIndex, Integer... methodsWithReturnType) {
		this.firstOffset = firstOffset;
		this.lastMethodIndex = lastMethodIndex;
		this.methodsWithReturnType = Arrays.asList(methodsWithReturnType);
	}

	public InterfaceClass(int firstOffset, int lastMethodIndex) {
		this(firstOffset, lastMethodIndex, new Integer[0]);
	}

	public InterfaceClass(int firstOffset) {
		this(firstOffset, 0, new Integer[0]);
	}

	/**
	 * @return The offset of the first method in this class
	 */
	public int getFirstOffset() {
		return firstOffset;
	}

	/**
	 * @return The index number of the last method in this class
	 */
	public int getLastMethodIndex() {
		return lastMethodIndex;
	}

	/**
	 * Checks if the method at the given index returns a value
	 * 
	 * @param methodId
	 *            MethodId to check
	 * @return true if server sends a return value
	 */
	public boolean hasReturnType(int methodId) {
		if (methodId < lastMethodIndex) {
			return methodsWithReturnType.contains(methodId);
		}
		return false;
	}

	/**
	 * Checks if a given short name in a the valid range of an object.
	 * 
	 * The valid range of a object is between baseName and baseName + firstOffset + lastMethodIndex * 8
	 * 
	 * @param shortName
	 *            Short Name address to check
	 * @param baseName
	 *            Base address of the object
	 */
	public boolean isInRange(int shortName, int baseName) {
		if (shortName >= baseName && shortName <= (baseName + firstOffset + lastMethodIndex * 8)) {
			return true;
		}
		return false;
	}
}

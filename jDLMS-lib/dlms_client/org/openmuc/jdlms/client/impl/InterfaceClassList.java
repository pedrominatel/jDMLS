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

import java.util.HashMap;
import java.util.Map;

/**
 * List of all supported COSEM interface classes
 * 
 * @author Karsten Mueller-Bier
 */
public class InterfaceClassList {

	/**
	 * Used as key in {@link InterfaceClassList}
	 * 
	 * @author Karsten Mueller-Bier
	 */
	private static class ClassVersionPair {
		private final int classId;
		private final int version;

		public ClassVersionPair(int classId, int version) {
			this.classId = classId;
			this.version = version;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof ClassVersionPair) {
				ClassVersionPair other = (ClassVersionPair) obj;
				return classId == other.classId && version == other.version;
			}
			return false;
		}

		@Override
		public int hashCode() {
			return classId * 128 + version;
		}
	}

	private static Map<ClassVersionPair, InterfaceClass> interfaceClassMap;

	public static InterfaceClass getClassInfo(int classId, int version) {
		ClassVersionPair key = new ClassVersionPair(classId, version);
		return interfaceClassMap.get(key);
	}

	static {
		interfaceClassMap = new HashMap<ClassVersionPair, InterfaceClass>(32);

		// Data class, no methods
		interfaceClassMap.put(new ClassVersionPair(1, 0), new InterfaceClass(9));

		// Register class
		interfaceClassMap.put(new ClassVersionPair(3, 0), new InterfaceClass(40, 1));

		// Extended register class
		interfaceClassMap.put(new ClassVersionPair(4, 0), new InterfaceClass(56, 1));

		// Demand register class
		interfaceClassMap.put(new ClassVersionPair(5, 0), new InterfaceClass(72, 2));

		// Register activation class
		interfaceClassMap.put(new ClassVersionPair(6, 0), new InterfaceClass(48, 3));

		// Profile generic class
		interfaceClassMap.put(new ClassVersionPair(7, 1), new InterfaceClass(88, 4));

		// Clock class
		interfaceClassMap.put(new ClassVersionPair(8, 0), new InterfaceClass(96, 6));

		// Script class
		interfaceClassMap.put(new ClassVersionPair(9, 0), new InterfaceClass(32, 0));

		// Schedule class
		interfaceClassMap.put(new ClassVersionPair(10, 0), new InterfaceClass(32, 3));

		// Special days table class
		interfaceClassMap.put(new ClassVersionPair(11, 0), new InterfaceClass(16, 2));

		// Activity calendar class
		interfaceClassMap.put(new ClassVersionPair(20, 0), new InterfaceClass(80, 1));

		// Association SN class
		interfaceClassMap.put(new ClassVersionPair(12, 1), new InterfaceClass(32, 8, 3, 4, 8));

		// SAP assignment class
		interfaceClassMap.put(new ClassVersionPair(17, 0), new InterfaceClass(32, 1));

		// Register monitor class
		interfaceClassMap.put(new ClassVersionPair(21, 0), new InterfaceClass(25));

		// Utilities table class
		interfaceClassMap.put(new ClassVersionPair(26, 0), new InterfaceClass(25));

		// Single action schedule class
		interfaceClassMap.put(new ClassVersionPair(22, 0), new InterfaceClass(25));

		// Register table class
		interfaceClassMap.put(new ClassVersionPair(61, 0), new InterfaceClass(40, 2));

		// Status mapping class
		interfaceClassMap.put(new ClassVersionPair(63, 0), new InterfaceClass(17));

		// IEC local port setup class
		interfaceClassMap.put(new ClassVersionPair(19, 0), new InterfaceClass(65));
		interfaceClassMap.put(new ClassVersionPair(19, 1), new InterfaceClass(65));

		// Modem configuration class
		interfaceClassMap.put(new ClassVersionPair(27, 0), new InterfaceClass(25));
		interfaceClassMap.put(new ClassVersionPair(27, 1), new InterfaceClass(25));

		// Auto answer class
		interfaceClassMap.put(new ClassVersionPair(28, 0), new InterfaceClass(41));

		// PSTN auto dial class
		interfaceClassMap.put(new ClassVersionPair(29, 0), new InterfaceClass(41));

		// Auto connect class
		interfaceClassMap.put(new ClassVersionPair(29, 1), new InterfaceClass(41));

		// IEC HDLC setup class
		interfaceClassMap.put(new ClassVersionPair(23, 0), new InterfaceClass(65));
		interfaceClassMap.put(new ClassVersionPair(23, 1), new InterfaceClass(65));

		// IEC twisted pair setup class
		interfaceClassMap.put(new ClassVersionPair(24, 0), new InterfaceClass(33));

		// TCP-UDP setup class
		interfaceClassMap.put(new ClassVersionPair(41, 0), new InterfaceClass(41));

		// IPv4 setup class
		interfaceClassMap.put(new ClassVersionPair(42, 0), new InterfaceClass(96, 3, 3));

		// / PPP setup class
		interfaceClassMap.put(new ClassVersionPair(44, 0), new InterfaceClass(33));

		// GPRS modem setup class
		interfaceClassMap.put(new ClassVersionPair(45, 0), new InterfaceClass(25));

		// SMTP setup class
		interfaceClassMap.put(new ClassVersionPair(46, 0), new InterfaceClass(41));
	}

}

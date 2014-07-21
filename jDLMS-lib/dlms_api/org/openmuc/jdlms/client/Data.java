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

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Container class holding data about to send to the smart meter or received by the smart meter
 * 
 * @author Karsten Mueller-Bier
 */
public class Data {
	/**
	 * Enumeration of all data types the {@link Data} class can hold.
	 * 
	 * @author Karsten Müller-Bier
	 */
	public static enum Choices {
		/**
		 * Holds no data
		 */
		NULL_DATA(0),
		/**
		 * Container holds multiple values of the same type
		 */
		ARRAY(1),
		/**
		 * Container holds multiple values of different types
		 */
		STRUCTURE(2),
		/**
		 * Container holds a boolean value
		 */
		BOOL(3),
		/**
		 * Container holds a byte array with each bit representing a different flag
		 */
		BIT_STRING(4),
		/**
		 * Container holds a 4 Byte integer value
		 */
		DOUBLE_LONG(5),
		/**
		 * Container holds a 4 Byte unsigned integer value
		 */
		DOUBLE_LONG_UNSIGNED(6),
		/**
		 * Container holds a byte array
		 */
		OCTET_STRING(9),
		/**
		 * Container holds a character string as byte array
		 */
		VISIBLE_STRING(10),
		/**
		 * Container holds a 2 digit number as BCD in a byte
		 */
		BCD(13),
		/**
		 * Container holds a 1 Byte integer value
		 */
		INTEGER(15),
		/**
		 * Container holds a 2 Byte integer value
		 */
		LONG_INTEGER(16),
		/**
		 * Container holds a 1 Byte unsigned value
		 */
		UNSIGNED(17),
		/**
		 * Container holds a 2 Byte unsigned value
		 */
		LONG_UNSIGNED(18),
		/**
		 * Container holds an array. It is transported using a special algorithm to save bandwidth (See IEC 62056-62)
		 */
		COMPACT_ARRAY(19),
		/**
		 * Container holds an 8 Byte integer value
		 */
		LONG64(20),
		/**
		 * Container holds an 8 Byte unsigned integer value
		 */
		LONG64_UNSIGNED(21),
		/**
		 * Container holds 1 Byte representing the value of an enumeration
		 */
		ENUMERATE(22),
		/**
		 * Container holds a 32 Bit floating point number
		 */
		FLOAT32(23),
		/**
		 * Container holds a 64 Bit floating point number
		 */
		FLOAT64(24),
		/**
		 * Container holds a Calendar with date and time set
		 */
		DATE_TIME(25),
		/**
		 * Container holds a Calendar with date set
		 */
		DATE(26),
		/**
		 * Container holds a Calendar with time set
		 */
		TIME(27),
		/**
		 * Data inside container is unknown
		 */
		DONT_CARE(255);

		private int value;

		private Choices(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Choices valueOf(long tagValue) {
			Choices[] values = Choices.values();

			for (Choices c : values) {
				if (c.value == tagValue) {
					return c;
				}
			}
			return NULL_DATA;
		}
	}

	private Choices choice;

	private List<Data> complexData = new LinkedList<Data>();
	private Boolean boolValue = null;
	private byte[] byteString = new byte[0];
	private Long numberValue = null;
	private Calendar dateTime;
	private boolean useMilliseconds = false;
	private Double doubleValue = null;

	/**
	 * Contructor. Creates a new container holding no data
	 */
	public Data() {
		choice = Choices.NULL_DATA;
	}

	/**
	 * Copy constructor. Creates a new container holding a copy of originals data
	 * 
	 * @param original
	 *            The container to copy
	 */
	public Data(Data original) {
		choice = original.choice;
		complexData = new LinkedList<Data>(original.complexData);
		boolValue = original.boolValue;
		byteString = new byte[original.byteString.length];
		System.arraycopy(original.byteString, 0, byteString, 0, original.byteString.length);
		numberValue = original.numberValue;
		dateTime = original.dateTime;
		useMilliseconds = original.useMilliseconds;
		doubleValue = original.doubleValue;
	}

	/**
	 * Returns an enumeration indicating what data this container is holding
	 * 
	 * @return The type of data in this container
	 */
	public Choices getChoiceIndex() {
		return choice;
	}

	/**
	 * Removes all data from this container
	 */
	public void setNull() {
		choice = Choices.NULL_DATA;
	}

	/**
	 * Sets the data of this container to an array of values.
	 * 
	 * @param array
	 *            The array of values
	 * @throws IllegalArgumentException
	 *             If a sub element of array has another data type than the first
	 */
	public void setArray(List<Data> array) {
		if (array.size() > 0) {
			Choices dataType = array.get(0).getChoiceIndex();
			for (Data sub : array) {
				if (sub.getChoiceIndex() != dataType) {
					throw new IllegalArgumentException("Array is of type " + dataType + ", but " + sub + " is of type "
							+ sub.getChoiceIndex());
				}
			}
		}
		choice = Choices.ARRAY;
		complexData = array;
	}

	/**
	 * Sets the data of this container to an structure of values.
	 * 
	 * @param structure
	 *            The structure of values
	 */
	public void setStructure(List<Data> structure) {
		choice = Choices.STRUCTURE;
		complexData = structure;
	}

	/**
	 * Sets the data of this container to a boolean value
	 * 
	 * @param newVal
	 *            The boolean value
	 */
	public void setbool(boolean newVal) {
		choice = Choices.BOOL;
		boolValue = newVal;
	}

	/**
	 * Sets the data of this container to a string of bits
	 * 
	 * @param bitString
	 *            The byte array holding the bit string
	 * @param numOfBits
	 *            How many bits this bit string contains
	 * @throws IllegalArgumentException
	 *             If bitString.length * 8 < numOfBits
	 */
	public void setBitString(byte[] bitString, int numOfBits) {
		if (numOfBits > bitString.length * 8) {
			throw new IllegalArgumentException("Bit String is too small");
		}
		choice = Choices.BIT_STRING;
		byteString = bitString.clone();
		numberValue = ((long) numOfBits) & 0xFFFFFFFF;
	}

	/**
	 * Sets the data of this container to a 4 Byte integer number
	 * 
	 * @param newVal
	 *            The number to store
	 * @throws IllegalArgumentException
	 *             If newVal is < -2^(31) or > 2^(31)-1
	 */
	public void setInteger32(long newVal) {
		if (newVal < Integer.MIN_VALUE || newVal > Integer.MAX_VALUE) {
			throw new IllegalArgumentException("Integer32 " + newVal + " out of range");
		}
		choice = Choices.DOUBLE_LONG;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to a 4 byte unsigned integer number
	 * 
	 * @param newVal
	 *            The number to store
	 * @throws IllegalArgumentException
	 *             If newVal is > 2^(32)-1 or negative
	 */
	public void setUnsigned32(long newVal) {
		if (newVal < 0 || newVal > 0x0FFFFFFFFL) {
			throw new IllegalArgumentException("Unsigned32 " + newVal + " out of range");
		}
		choice = Choices.DOUBLE_LONG_UNSIGNED;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to a byte array
	 * 
	 * @param string
	 *            The byte array to store
	 */
	public void setOctetString(byte[] string) {
		choice = Choices.OCTET_STRING;
		byteString = string.clone();
	}

	/**
	 * Sets the data of this container to string, encoded as byte array
	 * 
	 * @param string
	 *            The string to store
	 */
	public void setVisibleString(byte[] string) {
		choice = Choices.VISIBLE_STRING;
		byteString = string;
	}

	/**
	 * Sets the data of this container to a 2 digit BCD number
	 * 
	 * @param newVal
	 *            The BCD number to store
	 * @throws IllegalArgumentException
	 *             If newVal uses more than 2 digits
	 */
	public void setBcd(long newVal) {
		if (newVal < Byte.MIN_VALUE || newVal > Byte.MAX_VALUE) {
			throw new IllegalArgumentException("BCD " + newVal + " out of range");
		}
		choice = Choices.BCD;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to a 1 Byte integer number
	 * 
	 * @param newVal
	 *            The number to store
	 * @throws IllegalArgumentException
	 *             If newVal < -2^(7) or > 2^(7)-1
	 */
	public void setInteger8(long newVal) {
		if (newVal < Byte.MIN_VALUE || newVal > Byte.MAX_VALUE) {
			throw new IllegalArgumentException("Integer8 " + newVal + " out of range");
		}
		choice = Choices.INTEGER;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to a 2 Byte integer number
	 * 
	 * @param newVal
	 *            The number to store
	 * @throws IllegalArgumentException
	 *             If newVal < -2^(15) or > 2^(15)-1
	 */
	public void setInteger16(long newVal) {
		if (newVal < Short.MIN_VALUE || newVal > Short.MAX_VALUE) {
			throw new IllegalArgumentException("Integer16 " + newVal + " out of range");
		}
		choice = Choices.LONG_INTEGER;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to a 1 Byte unsigned integer number
	 * 
	 * @param newVal
	 *            The number to store
	 * @throws IllegalArgumentException
	 *             If newVal > 2^(8)-1 or negative
	 */
	public void setUnsigned8(long newVal) {
		if (newVal < 0 || newVal > 0xFF) {
			throw new IllegalArgumentException("Unsigned8 " + newVal + " out of range");
		}
		choice = Choices.UNSIGNED;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to a 2 Byte unsigned integer number
	 * 
	 * @param newVal
	 *            The number to store
	 * @throws IllegalArgumentException
	 *             If newVal > 2^(16)-1 or negative
	 */
	public void setUnsigned16(long newVal) {
		if (newVal < 0 || newVal > 0xFFFF) {
			throw new IllegalArgumentException("Unsigned16 " + newVal + " out of range");
		}
		choice = Choices.LONG_UNSIGNED;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to an 8 Byte integer number
	 * 
	 * @param newVal
	 *            The number to store
	 */
	public void setInteger64(long newVal) {
		choice = Choices.LONG64;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to an 8 Byte unsigned integer number
	 * 
	 * @param newVal
	 *            The number to store
	 * @throws IllegalArgumentException
	 *             If newVal is negative
	 */
	public void setUnsigned64(long newVal) {
		if (newVal < 0) {
			throw new IllegalArgumentException("Unsigned64 " + newVal + " out of range");
		}
		choice = Choices.LONG64_UNSIGNED;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to a 1 Byte unsigned integer value, representing an enumeration on the remote
	 * object
	 * 
	 * @param newVal
	 *            The enum value to store
	 * @throws IllegalArgumentException
	 *             if newVal is > 2^(8)-1 or negative
	 */
	public void setEnumerate(long newVal) {
		if (newVal < 0 || newVal > 0xFF) {
			throw new IllegalArgumentException("Enumeration " + newVal + " out of range");
		}
		choice = Choices.ENUMERATE;
		numberValue = newVal;
	}

	/**
	 * Sets the data of this container to a 32 bit floating point number
	 * 
	 * @param newVal
	 *            The number to store
	 */
	public void setFloat32(float newVal) {
		choice = Choices.FLOAT32;
		doubleValue = (double) newVal;
	}

	/**
	 * Sets the data of this container to a 64 bit floating point number
	 * 
	 * @param newVal
	 *            The number to store
	 */
	public void setFloat64(double newVal) {
		choice = Choices.FLOAT64;
		doubleValue = newVal;
	}

	/**
	 * Sets the data of this container to a Calendar object holding date and time
	 * 
	 * @param newVal
	 *            The date and time to store
	 * @param useMilliseconds
	 *            If the time is with milliseconds precision
	 */
	public void setDateTime(Calendar newVal, boolean useMilliseconds) {
		choice = Choices.DATE_TIME;
		dateTime = newVal;
		this.useMilliseconds = useMilliseconds;
	}

	/**
	 * Sets the data of this container to a Calendar object holding only a date but no time.
	 * <p>
	 * The Calendar may have its hour, minute, second and millisecond value set, but their values are ignored
	 * </p>
	 * 
	 * @param newVal
	 *            The date to store
	 */
	public void setDate(Calendar newVal) {
		choice = Choices.DATE;
		dateTime = newVal;
	}

	/**
	 * Sets the data of this container to a Calendar object holding only a time but no date.
	 * <p>
	 * The Calendar may have its day, month and year set, but their values are ignored
	 * </p>
	 * 
	 * @param newVal
	 *            The time to store
	 * @param useMilliseconds
	 *            If the time if with milliseconds precision
	 */
	public void setTime(Calendar newVal, boolean useMilliseconds) {
		choice = Choices.TIME;
		dateTime = newVal;
		this.useMilliseconds = useMilliseconds;
	}

	/**
	 * Checks if the data of this container is a number
	 * 
	 * @return True if {@link Data#getNumber()} may be called without causing an exception.
	 */
	public boolean isNumber() {
		return choice == Choices.BCD || choice == Choices.DOUBLE_LONG || choice == Choices.DOUBLE_LONG_UNSIGNED
				|| choice == Choices.ENUMERATE || choice == Choices.FLOAT32 || choice == Choices.FLOAT64
				|| choice == Choices.INTEGER || choice == Choices.LONG64 || choice == Choices.LONG64_UNSIGNED
				|| choice == Choices.LONG_INTEGER || choice == Choices.LONG_UNSIGNED || choice == Choices.UNSIGNED
				|| choice == Choices.BIT_STRING;
	}

	/**
	 * Checks if the data of this container is of a complex type.
	 * <p>
	 * A complex container holds one or more sub container of type {@link Data} as values.
	 * </p>
	 * <p>
	 * A container is of complex type if {@link Data#getChoiceIndex()} returns either {@link Choices#ARRAY},
	 * {@link Choices#STRUCTURE} or {@link Choices#COMPACT_ARRAY}.
	 * 
	 * @return True if {@link Data#getComplex()} may be called without causing an exception.
	 */
	public boolean isComplex() {
		return choice == Choices.ARRAY || choice == Choices.STRUCTURE || choice == Choices.COMPACT_ARRAY;
	}

	/**
	 * Checks if the data of this container is a byte array.
	 * <p>
	 * A container is a byte array if {@link Data#getChoiceIndex()} returns either {@link Choices#OCTET_STRING},
	 * {@link Choices#VISIBLE_STRING} or {@link Choices#BIT_STRING}.
	 * </p>
	 * 
	 * @return True if {@link Data#getByteArray()} may be called without causing an exception.
	 */
	public boolean isByteArray() {
		return choice == Choices.BIT_STRING || choice == Choices.OCTET_STRING || choice == Choices.VISIBLE_STRING;
	}

	/**
	 * Checks if the data of this container is a boolean.
	 * 
	 * @return True if {@link Data#getBoolean()} may be called without causing an exception.
	 */
	public boolean isBoolean() {
		return choice == Choices.BOOL;
	}

	/**
	 * Checks if the data of this container is a Calendar object.
	 * <p>
	 * A container is a calendar if {@link Data#getChoiceIndex()} returns either {@link Choices#DATE_TIME},
	 * {@link Choices#DATE} or {@link Choices#TIME}.
	 * </p>
	 * 
	 * @return True of {@link Data#getCalendar()} may be called without causing an exception.
	 */
	public boolean isCalendar() {
		return choice == Choices.DATE || choice == Choices.DATE_TIME || choice == Choices.TIME;
	}

	/**
	 * Returns the data of this container as a number.
	 * 
	 * @return The data as a number value
	 */
	public Number getNumber() {
		if (isNumber()) {
			Number result;
			if (choice == Choices.FLOAT32) {
				result = doubleValue.floatValue();
			}
			else if (choice == Choices.FLOAT64) {
				result = doubleValue;
			}
			else {
				result = numberValue;
			}

			return result;
		}
		throw new IllegalStateException("Data is no number");
	}

	/**
	 * Returns the data of this container as a list of Data objects. Each data object is a sub item of the called
	 * container. The returned list is read-only.
	 * 
	 * @return A read-only list of all child elements of this container
	 */
	public List<Data> getComplex() {
		if (isComplex()) {
			return Collections.unmodifiableList(complexData);
		}
		throw new IllegalStateException("Data is no complex type");
	}

	/**
	 * Returns the data of this container as a raw byte array.
	 * 
	 * @return The data as a byte array value
	 */
	public byte[] getByteArray() {
		if (isByteArray()) {
			return byteString;
		}
		throw new IllegalStateException("Data is no complex type");
	}

	/**
	 * Returns the data of this container as a boolean
	 * 
	 * @return The data as a boolean value
	 */
	public boolean getBoolean() {
		if (isBoolean()) {
			return boolValue;
		}
		throw new IllegalStateException("Data is no complex type");
	}

	/**
	 * Returns the data of this container as a Calendar object
	 * 
	 * @return The data as a Calendar object
	 */
	public Calendar getCalendar() {
		if (isCalendar()) {
			return dateTime;
		}
		throw new IllegalStateException("Data is no Date/Time type");
	}

	/**
	 * Checks to determine if the Calendar data inside this container has a milliseconds precision.
	 * 
	 * @return True if this container holds a Calendar object and this object has milliseconds precision.
	 */
	public boolean useMilliseconds() {
		if (isCalendar()) {
			return useMilliseconds;
		}
		return false;
	}
}

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
 * Container holding all data received from an event that the remote station sent
 * 
 * @author Karsten Müller-Bier
 */
public final class EventNotification {
	private final int classId;
	private final String obisCode;
	private final int attributeId;
	private final Data newValue;
	private final Long timestamp;

	/**
	 * Creates a new event notification without timestamp
	 * 
	 * @param classId
	 *            Class of the remote object that raised the event
	 * @param obisCode
	 *            Logical address of the remote object
	 * @param attributeId
	 *            Attribute that has been changed
	 * @param newValue
	 *            The new value of the attribute
	 */
	public EventNotification(int classId, String obisCode, int attributeId, Data newValue) {
		this.classId = classId;
		this.obisCode = obisCode;
		this.attributeId = attributeId;
		this.newValue = newValue;
		timestamp = null;
	}

	/**
	 * Creates a new event notification without timestamp
	 * 
	 * @param classId
	 *            Class of the remote object that raised the event
	 * @param obisCode
	 *            Logical address of the remote object
	 * @param attributeId
	 *            Attribute that has been changed
	 * @param newValue
	 *            The new value of the attribute
	 * @param timestamp
	 *            The timestamp of the remote station when the event has been raised
	 */
	public EventNotification(int classId, String obisCode, int attributeId, Data newValue, Long timestamp) {
		this.classId = classId;
		this.obisCode = obisCode;
		this.attributeId = attributeId;
		this.newValue = newValue;
		this.timestamp = timestamp;
	}

	public int getClassId() {
		return classId;
	}

	public String getObisCode() {
		return obisCode;
	}

	public int getAttributeId() {
		return attributeId;
	}

	public Data getNewValue() {
		return newValue;
	}

	/**
	 * @return The timestamp when this event has been generated at the remote station. Null if there is no timestamp
	 */
	public Long getTimestamp() {
		return timestamp;
	}

	/**
	 * @return True if this event has a timestamp
	 */
	public boolean hasTimestamp() {
		return timestamp != null;
	}
}

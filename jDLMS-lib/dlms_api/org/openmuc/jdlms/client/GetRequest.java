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
 * Collection of data needed for a single remote GET call
 * 
 * @author Karsten Mueller-Bier
 */
public class GetRequest implements IAttributeRequest {
	private final int classId;
	private final ObisCode obisCode;
	private final int attributeId;

	/**
	 * Structure defining access to a subset of an attribute. Consort IEC 62056-62 to see which attribute has which
	 * access selections. May be null if not needed. (A value of null reads the full attribute)
	 */
	private SelectiveAccessDescription access_selection;

	/**
	 * Creates a get parameter for that particular attribute
	 * 
	 * @param classId
	 *            Class of the object to read
	 * @param obisCode
	 *            Identifier of the remote object to read
	 * @param attributeId
	 *            Attribute of the object that is to read
	 */
	public GetRequest(int classId, ObisCode obisCode, int attributeId) {
		this.classId = classId;
		this.obisCode = obisCode;
		this.attributeId = attributeId;
	}

	@Override
	public int getClassId() {
		return classId;
	}

	@Override
	public String getObisCode() {
		return obisCode.getHexCode();
	}

	@Override
	public int getAttributeId() {
		return attributeId;
	}

	@Override
	public SelectiveAccessDescription getAccessSelection() {
		return access_selection;
	}

	/**
	 * Adds an selection filter to this get request
	 * 
	 * @param access
	 *            The filter to apply
	 */
	public void setAccessSelection(SelectiveAccessDescription access) {
		access_selection = access;
	}

	/**
	 * Creates a new GetParameter instance based on this instance. The new instance is referring to the same attribute
	 * at another remote object of the same class.
	 * 
	 * @param newObisCode
	 *            The address of the new remote object
	 * @return Copy of this instance with changed obisCode
	 */
	public GetRequest changeObisCode(ObisCode newObisCode) {
		return new GetRequest(classId, newObisCode, attributeId);
	}

	/**
	 * Creates a new GetParameter instance based on this instance. The new instance is referring to another attribute at
	 * the same remote object.
	 * 
	 * @param newId
	 *            The new attribute to read
	 * @return Copy of this instance with changed attributeId
	 */
	public GetRequest changeAttributeId(int newId) {
		return new GetRequest(classId, obisCode, newId);
	}

	/**
	 * Creates a new SetParameter instance based on this instance. The new instance is referring to the same attribute
	 * at the same remote object
	 * 
	 * @return The newly created SetParameter instance
	 */
	public SetRequest toSetRequest() {
		return new SetRequest(classId, obisCode, attributeId);
	}
}

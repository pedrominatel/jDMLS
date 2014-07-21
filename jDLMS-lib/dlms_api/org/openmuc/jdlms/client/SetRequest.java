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
 * Collection of data needed for a single remote SET call
 * 
 * @author Karsten Mueller-Bier
 */
public class SetRequest implements IAttributeRequest {
	private final int classId;
	private final ObisCode obisCode;
	private final int attributeId;
	private final Data data;

	private SelectiveAccessDescription access_selection;

	/**
	 * Creates a set parameter for that particular attribute with an empty data container
	 * 
	 * @param classId
	 *            Class of the object to change
	 * @param obisCode
	 *            Identifier of the remote object to change
	 * @param attributeId
	 *            Attribute of the object that is to change
	 */
	public SetRequest(int classId, ObisCode obisCode, int attributeId) {
		this(classId, obisCode, attributeId, new Data());
	}

	/**
	 * Creates a set parameter for that particular attribute with a copy of the given data container
	 * 
	 * @param classId
	 *            Class of the object to change
	 * @param obisCode
	 *            Identifier of the remote object to change
	 * @param attributeId
	 *            Attribute of the object that is to change
	 * @param data
	 *            Container of this parameter
	 */
	public SetRequest(int classId, ObisCode obisCode, int attributeId, Data data) {
		this.classId = classId;
		this.obisCode = obisCode;
		this.attributeId = attributeId;
		this.data = new Data(data);
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
	public SetRequest changeObisCode(ObisCode newObisCode) {
		return new SetRequest(classId, newObisCode, attributeId, data);
	}

	/**
	 * Creates a new GetParameter instance based on this instance. The new instance is referring to another attribute at
	 * the same remote object.
	 * 
	 * @param newId
	 *            The new attribute to read
	 * @return Copy of this instance with changed attributeId
	 */
	public SetRequest changeAttributeId(int newId) {
		return new SetRequest(classId, obisCode, newId);
	}

	/**
	 * @return The Data container of this parameter object
	 */
	public Data data() {
		return data;
	}

	public GetRequest toGetRequest() {
		return new GetRequest(classId, obisCode, attributeId);
	}
}

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
 * Collection of data needed for a single remote ACTION call
 * 
 * @author Karsten Mueller-Bier
 */
public final class MethodRequest {
	private final int classId;
	private final ObisCode obisCode;
	private final int methodId;
	/**
	 * Parameter transmitted to be used by the method. May be null if not needed. (Method without parameter)
	 */
	private final Data methodParameters;

	/**
	 * Creates an action parameter for that particular method with no data container
	 * 
	 * @param classId
	 *            Class of the object to change
	 * @param obisCode
	 *            Identifier of the remote object to change
	 * @param methodId
	 *            Method of the object that shall be called
	 */
	public MethodRequest(int classId, ObisCode obisCode, int methodId) {
		this.classId = classId;
		this.obisCode = obisCode;
		this.methodId = methodId;
		methodParameters = new Data();
	}

	/**
	 * Creates an action parameter for that particular method with a copy of the given data container
	 * 
	 * @param classId
	 *            Class of the object to change
	 * @param obisCode
	 *            Identifier of the remote object to change
	 * @param methodId
	 *            Method of the object that is to change
	 * @param data
	 *            Container of this parameter
	 */
	public MethodRequest(int classId, ObisCode obisCode, int methodId, Data data) {
		this.classId = classId;
		this.obisCode = obisCode;
		this.methodId = methodId;
		methodParameters = new Data(data);
	}

	public int getClassId() {
		return classId;
	}

	public String getObisCode() {
		return obisCode.getHexCode();
	}

	public int getMethodId() {
		return methodId;
	}

	/**
	 * @return The Data container of this parameter object
	 */
	public Data data() {
		return methodParameters;
	}

	/**
	 * Creates a new MethodParameter object based on this instance.
	 * 
	 * ClassId, methodId and the data container are copied from the original object. The obisCode is exchanged with the
	 * parameter of this method call.
	 * 
	 * @param newObisCode
	 *            The changed obisCode
	 * @return Copy of this objects with the new obisCode
	 */
	public MethodRequest changeObisCode(ObisCode newObisCode) {
		return new MethodRequest(classId, newObisCode, methodId, methodParameters);
	}
}

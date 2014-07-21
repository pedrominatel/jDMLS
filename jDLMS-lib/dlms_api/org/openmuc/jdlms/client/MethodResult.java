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
 * Container class holding the results of a remote method invocation via action operation
 * 
 * @author Karsten Mueller-Bier
 */
public final class MethodResult {

	private final Data resultData;
	private final MethodResultCode resultCode;

	public MethodResult(MethodResultCode resultCode) {
		resultData = null;
		this.resultCode = resultCode;
	}

	public MethodResult(MethodResultCode resultCode, Data resultData) {
		this.resultData = resultData;
		this.resultCode = resultCode;
	}

	/**
	 * Returns the data of return data of this method call. Note that this value is null if isSuccess() is false.
	 */
	public Data getResultData() {
		return resultData;
	}

	/**
	 * @return The result code of the method call
	 */
	public MethodResultCode getResultCode() {
		return resultCode;
	}

	/**
	 * @return True if the method has been successfully processed on the remote station
	 */
	public boolean isSuccess() {
		return resultCode == MethodResultCode.SUCCESS;
	}
}

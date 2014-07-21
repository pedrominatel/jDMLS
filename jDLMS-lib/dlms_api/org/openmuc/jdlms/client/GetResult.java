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
 * Container class holding the result of a get operation received from the smart meter
 * 
 * @author Karsten Mueller-Bier
 */
public class GetResult {

	private final Data data;
	private final AccessResultCode result;

	public GetResult(Data resultData) {
		data = resultData;
		result = AccessResultCode.SUCCESS;
	}

	public GetResult(AccessResultCode errorCode) {
		data = null;
		result = errorCode;
	}

	/**
	 * Returns the data of return data of this get operation. Note that this value is null if isSuccess() is false.
	 */
	public Data getResultData() {
		return data;
	}

	/**
	 * @return The result code of the get operation
	 */
	public AccessResultCode getResultCode() {
		return result;
	}

	/**
	 * @return True if the method has been successfully processed on the remote station
	 */
	public boolean isSuccess() {
		return result == AccessResultCode.SUCCESS;
	}
}

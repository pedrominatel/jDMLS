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
 * Additional Paramter of GetParameter to narrow the results of a get operation on the smart meter.
 * 
 * Please refer IEC 62056-62 to see what specific combination of selector and parameter are allowed for each object.
 * 
 * @author Karsten Mueller-Bier
 */
public class SelectiveAccessDescription {
	private final int accessSelector;
	private final Data accessParameter;

	public SelectiveAccessDescription(int accessSelector, Data accessParameter) {
		if (accessSelector < 0 || accessSelector > 0xFF) {
			throw new IllegalArgumentException("AccessSelector must be in range [0, 255]");
		}

		this.accessSelector = accessSelector;
		this.accessParameter = accessParameter;
	}

	/**
	 * @return The selector index, specifying what shall be filtered from the response
	 */
	public int getSelector() {
		return accessSelector;
	}

	/**
	 * @return The actual filter of the selection.
	 */
	public Data getParameter() {
		return accessParameter;
	}
}

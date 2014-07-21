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
 * Declares methods that both GetRequest and SetRequest use.
 * <p>
 * This interface is used internally and has no further purpose for the user of this library
 * </p>
 * 
 * @author Karsten Müller-Bier
 */
public interface IAttributeRequest {
	/**
	 * @return The class id this request refers to
	 */
	public int getClassId();

	/**
	 * @return The logical name of the remote object this request refers to
	 */
	public String getObisCode();

	/**
	 * @return The attribute id this request refers to
	 */
	public int getAttributeId();

	/**
	 * @return The selection mask this request uses, if any
	 */
	public SelectiveAccessDescription getAccessSelection();
}

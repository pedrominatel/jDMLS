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
package org.openmuc.jdlms.client.hdlc.common;

/**
 * This exception is thrown if the HdlcHeaderParser class parses an invalid HDLC frame
 * 
 * @author Karsten Mueller-Bier
 */
public class FrameInvalidException extends Exception {

	/**
	 * Default serialVersionUID, FrameInvalidException is not intended to be serialized
	 */
	private static final long serialVersionUID = 1L;

	transient private FrameRejectReason reason = null;

	public FrameInvalidException(String message) {
		super(message);
	}

	public FrameInvalidException(String message, FrameRejectReason reason) {
		super(message);
		this.reason = reason;
	}

	public FrameRejectReason getReason() {
		return reason;
	}
}

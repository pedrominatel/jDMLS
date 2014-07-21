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
 * Collection of bit patterns that define the frame type from a control byte
 * 
 * @author Karsten Mueller-Bier
 */
public enum FrameType {

	Information(0x00, 0x01), ReceiveReady(0x01, 0x0F), ReceiveNotReady(0x05, 0x0F), SetNormalResponseMode(0x83, 0xEF), Disconnect(
			0x43, 0xEF), UnnumberedAcknowledge(0x63, 0xEF), DisconnectMode(0x0F, 0xEF), FrameReject(0x87, 0xEF), UnnumberedInformation(
			0x63, 0xEF),

	ERR_INVALID_TYPE(0xFF, 0xFF);

	private int value;
	private int mask;

	private FrameType(int value, int mask) {
		this.value = value;
		this.mask = mask;
	}

	public int getValue() {
		return value;
	}

	/**
	 * Parses a control byte and returns the decoded Frame type
	 * 
	 * @param controlByte
	 *            byte to be checked
	 * @return The decoded FrameType instance
	 */
	public static FrameType decode(int controlByte) {

		for (FrameType t : FrameType.values()) {
			if ((controlByte & t.mask) == t.value) {
				return t;
			}
		}

		return ERR_INVALID_TYPE;
	}
}

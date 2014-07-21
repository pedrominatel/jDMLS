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

import java.io.IOException;
import java.io.InputStream;

/**
 * Class representing the information field of a FrameReject response frame For more information, see ISO 13239 section
 * 5.5.3.4.2
 * 
 * @author Karsten Mueller-Bier
 */
public class FrameRejectReason {
	private byte rejectedControlField;
	private int nextSendSequence;
	private int nextReceiveSequence;
	private boolean isCommandFrame;
	private boolean controlFieldUndefined;
	private boolean invalidInformationField;
	private boolean informationSizeExceeded;
	private boolean invalidReceiveSequence;

	public boolean isCommandFrame() {
		return isCommandFrame;
	}

	public boolean isControlFieldUndefined() {
		return controlFieldUndefined;
	}

	public boolean isInvalidInformationField() {
		return invalidInformationField;
	}

	public boolean isInformationSizeExceeded() {
		return informationSizeExceeded;
	}

	public boolean isInvalidReceiveSequence() {
		return invalidReceiveSequence;
	}

	public void setControlFieldUndefined(byte controlField) {
		resetData(controlField);
		informationSizeExceeded = true;
	}

	public void setInvalidInformationField(byte controlField) {
		resetData(controlField);
		informationSizeExceeded = true;
	}

	public void setInformationSizeExceeded(byte controlField) {
		resetData(controlField);
		informationSizeExceeded = true;
	}

	public void setInvalidReceiveSequence(byte controlField) {
		resetData(controlField);
		invalidReceiveSequence = true;
	}

	public void setSequenceCounter(int sendSequence, int receiveSequence) {
		nextSendSequence = sendSequence;
		nextReceiveSequence = receiveSequence;
	}

	/**
	 * Reads the code of a FrameRejectReason object form an InputStream and initializes the fields to the coded values
	 * 
	 * @param iStream
	 *            InputStream containing the FrameRejectReason code as next 3 bytes
	 * @throws IOException
	 */
	public void decode(InputStream iStream) throws IOException {
		int buffer = iStream.read();

		rejectedControlField = (byte) buffer;

		buffer = iStream.read();

		nextSendSequence = ((buffer & 0x40) >> 6) | ((buffer & 0x20) >> 4) | ((buffer & 0x10) >> 2);

		isCommandFrame = (buffer & 0x08) == 0;

		nextReceiveSequence = ((buffer & 0x04) >> 2) | (buffer & 0x02) | ((buffer & 0x01) << 2);

		buffer = iStream.read();

		controlFieldUndefined = (buffer & 0x80) == 0x80;

		invalidInformationField = (buffer & 0x40) == 0x40;

		informationSizeExceeded = (buffer & 0x20) == 0x20;

		invalidReceiveSequence = (buffer & 0x10) == 0x10;
	}

	/**
	 * Encodes this FrameRejectReason object into the according byte array
	 * 
	 * Before this method is called, one of the setter methods or the decode method ought to be called.
	 * 
	 * @return The bytes representing the encoded object
	 */
	public byte[] encode() {
		byte[] result = new byte[3];

		result[0] = rejectedControlField;

		byte encodedSendSequence = (byte) (((nextSendSequence & 0x01) << 6) | ((nextSendSequence & 0x02) << 4) | ((nextSendSequence & 0x04) << 2));

		byte encodedReceiveSequence = (byte) (((nextReceiveSequence & 0x01) << 2) | (nextReceiveSequence & 0x02) | ((nextReceiveSequence & 0x04) >> 2));

		result[1] = (byte) (encodedReceiveSequence | encodedSendSequence);
		result[1] |= isCommandFrame ? 0x00 : 0x08;

		result[2] = 0;
		result[2] |= controlFieldUndefined ? 0x80 : 0x00;
		result[2] |= invalidInformationField ? 0x40 : 0x00;
		result[2] |= informationSizeExceeded ? 0x20 : 0x00;
		result[2] |= invalidReceiveSequence ? 0x10 : 0x00;

		return result;
	}

	private void resetData(byte controlField) {
		controlFieldUndefined = informationSizeExceeded = invalidInformationField = invalidReceiveSequence = false;

		rejectedControlField = controlField;

		isCommandFrame = (rejectedControlField & 0x08) == 0;
	}
}

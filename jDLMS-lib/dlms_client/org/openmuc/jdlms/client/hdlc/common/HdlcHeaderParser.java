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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import org.openmuc.jdlms.client.hdlc.HdlcAddress;

/**
 * This class parses the header of incoming HDLC frames for source and destination addresses and checks the frame of
 * integrity using the transmitted HCS and FCS.
 * 
 * @author Karsten Mueller-Bier
 */
public class HdlcHeaderParser {
	ByteBuffer buffer = ByteBuffer.allocate(2048);

	private HdlcAddress lastValidSource = null;
	private HdlcAddress lastValidDestination = null;

	private final FcsCalc fcsCalc = new FcsCalc();

	/**
	 * Returns the source address of the last frame that has been successfully validated by
	 * {@link HdlcHeaderParser#readFrame(InputStream)}
	 */
	public HdlcAddress getLastValidSource() {
		return lastValidSource;
	}

	/**
	 * Returns the destination address of the last frame that has been successfully validated by
	 * {@link HdlcHeaderParser#readFrame(InputStream)}
	 */
	public HdlcAddress getLastValidDestination() {
		return lastValidDestination;
	}

	/**
	 * Reads a frame from the InputStream, checking header data and integrity through HCS and FCS calculation. The frame
	 * to be read mustn'd containg the starting flag
	 * 
	 * @param iStream
	 *            InputStream containing the frame to read
	 * @return Byte array containing the frame that's just been read
	 * @throws IOException
	 *             Error on InputStream
	 * @throws FrameInvalidException
	 *             Header check or integrity check failed
	 */
	public byte[] readFrame(InputStream iStream) throws IOException, FrameInvalidException {
		byte low, high;

		fcsCalc.reset();
		buffer.clear();

		high = readNextByte(iStream);
		low = readNextByte(iStream);

		// Check Frame of right format
		if ((high & 0xF0) != 0xA0) {
			throw new FrameInvalidException("Wrong frame format");
		}

		int frameLength = ((high & 0x07) << 8) | (low & 0xFF);

		frameLength -= 2; // 2 bytes already read to receive the length

		if (frameLength > iStream.available()) {
			throw new IOException("Frame incomplete");
		}

		low = 0;
		ByteBuffer addressBuffer = ByteBuffer.wrap(new byte[4]);
		HdlcAddress destination = null;
		while ((low & 0x01) == 0) {
			low = readNextByte(iStream);
			addressBuffer.put((low));
			frameLength--;
		}
		destination = HdlcAddress.decode(new ByteArrayInputStream(addressBuffer.array()));

		low = 0;
		addressBuffer.clear();
		HdlcAddress source = null;
		while ((low & 0x01) == 0) {
			low = readNextByte(iStream);
			addressBuffer.put((low));
			frameLength--;
		}
		source = HdlcAddress.decode(new ByteArrayInputStream(addressBuffer.array()));

		// Read control byte
		readNextByte(iStream);

		// Read and check HCS
		readNextByte(iStream);
		readNextByte(iStream);
		if (fcsCalc.checkData() == false) {
			throw new FrameInvalidException("HCS has wrong value");
		}

		frameLength -= 3;

		// If FrameLength is nonzero, this frame has an information field
		// appended by an additional Frame Checking Sequence (FCS) field
		// that needs to be checked
		if (frameLength > 0) {
			// Read Information field
			for (int i = frameLength - 2; i > 0; i--) {
				readNextByte(iStream);
			}

			// Read and check FCS
			readNextByte(iStream);
			readNextByte(iStream);

			if (fcsCalc.checkData() == false) {
				throw new FrameInvalidException("FCS has wrong value");
			}
		}

		// Frame type legal and integrity checked. Frame is valid
		lastValidDestination = destination;
		lastValidSource = source;
		return buffer.array();
	}

	private byte readNextByte(InputStream iStream) throws IOException {
		// Read the next Byte form stream and update FCS calculation and
		// frame buffer accordingly
		byte result = (byte) iStream.read();
		fcsCalc.update(result);
		buffer.put(result);

		return result;
	}
}

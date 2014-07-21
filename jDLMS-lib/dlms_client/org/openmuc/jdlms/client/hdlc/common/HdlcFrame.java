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
 * This class represents a complete HDLC frame ready to be sent, excluding opening and closing flag
 * 
 * @author Karsten Mueller-Bier
 */
public class HdlcFrame {
	private final FcsCalc fcsCalc;

	private FrameType frameType;

	private byte[] informationField;
	private HdlcParameterNegotiation negotiation;
	private FrameRejectReason rejectReason;
	private HdlcAddress source;
	private HdlcAddress destination;

	private int sendSeq = -1;
	private int receiveSeq = -1;
	private boolean isSegmented = false;

	private byte controlField;

	public HdlcFrame() {
		fcsCalc = new FcsCalc();
		frameType = FrameType.ERR_INVALID_TYPE;
	}

	public void setSource(HdlcAddress address) {
		source = address;
	}

	public void setDestination(HdlcAddress address) {
		destination = address;
	}

	/**
	 * Prepares this HdlcFrame object to be sent as Information frame
	 * 
	 * @param sendSeq
	 *            Send sequence number of this frame
	 * @param receiveSeq
	 *            Expected sequence number of the next frame to be received
	 * @param data
	 *            Information data to be sent
	 * @param isSegmented
	 *            True frame is part of a bigger message
	 */
	public void setInformationFrame(int sendSeq, int receiveSeq, byte[] data, boolean isSegmented) {
		frameType = FrameType.Information;
		this.sendSeq = sendSeq;
		this.receiveSeq = receiveSeq;
		informationField = data;
		this.isSegmented = isSegmented;

		controlField = (byte) frameType.getValue();
		controlField |= ((sendSeq % 8) << 1);
		controlField |= ((receiveSeq % 8) << 5);
		if (isSegmented == false) {
			controlField |= 0x10;
		}
	}

	/**
	 * Prepares this HdlcFrame object to be sent as ReceiveNotReady frame
	 * 
	 * @param receiveSeq
	 *            Expected sequence number of the next frame to be received
	 * @param poll
	 *            True if remote end point is allowed to send data
	 */
	public void setReceiveNotReady(int receiveSeq, boolean poll) {
		frameType = FrameType.ReceiveNotReady;
		informationField = null;
		isSegmented = false;

		controlField = (byte) frameType.getValue();
		controlField |= ((receiveSeq % 8) << 5);
		if (poll) {
			controlField |= 0x10;
		}
	}

	/**
	 * Prepares this HdlcFrame object to be sent as ReceiveReady frame
	 * 
	 * @param receiveSeq
	 *            Expected sequence number of the next frame to be received
	 * @param poll
	 *            True if remote end point is allowed to send data
	 */
	public void setReceiveReady(int receiveSeq, boolean poll) {
		frameType = FrameType.ReceiveReady;
		informationField = null;
		isSegmented = false;

		controlField = (byte) frameType.getValue();
		controlField |= ((receiveSeq % 8) << 5);
		if (poll) {
			controlField |= 0x10;
		}
	}

	/**
	 * Prepares this HdlcFrame object to be sent as SetNormalResponseMode frame
	 * 
	 * @param negotiationParams
	 *            Client connection values to be negotiated. May be null
	 * @param poll
	 *            True if remote end point is allowed to send data
	 */
	public void setSetNormalResponseMode(HdlcParameterNegotiation negotiationParams, boolean poll) {
		frameType = FrameType.SetNormalResponseMode;
		negotiation = negotiationParams;
		informationField = negotiation.encode();
		isSegmented = false;

		controlField = (byte) frameType.getValue();
		if (poll) {
			controlField |= 0x10;
		}
	}

	/**
	 * Prepares this HdlcFrame object to be sent as Disconnect frame
	 * 
	 * @param poll
	 *            True if remote end point is allowed to send data
	 */
	public void setDisconnect(byte[] information, boolean poll) {
		frameType = FrameType.Disconnect;
		informationField = information;

		controlField = (byte) frameType.getValue();
		if (poll) {
			controlField |= 0x10;
		}
	}

	/**
	 * Prepares this HdlcFrame object to be sent as UnnumberedAcknowledge frame
	 * 
	 * @param negotiationParams
	 *            Connection Values that have been negotiated by the server. May be null
	 * @param poll
	 *            True if remote end point is allowed to send data
	 */
	public void setUnnumberedAcknowledge(HdlcParameterNegotiation negotiationParams, boolean poll) {
		frameType = FrameType.UnnumberedAcknowledge;
		if (negotiationParams != null) {
			negotiation = negotiationParams;
			informationField = negotiation.encode();
		}
		else {
			informationField = new byte[0];
		}
		isSegmented = false;

		controlField = (byte) frameType.getValue();
		if (poll) {
			controlField |= 0x10;
		}
	}

	/**
	 * Prepares this HdlcFrame object to be sent as DisconnectMode frame
	 * 
	 * @param poll
	 *            True if remote end point is allowed to send data
	 */
	public void setDisconnectMode(byte[] information, boolean poll) {
		frameType = FrameType.DisconnectMode;
		informationField = information;
		isSegmented = false;

		controlField = (byte) frameType.getValue();
		if (poll) {
			controlField |= 0x10;
		}
	}

	/**
	 * Prepares this HdlcFrame object to be sent as FrameReject frame
	 * 
	 * @param reason
	 *            FrameRejectReason object indicating why the frame has been rejected
	 * @param poll
	 *            True if remote end point is allowed to send data
	 */
	public void setFrameReject(FrameRejectReason reason, boolean poll) {
		frameType = FrameType.FrameReject;
		rejectReason = reason;
		informationField = rejectReason.encode();
		isSegmented = false;

		controlField = (byte) frameType.getValue();
		if (poll) {
			controlField |= 0x10;
		}
	}

	/**
	 * Prepares this HdlcFrame object to be sent as UnnumberedInformation frame
	 * 
	 * @param information
	 *            Information data to be sent
	 * @param poll
	 *            True if remote end point is allowed to send data
	 */
	public void setUnnumberedInformation(byte[] information, boolean poll) {
		frameType = FrameType.UnnumberedInformation;
		informationField = information;
		isSegmented = false;

		controlField = (byte) frameType.getValue();
		if (poll) {
			controlField |= 0x10;
		}
	}

	public HdlcAddress getDestination() {
		return destination;
	}

	public HdlcAddress getSource() {
		return source;
	}

	public FrameType getFrameType() {
		return frameType;
	}

	public byte[] getInformationField() {
		return informationField;
	}

	public HdlcParameterNegotiation getNegotiation() {
		return negotiation;
	}

	public FrameRejectReason getRejectReason() {
		return rejectReason;
	}

	public int getSendSeq() {
		return sendSeq;
	}

	public int getReceiveSeq() {
		return receiveSeq;
	}

	public boolean isSegmented() {
		return isSegmented;
	}

	/**
	 * Decodes a byte array containing an HDLC frame into this object
	 * 
	 * @param iStream
	 *            InputStream pointing at the beginning of the HDLC frame
	 * @throws IOException
	 * @throws FrameInvalidException
	 */
	public void decode(InputStream iStream) throws IOException, FrameInvalidException {
		int byteRead = 0;
		int length = 0;
		sendSeq = receiveSeq = -1;

		// Read length (11 Bits) and subtract 2, as 2 bytes have already be read
		byteRead = iStream.read();
		length = byteRead & 0x07;
		isSegmented = (byteRead & 0x08) == 0x08;
		byteRead = iStream.read();
		length = (length << 8) | byteRead;
		length -= 2;

		destination = HdlcAddress.decode(iStream);
		source = HdlcAddress.decode(iStream);

		length = length - destination.getByteSize() - source.getByteSize();

		int frameTypeField = iStream.read();
		frameType = FrameType.decode(frameTypeField);
		if (frameType == FrameType.ERR_INVALID_TYPE) {
			FrameRejectReason reason = new FrameRejectReason();
			reason.setControlFieldUndefined(controlField);
			throw new FrameInvalidException("Control field unknown " + frameTypeField, reason);
		}
		length--;

		// Read over HCS, it can be assumed that the HdlcHeaderParser class
		// already got rid of all invalid frames
		iStream.read();
		iStream.read();
		length -= 2;

		if ((frameType == FrameType.ReceiveNotReady || frameType == FrameType.ReceiveReady) && length != 0) {
			FrameRejectReason reason = new FrameRejectReason();
			reason.setInvalidInformationField((byte) frameTypeField);
			throw new FrameInvalidException("RR and RNR frames mustn't have an " + "Information field", reason);
		}

		if (frameType == FrameType.Information) {
			// Send sequence number are the bits 1 to 3 of the frame type
			// field
			sendSeq = (frameTypeField & 0x0E) >> 1;
		}
		if (frameType == FrameType.Information || frameType == FrameType.ReceiveReady
				|| frameType == FrameType.ReceiveNotReady) {
			// Receive sequence number are the bits 5 to 7 of the frame type
			// field
			receiveSeq = (frameTypeField & 0xE0) >> 5;
		}

		if (length != 0) {
			informationField = new byte[length - 2];
			if (iStream.read(informationField, 0, length - 2) != length - 2) {
				throw new IOException("Error on reading information field");
			}

			switch (frameType) {
			case SetNormalResponseMode:
			case UnnumberedAcknowledge:
				negotiation = new HdlcParameterNegotiation();
				negotiation.decode(new ByteArrayInputStream(informationField));
				break;
			case FrameReject:
				rejectReason = new FrameRejectReason();
				rejectReason.decode(new ByteArrayInputStream(informationField));
				break;
			}
		}
	}

	/**
	 * Encodes a HdlcFrame object to its corresponding byte array representation. Before this method is called, make
	 * sure that one of the setter methods or decode has been called to create a valid HDLC frame or a
	 * FrameInvalidException is thrown.
	 * 
	 * @return Byte array representing this HdlcFrame object
	 * @throws FrameInvalidException
	 */
	public byte[] encode() throws FrameInvalidException {
		if (frameType == FrameType.ERR_INVALID_TYPE) {
			throw new FrameInvalidException("Frame not initialized prior to encode");
		}

		int length = 2 + destination.getByteSize() + source.getByteSize() + 1 + 2;
		if (informationField != null) {
			length += informationField.length + 2;
		}

		ByteBuffer code = ByteBuffer.wrap(new byte[length]);

		int frameFormat = 0xA000 | length;
		if (isSegmented) {
			frameFormat |= 0x0800;
		}

		code.putShort((short) frameFormat);
		code.put(destination.encode());
		code.put(source.encode());
		code.put(controlField);

		fcsCalc.reset();
		fcsCalc.update(code.array(), code.position());
		code.put(fcsCalc.getFcsBytes());
		fcsCalc.update(fcsCalc.getFcsBytes());

		if (informationField != null) {
			code.put(informationField);
			fcsCalc.update(informationField);
			code.put(fcsCalc.getFcsBytes());
		}

		return code.array();
	}
}

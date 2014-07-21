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
package org.openmuc.jdlms.client.hdlc.impl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.communication.IUpperLayer;
import org.openmuc.jdlms.client.hdlc.HdlcAddress;
import org.openmuc.jdlms.client.hdlc.common.FrameInvalidException;
import org.openmuc.jdlms.client.hdlc.common.FrameType;
import org.openmuc.jdlms.client.hdlc.common.HdlcAddressPair;
import org.openmuc.jdlms.client.hdlc.common.HdlcFrame;

/**
 * Class representing a HDLC connection sub layer. This class and its State classes (see {@link HdlcClientLayerState})
 * implement the subset of the HDLC standard stated in IEC 62056-46
 * 
 * @author Karsten Mueller-Bier
 */
public class HdlcClientLayer implements IUpperLayer, ILowerLayer<Object> {

	private static byte FLAG = 0x7E;
	private static byte[] LLCREQUEST = new byte[] { (byte) 0xE6, (byte) 0xE6, (byte) 0x00 };

	private final ILowerLayer<HdlcAddressPair> lowerLayer;
	private IUpperLayer upperLayer;
	private final boolean isConfirmed;

	private final HdlcAddress clientAddress;
	private final HdlcAddress serverAddress;

	private int sendSeq = 0;
	private int receiveSeq = 0;

	private HdlcClientLayerState state;

	private Queue<HdlcMessage> sendQueue = new ArrayBlockingQueue<HdlcMessage>(1);

	private int sendWindowSize;
	private int sendInformationLength;

	private final ByteArrayOutputStream segmentBuffer;

	private byte[] lastFrame = null;
	private int duplicatedFrames = 0;

	public HdlcClientLayer(ILowerLayer<HdlcAddressPair> lowerLayer, HdlcAddress clientAddress,
			HdlcAddress serverAddress, HdlcClientLayerState initialState, boolean isConfirmed) {
		this.lowerLayer = lowerLayer;
		this.clientAddress = clientAddress;
		this.serverAddress = serverAddress;
		this.isConfirmed = isConfirmed;
		state = initialState;
		segmentBuffer = new ByteArrayOutputStream();
	}

	@Override
	public void connect(long timeout) throws IOException {
		try {
			state.connect(this, timeout);
		} catch (IOException e) {
			lowerLayer.removeReceivingListener(this);
			throw e;
		}
	}

	@Override
	public synchronized void send(byte[] data) throws IOException {
		if (sendQueue.size() == 7) {
			throw new IOException("Send queue full");
		}
		if (data.length > sendInformationLength * sendWindowSize) {
			throw new IOException("Message too large. " + sendInformationLength * sendWindowSize
					+ " bytes allowed. Tried to send " + data.length);
		}

		if (data.length > sendInformationLength) {
			byte[] segment = new byte[sendInformationLength + LLCREQUEST.length];
			System.arraycopy(LLCREQUEST, 0, segment, 0, LLCREQUEST.length);
			ByteBuffer dataWrapper = ByteBuffer.wrap(data);
			while (dataWrapper.remaining() > sendInformationLength) {
				dataWrapper.get(segment, LLCREQUEST.length, segment.length - LLCREQUEST.length);
				state.send(this, segment, true);
			}
			dataWrapper.get(segment, LLCREQUEST.length, dataWrapper.remaining());
			state.send(this, segment, false);
		}
		else {
			byte[] frame = new byte[data.length + LLCREQUEST.length];
			System.arraycopy(LLCREQUEST, 0, frame, 0, LLCREQUEST.length);
			System.arraycopy(data, 0, frame, LLCREQUEST.length, data.length);
			state.send(this, frame, false);
		}
	}

	@Override
	public void disconnect() throws IOException {
		state.disconnect(this);
		sendSeq = 0;
		receiveSeq = 0;
		sendQueue.clear();
	}

	@Override
	public void registerReceivingListener(Object key, IUpperLayer listener) throws IllegalArgumentException {
		upperLayer = listener;
	}

	@Override
	public void removeReceivingListener(IUpperLayer listener) {
		if (upperLayer == listener) {
			upperLayer = null;
		}
	}

	@Override
	public void discardMessage(byte[] data) {
		Iterator<HdlcMessage> iter = sendQueue.iterator();
		ByteBuffer src = ByteBuffer.wrap(data);
		HdlcFrame frame = new HdlcFrame();
		while (iter.hasNext()) {
			HdlcMessage message = iter.next();
			try {
				frame.decode(new ByteArrayInputStream(message.data, 1, message.data.length - 2));
				ByteBuffer check = ByteBuffer.wrap(frame.getInformationField());
				check.position(check.position() + 3);
				if (src.equals(check)) {
					iter.remove();
					break;
				}
			} catch (IOException e) {
				// ignore
			} catch (FrameInvalidException e) {
				// ignore
			}
		}
	}

	@Override
	public void dataReceived(byte[] data) {
		if (Arrays.equals(data, lastFrame)) {
			duplicatedFrames++;
			if (duplicatedFrames >= 5) {
				HdlcFrame frame = new HdlcFrame();
				try {
					frame.decode(new ByteArrayInputStream(lastFrame));
				} catch (IOException e) {
				} catch (FrameInvalidException e) {
				}
				if (frame.getFrameType() == FrameType.ReceiveReady) {
					int newSendSeq = frame.getReceiveSeq();
					recreateSendQueue(newSendSeq);
				}
			}
		}
		else {
			lastFrame = Arrays.copyOf(data, data.length);
			duplicatedFrames = 0;
		}
		state.dataReceived(this, data);
	}

	@Override
	public void remoteDisconnect() {
		state.remoteDisconnect(this);
	}

	/**
	 * Changes the internal State of this HDLC connection
	 * 
	 * @param nextState
	 *            The next state
	 */
	public void changeState(HdlcClientLayerState nextState) {
		state = nextState;
	}

	public HdlcClientLayerState getState() {
		return state;
	}

	/**
	 * Sets the maximum send size and window size to the values received from the smart meter
	 * 
	 * @param sendInformationLength
	 *            Maximum send size
	 * @param sendWindowSize
	 *            Window size of the smart meter
	 */
	public void setSendParameter(int sendInformationLength, int sendWindowSize) {
		this.sendInformationLength = sendInformationLength - LLCREQUEST.length;
		this.sendWindowSize = sendWindowSize;

		ArrayBlockingQueue<HdlcMessage> oldQueue = (ArrayBlockingQueue<HdlcClientLayer.HdlcMessage>) sendQueue;
		if (oldQueue.remainingCapacity() + oldQueue.size() < sendWindowSize) {
			sendQueue = new ArrayBlockingQueue<HdlcClientLayer.HdlcMessage>(sendWindowSize);
			sendQueue.addAll(oldQueue);
		}
	}

	/**
	 * Sends a ReceiveReady frame to the smart meter, indicating that the next segment of the frame can be sent
	 * 
	 * @throws IOException
	 */
	public void acknowledgeReceive() throws IOException {
		HdlcFrame frame = new HdlcFrame();
		frame.setReceiveReady(receiveSeq, true);
		frame.setDestination(serverAddress);
		frame.setSource(clientAddress);

		sendFrame(frame);
	}

	/**
	 * Acknowledges all frames inside the send buffer below and including sendSeq, removing them from the send repeat
	 * buffer
	 * 
	 * @param sendSeq
	 *            Frame number that the smart meter acknowledged
	 */
	public void acknowledgeSend(int sendSeq) {
		boolean found = false;

		sendSeq = sendSeq == 0 ? 8 : sendSeq;

		for (HdlcMessage m : sendQueue) {
			if (m.getSequenceCounter() == sendSeq - 1) {
				found = true;
			}
		}

		if (found) {
			while (sendQueue.poll().sequenceCounter != sendSeq - 1) {
				; // do nothing, sendQueue.poll() already removed the frame
			}
		}
	}

	/**
	 * Resends all frame still in the send repeat buffer
	 */
	public void resend() {
		try {
			for (HdlcMessage m : sendQueue) {
				lowerLayer.send(m.getData());
			}
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		}

	}

	/**
	 * @return HDLC address of the local client
	 */
	public HdlcAddress getClientAddress() {
		return clientAddress;
	}

	/**
	 * @return HDLC address of the remote smart meter
	 */
	public HdlcAddress getServerAddress() {
		return serverAddress;
	}

	public ILowerLayer<HdlcAddressPair> getLowerLayer() {
		return lowerLayer;
	}

	public IUpperLayer getUpperLayer() {
		return upperLayer;
	}

	/**
	 * Returns and increments the send sequence number for the next message
	 */
	public int getSendSeq() {
		int result = sendSeq;
		sendSeq = (++sendSeq) % 8;
		return result;
	}

	/**
	 * @return Sequence number of the next frame expected from the smart meter
	 */
	public int getReceiveSeq() {
		return receiveSeq;
	}

	/**
	 * Encloses the frame to be sent with the HDLC flag (0x7E) and sends it to the smart meter
	 * 
	 * @param frame
	 *            HDLC frame to be sent
	 */
	public void sendFrame(HdlcFrame frame) {
		try {
			byte[] encodedFrame = frame.encode();
			byte[] dataToSend = new byte[encodedFrame.length + 2];
			System.arraycopy(encodedFrame, 0, dataToSend, 1, encodedFrame.length);
			dataToSend[0] = FLAG;
			dataToSend[dataToSend.length - 1] = FLAG;

			synchronized (lowerLayer) {
				lowerLayer.send(dataToSend);
			}

		} catch (FrameInvalidException e) {
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		}
	}

	/**
	 * Same as {@link HdlcClientLayer#send(byte[])}, but buffers the sent frame in the send repeat buffer
	 * 
	 * @param frame
	 *            HDLC frame to be sent
	 * @throws IOException
	 */
	public void sendAndBufferFrame(HdlcFrame frame) throws IOException {
		try {
			byte[] encodedFrame = frame.encode();
			byte[] dataToSend = new byte[encodedFrame.length + 2];
			System.arraycopy(encodedFrame, 0, dataToSend, 1, encodedFrame.length);
			dataToSend[0] = FLAG;
			dataToSend[dataToSend.length - 1] = FLAG;

			while (sendQueue.offer(new HdlcMessage(dataToSend, frame.getSendSeq())) == false) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}

			synchronized (lowerLayer) {
				lowerLayer.send(dataToSend);
			}

		} catch (FrameInvalidException e) {
		}
	}

	public boolean isConfirmed() {
		return isConfirmed;
	}

	public void increaseReceiveSeq() {
		receiveSeq = (++receiveSeq) % 8;
	}

	/**
	 * Buffers a received segment from a bigger HDLC frame
	 * 
	 * @param segment
	 *            Segment to buffer
	 */
	public void bufferSegment(HdlcFrame segment) {
		try {
			segmentBuffer.write(segment.getInformationField());
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
		}
	}

	/**
	 * @return true if there is data inside the receiving segment buffer
	 */
	public boolean hasSegmentBuffered() {
		return segmentBuffer.size() > 0;
	}

	/**
	 * Clears the receiving segment buffer and returning the former content
	 * 
	 * @return Content of the receiving segment buffer
	 */
	public byte[] getBufferedSegment() {
		byte[] segment = segmentBuffer.toByteArray();
		segmentBuffer.reset();
		return segment;
	}

	/**
	 * Complete HDLC frame to buffer with easy access to the send sequence number. Used for the send repeat buffer of
	 * {@link HdlcClientLayer}
	 * 
	 * @author Karsten Mueller-Bier
	 */
	private static class HdlcMessage {
		private final byte[] data;
		private final int sequenceCounter;

		public HdlcMessage(byte[] data, int sequenceCounter) {
			this.data = data;
			this.sequenceCounter = sequenceCounter;
		}

		public byte[] getData() {
			return data;
		}

		public int getSequenceCounter() {
			return sequenceCounter;
		}
	}

	private void recreateSendQueue(int newSendSeq) {
		synchronized (sendQueue) {
			HdlcFrame frame = new HdlcFrame();
			Queue<HdlcMessage> bufferedQueue = new LinkedList<HdlcMessage>();
			bufferedQueue.addAll(sendQueue);
			sendQueue.clear();

			while (!bufferedQueue.isEmpty()) {
				HdlcMessage message = bufferedQueue.poll();
				try {
					frame.decode(new ByteArrayInputStream(message.getData(), 1, message.getData().length - 2));
				} catch (IOException e) {
				} catch (FrameInvalidException e) {
				}
				byte[] data = frame.getInformationField();
				frame.setInformationFrame(newSendSeq, frame.getReceiveSeq(), data, frame.isSegmented());
				try {
					data = frame.encode();
				} catch (FrameInvalidException e) {
					e.printStackTrace();
				}
				ByteBuffer frameBuffer = ByteBuffer.allocate(data.length + 2);
				frameBuffer.put((byte) 0x7E).put(data).put((byte) 0x7E);

				sendQueue.add(new HdlcMessage(frameBuffer.array(), newSendSeq));
				newSendSeq = (newSendSeq + 1) % 8;
			}

			sendSeq = newSendSeq;
		}
	}
}

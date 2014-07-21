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
package org.openmuc.jdlms.client.hdlc.states;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.openmuc.jdlms.client.hdlc.common.FrameInvalidException;
import org.openmuc.jdlms.client.hdlc.common.FrameType;
import org.openmuc.jdlms.client.hdlc.common.HdlcFrame;
import org.openmuc.jdlms.client.hdlc.impl.HdlcClientLayer;
import org.openmuc.jdlms.client.hdlc.impl.HdlcClientLayerState;


public class Connected extends HdlcClientLayerState {

	public static final Connected instance = new Connected();


	@Override
	public void connect(HdlcClientLayer wrapper, long timeout) throws IOException {
		return;
	}

	@Override
	public void send(HdlcClientLayer wrapper, byte[] data, boolean isSegmented) throws IOException {
		HdlcFrame frame = new HdlcFrame();

		if (wrapper.isConfirmed()) {
			frame.setInformationFrame(wrapper.getSendSeq(), wrapper.getReceiveSeq(), data, isSegmented);
		}
		else {
			frame.setUnnumberedInformation(data, false);
		}
		frame.setDestination(wrapper.getServerAddress());
		frame.setSource(wrapper.getClientAddress());

		wrapper.sendAndBufferFrame(frame);
	}

	@Override
	public void disconnect(HdlcClientLayer wrapper) throws IOException {
		HdlcFrame frame = new HdlcFrame();
		frame.setDisconnect(null, true);
		frame.setDestination(wrapper.getServerAddress());
		frame.setSource(wrapper.getClientAddress());

		wrapper.sendAndBufferFrame(frame);
		wrapper.changeState(Disconnecting.instance);
		try {
			wrapper.disconnect();
		} catch (IOException e) {
		}
	}

	@Override
	public void dataReceived(HdlcClientLayer wrapper, byte[] data) {
		HdlcFrame frame = new HdlcFrame();
		try {
			frame.decode(new ByteArrayInputStream(data));
		} catch (IOException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
			return;
		} catch (FrameInvalidException e) {
			//TODO LoggingHelper.logStackTrace(e, logger);
			return;
		}

		wrapper.increaseReceiveSeq();
		if (frame.isSegmented()) {
			wrapper.bufferSegment(frame);
			try {
				wrapper.acknowledgeReceive();
			} catch (IOException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
			}
		}
		else if (frame.getFrameType() == FrameType.Information) {
			wrapper.acknowledgeSend(frame.getReceiveSeq());
			byte[] dlms;
			byte[] wholeFrame;
			if (wrapper.hasSegmentBuffered()) {
				wrapper.bufferSegment(frame);
				wholeFrame = wrapper.getBufferedSegment();
			}
			else {
				wholeFrame = frame.getInformationField();
			}
			dlms = new byte[wholeFrame.length - 3];
			System.arraycopy(wholeFrame, 3, dlms, 0, dlms.length);
			wrapper.getUpperLayer().dataReceived(dlms);
		}
		else if (frame.getFrameType() == FrameType.ReceiveReady) {
			wrapper.acknowledgeSend(frame.getReceiveSeq());
			wrapper.resend();
		}
	}

	@Override
	public void remoteDisconnect(HdlcClientLayer wrapper) {
		wrapper.getUpperLayer().remoteDisconnect();
		wrapper.changeState(Disconnected.instance);
		wrapper.getLowerLayer().removeReceivingListener(wrapper);
	}
}

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
import org.openmuc.jdlms.client.hdlc.common.HdlcParameterNegotiation;
import org.openmuc.jdlms.client.hdlc.impl.HdlcClientLayer;
import org.openmuc.jdlms.client.hdlc.impl.HdlcClientLayerState;

public class Connecting extends HdlcClientLayerState {

	public static final Connecting instance = new Connecting();

	@Override
	public void connect(HdlcClientLayer wrapper, long timeout) throws IOException {
		synchronized (wrapper) {
			try {
				if (wrapper.getState() == this) {
					wrapper.wait(timeout);
					if (wrapper.getState() == this) {
						wrapper.changeState(Disconnected.instance);
						wrapper.getLowerLayer().removeReceivingListener(wrapper);
						throw new IOException("Device does not answer to HDLC connect");
					}
				}
			} catch (InterruptedException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
				throw new IOException("Interrupted while establishing connection");
			}
		}
	}

	@Override
	public void send(HdlcClientLayer wrapper, byte[] data, boolean isSegmented) throws IOException {
		throw new IOException("Still connecting");
	}

	@Override
	public void disconnect(HdlcClientLayer wrapper) throws IOException {
		HdlcFrame frame = new HdlcFrame();
		frame.setDisconnect(null, false);
		frame.setDestination(wrapper.getServerAddress());
		frame.setSource(wrapper.getClientAddress());

		wrapper.sendAndBufferFrame(frame);
		wrapper.changeState(Disconnecting.instance);
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

		if (frame.getFrameType() == FrameType.UnnumberedAcknowledge) {
			if (frame.getNegotiation() != null) {
				HdlcParameterNegotiation negotiation = frame.getNegotiation();
				wrapper.setSendParameter(negotiation.getReceiveInformationLength(), negotiation.getReceiveWindowSize());
			}
			synchronized (wrapper) {
				wrapper.changeState(Connected.instance);
				wrapper.notifyAll();
			}
		}
		else if (frame.getFrameType() == FrameType.DisconnectMode) {
			remoteDisconnect(wrapper);
			try {
				wrapper.getLowerLayer().disconnect();
			} catch (IOException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
			}
		}
	}

	@Override
	public void remoteDisconnect(HdlcClientLayer wrapper) {
		wrapper.changeState(Disconnected.instance);
		wrapper.getUpperLayer().remoteDisconnect();
		wrapper.getLowerLayer().removeReceivingListener(wrapper);
	}

}

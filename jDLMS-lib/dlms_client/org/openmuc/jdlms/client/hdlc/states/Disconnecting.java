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

public class Disconnecting extends HdlcClientLayerState {

	public static final Disconnecting instance = new Disconnecting();


	@Override
	public void connect(HdlcClientLayer wrapper, long timeout) throws IOException {
		throw new IOException("Still disconnecting");
	}

	@Override
	public void send(HdlcClientLayer wrapper, byte[] data, boolean isSegmented) throws IOException {
		throw new IOException("Disconnecting");
	}

	@Override
	public void disconnect(HdlcClientLayer wrapper) throws IOException {
		synchronized (wrapper) {
			try {
				while (wrapper.getState() == this) {
					wrapper.wait(5000);
				}
			} catch (InterruptedException e) {
			}
		}
		wrapper.changeState(Disconnected.instance);
		wrapper.getLowerLayer().removeReceivingListener(wrapper);
		wrapper.getLowerLayer().disconnect();
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

		if (frame.getFrameType() == FrameType.UnnumberedAcknowledge || frame.getFrameType() == FrameType.DisconnectMode) {
			synchronized (wrapper) {
				wrapper.changeState(Disconnected.instance);
				wrapper.notifyAll();
			}
		}
	}

	@Override
	public void remoteDisconnect(HdlcClientLayer wrapper) {
		wrapper.changeState(Disconnected.instance);
		wrapper.getLowerLayer().removeReceivingListener(wrapper);
	}

}

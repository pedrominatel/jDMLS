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

import java.io.IOException;

import org.openmuc.jdlms.client.hdlc.common.HdlcAddressPair;
import org.openmuc.jdlms.client.hdlc.common.HdlcFrame;
import org.openmuc.jdlms.client.hdlc.common.HdlcParameterNegotiation;
import org.openmuc.jdlms.client.hdlc.impl.HdlcClientLayer;
import org.openmuc.jdlms.client.hdlc.impl.HdlcClientLayerState;

public class Disconnected extends HdlcClientLayerState {

	public static final Disconnected instance = new Disconnected();

	@Override
	public void connect(HdlcClientLayer wrapper, long timeout) throws IOException {
		wrapper.getLowerLayer().connect(timeout);
		wrapper.getLowerLayer().registerReceivingListener(
				new HdlcAddressPair(wrapper.getClientAddress(), wrapper.getServerAddress()), wrapper);

		if (wrapper.isConfirmed()) {
			HdlcParameterNegotiation negotiation = new HdlcParameterNegotiation();
			negotiation.setReceiveInformationLength(HdlcParameterNegotiation.MIN_INFORMATION_LENGTH);
			negotiation.setReceiveWindowSize(HdlcParameterNegotiation.MIN_WINDOW_SIZE);

			HdlcFrame frame = new HdlcFrame();
			frame.setSetNormalResponseMode(negotiation, true);
			frame.setDestination(wrapper.getServerAddress());
			frame.setSource(wrapper.getClientAddress());

			wrapper.changeState(Connecting.instance);
			wrapper.sendFrame(frame);

			wrapper.connect(timeout); // Calling connect while state is connecting
										// will wait for meter response
		}
		else {
			wrapper.setSendParameter(1024, 1);
			wrapper.changeState(Connected.instance);
		}
	}

	@Override
	public void send(HdlcClientLayer wrapper, byte[] data, boolean isSegmented) throws IOException {
		throw new IOException("Connection closed");
	}

	@Override
	public void disconnect(HdlcClientLayer wrapper) throws IOException {
		// ignore
	}

	@Override
	public void dataReceived(HdlcClientLayer wrapper, byte[] data) {
		// ignore, connection isn't open
	}

	@Override
	public void remoteDisconnect(HdlcClientLayer wrapper) {
		// ignore, already closed
	}

}

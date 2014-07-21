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
package org.openmuc.jdlms.client.ip.impl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.client.ClientConnectionSettings;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.impl.ILowerLayerFactory;
import org.openmuc.jdlms.client.ip.TcpClientConnectionSettings;
import org.openmuc.jdlms.client.ip.common.ITcpLayer;
import org.openmuc.jdlms.client.ip.common.TcpLayer;

/**
 * Creates and pools all TCP sub-layers that are requested
 * 
 * @author Karsten Mueller-Bier
 */
public class TcpClientLayerFactory implements ILowerLayerFactory {

	private int tcpPort = -1;

	/**
	 * Pool of already established {@link ITcpLayer} connections.
	 */
	private final Map<InetSocketAddress, ITcpLayer> tcpLayers = new HashMap<InetSocketAddress, ITcpLayer>();

	/**
	 * Pool of already created {@link TcpClientLayer} objects. The {@link TcpClientConnectionSettings} object used to
	 * create a TcpClientLayer is also its key in this pool
	 */
	private final Map<TcpClientConnectionSettings, TcpClientLayer> tcpClientLayers = new HashMap<TcpClientConnectionSettings, TcpClientLayer>();

	@Override
	public ILowerLayer<Object> build(ClientConnectionSettings<?> setting) throws IOException {

		if (setting.isFullyParametrized() == false) {
			throw new IllegalArgumentException("Settings not fully parameterized");
		}

		if (accepts(setting.getClass()) == false) {
			throw new IllegalArgumentException("Wrong sub layer builder");
		}

		TcpClientConnectionSettings settings = (TcpClientConnectionSettings) setting;

		TcpClientLayer result = null;
		if (tcpClientLayers.containsKey(settings)) {
			result = tcpClientLayers.get(settings);
		}
		else {
			ITcpLayer lowerLayer;
			if (tcpLayers.containsKey(settings.getServerAddress())) {
				lowerLayer = tcpLayers.get(settings.getServerAddress());
			}
			else {
				if (tcpPort != -1 && tcpPort != settings.getLocalPort()) {
					throw new IllegalArgumentException("Port " + tcpPort + " already defined for TCP");
				}
				lowerLayer = new TcpLayer(SocketChannel.open(), settings.getServerAddress());
				tcpLayers.put(settings.getServerAddress(), lowerLayer);
				if (tcpPort == -1) {
					tcpPort = settings.getLocalPort();
				}
			}

			result = new TcpClientLayer(lowerLayer, settings.getClientWPort(), settings.getServerWPort(),
					settings.getServerAddress());

			tcpClientLayers.put(settings, result);
		}

		return result;
	}

	@Override
	public boolean accepts(Class<?> clazz) {
		return TcpClientConnectionSettings.class.isAssignableFrom(clazz);
	}

}

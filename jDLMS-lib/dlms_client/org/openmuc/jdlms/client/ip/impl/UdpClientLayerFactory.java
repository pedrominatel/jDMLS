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
import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.client.ClientConnectionSettings;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.impl.ILowerLayerFactory;
import org.openmuc.jdlms.client.ip.UdpClientConnectionSettings;
import org.openmuc.jdlms.client.ip.common.IUdpLayer;
import org.openmuc.jdlms.client.ip.common.UdpLayerFactory;

/**
 * Creates and pools all UDP sub-layers that are requested
 * 
 * @author Karsten Mueller-Bier
 */
public class UdpClientLayerFactory implements ILowerLayerFactory {

	private final UdpLayerFactory lowerLayerFactory = new UdpLayerFactory();

	private final Map<UdpClientConnectionSettings, UdpClientLayer> udpLayers = new HashMap<UdpClientConnectionSettings, UdpClientLayer>();

	@Override
	public ILowerLayer<Object> build(ClientConnectionSettings<?> setting) throws IOException {

		if (setting.isFullyParametrized() == false) {
			throw new IllegalArgumentException("Settings not fully parameterized");
		}

		if (accepts(setting.getClass()) == false) {
			throw new IllegalArgumentException("Wrong sub layer builder");
		}

		UdpClientConnectionSettings settings = (UdpClientConnectionSettings) setting;

		UdpClientLayer result = null;
		if (udpLayers.containsKey(settings)) {
			result = udpLayers.get(settings);
		}
		else {
			IUdpLayer lowerLayer = lowerLayerFactory.build(settings.getLocalPort());

			result = new UdpClientLayer(lowerLayer, settings.getClientWPort(), settings.getServerWPort(),
					settings.getServerAddress());

			udpLayers.put(settings, result);
		}

		return result;
	}

	@Override
	public boolean accepts(Class<?> clazz) {
		return UdpClientConnectionSettings.class.isAssignableFrom(clazz);
	}

}

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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openmuc.jdlms.client.ClientConnectionSettings;
import org.openmuc.jdlms.client.ClientConnectionSettings.ConfirmedMode;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.hdlc.HdlcClientConnectionSettings;
import org.openmuc.jdlms.client.hdlc.common.HdlcAddressPair;
import org.openmuc.jdlms.client.impl.ILowerLayerFactory;

/**
 * Creates and pools all HDLC sub layer that are requested
 * 
 * @author Karsten Mueller-Bier
 */
public class HdlcClientLayerFactory implements ILowerLayerFactory {

	//private static LocalDataExchangeFactory lowerLayerBuilder = new LocalDataExchangeFactory();

	private final Map<HdlcLayersKey, HdlcClientLayer> hdlcLayers = new HashMap<HdlcLayersKey, HdlcClientLayer>(8);

	@Override
	public ILowerLayer<Object> build(ClientConnectionSettings<?> setting) throws IOException {
		
		HdlcClientLayer result = null;

		if (setting.isFullyParametrized() == false) {
			throw new IllegalArgumentException("Settings not fully parametrized");
		}

		if (accepts(setting.getClass()) == false) {
			throw new IllegalArgumentException("Wrong sub layer builder");
		}

		HdlcClientConnectionSettings settings = (HdlcClientConnectionSettings) setting;

		//LocalDataExchangeClient lowerLayer;
		

		HdlcLayersKey key = new HdlcLayersKey(new HdlcAddressPair(settings.getClientAddress(),
				settings.getServerAddress()), settings.getBtAddress());
		
		
		if (hdlcLayers.containsKey(key)) {
			result = hdlcLayers.get(key);
			return result;
		}
//		else {
//			//lowerLayer = lowerLayerBuilder.build(settings.getBtAddress(), settings.doesUseHandshake());
//
////			result = new HdlcClientLayer(lowerLayer, settings.getClientAddress(), settings.getServerAddress(),
////					HdlcClientLayerState.beginningState(), settings.getConfirmedMode() == ConfirmedMode.CONFIRMED);
////			hdlcLayers.put(key, result);
//		}

		return result;
	}

	@Override
	public boolean accepts(Class<?> clazz) {
		return HdlcClientConnectionSettings.class.isAssignableFrom(clazz);
	}

	private class HdlcLayersKey {
		private final HdlcAddressPair addressPair;
		private final String btDevice;

		public HdlcLayersKey(HdlcAddressPair pair, String value) {
			addressPair = pair;
			btDevice = value;
		}

		@Override
		public int hashCode() {
			return addressPair.hashCode() ^ btDevice.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof HdlcLayersKey) {
				HdlcLayersKey o = (HdlcLayersKey) obj;
				return addressPair.equals(o.addressPair) && btDevice.equals(o.btDevice);
			}
			return false;
		}
	}
}

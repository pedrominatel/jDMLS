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
package org.openmuc.jdlms.client.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.openmuc.jdlms.client.ClientConnectionSettings;
import org.openmuc.jdlms.client.ClientConnectionSettings.ConfirmedMode;
import org.openmuc.jdlms.client.IClientConnection;
import org.openmuc.jdlms.client.IClientConnectionFactory;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.cosem.context.MechanismName;

/**
 * Actual implementation of {@link ClientConnectionFactory}. Creates and pools DLMS connections
 * 
 * @author Karsten Mueller-Bier
 */
@SuppressWarnings("rawtypes")
public class ClientConnectionFactory implements IClientConnectionFactory {

	// Used to pool created connections
	private final Map<ClientConnectionSettings, IClientConnection> connections = new HashMap<ClientConnectionSettings, IClientConnection>(
			16);

	private final ConnectModule connectModule = new ConnectModule();

	private ServiceLoader<ILowerLayerFactory> factoryService;

	@Override
	public IClientConnection createClientConnection(ClientConnectionSettings settings) throws IOException {
		// Guard conditions
		if (settings.isFullyParametrized() == false) {
			throw new IllegalArgumentException("ConnectionSettings not fully parametrized");
		}

		IClientConnection result;

		if (connections.containsKey(settings)) {
			// A connection with this settings has already been created
			// reuse that
			result = connections.get(settings);
		}
		else {
			ILowerLayer<Object> lowerLayer = null;
			if (factoryService == null) {
				factoryService = ServiceLoader.load(ILowerLayerFactory.class);
			}

			ILowerLayerFactory factory = getLowerLayerFactory(settings.getClass());
			if (factory == null) {
				throw new IllegalArgumentException("No connection builder for " + settings.getClass() + " found");
			}

			lowerLayer = factory.build(settings);

			MechanismName mechanism = null;

			switch (settings.getAuthentication()) {
			case LOWEST:
				mechanism = MechanismName.LOWEST;
				break;
			case LOW:
				mechanism = MechanismName.LOW;
				break;
			case HIGH_MD5:
				mechanism = MechanismName.HIGH_MD5;
				break;
			case HIGH_SHA1:
				mechanism = MechanismName.HIGH_SHA1;
				break;
			default:
				throw new IllegalArgumentException("Unknown authentication method: " + settings.getAuthentication());
			}

			switch (settings.getReferencingMethod()) {
			case LN:
				result = new LNConnection(settings.getConfirmedMode() == ConfirmedMode.CONFIRMED, mechanism,
						lowerLayer, connectModule);
				break;
			case SN:
				result = new SNConnection(settings.getConfirmedMode() == ConfirmedMode.CONFIRMED, mechanism,
						lowerLayer, connectModule);
				break;
			default:
				throw new IllegalArgumentException("Unknown referencing method: " + settings.getReferencingMethod());
			}

			connections.put(settings, result);
		}

		return result;
	}

	protected ILowerLayerFactory getLowerLayerFactory(Class<? extends ClientConnectionSettings> settingsClass) {
		for (ILowerLayerFactory factory : factoryService) {
			if (factory.accepts(settingsClass)) {
				return factory;
			}
		}
		return null;
	}
}

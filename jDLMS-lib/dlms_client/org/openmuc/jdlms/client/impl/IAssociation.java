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

import org.openmuc.asn1.cosem.InitiateRequest;
import org.openmuc.jdlms.client.communication.ILowerLayer;
import org.openmuc.jdlms.client.cosem.context.ApplicationContext;
import org.openmuc.jdlms.client.cosem.context.MechanismName;

public interface IAssociation {
	/**
	 * @return The application context of the association. Contains referencing and encryption mode
	 */
	public abstract ApplicationContext getAppContext();

	/**
	 * @return The authentication mechanism of the association.
	 */
	public abstract MechanismName getAuthName();

	public abstract boolean isConfirmedMode();

	/**
	 * @return The xDLMS Initiate Request of the association. Contains confirm mode
	 */
	public abstract InitiateRequest getXDlmsRequest();

	/**
	 * @return Retrieves the ILowerLayer form this association. The association is removed from the lower layer
	 */
	public abstract ILowerLayer<Object> moveLowerLayer();

	/**
	 * Registers the association with the ILowerLayer. Used in tandem with getLowerLayer()
	 */
	public abstract void setLowerLayer(ILowerLayer<Object> lowerLayer);

	/**
	 * Process step 3 and 4 of HIGH level security authentication
	 * 
	 * @param processedChallenge
	 *            Challenge of the server, processed with the pre-shared secret
	 * @param timeout
	 *            Amount of milliseconds waited before the request is aborted
	 * @return The computed challenge from the remote server. Null if authentication failed
	 * @throws IOException
	 */
	public abstract byte[] hlsAuthentication(byte[] processedChallenge, long timeout) throws IOException;
}

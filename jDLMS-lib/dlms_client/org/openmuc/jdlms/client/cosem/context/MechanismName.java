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
package org.openmuc.jdlms.client.cosem.context;

import java.util.ArrayList;
import java.util.List;

import org.openmuc.jasn1.ber.types.BerObjectIdentifier;

/**
 * Object Identifier indicating what level of authentication this connection needs for establishment.
 * 
 * @author Karsten Mueller-Bier
 */
public final class MechanismName extends CosemObjectIdentifier {

	private static final List<MechanismName> mechanisms = new ArrayList<MechanismName>(5);

	/**
	 * Lowest authentication level. None authentication used
	 */
	public static final MechanismName LOWEST = new MechanismName(0);

	/**
	 * Low authentication level. Client needs to authenticate itself by sending secret.
	 */
	public static final MechanismName LOW = new MechanismName(1);

	/**
	 * High authentication level using manufacturer specific hash algorithm. Both client and smart meter need to
	 * authenticate each other
	 */
	public static final MechanismName HIGH_MANUFACTURER = new MechanismName(2);

	/**
	 * High authentication level using MD5. Both client and smart meter need to authenticate each other
	 */
	public static final MechanismName HIGH_MD5 = new MechanismName(3);

	/**
	 * High authentication level using SHA1. Both client and smart meter need to authenticate each other
	 */
	public static final MechanismName HIGH_SHA1 = new MechanismName(4);

	private static final int AUTHENTICATION_MECHANISM_NAME = 2;
	private final int authenticationId;

	private MechanismName(int contextId) {
		super(AUTHENTICATION_MECHANISM_NAME, contextId);
		authenticationId = contextId;
		mechanisms.add(this);
	}

	/**
	 * Parses a received Object Identifier, returning the encoded MechanismName. This method is used inside the library
	 * and doesn't need to be invoked by the user of jDLMS
	 * 
	 * @param objectId
	 *            the received Object Identifier
	 * @return the decoded MechanismName
	 */
	public static MechanismName extractMechanismId(BerObjectIdentifier objectId) {
		if (!checkCosemObjectIdentifier(objectId)
				|| objectId.objectIdentifierComponents[5] != AUTHENTICATION_MECHANISM_NAME) {
			throw new IllegalArgumentException("objectId id no MechanismName");
		}

		int authenticationId = objectId.objectIdentifierComponents[6];
		for (MechanismName mechanism : mechanisms) {
			if (mechanism.authenticationId == authenticationId) {
				return mechanism;
			}
		}

		throw new IllegalArgumentException("objectId has unknown MechanismId");
	}
}

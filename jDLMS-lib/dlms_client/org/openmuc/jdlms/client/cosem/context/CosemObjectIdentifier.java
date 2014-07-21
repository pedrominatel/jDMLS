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

import org.openmuc.jasn1.ber.types.BerObjectIdentifier;

/**
 * Base class for all Object Identifier used in COSEM context while transmitting Application Association and Application
 * Release messages.
 * 
 * @author Karsten Mueller-Bier
 */
public abstract class CosemObjectIdentifier extends BerObjectIdentifier {

	public static final int JOINT_ISO_CCITT = 2;
	public static final int COUNTRY = 16;
	public static final int COUNTRY_NAME = 756;
	public static final int IDENTIFIED_ORGANISATION = 5;
	public static final int DLMS_UA = 8;

	protected CosemObjectIdentifier(int applicationContext, int contextId) {
		super(new int[] { JOINT_ISO_CCITT, COUNTRY, COUNTRY_NAME, IDENTIFIED_ORGANISATION, DLMS_UA, applicationContext,
				contextId });
	}

	protected static boolean checkCosemObjectIdentifier(BerObjectIdentifier objectId) {
		if (objectId.objectIdentifierComponents.length != 7) {
			return false;
		}
		if (objectId.objectIdentifierComponents[0] != JOINT_ISO_CCITT) {
			return false;
		}
		if (objectId.objectIdentifierComponents[1] != COUNTRY) {
			return false;
		}
		if (objectId.objectIdentifierComponents[2] != COUNTRY_NAME) {
			return false;
		}
		if (objectId.objectIdentifierComponents[3] != IDENTIFIED_ORGANISATION) {
			return false;
		}
		if (objectId.objectIdentifierComponents[4] != DLMS_UA) {
			return false;
		}

		return true;
	}
}

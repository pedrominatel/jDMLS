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
 * Object identifier indicating what sort of referencing method is used on the established connection
 * 
 * @author Karsten Mueller-Bier
 */
public final class ApplicationContext extends CosemObjectIdentifier {

	private static final List<ApplicationContext> applications = new ArrayList<ApplicationContext>(4);

	/**
	 * Logical Name (LN) referencing without encrypting each message
	 */
	public static final ApplicationContext LOGICAL_NAME_NO_CIPHERING = new ApplicationContext(1);

	/**
	 * Logical Name (LN) referencing and encryption of each message
	 */
	public static final ApplicationContext SHORT_NAME_NO_CIPHERING = new ApplicationContext(2);

	/**
	 * Short Name (SN) referencing without encrypting each message
	 */
	public static final ApplicationContext LOGICAL_NAME_WITH_CIPHERING = new ApplicationContext(3);

	/**
	 * Short Name (SN) referencing and encryption of each message
	 */
	public static final ApplicationContext SHORT_NAME_WITH_CIPHERING = new ApplicationContext(4);

	private static final int APPLICATION_CONTEXT = 1;
	private final int applicationId;

	private ApplicationContext(int contextId) {
		super(APPLICATION_CONTEXT, contextId);
		applicationId = contextId;
		applications.add(this);
	}

	/**
	 * Parses a received Object Identifier, returning the encoded ApplicationContext. This method is used inside the
	 * library and doesn't need to be invoked by the user of jDLMS
	 * 
	 * @param objectId
	 *            the received Object Identifier
	 * @return the decoded ApplicationContext
	 */
	public static ApplicationContext extractApplicationContextId(BerObjectIdentifier objectId) {
		if (!checkCosemObjectIdentifier(objectId) || objectId.objectIdentifierComponents[5] != APPLICATION_CONTEXT) {
			throw new IllegalArgumentException("objectId is no ApplicationContext");
		}

		int applicationId = objectId.objectIdentifierComponents[6];

		for (ApplicationContext app : applications) {
			if (app.applicationId == applicationId) {
				return app;
			}
		}

		return null;
	}
}

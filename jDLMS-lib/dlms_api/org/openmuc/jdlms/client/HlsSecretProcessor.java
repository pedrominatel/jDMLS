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
package org.openmuc.jdlms.client;

import java.io.IOException;

/**
 * Interface used to provide a manufacturer specific way for processing the secret with a given salt according to the
 * high level authentication stated in IEC 62056-62 4.7.2 Pass 3 and 4.
 * 
 * An example for processing the secret is appending the secret to the salt and generating a MD5 digest, which is
 * returned as the result. Note that you do not have to implement this specific implementation, as it is one of the two
 * standard methods provided by jDLMS by using {@link ClientConnectionSettings.Authentication#HIGH_MD5}.
 * 
 * @author Karsten Mueller-Bier
 */
public interface HlsSecretProcessor {

	/**
	 * Callback method to provide an algorithm for processing a secret byte sequence with a salt byte sequence
	 * 
	 * @param secret
	 *            The pre shared secret
	 * @param salt
	 *            The generated salt
	 * @return The processed byte sequence
	 */
	public byte[] process(byte[] secret, byte[] salt) throws IOException;
}

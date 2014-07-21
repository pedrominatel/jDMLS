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
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.openmuc.jdlms.client.HlsSecretProcessor;

/**
 * Implementation of HIGH Level authentication using SHA-1 hashing as described in IEC 62056-62
 * 
 * @author Karsten Mueller-Bier
 */
public class HlsProcessorSha1 implements HlsSecretProcessor {

	@Override
	public byte[] process(byte[] secret, byte[] salt) throws IOException {
		MessageDigest md = null;
		try {
			md = MessageDigest.getInstance("SHA1");
		} catch (NoSuchAlgorithmException e) {
			throw new IOException("Could not process secret. No SHA-1 algorithm installed", e);
		}

		byte[] input = ByteBuffer.allocate(secret.length + salt.length).put(salt).put(secret).array();

		byte[] result = md.digest(input);

		return result;
	}

}

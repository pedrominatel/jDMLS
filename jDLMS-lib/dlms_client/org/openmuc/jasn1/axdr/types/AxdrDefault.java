/*
 * Copyright Fraunhofer ISE, 2012
 * Author(s): Karsten Mueller-Bier
 *    
 * This file is part of jASN1.
 * For more information visit http://www.openmuc.org
 * 
 * jASN1 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * jASN1 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with jASN1.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package org.openmuc.jasn1.axdr.types;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.axdr.AxdrByteArrayOutputStream;
import org.openmuc.jasn1.axdr.AxdrType;

public class AxdrDefault<T extends AxdrType> {

	private T value;
	private final T defaultValue;

	public AxdrDefault(T value, T defaultValue) {
		if (value == null) {
			throw new IllegalArgumentException("Value cannot be null");
		}
		if (defaultValue == null) {
			throw new IllegalArgumentException("Default cannot be null");
		}
		this.value = value;
		this.defaultValue = defaultValue;
	}

	public int encode(AxdrByteArrayOutputStream axdrOStream) throws IOException {
		int codeLength = 0;

		boolean usage = !value.equals(defaultValue);

		if (usage) {
			codeLength += value.encode(axdrOStream);
		}

		codeLength += new AxdrBoolean(usage).encode(axdrOStream);

		return codeLength;
	}

	public int decode(InputStream iStream) throws IOException {
		int codeLength = 0;

		AxdrBoolean axdrUsage = new AxdrBoolean();
		codeLength += axdrUsage.decode(iStream);

		if (axdrUsage.getValue()) {
			codeLength += value.decode(iStream);
		}
		else {
			setValueToDefault();
		}

		return codeLength;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		if (value == null) {
			try {
				setValueToDefault();
			} catch (IOException e) {
				//TODO LoggingHelper.logStackTrace(e, logger);
			}
		}
		else {
			this.value = value;
		}
	}

	private void setValueToDefault() throws IOException {
		AxdrByteArrayOutputStream buffer = new AxdrByteArrayOutputStream(32, true);
		defaultValue.encode(buffer);
		value.decode(new ByteArrayInputStream(buffer.getArray()));
	}
}

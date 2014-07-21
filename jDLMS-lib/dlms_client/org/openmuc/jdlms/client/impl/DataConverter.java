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

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.asn1.cosem.Data.SubSeqOf_array;
import org.openmuc.asn1.cosem.Data.SubSeqOf_structure;
import org.openmuc.asn1.cosem.EVENT_NOTIFICATION_Request;
import org.openmuc.asn1.cosem.Enum;
import org.openmuc.asn1.cosem.Get_Data_Result;
import org.openmuc.asn1.cosem.Integer16;
import org.openmuc.asn1.cosem.Integer32;
import org.openmuc.asn1.cosem.Integer64;
import org.openmuc.asn1.cosem.Integer8;
import org.openmuc.asn1.cosem.Unsigned16;
import org.openmuc.asn1.cosem.Unsigned32;
import org.openmuc.asn1.cosem.Unsigned64;
import org.openmuc.asn1.cosem.Unsigned8;
import org.openmuc.jasn1.axdr.types.AxdrBitString;
import org.openmuc.jasn1.axdr.types.AxdrBoolean;
import org.openmuc.jasn1.axdr.types.AxdrNull;
import org.openmuc.jasn1.axdr.types.AxdrOctetString;
import org.openmuc.jasn1.axdr.types.AxdrVisibleString;
import org.openmuc.jdlms.client.AccessResultCode;
import org.openmuc.jdlms.client.Data;
import org.openmuc.jdlms.client.Data.Choices;
import org.openmuc.jdlms.client.EventNotification;
import org.openmuc.jdlms.client.GetResult;

public class DataConverter {

	public static Data toApi(org.openmuc.asn1.cosem.Data pdu) {

		Data result = new Data();

		org.openmuc.asn1.cosem.Data.Choices choice = pdu.getChoiceIndex();

		if (choice == org.openmuc.asn1.cosem.Data.Choices.NULL_DATA) {
			result.setNull();
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.ARRAY) {
			List<Data> innerData = new LinkedList<Data>();
			for (org.openmuc.asn1.cosem.Data item : pdu.array.list()) {
				innerData.add(toApi(item));
			}
			result.setArray(innerData);
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.STRUCTURE) {
			List<Data> innerData = new LinkedList<Data>();
			for (org.openmuc.asn1.cosem.Data item : pdu.structure.list()) {
				innerData.add(toApi(item));
			}
			result.setStructure(innerData);
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.BOOL) {
			result.setbool(pdu.bool.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.BIT_STRING) {
			result.setBitString(pdu.bit_string.getValues(), pdu.bit_string.getMaxLength());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.DOUBLE_LONG) {
			result.setInteger32(pdu.double_long.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.DOUBLE_LONG_UNSIGNED) {
			result.setUnsigned32(pdu.double_long_unsigned.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.OCTET_STRING) {
			result.setOctetString(pdu.octet_string.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.VISIBLE_STRING) {
			result.setVisibleString(pdu.visible_string.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.BCD) {
			result.setBcd(pdu.bcd.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.INTEGER) {
			result.setInteger8(pdu.integer.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.LONG_INTEGER) {
			result.setInteger16(pdu.long_integer.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.UNSIGNED) {
			result.setUnsigned8(pdu.unsigned.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.LONG_UNSIGNED) {
			result.setUnsigned16(pdu.long_unsigned.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.COMPACT_ARRAY) {
			// TODO implement compact array
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.LONG64) {
			result.setInteger64(pdu.long64.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.LONG64_UNSIGNED) {
			result.setUnsigned64(pdu.long64_unsigned.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.ENUMERATE) {
			result.setEnumerate(pdu.enumerate.getValue());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.FLOAT32) {
			ByteBuffer buf = ByteBuffer.wrap(pdu.float32.getValue());
			result.setFloat32(buf.getFloat());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.FLOAT64) {
			ByteBuffer buf = ByteBuffer.wrap(pdu.float64.getValue());
			result.setFloat64(buf.getDouble());
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.DATE_TIME) {
			ByteBuffer data = ByteBuffer.wrap(pdu.date_time.getValue());
			Calendar cal = Calendar.getInstance();
			int year = data.get() << 8;
			year |= (data.get() & 0xFF);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, data.get());
			cal.set(Calendar.DAY_OF_MONTH, data.get());
			data.get(); // Day of week. Not needed because we have set day of
			// month earlier
			cal.set(Calendar.HOUR, data.get());
			cal.set(Calendar.MINUTE, data.get());
			cal.set(Calendar.SECOND, data.get());
			cal.set(Calendar.MILLISECOND, data.get() * 10);
			int timeZoneOffset = data.get() << 8;
			timeZoneOffset |= (data.get() & 0xFF);
			cal.set(Calendar.ZONE_OFFSET, timeZoneOffset * 60000);
			result.setDateTime(cal, true);
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.DATE) {
			ByteBuffer data = ByteBuffer.wrap(pdu.date.getValue());
			Calendar cal = Calendar.getInstance();
			int year = data.get() << 8;
			year |= (data.get() & 0xFF);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, data.get());
			cal.set(Calendar.DAY_OF_MONTH, data.get());
			result.setDate(cal);
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.TIME) {
			ByteBuffer data = ByteBuffer.wrap(pdu.time.getValue());
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR, data.get());
			cal.set(Calendar.MINUTE, data.get());
			cal.set(Calendar.SECOND, data.get());
			cal.set(Calendar.MILLISECOND, data.get() * 10);
			result.setTime(cal, true);
		}

		else if (choice == org.openmuc.asn1.cosem.Data.Choices.DONT_CARE) {
			result.setNull();
		}

		return result;
	}

	public static org.openmuc.asn1.cosem.Data toPdu(Data data) {
		org.openmuc.asn1.cosem.Data result = new org.openmuc.asn1.cosem.Data();

		Data.Choices choice = data.getChoiceIndex();
		ByteBuffer buffer;

		if (choice == Choices.DONT_CARE) {
			result.setdont_care(new AxdrNull());
		}

		if (choice == Choices.TIME) {
			Calendar cal = data.getCalendar();
			buffer = ByteBuffer.allocate(12);
			buffer.put((byte) cal.get(Calendar.HOUR));
			buffer.put((byte) cal.get(Calendar.MINUTE));
			buffer.put((byte) cal.get(Calendar.SECOND));
			buffer.put((byte) (data.useMilliseconds() ? cal.get(Calendar.MILLISECOND / 10) : 0xFF));

			result.settime(new AxdrOctetString(buffer.array()));
		}

		if (choice == Choices.DATE) {
			Calendar cal = data.getCalendar();

			// Convert between Calendar.Day_OF_WEEK (defining Sunday as 1) and
			// DLMS
			// DayOfWeek (defining Monday as 1 and Sunday as 7)
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if (dayOfWeek == 0) {
				dayOfWeek = 7;
			}

			buffer = ByteBuffer.allocate(12);
			buffer.putShort((short) cal.get(Calendar.YEAR));
			buffer.put((byte) (cal.get(Calendar.MONTH) + 1));
			buffer.put((byte) cal.get(Calendar.DAY_OF_MONTH));
			buffer.put((byte) dayOfWeek);

			result.setdate(new AxdrOctetString(buffer.array()));
		}

		if (choice == Choices.DATE_TIME) {
			Calendar cal = data.getCalendar();

			// Convert between Calendar.Day_OF_WEEK (defining Sunday as 1) and
			// DLMS DayOfWeek (defining Monday as 1 and Sunday as 7)
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
			if (dayOfWeek == 0) {
				dayOfWeek = 7;
			}
			// Calculate the offset of this time zone in minutes
			int timeZoneOffset = cal.get(Calendar.ZONE_OFFSET) / 60000;

			buffer = ByteBuffer.allocate(12);
			buffer.putShort((short) cal.get(Calendar.YEAR));
			buffer.put((byte) (cal.get(Calendar.MONTH) + 1));
			buffer.put((byte) cal.get(Calendar.DAY_OF_MONTH));
			buffer.put((byte) dayOfWeek);
			buffer.put((byte) cal.get(Calendar.HOUR));
			buffer.put((byte) cal.get(Calendar.MINUTE));
			buffer.put((byte) cal.get(Calendar.SECOND));
			buffer.put((byte) (data.useMilliseconds() ? cal.get(Calendar.MILLISECOND) / 10 : 0xFF));
			buffer.putShort((short) timeZoneOffset);
			buffer.put(cal.getTimeZone().inDaylightTime(cal.getTime()) ? (byte) 0x80 : 0x00);

			result.setoctet_string(new AxdrOctetString(buffer.array()));
		}

		if (choice == Choices.FLOAT64) {
			buffer = ByteBuffer.allocate(8);
			buffer.putDouble(data.getNumber().doubleValue());
			buffer.flip();

			result.setfloat64(new AxdrOctetString(8, buffer.array()));
		}

		if (choice == Choices.FLOAT32) {
			buffer = ByteBuffer.allocate(4);
			buffer.putDouble(data.getNumber().floatValue());
			buffer.flip();

			result.setfloat32(new AxdrOctetString(4, buffer.array()));
		}

		if (choice == Choices.ENUMERATE) {
			result.setenumerate(new Enum(data.getNumber().longValue()));
		}

		if (choice == Choices.LONG64_UNSIGNED) {
			result.setlong64_unsigned(new Unsigned64(data.getNumber().longValue()));
		}

		if (choice == Choices.LONG64) {
			result.setlong64(new Integer64(data.getNumber().longValue()));
		}

		if (choice == Choices.COMPACT_ARRAY) {
			// TODO Implement compact array
		}

		if (choice == Choices.LONG_UNSIGNED) {
			result.setlong_unsigned(new Unsigned16(data.getNumber().longValue()));
		}

		if (choice == Choices.UNSIGNED) {
			result.setunsigned(new Unsigned8(data.getNumber().longValue()));
		}

		if (choice == Choices.LONG_INTEGER) {
			result.setlong_integer(new Integer16(data.getNumber().longValue()));
		}

		if (choice == Choices.INTEGER) {
			result.setinteger(new Integer8(data.getNumber().longValue()));
		}

		if (choice == Choices.BCD) {
			result.setbcd(new Integer8(data.getNumber().longValue()));
		}

		if (choice == Choices.VISIBLE_STRING) {
			result.setvisible_string(new AxdrVisibleString(data.getByteArray()));
		}

		if (choice == Choices.OCTET_STRING) {
			result.setoctet_string(new AxdrOctetString(data.getByteArray()));
		}

		if (choice == Choices.DOUBLE_LONG_UNSIGNED) {
			result.setdouble_long_unsigned(new Unsigned32(data.getNumber().longValue()));
		}

		if (choice == Choices.DOUBLE_LONG) {
			result.setdouble_long(new Integer32(data.getNumber().longValue()));
		}

		if (choice == Choices.BIT_STRING) {
			result.setbit_string(new AxdrBitString(data.getByteArray(), data.getNumber().intValue()));
		}

		if (choice == Choices.BOOL) {
			result.setbool(new AxdrBoolean(data.getBoolean()));
		}

		if (choice == Choices.STRUCTURE) {
			result.setstructure(new SubSeqOf_structure());
			for (Data element : data.getComplex()) {
				result.structure.add(toPdu(element));
			}
		}

		if (choice == Choices.ARRAY) {
			result.setarray(new SubSeqOf_array());
			for (Data element : data.getComplex()) {
				result.array.add(toPdu(element));
			}
		}

		if (choice == Choices.NULL_DATA) {
			result.setnull_data(new AxdrNull());
		}

		return result;
	}

	public static GetResult toApi(Get_Data_Result pdu) {
		GetResult result = null;

		if (pdu.getChoiceIndex() == Get_Data_Result.Choices.DATA) {
			result = new GetResult(toApi(pdu.data));
		}
		else {
			result = new GetResult(AccessResultCode.fromValue((int) pdu.data_access_result.getValue()));
		}

		return result;
	}

	public static EventNotification toApi(EVENT_NOTIFICATION_Request pdu) {
		int classId = (int) pdu.cosem_attribute_descriptor.class_id.getValue();
		int attributeId = (int) pdu.cosem_attribute_descriptor.attribute_id.getValue();

		StringBuilder sb = new StringBuilder();
		for (Byte b : pdu.cosem_attribute_descriptor.instance_id.getValue()) {
			sb.append(Integer.toHexString((int) b & 0xFF));
		}
		String obisCode = sb.toString();

		Long timestamp = null;
		if (pdu.time.isUsed()) {
			ByteBuffer data = ByteBuffer.wrap(pdu.time.getValue().getValue());
			Calendar cal = Calendar.getInstance();
			int year = data.get() << 8;
			year |= (data.get() & 0xFF);
			cal.set(Calendar.YEAR, year);
			cal.set(Calendar.MONTH, data.get());
			cal.set(Calendar.DAY_OF_MONTH, data.get());
			data.get(); // Day of week. Not needed because we have set day of
			// month earlier
			cal.set(Calendar.HOUR, data.get());
			cal.set(Calendar.MINUTE, data.get());
			cal.set(Calendar.SECOND, data.get());
			cal.set(Calendar.MILLISECOND, data.get() * 10);
			int timeZoneOffset = data.get() << 8;
			timeZoneOffset |= (data.get() & 0xFF);
			cal.set(Calendar.ZONE_OFFSET, timeZoneOffset * 60000);

			timestamp = cal.getTimeInMillis();
		}

		Data newValue = null;
		if (pdu.attribute_value != null) {
			newValue = toApi(pdu.attribute_value);
		}

		EventNotification result = new EventNotification(classId, obisCode, attributeId, newValue, timestamp);
		return result;
	}
}

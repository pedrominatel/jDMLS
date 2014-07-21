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

import org.openmuc.asn1.cosem.ACTION_Response;
import org.openmuc.asn1.cosem.GET_Response;
import org.openmuc.asn1.cosem.GET_Response.Choices;
import org.openmuc.asn1.cosem.SET_Response;

public class PduHelper {

	public static int getInvokeId(GET_Response pdu) {
		int invokeId = -1;
		if (pdu.getChoiceIndex() == Choices.GET_RESPONSE_NORMAL) {
			invokeId = (pdu.get_response_normal.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == Choices.GET_RESPONSE_WITH_DATABLOCK) {
			invokeId = (pdu.get_response_with_datablock.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == Choices.GET_RESPONSE_WITH_LIST) {
			invokeId = (pdu.get_response_with_list.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		return invokeId;
	}

	public static int getInvokeId(SET_Response pdu) {
		int invokeId = -1;
		if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.SET_Response.Choices.SET_RESPONSE_NORMAL) {
			invokeId = (pdu.set_response_normal.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.SET_Response.Choices.SET_RESPONSE_WITH_LIST) {
			invokeId = (pdu.set_response_with_list.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.SET_Response.Choices.SET_RESPONSE_DATABLOCK) {
			invokeId = (pdu.set_response_datablock.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.SET_Response.Choices.SET_RESPONSE_LAST_DATABLOCK) {
			invokeId = (pdu.set_response_last_datablock.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.SET_Response.Choices.SET_RESPONSE_LAST_DATABLOCK_WITH_LIST) {
			invokeId = (pdu.set_response_last_datablock_with_list.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		return invokeId;
	}

	public static int getInvokeId(ACTION_Response pdu) {
		int invokeId = -1;
		if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.ACTION_Response.Choices.ACTION_RESPONSE_NORMAL) {
			invokeId = (pdu.action_response_normal.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.ACTION_Response.Choices.ACTION_RESPONSE_WITH_LIST) {
			invokeId = (pdu.action_response_with_list.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.ACTION_Response.Choices.ACTION_RESPONSE_NEXT_PBLOCK) {
			invokeId = (pdu.action_response_next_pblock.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		else if (pdu.getChoiceIndex() == org.openmuc.asn1.cosem.ACTION_Response.Choices.ACTION_RESPONSE_WITH_PBLOCK) {
			invokeId = (pdu.action_response_with_pblock.invoke_id_and_priority.getValues()[0] & 0xF0) >>> 4;
		}
		return invokeId;
	}

}

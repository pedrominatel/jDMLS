/**
 * This class file was automatically generated by jASN1 (http://www.openmuc.org)
 */

package org.openmuc.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.axdr.AxdrByteArrayOutputStream;
import org.openmuc.jasn1.axdr.AxdrType;
import org.openmuc.jasn1.axdr.types.AxdrEnum;

public class SET_Response implements AxdrType {

	public byte[] code = null;

	public static enum Choices {
		_ERR_NONE_SELECTED(-1), SET_RESPONSE_NORMAL(1), SET_RESPONSE_DATABLOCK(2), SET_RESPONSE_LAST_DATABLOCK(3), SET_RESPONSE_LAST_DATABLOCK_WITH_LIST(
				4), SET_RESPONSE_WITH_LIST(5), ;

		private int value;

		private Choices(int value) {
			this.value = value;
		}

		public int getValue() {
			return value;
		}

		public static Choices valueOf(long tagValue) {
			Choices[] values = Choices.values();

			for (Choices c : values) {
				if (c.value == tagValue) {
					return c;
				}
			}
			return _ERR_NONE_SELECTED;
		}
	}

	private Choices choice;

	public Set_Response_Normal set_response_normal = null;

	public Set_Response_Datablock set_response_datablock = null;

	public Set_Response_Last_Datablock set_response_last_datablock = null;

	public Set_Response_Last_Datablock_With_List set_response_last_datablock_with_list = null;

	public Set_Response_With_List set_response_with_list = null;

	public SET_Response() {
	}

	public SET_Response(byte[] code) {
		this.code = code;
	}

	@Override
	public int encode(AxdrByteArrayOutputStream axdrOStream) throws IOException {
		if (code != null) {
			for (int i = code.length - 1; i >= 0; i--) {
				axdrOStream.write(code[i]);
			}
			return code.length;

		}
		if (choice == Choices._ERR_NONE_SELECTED) {
			throw new IOException("Error encoding AxdrChoice: No item in choice was selected.");
		}

		int codeLength = 0;

		if (choice == Choices.SET_RESPONSE_WITH_LIST) {
			codeLength += set_response_with_list.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(5);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		if (choice == Choices.SET_RESPONSE_LAST_DATABLOCK_WITH_LIST) {
			codeLength += set_response_last_datablock_with_list.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(4);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		if (choice == Choices.SET_RESPONSE_LAST_DATABLOCK) {
			codeLength += set_response_last_datablock.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(3);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		if (choice == Choices.SET_RESPONSE_DATABLOCK) {
			codeLength += set_response_datablock.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(2);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		if (choice == Choices.SET_RESPONSE_NORMAL) {
			codeLength += set_response_normal.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(1);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		// This block should be unreachable
		throw new IOException("Error encoding AxdrChoice: No item in choice was encoded.");
	}

	@Override
	public int decode(InputStream iStream) throws IOException {
		int codeLength = 0;
		AxdrEnum choosen = new AxdrEnum();

		codeLength += choosen.decode(iStream);
		resetChoices();
		choice = Choices.valueOf(choosen.getValue());

		if (choice == Choices.SET_RESPONSE_NORMAL) {
			set_response_normal = new Set_Response_Normal();
			codeLength += set_response_normal.decode(iStream);
			return codeLength;
		}

		if (choice == Choices.SET_RESPONSE_DATABLOCK) {
			set_response_datablock = new Set_Response_Datablock();
			codeLength += set_response_datablock.decode(iStream);
			return codeLength;
		}

		if (choice == Choices.SET_RESPONSE_LAST_DATABLOCK) {
			set_response_last_datablock = new Set_Response_Last_Datablock();
			codeLength += set_response_last_datablock.decode(iStream);
			return codeLength;
		}

		if (choice == Choices.SET_RESPONSE_LAST_DATABLOCK_WITH_LIST) {
			set_response_last_datablock_with_list = new Set_Response_Last_Datablock_With_List();
			codeLength += set_response_last_datablock_with_list.decode(iStream);
			return codeLength;
		}

		if (choice == Choices.SET_RESPONSE_WITH_LIST) {
			set_response_with_list = new Set_Response_With_List();
			codeLength += set_response_with_list.decode(iStream);
			return codeLength;
		}

		throw new IOException("Error decoding AxdrChoice: Identifier matched to no item.");
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		AxdrByteArrayOutputStream axdrOStream = new AxdrByteArrayOutputStream(encodingSizeGuess);
		encode(axdrOStream);
		code = axdrOStream.getArray();
	}

	public Choices getChoiceIndex() {
		return choice;
	}

	public void setset_response_normal(Set_Response_Normal newVal) {
		resetChoices();
		choice = Choices.SET_RESPONSE_NORMAL;
		set_response_normal = newVal;
	}

	public void setset_response_datablock(Set_Response_Datablock newVal) {
		resetChoices();
		choice = Choices.SET_RESPONSE_DATABLOCK;
		set_response_datablock = newVal;
	}

	public void setset_response_last_datablock(Set_Response_Last_Datablock newVal) {
		resetChoices();
		choice = Choices.SET_RESPONSE_LAST_DATABLOCK;
		set_response_last_datablock = newVal;
	}

	public void setset_response_last_datablock_with_list(Set_Response_Last_Datablock_With_List newVal) {
		resetChoices();
		choice = Choices.SET_RESPONSE_LAST_DATABLOCK_WITH_LIST;
		set_response_last_datablock_with_list = newVal;
	}

	public void setset_response_with_list(Set_Response_With_List newVal) {
		resetChoices();
		choice = Choices.SET_RESPONSE_WITH_LIST;
		set_response_with_list = newVal;
	}

	private void resetChoices() {
		choice = Choices._ERR_NONE_SELECTED;
		set_response_normal = null;
		set_response_datablock = null;
		set_response_last_datablock = null;
		set_response_last_datablock_with_list = null;
		set_response_with_list = null;
	}

}

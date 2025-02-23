/**
 * This class file was automatically generated by jASN1 (http://www.openmuc.org)
 */

package org.openmuc.asn1.cosem;

import java.io.IOException;
import java.io.InputStream;

import org.openmuc.jasn1.axdr.AxdrByteArrayOutputStream;
import org.openmuc.jasn1.axdr.AxdrType;
import org.openmuc.jasn1.axdr.types.AxdrEnum;

public class GET_Request implements AxdrType {

	public byte[] code = null;

	public static enum Choices {
		_ERR_NONE_SELECTED(-1), GET_REQUEST_NORMAL(1), GET_REQUEST_NEXT(2), GET_REQUEST_WITH_LIST(3), ;

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

	public Get_Request_Normal get_request_normal = null;

	public Get_Request_Next get_request_next = null;

	public Get_Request_With_List get_request_with_list = null;

	public GET_Request() {
	}

	public GET_Request(byte[] code) {
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

		if (choice == Choices.GET_REQUEST_WITH_LIST) {
			codeLength += get_request_with_list.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(3);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		if (choice == Choices.GET_REQUEST_NEXT) {
			codeLength += get_request_next.encode(axdrOStream);
			AxdrEnum c = new AxdrEnum(2);
			codeLength += c.encode(axdrOStream);
			return codeLength;
		}

		if (choice == Choices.GET_REQUEST_NORMAL) {
			codeLength += get_request_normal.encode(axdrOStream);
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

		if (choice == Choices.GET_REQUEST_NORMAL) {
			get_request_normal = new Get_Request_Normal();
			codeLength += get_request_normal.decode(iStream);
			return codeLength;
		}

		if (choice == Choices.GET_REQUEST_NEXT) {
			get_request_next = new Get_Request_Next();
			codeLength += get_request_next.decode(iStream);
			return codeLength;
		}

		if (choice == Choices.GET_REQUEST_WITH_LIST) {
			get_request_with_list = new Get_Request_With_List();
			codeLength += get_request_with_list.decode(iStream);
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

	public void setget_request_normal(Get_Request_Normal newVal) {
		resetChoices();
		choice = Choices.GET_REQUEST_NORMAL;
		get_request_normal = newVal;
	}

	public void setget_request_next(Get_Request_Next newVal) {
		resetChoices();
		choice = Choices.GET_REQUEST_NEXT;
		get_request_next = newVal;
	}

	public void setget_request_with_list(Get_Request_With_List newVal) {
		resetChoices();
		choice = Choices.GET_REQUEST_WITH_LIST;
		get_request_with_list = newVal;
	}

	private void resetChoices() {
		choice = Choices._ERR_NONE_SELECTED;
		get_request_normal = null;
		get_request_next = null;
		get_request_with_list = null;
	}

}

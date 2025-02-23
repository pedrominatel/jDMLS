/**
 * This class file was automatically generated by jASN1 (http://www.openmuc.org)
 */

package org.openmuc.asn1.iso.acse;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.openmuc.jasn1.ber.BerByteArrayOutputStream;
import org.openmuc.jasn1.ber.BerIdentifier;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.types.BerObjectIdentifier;

public class Application_context_name_list {

	public final static BerIdentifier identifier = new BerIdentifier(BerIdentifier.UNIVERSAL_CLASS,
			BerIdentifier.CONSTRUCTED, 16);
	protected BerIdentifier id;

	public byte[] code = null;
	public List<BerObjectIdentifier> seqOf = null;

	public Application_context_name_list() {
		id = identifier;
	}

	public Application_context_name_list(byte[] code) {
		id = identifier;
		this.code = code;
	}

	public Application_context_name_list(List<BerObjectIdentifier> seqOf) {
		id = identifier;
		this.seqOf = seqOf;
	}

	public int encode(BerByteArrayOutputStream berOStream, boolean explicit) throws IOException {
		int codeLength;

		if (code != null) {
			codeLength = code.length;
			for (int i = code.length - 1; i >= 0; i--) {
				berOStream.write(code[i]);
			}
		}
		else {
			codeLength = 0;
			for (int i = (seqOf.size() - 1); i >= 0; i--) {
				codeLength += seqOf.get(i).encode(berOStream, true);
			}

			codeLength += BerLength.encodeLength(berOStream, codeLength);

		}

		if (explicit) {
			codeLength += id.encode(berOStream);
		}

		return codeLength;
	}

	public int decode(InputStream iStream, boolean explicit) throws IOException {
		int codeLength = 0;
		int subCodeLength = 0;
		seqOf = new LinkedList<BerObjectIdentifier>();

		if (explicit) {
			codeLength += id.decodeAndCheck(iStream);
		}

		BerLength length = new BerLength();
		codeLength += length.decode(iStream);

		while (subCodeLength < length.val) {
			BerObjectIdentifier element = new BerObjectIdentifier();
			subCodeLength += element.decode(iStream, true);
			seqOf.add(element);
		}
		if (subCodeLength != length.val) {
			throw new IOException("Decoded SequenceOf or SetOf has wrong length tag");

		}
		codeLength += subCodeLength;

		return codeLength;
	}

	public void encodeAndSave(int encodingSizeGuess) throws IOException {
		BerByteArrayOutputStream berOStream = new BerByteArrayOutputStream(encodingSizeGuess);
		encode(berOStream, false);
		code = berOStream.getArray();
	}
}

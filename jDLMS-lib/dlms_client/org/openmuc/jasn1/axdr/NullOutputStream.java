package org.openmuc.jasn1.axdr;

import java.io.IOException;
import java.nio.ByteBuffer;

public class NullOutputStream extends AxdrByteArrayOutputStream {

	public NullOutputStream() {
		super(null, 0);
	}

	@Override
	public void write(int arg0) throws IOException {
		return;
	}

	@Override
	public void write(byte arg0) throws IOException {
		return;
	}

	@Override
	public void write(byte[] byteArray) throws IOException {
		return;
	}

	@Override
	public byte[] getArray() {
		return null;
	}

	@Override
	public ByteBuffer getByteBuffer() {
		return null;
	}
}

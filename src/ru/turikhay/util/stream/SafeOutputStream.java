package ru.turikhay.util.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An <code>OutputStream</code> without <code>IOException</code>s.
 * 
 * @author Artur Khusainov
 * @see OutputStream
 * @see IOException
 * 
 */

public abstract class SafeOutputStream extends OutputStream {
	@Override
	public void write(byte b[]) {
		try {
			super.write(b);
		} catch (IOException ignored) {
		}
	}

	@Override
	public void write(byte b[], int off, int len) {
		try {
			super.write(b, off, len);
		} catch (IOException ignored) {
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() {
	}

	@Override
	public abstract void write(int b);
}

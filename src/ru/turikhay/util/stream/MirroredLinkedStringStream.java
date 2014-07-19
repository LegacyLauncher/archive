package ru.turikhay.util.stream;

import java.io.IOException;
import java.io.OutputStream;

/**
 * <code>MirroredLinkedStringStream</code> allows you to mirror all data into
 * the one extra <code>OutputStream</code> <br/>
 * Methods in <code>OutputStream</code> are not exception-safe, so this
 * implementation throws <code>RuntimeException</code> if the
 * <code>IOException</code> occurs. <br/>
 * Also, mirror may be <code>null</code>.
 * 
 * @author Artur Khusainov
 * 
 */
public class MirroredLinkedStringStream extends LinkedStringStream {
	private OutputStream mirror;

	public MirroredLinkedStringStream() {
	}

	private MirroredLinkedStringStream(Logger logger, OutputStream mirror) {
		super(logger);

		this.mirror = mirror;
	}

	public MirroredLinkedStringStream(Logger logger) {
		this(logger, null);
	}

	public MirroredLinkedStringStream(OutputStream mirror) {
		this(null, mirror);
	}

	public OutputStream getMirror() {
		return mirror;
	}

	public void setMirror(OutputStream stream) {
		this.mirror = stream;
	}

	@Override
	public void write(char b) {
		super.write(b);

		if (mirror != null)
			try {
				mirror.write(b);
			} catch (IOException e) {
				throw new RuntimeException("Cannot log into the mirror!", e);
			}
	}

	@Override
	public void flush() {
		super.flush();

		if (mirror != null)
			try {
				mirror.flush();
			} catch (IOException e) {
				throw new RuntimeException("Cannot flush the mirror!", e);
			}
	}

}

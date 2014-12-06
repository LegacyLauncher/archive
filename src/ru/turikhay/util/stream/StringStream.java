package ru.turikhay.util.stream;


/**
 * <code>StringStream</code> is the <code>{@link SafeOutputStream}</code> that
 * allows you to get output data as <code>String</code> <br/>
 * This class uses <code>{@link StringBuffer}</code> as data storage, so use of
 * <code>write()</code> is thread-safe.
 *
 * @author Artur Khusainov
 *
 */
public class StringStream extends SafeOutputStream {
	protected final StringBuffer buffer;
	protected int caret;

	public StringStream() {
		this.buffer = new StringBuffer();
	}

	/**
	 * Writes specified int as char.
	 */
	@Override
	public void write(int b) {
		this.write((char) b);
	}

	protected void write(char c) {
		this.buffer.append(c);

		this.caret++;
	}

	public void write(char[] c) {
		if (c == null)
			throw new NullPointerException();

		if (c.length == 0)
			return;

		for (int i = 0; i < c.length; i++)
			write(c[i]);
	}

	public char getCharAt(int i) {
		return buffer.charAt(i);
	}

	public String getOutput() {
		return buffer.toString();
	}

	public int getLength() {
		return buffer.length();
	}

}

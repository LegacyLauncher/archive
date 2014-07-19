package com.turikhay.util.logger;

/**
 * <code>BufferedStringStream</code> flushes itself if it detects the new line
 * character (<code>\n</code>)
 * 
 * @author Artur Khusainov
 * 
 */
public class BufferedStringStream extends StringStream {
	int caretFlush;

	@Override
	public void write(char b) {
		super.write(b);

		if (b != '\n')
			return;

		flush();

		this.caretFlush = caret;
	}
}
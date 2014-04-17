package com.turikhay.util.logger;

/**
 * <code>LinkedStringStream</code> is the
 * <code>{@link BufferedStringStream}</code> that is capable with
 * <code>{@link Logger}</code> <br/>
 * <code>LinkedStringStream</code> applies changes into <code>Logger</code>
 * using <code>Logger.rawlog()</code> method
 * 
 * @author Artur Khusainov
 * 
 */
public class LinkedStringStream extends BufferedStringStream {
	private Logger logger;

	public LinkedStringStream() {
	}

	LinkedStringStream(Logger logger) {
		this.logger = logger;
	}

	public Logger getLogger() {
		return logger;
	}

	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	/**
	 * This method applies changes into <code>{@link Logger}</code> using
	 * <code>Logger.rawlog()</code> method
	 */
	@Override
	public void flush() {
		if (logger == null)
			return;

		char[] chars = new char[caret - caretFlush];
		buffer.getChars(caretFlush, caret, chars, 0);

		logger.rawlog(chars);
	}

}

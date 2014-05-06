package ru.turikhay.util.logger;

/**
 * <code>Logger</code> interface is used to create simple logger classes.
 * 
 * @author Artur Khusainov
 */

public interface Logger {
	/**
	 * Logs <code>String</code> into Logger. <br/>
	 * In most implementations, this method shouldn't apply changes into GUI.
	 * 
	 * @param s
	 *            String to be logged.
	 * 
	 */
	void log(String s);

	/**
	 * Logs into Logger. <br/>
	 * In most implementations, this method shouldn't apply changes into GUI.
	 * 
	 * @param o
	 *            Objects to be logged.
	 * 
	 */
	void log(Object... o);

	/**
	 * Applies changes into the Logger (in most implementations called by
	 * flush() method). <br/>
	 * This method shouldn't be called to log values.
	 * 
	 * 
	 * @param s
	 *            String to apply.
	 * 
	 */
	void rawlog(String s);

	/**
	 * Applies changes into the Logger (in most implementations called by
	 * flush() method). <br/>
	 * This method shouldn't be called to log values.
	 * 
	 * 
	 * @param c
	 *            Characters to apply.
	 * 
	 */
	void rawlog(char[] c);
}

package ru.turikhay.util.stream;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.Locale;

import ru.turikhay.util.U;

/**
 * <code>PrintLogger</code> is used as simple <code>Logger</code> of
 * system-<b>in</b>dependent <code>PrintStream</code>. <br/>
 * New line character (<code>\n</code>) and encoding (UTF-16) are constant
 * values and don't depend on environment. All methonds are synchronized.
 * 
 * @author Artur Khusainov
 * 
 */

public class PrintLogger extends PrintStream implements Logger {
	private LinkedStringStream stream;
	private Logger mirror;

	private Formatter formatter;

	public PrintLogger(LinkedStringStream stream, PrintStream printMirror,
			Logger mirror) {
		super(stream);

		this.stream = stream;
		this.mirror = mirror;
	}

	private PrintLogger(LinkedStringStream stream, Logger mirror) {
		super(stream);

		this.stream = stream;
		this.mirror = mirror;
	}

	public PrintLogger(LinkedStringStream stream) {
		this(stream, null);
	}

	public Logger getMirror() {
		return mirror;
	}

	public void setMirror(Logger logger) {
		if (this == logger)
			throw new IllegalArgumentException();

		this.mirror = logger;
	}

	@Override
	public void log(Object... o) {
		log(U.toLog(o));
	}

	@Override
	public void log(String s) {
		println(s);

		if (mirror != null)
			mirror.log(s);
	}

	@Override
	public void rawlog(String s) {
		if (mirror != null)
			mirror.rawlog(s);
	}

	@Override
	public void rawlog(char[] c) {
		if (mirror != null)
			mirror.rawlog(c);
	}

	public LinkedStringStream getStream() {
		return stream;
	}

	// PrintStream methods overrides

	@Override
	public synchronized void write(int b) {
		stream.write(b);
	}

	@Override
	public synchronized void write(byte buf[], int off, int len) {
		stream.write(buf, off, len);
	}

	private synchronized void write(String s) {
		if (s == null)
			s = "null";
		stream.write(s.toCharArray());
	}

	@Override
	public synchronized void print(char c) {
		stream.write(c);
	}

	@Override
	public synchronized void print(char[] s) {
		stream.write(s);
	}

	@Override
	public synchronized void print(boolean b) {
		write(b ? "true" : "false");
	}

	@Override
	public synchronized void print(int i) {
		write(String.valueOf(i));
	}

	@Override
	public synchronized void print(long l) {
		write(String.valueOf(l));
	}

	@Override
	public synchronized void print(float f) {
		write(String.valueOf(f));
	}

	@Override
	public synchronized void print(double d) {
		write(String.valueOf(d));
	}

	@Override
	public synchronized void print(String s) {
		if (s == null)
			s = "null";
		write(s);
	}

	@Override
	public synchronized void print(Object obj) {
		write(String.valueOf(obj));
	}

	private void newLine() {
		write('\n');
	}

	@Override
	public synchronized void println() {
		newLine();
	}

	@Override
	public synchronized void println(boolean x) {
		print(x);
		newLine();
	}

	@Override
	public synchronized void println(char x) {
		print(x);
		newLine();
	}

	@Override
	public synchronized void println(int x) {
		print(x);
		newLine();
	}

	@Override
	public synchronized void println(long x) {
		print(x);
		newLine();
	}

	@Override
	public synchronized void println(float x) {
		print(x);
		newLine();
	}

	@Override
	public synchronized void println(double x) {
		print(x);
		newLine();
	}

	@Override
	public synchronized void println(char[] x) {
		print(x);
		newLine();
	}

	@Override
	public synchronized void println(String x) {
		print(x);
		newLine();
	}

	@Override
	public synchronized void println(Object x) {
		print(String.valueOf(x));
		newLine();
	}

	@Override
	public synchronized PrintStream printf(String format, Object... args) {
		return format(format, args);
	}

	@Override
	public synchronized PrintStream printf(Locale l, String format,
			Object... args) {
		return format(l, format, args);
	}

	@Override
	public synchronized PrintStream format(String format, Object... args) {

		if ((formatter == null) || (formatter.locale() != Locale.getDefault()))
			formatter = new Formatter((Appendable) this);

		formatter.format(Locale.getDefault(), format, args);
		return this;
	}

	@Override
	public synchronized PrintStream format(Locale l, String format,
			Object... args) {
		if ((formatter == null) || (formatter.locale() != l))
			formatter = new Formatter(this, l);

		formatter.format(l, format, args);
		return this;
	}

	@Override
	public synchronized PrintStream append(CharSequence csq) {
		if (csq == null)
			print("null");
		else
			print(csq.toString());

		return this;
	}

	@Override
	public synchronized PrintStream append(CharSequence csq, int start, int end) {
		CharSequence cs = (csq == null ? "null" : csq);
		write(cs.subSequence(start, end).toString());

		return this;
	}

	@Override
	public synchronized PrintStream append(char c) {
		print(c);
		return this;
	}
}

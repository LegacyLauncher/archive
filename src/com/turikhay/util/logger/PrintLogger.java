package com.turikhay.util.logger;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.Locale;

import com.turikhay.util.U;

/**
 * <code>PrintLogger</code> is used as simple <code>Logger</code> of system-<b>in</b>dependent <code>PrintStream</code>.
 * <br/>
 * New line character (<code>\n</code>) and encoding (UTF-16) are constant values and don't depend on environment.
 * All methonds are synchronized.
 * 
 * @author Artur Khusainov
 *
 */

public class PrintLogger extends PrintStream implements Logger {
	private LinkedStringStream stream;
	private Logger mirror;
	
    private Formatter formatter;
    
	public PrintLogger(LinkedStringStream stream, PrintStream printMirror, Logger mirror) {
		super(stream);
		
		this.stream = stream;
		this.mirror = mirror;
	}
	
	public PrintLogger(LinkedStringStream stream, Logger mirror) {
		super(stream);
		
		this.stream = stream;
		this.mirror = mirror;
	}
	
	public PrintLogger(LinkedStringStream stream) {
		this(stream, null);
	}
	
	public Logger getMirror(){
		return mirror;
	}
	
	public void setMirror(Logger logger){
		if(this == logger)
			throw new IllegalArgumentException();
		
		this.mirror = logger;
	}

	public void log(Object... o) {
		log(U.toLog(o));
	}
	
	public void log(String s) {		
		println(s);
		
		if(mirror != null)
			mirror.log(s);
	}
	
	public void rawlog(String s) {
		if(mirror != null)
			mirror.rawlog(s);
	}
	
	public void rawlog(char[] c) {
		if(mirror != null)
			mirror.rawlog(c);
	}
	
	public LinkedStringStream getStream(){
		return stream;
	}
	
	// PrintStream methods overrides
	
    public synchronized void write(int b) {
    	stream.write(b);
    }
    
    public synchronized void write(byte buf[], int off, int len) {
    	stream.write(buf, off, len);
    }

    private synchronized void write(String s) {
    	if(s == null) s = "null";
    	stream.write(s.toCharArray());
    }
    
    public synchronized void print(char c) {
    	stream.write(c);
    }
    
    public synchronized void print(char[] s) {
    	stream.write(s);
    }
    
    public synchronized void print(boolean b) {
    	write(b ? "true" : "false");
    }
    
    public synchronized void print(int i) {
    	write(String.valueOf(i));
    }
    
    public synchronized void print(long l) {
    	write(String.valueOf(l));
    }

    public synchronized void print(float f) {
    	write(String.valueOf(f));
    }
    
    public synchronized void print(double d) {
    	write(String.valueOf(d));
    }
    
    public synchronized void print(String s) {
    	if(s == null) s = "null";
    	write(s);
    }
    
    public synchronized void print(Object obj) {
    	write(String.valueOf(obj));
    }
    
    private void newLine(){
    	write('\n');
    }
    
    public synchronized void println() {
    	newLine();
    }
    
    public synchronized void println(boolean x) {
	    print(x);
	    newLine();
    }
    
    public synchronized void println(char x) {
	    print(x);
	    newLine();
    }
    
    public synchronized void println(int x) {
	    print(x);
	    newLine();
    }
    
    public synchronized void println(long x) {
	    print(x);
	    newLine();
    }
    
    public synchronized void println(float x) {
	    print(x);
	    newLine();
    }
    
    public synchronized void println(double x) {
	    print(x);
	    newLine();
    }

    public synchronized void println(char[] x) {
	    print(x);
	    newLine();
    }
    
    public synchronized void println(String x) {
	    print(x);
	    newLine();
    }
    
    public synchronized void println(Object x) {
        print(String.valueOf(x));
        newLine();
    }
    
    public synchronized PrintStream printf(String format, Object... args) {
    	return format(format, args);
    }
    
    public synchronized PrintStream printf(Locale l, String format, Object ... args) {
    	return format(l, format, args);
    }
    
    public synchronized PrintStream format(String format, Object ... args) {
    	
		if ((formatter == null) || (formatter.locale() != Locale.getDefault()))
		    formatter = new Formatter((Appendable) this);
		
		formatter.format(Locale.getDefault(), format, args);		
		return this;
    }
    
    public synchronized PrintStream format(Locale l, String format, Object ... args) {
		if ((formatter == null) || (formatter.locale() != l))
		    formatter = new Formatter(this, l);
		
		formatter.format(l, format, args);
		return this;
    }
    
    public synchronized PrintStream append(CharSequence csq) {
    	if (csq == null) print("null");
    	else print(csq.toString());
    	
    	return this;
    }
    
    public synchronized PrintStream append(CharSequence csq, int start, int end) {
    	CharSequence cs = (csq == null ? "null" : csq);
    	write(cs.subSequence(start, end).toString());
    	
    	return this;
    }
    
    public synchronized PrintStream append(char c) {
    	print(c);
    	return this;
    }
}

package com.turikhay.util.streams;

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
    public void write(byte b[]){
    	try { super.write(b); } catch (IOException ignored) {}
    }
    public void write(byte b[], int off, int len){
    	try { super.write(b, off, len); } catch (IOException ignored) {}
    }
    public void flush() {}    
    public void close() {}
    
    public abstract void write(int b);
}

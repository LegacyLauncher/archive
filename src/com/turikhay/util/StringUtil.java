package com.turikhay.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import com.turikhay.tlauncher.exceptions.ParseException;

public class StringUtil {
	public static String addQuotes(String a, char quote){
		if(a == null) return null;
		if(a.length() == 0) return "";
		
		return quote + a.replaceAll("\\" + quote, "\\\\" + quote) + quote;
	}
	
	public static String addQuotes(String a){ return addQuotes(a, '"'); }
	
    public static String addSlashes(String str, EscapeGroup group){
        if(str==null) return "";
        StringBuffer s = new StringBuffer ((String) str);
        for (int i=0;i<s.length();i++)
        	for(char c : group.getChars())
        		if(s.charAt(i) == c) s.insert(i++, '\\');
            return s.toString();
    }
    
    public static String[] addSlashes(String[] str, EscapeGroup group){
    	if(str == null) return null;
    	
    	int len = str.length;
    	String[] ret = new String[len];
    	
    	for(int i=0;i<len;i++)
    		ret[i] = addSlashes(str[i], group);
    	
    	return ret;
    }
	
	public static String iconv(String inChar, String outChar, String str){
		Charset in = Charset.forName(inChar), out = Charset.forName(outChar);
		CharsetDecoder decoder = in.newDecoder();
		CharsetEncoder encoder = out.newEncoder();

		try {
		    // Convert a string to ISO-LATIN-1 bytes in a ByteBuffer
		    // The new ByteBuffer is ready to be read.
		    ByteBuffer bbuf = encoder.encode(CharBuffer.wrap(str));

		    // Convert ISO-LATIN-1 bytes in a ByteBuffer to a character ByteBuffer and then to a string.
		    // The new ByteBuffer is ready to be read.
		    CharBuffer cbuf = decoder.decode(bbuf);
		    
		    return cbuf.toString();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static boolean parseBoolean(String b, boolean ignoreCase) throws ParseException {
		if(b == null)
			throw new ParseException("String cannot be NULL!");
		
		if(b.equalsIgnoreCase("true")) return true;
		if(b.equalsIgnoreCase("false")) return false;
		
		throw new ParseException("Cannot parse value (" + b + ")!");
	}
	
	public static boolean parseBoolean(String b) throws ParseException { return parseBoolean(b, true); }
	
	public enum EscapeGroup {
		COMMAND('\'', '"', ' '),
		REGEXP(COMMAND, '/', '\\', '?', '*', '+', '[', ']', ':', '{', '}', '(', ')');
		
		private final char[] chars;
		
		private EscapeGroup(char... symbols){
			this.chars = symbols;
		}
		
		private EscapeGroup(EscapeGroup extend, char... symbols){
			int len = extend.chars.length + symbols.length;
			this.chars = new char[len]; int x=0;
			
			for(;x<extend.chars.length;x++) this.chars[x] = extend.chars[x];
			for(int i=0;i<symbols.length;i++) this.chars[i+x] = symbols[i];
		}
		
		public char[] getChars(){
			return chars;
		}
	}
}

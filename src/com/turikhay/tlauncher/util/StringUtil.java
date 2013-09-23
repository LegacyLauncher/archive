package com.turikhay.tlauncher.util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.List;

public class StringUtil {
	public static String addQuotes(List<String> a, String quotes, String del){
		if(a == null) return null;
		if(a.size() == 0) return "";
		
		String t = "";
		for(String cs : a)
			t += del + quotes + cs + quotes;
		return t.substring(del.length());
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
}

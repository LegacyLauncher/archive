package com.turikhay.tlauncher.util;

import java.io.*;
import java.net.URL;
import java.security.MessageDigest;

public class FileUtil {	
	public static void saveFile(File file, String text) throws IOException {
		FileWriter fstream = new FileWriter(file);
		BufferedWriter out = new BufferedWriter(fstream);
		//		
		out.write(text);
		out.close();
	}
	
	public static String readFile(File file) throws IOException {
		String toret = "";
		FileReader file_r = new FileReader(file);
		BufferedReader buff = new BufferedReader(file_r);
		boolean eof = false;
		
		while (!eof) {
			String line = buff.readLine();
			if(line == null) eof = true;
			else
				toret += "\n" + line;
		}
		buff.close();
		
		if(toret.length() > 0) toret = toret.substring(1);
		return toret;
	}
	
	public static String getFilename(URL url){
		String inServer = url.getPath();
		String[] folders = inServer.split("/");
		int size = folders.length;
		if(size == 0) return "";
		return folders[size - 1];
	}
	
	public static byte[] createChecksum(File file){
		InputStream fis = null; try{
		fis = new FileInputStream(file);

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");
	    int numRead;

	    do {
	    	numRead = fis.read(buffer);
	    	if (numRead > 0) {
	    		complete.update(buffer, 0, numRead);
	    	}
	    } while (numRead != -1);
	    return complete.digest();
	}catch(Exception e){ return null; }finally{ close(fis); }}
	
	public static String getMD5Checksum(File file) {
		if(!file.exists()) return null;
		
		byte[] b = createChecksum(file);
		if(b == null) return null;
		
		String result = "";
		for (int i=0; i < b.length; i++) {
			result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
		}
		return result;
	}
	
	public static void close(Closeable a){
		try{ a.close(); }catch(Exception e){ e.printStackTrace(); }
	}
}

package com.turikhay.tlauncher.settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.turikhay.tlauncher.util.FileUtil;

public class Settings {
	final InputType inputType;
	final String filename;
	
	public final static char DEFAULT_DELIMITER_CHAR = '=', DEFAULT_NEWLINE_CHAR = '\n', DEFAULT_COMMENT_CHAR = '#';
	public final static String DEFAULT_CHARSET = "UTF-8";
	
	private final String CHARSET, DELIMITER_CHAR, NEWLINE_CHAR, COMMENT_CHAR;
	
	final Object input;
	
	protected Map<String, String> s = new HashMap<String, String>();
	
	public Settings(){
		throw new SettingsException("This constructor mustn't be called.");
	}
	
	public Settings(URL resource, String charset, char delimiter, char newline, char comment_char) throws IOException {		
		if(resource == null)
			throw new SettingsException("Given resourse is NULL!");
		
		input = resource;
		filename = resource.getFile();
		inputType = InputType.STREAM;
		
		CHARSET = charset;
		DELIMITER_CHAR = delimiter + "";
		NEWLINE_CHAR = newline + "";
		COMMENT_CHAR = comment_char + "";
		
		InputStream stream = resource.openStream();		
		this.readFromStream(stream);
	}
	public Settings(URL resource) throws IOException { this(resource, DEFAULT_CHARSET, DEFAULT_DELIMITER_CHAR, DEFAULT_NEWLINE_CHAR, DEFAULT_COMMENT_CHAR); }
	
	public Settings(InputStream stream, String charset, char delimiter, char newline, char comment_char) throws IOException {
		if(stream == null)
			throw new SettingsException("Given stream is NULL!");
		
		input = stream;
		filename = null;
		inputType = InputType.STREAM;
		
		CHARSET = charset;
		DELIMITER_CHAR = delimiter + "";
		NEWLINE_CHAR = newline + "";
		COMMENT_CHAR = comment_char + "";
		
		this.readFromStream(stream);
	}
	public Settings(InputStream stream) throws IOException { this(stream, DEFAULT_CHARSET, DEFAULT_DELIMITER_CHAR, DEFAULT_NEWLINE_CHAR, DEFAULT_COMMENT_CHAR); }
	
	public Settings(File file, String charset, char delimiter, char newline, char comment_char) throws IOException {
		if(file == null)
			throw new SettingsException("Given file is NULL!");
		
		input = file;
		filename = file.getName();
		inputType = InputType.FILE;
		
		CHARSET = charset;
		DELIMITER_CHAR = delimiter + "";
		NEWLINE_CHAR = newline + "";
		COMMENT_CHAR = comment_char + "";
			
		if(!file.exists()){ file.getParentFile().mkdirs(); file.createNewFile(); }
		
		InputStream is = new FileInputStream(file);
		this.readFromStream(is);		
	}
	public Settings(File file) throws IOException { this(file, DEFAULT_CHARSET, DEFAULT_DELIMITER_CHAR, DEFAULT_NEWLINE_CHAR, DEFAULT_COMMENT_CHAR); }
	
	private void readFromStream(InputStream stream) throws IOException {
		if(stream == null)
			throw new SettingsException(this, "Given stream is NULL!");
		if(stream.available() == 0)
			return;
		
		InputStreamReader reader = new InputStreamReader(stream, CHARSET);
		
		String b = "";
		while(reader.ready())
			b += (char) reader.read();
		
		reader.close();
		
		this.readFromString(b);
	}
	
	private void readFromString(String string){
		if(string == null)
			throw new SettingsException(this, "Given string is NULL!");
		if(string.length() == 0)
			return;
		
		String[] lines = string.split(NEWLINE_CHAR), sline;
		String line, curkey, curvalue;
		
		//int lines_r = 0;
		
		for(int x=0;x<lines.length;x++){// ++lines_r;
			line = lines[x];
			if(line.startsWith(COMMENT_CHAR)) continue;
			
			sline = line.split(DELIMITER_CHAR);
			
			curkey = sline[0];
			if(curkey == "") continue;
			
			curvalue = "";
			for(int y=1;y<sline.length;y++) curvalue += DELIMITER_CHAR + sline[y];
			if(curvalue != "") curvalue = curvalue.substring(1).replace("\\n", NEWLINE_CHAR);
			
			s.put(curkey, curvalue);
		}
	}
	
	public String get(String key){
		String r = s.get(key);
		if(r == "") return key;
		return r;
	}
	
	public String get(String key, String replace, Object with){
		String r = s.get(key);
		if(r == null) return key;
		return r.replace("%"+replace, with+"");
	}
	
	public String get(String key, String replace0, Object with0, String replace1, Object with1){
		String r = s.get(key);
		if(r == null) return key;
		return r.replace("%"+replace0, with0+"").replace("%"+replace1, with1+"");
	}
	
	public int getInteger(String key){
		try{ return Integer.parseInt(s.get(key)); }catch(Exception e){ return 0; }
	}
	
	public long getLong(String key){
		try{ return Long.parseLong(s.get(key)); }catch(Exception e){ return 0; }
	}
	
	public double getDouble(String key){
		try{ return Double.parseDouble(s.get(key)); }catch(Exception e){ return 0; }
	}
	
	public float getFloat(String key){
		try{ return Float.parseFloat(s.get(key)); }catch(Exception e){ return 0; }
	}
	
	public boolean getBoolean(String key){
		try{ return Boolean.parseBoolean(s.get(key)); }catch(Exception e){ return false; }
	}
	
	public String[] getAll(){
		int size = s.size(), x = -1;
		String[] r = new String[size];
		
		for(Entry<String, String> curen : s.entrySet()){ ++x;
			r[x] = curen.getKey() + " "+ DELIMITER_CHAR +" \"" + curen.getValue() + "\"";
		}
		
		return r;
	}
	
	public void set(String key, Object value, boolean save){
		if(s.containsKey(key)) s.remove(key);
		if(value == null) value = "";
		s.put(key, value.toString());
		
		if(save)
			try{ save(); }catch(IOException e){ throw new SettingsException(this, "Cannot save set value!", e); }
	}
	
	public void set(Map<String, Object> map, boolean save){
		for(Entry<String, Object> curen : map.entrySet())
			this.set(curen.getKey(), curen.getValue(), false);

		if(save)
			try{ save(); }catch(IOException e){ throw new SettingsException(this, "Cannot save map!", e); }
	}
	
	public void set(String key, Object value){
		this.set(key, value, true);
	}
	
	public String createString(){
		String r = "";
		for(Entry<String, String> curen : s.entrySet())
			r += NEWLINE_CHAR + curen.getKey() + DELIMITER_CHAR + curen.getValue().replace(NEWLINE_CHAR, "\\" + NEWLINE_CHAR);
		
		if(r.length() > 0) return r.substring(1);
		return "";
	}
	
	public void save() throws IOException {
		switch(inputType){
		case FILE:
			File file = (File) input;
			String towrite = this.createString();
			
			FileUtil.saveFile(file, towrite);
			break;
		case STREAM:
			throw new SettingsException("Cannot write in input stream!");
		default:
			break;
		}
	}
	
	public boolean canBeSaved(){
		switch(inputType){
		case FILE: return true;
		
		case STREAM:
		default: return false;
		}
	}
	
	private enum InputType {
		FILE, STREAM
	}
}

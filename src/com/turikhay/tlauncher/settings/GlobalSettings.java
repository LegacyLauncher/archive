package com.turikhay.tlauncher.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import joptsimple.OptionSet;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.util.FileUtil;
import com.turikhay.util.IntegerArray;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;

public class GlobalSettings extends Settings {	
	private final static Pattern lang_pattern = Pattern.compile("lang/([\\w]+)\\.ini");
	
	public final static Locale DEFAULT_LOCALE = Locale.US;
	public final static List<String> DEFAULT_LOCALES = getDefaultLocales();
	public final static List<String> SUPPORTED_LOCALE = getSupportedLocales();
	
	private File file;
	private boolean saveable;
	private static boolean firstRun;
	
	private GlobalDefaults d;
	private Map<String, Object> cs = new HashMap<String, Object>(); // unsaveable keys
	
	double version = 0.14;
	
	public static GlobalSettings createInstance(OptionSet set) throws IOException {		
		Object path = (set != null)? set.valueOf("settings") : null;
		
		if(path == null){
			URL resource = GlobalSettings.class.getResource("/settings.ini");
			if(resource != null)
				return new GlobalSettings(resource, set);
		}
		
		File file = (path == null)? getDefaultFile() : new File(path.toString());
		if(!file.exists()) firstRun = true;
		
		return new GlobalSettings(file, set);
	}
	private GlobalSettings(URL url, OptionSet set) throws IOException {
		super(url);
		U.log("Settings URL:", url);
		init(set);
	}
	private GlobalSettings(File file, OptionSet set) throws IOException {
		super(file);
		U.log("Settings file:", file);
		init(set);
	}
	
	private void init(OptionSet set) throws IOException {
		this.d = new GlobalDefaults(this);
		this.cs = ArgumentParser.parse(set);
		
		boolean forcedrepair = this.getDouble("settings.version") != version;
		saveable = (input instanceof File);
		
		for(Entry<String, Object> curen : d.getMap().entrySet()){
			String key = curen.getKey(), value = s.get(key); Object defvalue = d.get(key);
			
			if(defvalue == null) continue;
			
			try {
				if(forcedrepair) throw new Exception();
				if(defvalue instanceof Integer) Integer.parseInt(value);
				else if(defvalue instanceof Boolean) this.parseBoolean(value);
				else if(defvalue instanceof Double) Double.parseDouble(value);
				else if(defvalue instanceof Long) Long.parseLong(value);
				else if(defvalue instanceof ActionOnLaunch) this.parseLaunchAction(value);
				else if(defvalue instanceof IntegerArray) IntegerArray.parseIntegerArray(value);
			}catch(Exception e){
				repair(key, defvalue, !saveable);
				value = defvalue.toString();
			}
			
			if(!saveable)
				cs.put(key, value);
		}
		
		if(saveable)
			this.save();
	}
	
	public boolean isFirstRun(){
		return firstRun;
	}
	
	public String get(String key){
		Object r = (cs.containsKey(key))? cs.get(key) : s.get(key);
		if(r == null || r.equals("")) return null;
		return r.toString();
	}
	
	public void set(String key, Object value, boolean save){
		if(!cs.containsKey(key)){ super.set(key, value, save); return; }
		
		if(value == null) value = "";
		cs.remove(key); cs.put(key, value.toString());
		
		if(save)
			try{ save(); }catch(IOException e){ throw new SettingsException(this, "Cannot save set value!", e); }
	}
	
	public boolean isSaveable(String key){
		return !cs.containsKey(key);
	}
	
	public boolean isSaveable(){
		return saveable;
	}
	
	public String getDefault(String key){
		String r = d.get(key) + "";
		if(r == "") return null;
		return r;
	}
	
	public int getDefaultInteger(String key){
		try{ return Integer.parseInt(d.get(key)+""); }catch(Exception e){ return 0; }
	}
	
	public int getInteger(String key, int min, int max){
		int i = this.getInteger(key);
		if(i < min || i > max) return this.getDefaultInteger(key);
		return i;
	}
	
	public int getInteger(String key, int min){
		return getInteger(key, min, Integer.MAX_VALUE);
	}
	
	public long getDefaultLong(String key){
		try{ return Long.parseLong(d.get(key)+""); }catch(Exception e){ return 0; }
	}
	
	public double getDefaultDouble(String key){
		try{ return Double.parseDouble(d.get(key)+""); }catch(Exception e){ return 0; }
	}
	
	public float getDefaultFloat(String key){
		try{ return Float.parseFloat(d.get(key)+""); }catch(Exception e){ return 0; }
	}
	
	public boolean getDefaultBoolean(String key){
		try{ return Boolean.parseBoolean(d.get(key)+""); }catch(Exception e){ return false; }
	}
	
	public Locale getLocale(){
		String locale = get("locale");
		
		for(Locale lookup : Locale.getAvailableLocales()){
			String lookup_name = lookup.toString();
			for(String curloc : SUPPORTED_LOCALE){
				if(!lookup_name.equals(curloc)) continue;
				if(!curloc.equals(locale)) continue;
				// Selected locale is supported
				return lookup;
			}
		}
		
		return DEFAULT_LOCALE;
	}
	
	public static Locale getSupported(){
		Locale using = Locale.getDefault();
		String using_name = using.toString();
		
		for(String supported : SUPPORTED_LOCALE)
			if(supported.equals(using_name))
				return using;
		
		return Locale.US;
	}
	
	public ActionOnLaunch getActionOnLaunch(){
		String action = get("minecraft.onlaunch");
		ActionOnLaunch get = ActionOnLaunch.get(action);
		
		return (get != null)? get : ActionOnLaunch.getDefault();
	}
	
	private boolean parseBoolean(String b) throws Exception {
		if(b.equalsIgnoreCase("true")) return true;
		if(b.equalsIgnoreCase("false")) return false;
		
		throw new Exception();
	}
	
	private void parseLaunchAction(String b) throws Exception {
		if(!ActionOnLaunch.parse(b))
			throw new Exception();
	}
	
	private void repair(String key, Object value, boolean unsaveable) throws IOException {
		U.log("Field \""+key+"\" in GlobalSettings is invalid.");		
		
		set(key, value.toString(), false);
		
		if(unsaveable)
			cs.put(key, value);
	}
	
	public static File getDefaultFile(){
		return MinecraftUtil.getSystemRelatedFile(TLauncher.getSettingsFile());
	}
	
	public File getFile(){
		if(file == null) return getDefaultFile();
		return file;
	}
	
	public void setFile(File f){
		if(f == null) throw new IllegalArgumentException("File cannot be NULL!");
		
		U.log("Set settings file: "+ f.toString());
		file = f;
	}
	
	public void save() throws IOException {
		if(!(input instanceof File))
			return;
		
		File file = (File) input;
		
		StringBuilder r = new StringBuilder(); boolean first = true;
		for(Entry<String, String> curen : s.entrySet()){
			String key = curen.getKey(); Object value = curen.getValue();
			if(value == null) value = ""; 
			
			if(!first) r.append(NEWLINE_CHAR); else first = false;
			r.append(key + DELIMITER_CHAR + value.toString().replace(NEWLINE_CHAR, "\\" + NEWLINE_CHAR));
		}
		String towrite = r.toString();
			
		FileOutputStream os = new FileOutputStream(file);
		OutputStreamWriter ow = new OutputStreamWriter(os, "UTF-8");
		ow.write(towrite);
		ow.close();
	}
	
	public int[] getWindowSize() {
		int[] d_sizes = new int[]{ 925, 530 }, w_sizes;
		try{ w_sizes = IntegerArray.parseIntegerArray(get("minecraft.size")).toArray(); }
		catch(Exception e){ w_sizes = d_sizes; }
		
		if(w_sizes[0] < d_sizes[0] || w_sizes[1] < d_sizes[1]) w_sizes = d_sizes;
		
		return w_sizes;
	}
	
	private static List<String> getSupportedLocales() {
		File file = FileUtil.getRunningJar();
		
		List<String> locales = new ArrayList<String>();
		try{			
			URL jar = file.toURI().toURL();
			
			ZipInputStream zip = new ZipInputStream(jar.openStream());
			while(true) {
				ZipEntry e = zip.getNextEntry();
				if(e == null) break;
		    
				String name = e.getName();
				if (!name.startsWith("lang/")) continue;
				Matcher mt = lang_pattern.matcher(name);
				if(!mt.matches()) continue;
				
				U.log("Found locale:", mt.group());
				locales.add(mt.group(1));
			}
		} catch(Exception e){
			U.log("Cannot get locales!", e);
			return DEFAULT_LOCALES;
		}
		
		if(locales.isEmpty())
			return DEFAULT_LOCALES;
		
		return locales;
	}
	
	private static List<String> getDefaultLocales(){
		List<String> l = new ArrayList<String>();
		
		l.add("en_US");
		l.add("ru_RU");
		l.add("uk_UA");
		
		return l;
	}
	
	public enum ActionOnLaunch {
		HIDE, EXIT;
		
		public static boolean parse(String val){
			for(ActionOnLaunch cur : values())
				if(cur.toString().toLowerCase().equals(val))
					return true;
			return false;
		}
		
		public static ActionOnLaunch get(String val){
			for(ActionOnLaunch cur : values())
				if(cur.toString().toLowerCase().equals(val))
					return cur;
			return null;
		}
		
		public String toString(){
			return super.toString().toLowerCase();
		}
		
		public static ActionOnLaunch getDefault(){
			return HIDE;
		}
	}
}

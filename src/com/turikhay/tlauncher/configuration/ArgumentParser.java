package com.turikhay.tlauncher.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import com.turikhay.tlauncher.ui.alert.Alert;

public class ArgumentParser {
	private static Map<String, String> m = createLinkMap();
	private static OptionParser parser = createParser();
	
	public static OptionParser getParser(){
		return parser;
	}
	
	public static OptionSet parseArgs(String[] args) throws IOException {
		OptionSet set = null;
		try{ set = parser.parse(args); }
		catch(OptionException e){
			e.printStackTrace();
			parser.printHelpOn(System.out);
			
			Alert.showError(e, false);
		}
		
		return set;
	}
	
	public static Map<String, Object> parse(OptionSet set){
		Map<String, Object> r = new HashMap<String, Object>();
		if(set == null) return r;
		
		for(Entry<String, String> a : m.entrySet()){
			String key = a.getKey(); Object value = null;
			if(key.startsWith("-")){ key = key.substring(1); value = true; }
			if(!set.has(key)) continue;
			
			if(value == null) value = set.valueOf(key);
			r.put(a.getValue(), value);
		}
		
		return r;
	}
	
	private static Map<String, String> createLinkMap() {
		Map<String, String> r = new HashMap<String, String>();
		
		r.put("directory", "minecraft.gamedir");
		r.put("java-directory", "minecraft.javadir");
		r.put("version", "login.version");
		r.put("username", "login.username");
		r.put("javaargs", "minecraft.javaargs");
		r.put("margs", "minecraft.args");
		r.put("width", "minecraft.size.width");
		r.put("height", "minecraft.size.height");
		
		r.put("-console", "gui.console");
		
		return r;
	}
	
	private static OptionParser createParser() {
		OptionParser parser = new OptionParser();
		
		parser.accepts("help", "Shows this help");
		parser.accepts("nogui", "Starts minimal version");
		parser.accepts("directory", "Specifies Minecraft directory").withRequiredArg();
		parser.accepts("java-directory", "Specifies Java directory").withRequiredArg();
		parser.accepts("version", "Specifies version to run").withRequiredArg();
		parser.accepts("username", "Specifies username").withRequiredArg();
		parser.accepts("javaargs", "Specifies JVM arguments").withRequiredArg();
		parser.accepts("margs", "Specifies Minecraft arguments").withRequiredArg();
		parser.accepts("width", "Specifies window width").withRequiredArg().ofType(Integer.class);
		parser.accepts("height", "Specifies window height").withRequiredArg().ofType(Integer.class);
		parser.accepts("force", "Specifies state of force updating");
		parser.accepts("nocheck", "Disables file checking");
		parser.accepts("console", "Specifies availability of console");
		parser.accepts("settings", "Specifies path to settings file").withRequiredArg();
		
		return parser;
	}
}

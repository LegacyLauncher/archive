package com.turikhay.tlauncher.minecraft;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.turikhay.util.U;

public class CrashDescriptor {
	final String prefix = "[CrashDescriptor]";
	
	final static String forge_prefix = "^(?:[0-9-]+ [0-9:]+ \\[[\\w]+\\]\\ {0,1}\\[{0,1}[\\w]*\\]{0,1}\\ {0,1}){0,1}";
	final static Pattern
		crash_pattern = Pattern.compile("^.*[\\#\\@\\!\\@\\#][ ]Game[ ]crashed!.+[\\#\\@\\!\\@\\#][ ](.+)$"),
		start_crash_report_pattern = Pattern.compile(forge_prefix + "---- Minecraft Crash Report ----");
	final static CrashSignature[] signatures = initSigns();
	
	private final MinecraftLauncher context;
	
	CrashDescriptor(MinecraftLauncher l){
		this.context = l;
	}
	
	public Crash scan(int exit){
		Crash crash = new Crash();
		String[] lines = context.con.getOutput().split("\n");
		
		for(int i = lines.length - 1; i > -1; i--){
			String line = lines[i];
			
			Matcher start_crash = start_crash_report_pattern.matcher(line);
			if(start_crash.matches()){
				log("Will not search further - start of crash report exceed.");
				break;
			}
			
			Matcher mt = crash_pattern.matcher(line);
			if(mt.matches()){
				crash.setFile(mt.group(1));
				continue;
			}
			
			for(CrashSignature sign : signatures)
				if(sign.match(line)){
					if(sign instanceof FakeCrashSignature){
						log("Minecraft closed with an illegal exit code not due to error. Cancelling.");
						log("Catched by signature:", sign.name);
						return null;
					}
					
					if(sign.exitcode != 0 && sign.exitcode != exit) continue;
					
					log("Signature \""+sign.name+"\" matches!");
					if(!crash.hasSignature(sign)) crash.addSignature(sign);
				}
		}
		
		return crash;
	}
	
	public static boolean parseExit(int code){
		return (code == 0);
	}
	
	private static CrashSignature[] initSigns(){
		CrashSignature[] r = new CrashSignature[5];
		
		r[0] = new CrashSignature(0, forge_prefix + "[\\s]*org\\.lwjgl\\.LWJGLException\\: Pixel format not accelerated", "Old graphics driver", "opengl");
		r[1] = new CrashSignature(0, forge_prefix + "(?:Exception in thread \".*\" ){0,1}java\\.lang\\.(?:Error|NoClass|Exception|Error|Throwable|Illegal){1}.+", "Probably modified JAR", "invalid-modify");
		r[2] = new CrashSignature(1, "Exception in thread \"main\" java.lang.SecurityException: SHA1 digest error for .+", "Undeleted META-INF", "meta-inf");
		r[3] = new FakeCrashSignature(1, forge_prefix + "Someone is closing me!", "ALC cleanup bug");
		r[4] = new CrashSignature(1, "^Error: Could not find or load main class .+", "Missing main class", "missing-main");
		
		return r;
	}
	
	void log(Object... w){ if(context.con != null) context.con.log(prefix, w); U.log(prefix, w); }
}

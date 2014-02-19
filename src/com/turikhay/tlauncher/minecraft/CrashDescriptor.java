package com.turikhay.tlauncher.minecraft;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.turikhay.util.U;
import com.turikhay.util.logger.PrintLogger;
import com.turikhay.util.logger.StringStream;

public class CrashDescriptor {
	final String prefix = "[CrashDescriptor]";
	
	final static String forge_prefix = "^(?:[0-9-]+ [0-9:]+ \\[[\\w]+\\]\\ {0,1}\\[{0,1}[\\w]*\\]{0,1}\\ {0,1}){0,1}";
	final static Pattern
		crash_pattern = Pattern.compile("^.*[\\#\\@\\!\\@\\#][ ]Game[ ]crashed!.+[\\#\\@\\!\\@\\#][ ](.+)$"),
		start_crash_report_pattern = Pattern.compile(forge_prefix + "---- Minecraft Crash Report ----");
	final static CrashSignature[] signatures = initSigns();
	
	private final PrintLogger context;
	private final StringStream stream;
	
	CrashDescriptor(MinecraftLauncher launcher){
		this.context = launcher.getLogger();
		this.stream = launcher.getStream();
	}
	
	CrashDescriptor(String version, StringStream stream){		
		this.context = null;
		this.stream = stream;
	}
	
	public Crash scan(int exit){
		Crash crash = new Crash();
		
		String[] lines = stream.getOutput().split("\n");
		
		for(int i = lines.length - 1; i > -1; i--){
			String line = lines[i];
			
			Matcher startM = start_crash_report_pattern.matcher(line);
			if(startM.matches()){
				log("Will not search further - start of crash report exceed.");
				break;
			}
			
			Matcher fileM = crash_pattern.matcher(line);
			if(fileM.matches()){
				crash.setFile(fileM.group(1));
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
		r[3] = new CrashSignature(1, "Error: Could not find or load main class .+$", "Missing main class", "missing-main");
		r[4] = new FakeCrashSignature(1, forge_prefix + "Someone is closing me!", "Direct close bug");
		
		return r;
	}
	
	void log(Object... w){ if(context != null) context.log(prefix, w); U.log(prefix, w); }
}

/*package com.turikhay.tlauncher.minecraft;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.google.gson.Gson;
import com.turikhay.tlauncher.ui.Alert;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;
import com.turikhay.util.logger.PrintLogger;
import com.turikhay.util.logger.StringStream;

public class CrashDescriptor {
	final String prefix = "[CrashDescriptor]";
	
	private static CrashSignature[] signatures;
	private static Pattern startReport, reportedLine, versionLine;
	
	private final PrintLogger context;
	private final StringStream stream;
	
	CrashDescriptor(MinecraftLauncher launcher){
		signatures = initSigns();
		
		this.context = launcher.getLogger();
		this.stream = launcher.getStream();
	}
	
	CrashDescriptor(StringStream stream){
		signatures = initSigns();
		
		this.context = null;
		this.stream = stream;
	}
	
	public Crash scan(int exit){
		Crash crash = new Crash();
		String version = null;
		
		Alert.showMessage("", stream.getOutput());
		Alert.showMessage("", U.toLog(signatures));
		
		String[] lines = stream.getOutput().split("\n");
		
		for(int i = lines.length - 1; i > -1; i--){
			String line = lines[i];
			
			Matcher startM = startReport.matcher(line);
			if(startM.matches()){
				log("Will not search further - start of crash report exceed.");
				break;
			}
			
			Matcher fileM = reportedLine.matcher(line);
			if(fileM.matches()){
				crash.setFile(fileM.group(1));
				continue;
			}
			
			Matcher versionM = versionLine.matcher(line);
			if(versionM.matches())
				version = versionM.group(1);
			
			for(CrashSignature sign : signatures)
				if(version != null && sign.getVersion().equals(version))
					log(sign.getVersion());
				else if(sign.match(line)){
					if(sign.isFake()){
						log("Minecraft closed with an illegal exit code not due to error. Cancelling.");
						log("Catched by signature:", sign.getName());
						return null;
					}
					
					if(sign.getExitCode() != 0 && sign.getExitCode() != exit) continue;
					
					log("Signature \""+sign.getName()+"\" matches!");
					if(!crash.hasSignature(sign)) crash.addSignature(sign);
				}
		}
		
		return crash;
	}
	
	public static boolean parseExit(int code){
		return (code == 0);
	}
	
	public static CrashSignature[] initSigns(){
		try{
			return initSigns_();
		}catch(Exception e){
			U.log("Cannot init CrashDescriptor!", e);
			return new CrashSignature[0];
		}
	}
	
	private static CrashSignature[] initSigns_(){		
		Gson gson = new Gson();
		CrashSignatureList list;
		
		try{
			list = gson.fromJson(FileUtil.getResource(CrashDescriptor.class.getResource("signatures.json")), CrashSignatureList.class);
		} catch(Exception e){
			throw new RuntimeException(e);
		}
		
		List<CrashSignature> signatures = list.getSignatures();
		
		Map<String, String> variables_ = list.getVariables(), variables = new HashMap<String, String>();
		StrSubstitutor sub = new StrSubstitutor(variables);
		
		for(Entry<String, String> en : variables_.entrySet())
			variables.put(en.getKey(), sub.replace(en.getValue()));		
		
		startReport = Pattern.compile(variables.get("start_crash"));
		reportedLine = Pattern.compile(variables.get("crash"));
		versionLine = Pattern.compile(variables.get("version"));
		
		CrashSignature[] r = new CrashSignature[signatures.size()];
		
		for(int i=0;i<r.length;i++){
			CrashSignature sign = signatures.get(i);
			String pattern = sign.getPattern();
			
			if(sign.isForge())
				pattern = variables.get("forge") + pattern;
			
			pattern = sub.replace(pattern);
			
			sign.setPattern(pattern);
			
			r[i] = sign;
		}
		
		return r;
	}
	
	void log(Object... w){ if(context != null) context.log(prefix, w); U.log(prefix, w); }
	
	static class CrashSignatureList {
		private Map<String, String> variables;
		private List<CrashSignature> signatures;
		
		public Map<String, String> getVariables(){
			return Collections.unmodifiableMap(variables);
		}
		
		public List<CrashSignature> getSignatures(){
			return Collections.unmodifiableList(signatures);
		}
	}
}
*/
package com.turikhay.tlauncher.minecraft;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.util.U;

public class Crash {
	private String file;
	private List<CrashSignature> signatures = new ArrayList<CrashSignature>();
	
	void addSignature(CrashSignature sign){ signatures.add(sign); }
	void removeSignature(CrashSignature sign){ signatures.remove(sign); }
	
	void setFile(String path){ this.file = path; }
	
	public String getFile(){ return this.file; }
	public List<CrashSignature> getSignatures(){
		return Collections.unmodifiableList(signatures);
	}
	public boolean hasSignature(CrashSignature s){
		return signatures.contains(s);
	}
	public boolean isRecognized(){
		return !signatures.isEmpty();
	}
	
	public static void handle(Crash crash){
		String p = "crash.", title = Localizable.get(p + "title"), report = crash.getFile();
		
		if(!crash.isRecognized()){
			Alert.showError(title, Localizable.get(p + "unknown"), null);
		} else {
			for(CrashSignature sign : crash.getSignatures()){
				String path = sign.path, message = Localizable.get(p + path), url = Localizable.get(p + path + ".url");
				URI uri = U.makeURI(url);
			
				if(uri != null){
					if(Alert.showQuestion(title, message, report, false))
						OperatingSystem.openLink(uri);
				} else
					Alert.showMessage(title, message, report);
			}
		}
		
		if(report == null) return;
		
		if(Alert.showQuestion(p + "store", false)){
			U.log("Removing crash report...");
			
			File file = new File(report);
			if(!file.exists())
				U.log("File is already removed. LOL.");
			else {
				try{
					
					if(!file.delete())
						throw new Exception("file.delete() returned false");
					
				}catch(Exception e){
					U.log("Can't delete crash report file. Okay.");
					Alert.showAsyncMessage(p + "store.failed", e);
					
					return;
				}
				U.log("Yay, crash report file doesn't exist by now.");
			}
			Alert.showAsyncMessage(p + "store.success");
		}
	}
}

package com.turikhay.tlauncher.minecraft;

import java.util.ArrayList;
import java.util.List;

public class Crash {
	private String file;
	private List<CrashSignature> signatures = new ArrayList<CrashSignature>();
	
	void addSignature(CrashSignature sign){ signatures.add(sign); }
	void removeSignature(CrashSignature sign){ signatures.remove(sign); }
	
	void setFile(String path){ this.file = path; }
	
	public String getFile(){ return this.file; }
	public List<CrashSignature> getSignatures(){
		List<CrashSignature> r = new ArrayList<CrashSignature>();
		
		for(CrashSignature sign : signatures)
			r.add(sign);
		
		return r;
	}
	public boolean hasSignature(CrashSignature s){
		return signatures.contains(s);
	}
	public boolean isRecognized(){
		return !signatures.isEmpty();
	}
}

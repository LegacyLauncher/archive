package net.minecraft.launcher.versions;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.launcher.OperatingSystem;

import org.apache.commons.lang3.text.StrSubstitutor;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.repository.VersionRepository;

public class Library {
	private static final StrSubstitutor SUBSTITUTOR;
	
	private String name;
	private List<Rule> rules;
	private Map<OperatingSystem, String> natives;
	private ExtractRules extract;
	private String url, exact_url;
	
	public String getName() {
		return name;
	}
	
	public List<Rule> getRules(){
		return Collections.unmodifiableList(rules);
	}
	
	public boolean appliesToCurrentEnvironment() {
	    if(this.rules == null) return true;
	    
	    Rule.Action lastAction = Rule.Action.DISALLOW;

	    for(Rule rule : this.rules) {
	      Rule.Action action = rule.getAppliedAction();
	      
	      if (action != null) lastAction = action;
	    }

	    return lastAction == Rule.Action.ALLOW;
	}
	
	public Map<OperatingSystem, String> getNatives() {
		return this.natives;
	}

	public ExtractRules getExtractRules() {
		return this.extract;
	}
	
	public String getArtifactBaseDir() {
		if(name == null)
			throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
		
		String[] parts = this.name.split(":", 3);
		return String.format("%s/%s/%s", new Object[] { parts[0].replaceAll("\\.", "/"), parts[1], parts[2] });
	}

	public String getArtifactPath() {
		return getArtifactPath(null);
	}

	public String getArtifactPath(String classifier) {
		if(name == null)
			throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
		
		return String.format("%s/%s", new Object[] { getArtifactBaseDir(), getArtifactFilename(classifier) });
	}

	public String getArtifactFilename(String classifier) {
		if(this.name == null)
			throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");

		String[] parts = this.name.split(":", 3); 
		String result;
		    
		if(classifier == null)
			result = String.format("%s-%s.jar", new Object[] { parts[1], parts[2] });
		else
			result = String.format("%s-%s%s.jar", new Object[] { parts[1], parts[2], "-" + classifier });

		return SUBSTITUTOR.replace(result);
	}
	
	public String toString() {
		return "Library{name='" + this.name + '\'' + ", rules=" + this.rules + ", natives=" + this.natives + ", extract=" + this.extract + '}';
	}
	
	public String getURL(VersionRepository source, OperatingSystem os) {
		if(exact_url != null) return exact_url;
		
		String nativePath = natives != null && appliesToCurrentEnvironment() ? natives.get(os) : null;
		String download_url;
		
		if(url == null) download_url = TLauncher.getLibraryRepo()[0]; // TODO change return type to downloadable
		else download_url = (url.startsWith("/"))? source.getSelectedRepo() + url.substring(1) : url;
		
		return download_url + getArtifactPath(nativePath);
	}
	
	static {
		HashMap<String, String> map = new HashMap<String, String>();
		OperatingSystem os = OperatingSystem.getCurrentPlatform();
		
		map.put("platform", os.getName());
		map.put("arch", os.getArch());
		  
		SUBSTITUTOR = new StrSubstitutor(map);
	}
}

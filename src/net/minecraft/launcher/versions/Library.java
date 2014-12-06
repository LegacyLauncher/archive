package net.minecraft.launcher.versions;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.StrSubstitutor;

import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.ForgeLibDownloadable;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;

public class Library {
	private static final String FORGE_LIB_SUFFIX = ".pack.xz";
	private static final StrSubstitutor SUBSTITUTOR;

	private String name;
	private List<Rule> rules;
	private Map<OS, String> natives;
	private ExtractRules extract;
	private String url, exact_url, packed;

	@Override
	public boolean equals(Object o) {
		if(o == null || !(o instanceof Library))
			return false;

		Library lib = (Library) o;
		return name == null? lib.name == null : name.equalsIgnoreCase(lib.name);
	}

	public String getName() {
		return name;
	}

	public List<Rule> getRules() {
		return Collections.unmodifiableList(rules);
	}

	public boolean appliesToCurrentEnvironment() {
		if (this.rules == null)
			return true;

		Rule.Action lastAction = Rule.Action.DISALLOW;

		for (Rule rule : this.rules) {
			Rule.Action action = rule.getAppliedAction();

			if (action != null)
				lastAction = action;
		}

		return lastAction == Rule.Action.ALLOW;
	}

	public Map<OS, String> getNatives() {
		return this.natives;
	}

	public ExtractRules getExtractRules() {
		return this.extract;
	}

	String getArtifactBaseDir() {
		if (name == null)
			throw new IllegalStateException(
					"Cannot get artifact dir of empty/blank artifact");

		String[] parts = this.name.split(":", 3);
		return String.format("%s/%s/%s",
				new Object[] { parts[0].replaceAll("\\.", "/"), parts[1],
				parts[2] });
	}

	public String getArtifactPath() {
		return getArtifactPath(null);
	}

	public String getArtifactPath(String classifier) {
		if (name == null)
			throw new IllegalStateException(
					"Cannot get artifact path of empty/blank artifact");

		return String.format("%s/%s", new Object[] { getArtifactBaseDir(),
				getArtifactFilename(classifier) });
	}

	String getArtifactFilename(String classifier) {
		if (this.name == null)
			throw new IllegalStateException(
					"Cannot get artifact filename of empty/blank artifact");

		String[] parts = this.name.split(":", 3);
		String result;

		if (classifier == null)
			result = String.format("%s-%s.jar", new Object[] { parts[1],
					parts[2] });
		else
			result = String.format("%s-%s%s.jar", new Object[] { parts[1],
					parts[2], "-" + classifier });

		return SUBSTITUTOR.replace(result);
	}

	@Override
	public String toString() {
		return "Library{name='" + this.name + '\'' + ", rules=" + this.rules
				+ ", natives=" + this.natives + ", extract=" + this.extract
				+ ", packed='"+ packed +"'}";
	}

	public Downloadable getDownloadable(Repository versionSource, File file, OS os) {

		String path;
		Repository repo = null;
		boolean isForge = "forge".equals(packed);

		if(exact_url == null) {
			String nativePath = natives != null && appliesToCurrentEnvironment() ? natives.get(os) : null;

			path = getArtifactPath(nativePath) + (isForge? FORGE_LIB_SUFFIX : "");

			if (url == null) {
				repo = Repository.LIBRARY_REPO;
			} else {
				if(url.startsWith("/")) {
					repo = versionSource;
					path = url.substring(1) + path;
				} else {
					// repo = null;
					path = url + path;
				}
			}
		} else {
			path = exact_url;
		}

		if(isForge) {
			File tempFile = new File(file.getAbsolutePath() +FORGE_LIB_SUFFIX);
			return new ForgeLibDownloadable(path, tempFile, file);
		}

		return repo == null ? new Downloadable(path, file) : new Downloadable(repo, path, file);
	}

	static {
		HashMap<String, String> map = new HashMap<String, String>();

		map.put("platform", OS.CURRENT.getName());
		map.put("arch", OS.Arch.CURRENT.asString());

		SUBSTITUTOR = new StrSubstitutor(map);
	}
}

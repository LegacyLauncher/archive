package net.minecraft.launcher.versions;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.launcher.OperatingSystem;

public class Rule {
	private Action action = Action.ALLOW;
	private OSRestriction os;

	public Rule() {
	}

	public Rule(Rule rule) {
		this.action = rule.action;

		if (rule.os != null)
			this.os = new OSRestriction(rule.os);
	}

	public Action getAppliedAction() {
		if (os != null && !os.isCurrentOperatingSystem())
			return null;

		return this.action;
	}

	@Override
	public String toString() {
		return "Rule{action=" + this.action + ", os=" + this.os + '}';
	}

	public static enum Action {
		ALLOW, DISALLOW
	}

	public class OSRestriction {
		private OperatingSystem name;
		private String version;

		public OSRestriction(OSRestriction osRestriction) {
			this.name = osRestriction.name;
			this.version = osRestriction.version;
		}

		public boolean isCurrentOperatingSystem() {
			if (name != null && name != OperatingSystem.getCurrentPlatform())
				return false;

			if (this.version != null)
				try {
					Pattern pattern = Pattern.compile(this.version);
					Matcher matcher = pattern.matcher(System
							.getProperty("os.version"));
					if (!matcher.matches())
						return false;
				} catch (Throwable ignored) {
				}

			return true;
		}

		@Override
		public String toString() {
			return "OSRestriction{name=" + this.name + ", version='"
					+ this.version + '\'' + '}';
		}
	}
}

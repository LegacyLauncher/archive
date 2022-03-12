package net.minecraft.launcher.versions;

import ru.turikhay.util.OS;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
    private final Rule.Action action;
    private Rule.OSRestriction os;
    private Map<String, Object> features;

    public Rule() {
        action = Rule.Action.ALLOW;
    }

    public Rule(Rule rule) {
        action = rule.action;
        if (rule.os != null) {
            os = new OSRestriction(rule.os);
        }
        if (rule.features != null) {
            features = rule.features;
        }
    }

    public Rule.Action getAppliedAction(Rule.FeatureMatcher featureMatcher) {
        if ((this.os != null) && (!this.os.isCurrentOperatingSystem())) return null;
        if (this.features != null) {
            if (featureMatcher == null) return null;
            for (Map.Entry<String, Object> feature : this.features.entrySet()) {
                if (!featureMatcher.hasFeature(feature.getKey(), feature.getValue())) {
                    return null;
                }
            }
        }

        return this.action;
    }


    public String toString() {
        return "Rule{action=" + action + ", os=" + os + '}';
    }

    public enum Action {
        ALLOW,
        DISALLOW
    }

    public static class OSRestriction {
        private OS name;
        private String version;

        public OSRestriction() {
        }

        public OSRestriction(Rule.OSRestriction osRestriction) {
            name = osRestriction.name;
            version = osRestriction.version;
        }

        public boolean isCurrentOperatingSystem() {
            if (name != null && name != OS.CURRENT) {
                return false;
            } else {
                if (version != null) {
                    try {
                        Pattern pattern = Pattern.compile(version);
                        Matcher matcher = pattern.matcher(System.getProperty("os.version"));
                        if (!matcher.matches()) {
                            return false;
                        }
                    } catch (Throwable ignored) {
                    }
                }

                return true;
            }
        }

        public String toString() {
            return "OSRestriction{name=" + name + ", version='" + version + '\'' + '}';
        }
    }

    public interface FeatureMatcher {
        boolean hasFeature(String paramString, Object paramObject);
    }

}

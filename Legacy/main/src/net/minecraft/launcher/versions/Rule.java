package net.minecraft.launcher.versions;

import ru.turikhay.util.OS;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Rule {
    private Rule.Action action;
    private Rule.OSRestriction os;

    public Rule() {
        action = Rule.Action.ALLOW;
    }

    public Rule(Rule rule) {
        action = Rule.Action.ALLOW;
        action = rule.action;
        if (rule.os != null) {
            os = new Rule.OSRestriction(rule.os);
        }

    }

    public Rule.Action getAppliedAction() {
        return os != null && !os.isCurrentOperatingSystem() ? null : action;
    }

    public String toString() {
        return "Rule{action=" + action + ", os=" + os + '}';
    }

    public enum Action {
        ALLOW,
        DISALLOW
    }

    public class OSRestriction {
        private OS name;
        private String version;

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
                    } catch (Throwable var3) {
                    }
                }

                return true;
            }
        }

        public String toString() {
            return "OSRestriction{name=" + name + ", version=\'" + version + '\'' + '}';
        }
    }
}

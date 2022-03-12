package net.minecraft.launcher.versions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExtractRules {
    private final List<String> exclude = new ArrayList<>();

    public ExtractRules() {
    }

    public ExtractRules(String[] exclude) {
        if (exclude != null) {
            Collections.addAll(this.exclude, exclude);
        }
    }

    public ExtractRules(ExtractRules rules) {
        this.exclude.addAll(rules.exclude);
    }

    public List<String> getExcludes() {
        return exclude;
    }

    public boolean shouldExtract(String path) {

        for (String rule : exclude) {
            if (path.startsWith(rule)) {
                return false;
            }
        }

        return true;
    }
}

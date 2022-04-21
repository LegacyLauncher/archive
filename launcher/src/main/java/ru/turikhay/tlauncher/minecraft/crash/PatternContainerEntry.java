package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public abstract class PatternContainerEntry extends CrashEntry {
    private final List<PatternEntry> patternEntries = new ArrayList<>();
    private boolean anyPatternMakesCapable;

    public PatternContainerEntry(CrashManager manager, String name) {
        super(manager, name);
    }

    public final boolean isAnyPatternMakesCapable() {
        return anyPatternMakesCapable;
    }

    protected final void setAnyPatternMakesCapable(boolean anyPatternMakesCapable) {
        this.anyPatternMakesCapable = anyPatternMakesCapable;
    }

    protected final void addPattern(PatternEntry entry) {
        patternEntries.add(entry);
    }

    protected final PatternEntry addPattern(String name, Pattern pattern) {
        PatternEntry entry = new PatternEntry(getManager(), name, pattern);
        addPattern(entry);
        return entry;
    }

    @Override
    protected boolean checkCapability() throws Exception {
        if (!super.checkCapability()) {
            return false;
        }

        List<PatternEntry> capablePatterns = new ArrayList<>();
        for (PatternEntry entry : patternEntries) {
            if (entry.checkCapability()) {
                capablePatterns.add(entry);
                if (anyPatternMakesCapable) {
                    break;
                }
            }
        }
        return !capablePatterns.isEmpty() && checkCapability(capablePatterns);
    }

    protected abstract boolean checkCapability(List<PatternEntry> capablePatterns);

    @Override
    public ToStringBuilder buildToString() {
        return super.buildToString()
                .append("patterns", patternEntries);
    }
}

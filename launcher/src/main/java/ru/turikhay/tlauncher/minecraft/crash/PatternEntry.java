package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.tlauncher.minecraft.launcher.ChildProcessLogger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternEntry extends CrashEntry {
    private final Pattern pattern;
    private Matcher match;

    public PatternEntry(CrashManager manager, String name, Pattern pattern) {
        super(manager, name);
        this.pattern = Objects.requireNonNull(pattern, "pattern");
    }

    protected Matcher getMatch() {
        return match;
    }

    @Override
    protected boolean checkCapability() throws Exception {
        if (!super.checkCapability()) {
            return false;
        }

        try (Scanner scanner = getScanner()) {
            while (scanner.hasNextLine()) {
                Matcher matcher = pattern.matcher(scanner.nextLine());
                if (matcher.matches()) {
                    match = matcher;
                    getManager().getCrash().addExtra("pattern:" + pattern, matcher.toString());
                    return true;
                }
            }
        }
        return false;
    }

    Scanner getScanner() throws IOException {
        return getScanner(getManager().getProcessLogger());
    }

    static Scanner getScanner(ChildProcessLogger processLogger) throws IOException {
        return new Scanner(processLogger.getLogFile().read());
    }

    static boolean matchPatterns(Scanner scanner, List<Pattern> patterns, ArrayList<String> matches) {
        Pattern expectedPattern;
        int expectedPatternIndex = 0;
        while (scanner.hasNextLine()) {
            expectedPattern = patterns.get(expectedPatternIndex);
            String line = scanner.nextLine();

            Matcher m = expectedPattern.matcher(line);
            if (m.matches()) {
                if (matches != null) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        matches.add(m.group(i));
                    }
                }
                if (++expectedPatternIndex == patterns.size()) {
                    break;
                }
            }
        }
        return expectedPatternIndex == patterns.size();
    }

    @Override
    public ToStringBuilder buildToString() {
        return super.buildToString()
                .append("pattern", pattern);
    }
}

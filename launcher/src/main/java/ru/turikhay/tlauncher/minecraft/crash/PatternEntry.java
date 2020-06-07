package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternEntry extends CrashEntry {
    private final Pattern pattern;
    private Matcher match;

    public PatternEntry(CrashManager manager, String name, Pattern pattern) {
        super(manager, name);
        this.pattern = U.requireNotNull(pattern, "pattern");
    }

    protected Matcher getMatch() {
        return match;
    }

    @Override
    protected boolean checkCapability() throws Exception {
        if (!super.checkCapability()) {
            return false;
        }

        Scanner scanner = getScanner();
        while (scanner.hasNextLine()) {
            Matcher matcher = pattern.matcher(scanner.nextLine());
            if (matcher.matches()) {
                match = matcher;
                getManager().getCrash().addExtra("pattern:" + pattern.toString(), matcher.toString());
                return true;
            }
        }
        return false;
    }

    Scanner getScanner() {
        return getScanner(getManager().getOutput());
    }

    static Scanner getScanner(CharSequence output) {
        return new Scanner(new CharSequenceInputStream(output, FileUtil.DEFAULT_CHARSET));
    }

    static boolean matchPatterns(Scanner scanner, List<Pattern> patterns, ArrayList<String> matches) {
        Pattern expectedPattern; int expectedPatternIndex = 0;
        while(scanner.hasNextLine()) {
            expectedPattern = patterns.get(expectedPatternIndex);
            String line = scanner.nextLine();

            Matcher m = expectedPattern.matcher(line);
            if(m.matches()) {
                if(matches != null) {
                    for (int i = 1; i <= m.groupCount(); i++) {
                        matches.add(m.group(i));
                    }
                }
                if(++expectedPatternIndex == patterns.size()) {
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

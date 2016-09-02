package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.commons.io.input.CharSequenceInputStream;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternEntry extends CrashEntry {
    private final Pattern pattern;

    public PatternEntry(CrashManager manager, String name, Pattern pattern) {
        super(manager, name);
        this.pattern = U.requireNotNull(pattern, "pattern");
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
                return true;
            }
        }
        return false;
    }

    public Scanner getScanner() {
        return getScanner(getManager().getOutput());
    }

    public static Scanner getScanner(CharSequence output) {
        return new Scanner(new CharSequenceInputStream(output, FileUtil.DEFAULT_CHARSET));
    }

    @Override
    public ToStringBuilder buildToString() {
        return super.buildToString()
                .append("pattern", pattern);
    }
}

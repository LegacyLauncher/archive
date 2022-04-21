package ru.turikhay.tlauncher.minecraft.crash;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.regex.Pattern;

public class BadMainClassEntry extends PatternEntry {
    private static final Logger LOGGER = LogManager.getLogger(BadMainClassEntry.class);

    public BadMainClassEntry(CrashManager manager) {
        super(manager, "bad-mainclass", Pattern.compile("^Error: Could not find or load main class (.+)$"));
        setExitCode(1);
    }

    @Override
    protected boolean checkCapability() throws Exception {
        if (!super.checkCapability()) {
            return false;
        }

        String requestedMainClass = getMatch().group(1);
        LOGGER.info("Found bad main class: {}", requestedMainClass);

        if (getManager().getLauncher() != null) {
            String jvmArgs = getManager().getLauncher().getConfiguration().get("minecraft.javaargs");
            if (jvmArgs != null) {
                if (jvmArgs.contains(requestedMainClass)) {
                    setPath("check-javaargs");
                    newButton("clear-javaargs", () -> getManager().getLauncher().getConfiguration().set("minecraft.javaargs", null));
                }
                addButton(getManager().getButton("open-settings"));
                return true;
            }
        }

        return true;
    }
}

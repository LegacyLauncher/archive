package ru.turikhay.tlauncher.configuration;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import ru.turikhay.tlauncher.managers.JavaManagerConfig;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.util.OS;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class ArgumentParser {
    private static final Map<String, String> m = new HashMap();
    private static final OptionParser parser;

    static {
        m.put("directory", "minecraft.gamedir");
        m.put("profiles", "profiles");
        m.put("java-executable", "minecraft.cmd");
        m.put("version", "login.version");
        m.put("username", "login.account");
        m.put("usertype", "login.account.type");
        m.put("javaargs", "minecraft.javaargs");
        m.put("margs", "minecraft.args");
        m.put("window", "minecraft.size");
        m.put("background", "gui.background");
        m.put("fullscreen", "minecraft.fullscreen");
        m.put("theme", "gui.theme");
        m.put("jre-dir", JavaManagerConfig.PATH_ROOT_DIR);
        m.put("-block-settings", "gui.settings.blocked");

        parser = new OptionParser();

        parser.accepts("help", "Prints help");
        parser.accepts("debug", "Runs in debug mode");
        parser.accepts("no-terminate", "Do not terminate Bootstrapper if started with it");
        parser.accepts("directory", "Specifies Minecraft directory").withRequiredArg();
        parser.accepts("profiles", "Specifies profile file").withRequiredArg();
        parser.accepts("java-directory", "Specifies Java directory (use java-executable instead)").withRequiredArg();
        parser.accepts("java-executable", "Specifies Java executable").withRequiredArg();
        parser.accepts("version", "Specifies version to run").withRequiredArg();
        parser.accepts("username", "Specifies username").withRequiredArg();
        parser.accepts("usertype", "Specifies user type (if multiple with the same name)").withRequiredArg();
        parser.accepts("javaargs", "Specifies JVM arguments").withRequiredArg();
        parser.accepts("margs", "Specifies Minecraft arguments").withRequiredArg();
        parser.accepts("window", "Specifies window size in format: width;height").withRequiredArg();
        parser.accepts("settings", "Specifies path to settings file").withRequiredArg();
        parser.accepts("background", "Specifies background image. URL links, JPEG and PNG formats are supported.").withRequiredArg();
        parser.accepts("fullscreen", "Specifies whether fullscreen mode enabled or not").withRequiredArg();
        parser.accepts("theme", "Specifies theme file").withRequiredArg();
        parser.accepts("jre-dir", "Specifies where Mojang JRE is saved").withRequiredArg();
        parser.accepts("block-settings", "Disables settings and folder buttons");
    }

    public static OptionParser getParser() {
        return parser;
    }

    public static String getHelp() {
        StringWriter writer = new StringWriter();
        try {
            parser.printHelpOn(writer);
        } catch (IOException ioE) {
        }
        writer.flush();
        return writer.toString();
    }

    public static OptionSet parseArgs(String[] args) throws OptionException, IOException {
        try {
            return parser.parse(args);
        } catch (OptionException e) {
            Alert.showLocError("args.error", e);
            throw e;
        }
    }

    public static Map<String, Object> parse(OptionSet set) {
        HashMap r = new HashMap();
        if (set == null) {
            return r;
        } else {
            if(set.has("java-directory")) {
                r.put(
                        "java-executable",
                        // <java dir>/bin/java[w.exe]
                        set.valueOf("java-directory") + File.pathSeparator +
                                "bin" + File.pathSeparator +
                                "java" + (OS.WINDOWS.isCurrent()? "w.exe" : "")
                );
            }

            Iterator var3 = m.entrySet().iterator();

            while (var3.hasNext()) {
                Entry a = (Entry) var3.next();
                String key = (String) a.getKey();
                Object value = null;
                if (key.startsWith("-")) {
                    key = key.substring(1);
                    value = Boolean.valueOf(true);
                }

                if (set.has(key)) {
                    if (value == null) {
                        value = set.valueOf(key);
                    }

                    r.put(a.getValue(), value);
                }
            }

            return r;
        }
    }
}

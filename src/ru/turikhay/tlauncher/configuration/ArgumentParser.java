package ru.turikhay.tlauncher.configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import ru.turikhay.tlauncher.ui.alert.Alert;

public class ArgumentParser {
   private static final Map m = new HashMap();
   private static final OptionParser parser;

   public static OptionParser getParser() {
      return parser;
   }

   public static OptionSet parseArgs(String[] args) throws IOException {
      try {
         return parser.parse(args);
      } catch (OptionException var2) {
         var2.printStackTrace();
         parser.printHelpOn((OutputStream)System.out);
         Alert.showError(var2, false);
         return null;
      }
   }

   public static Map parse(OptionSet set) {
      HashMap r = new HashMap();
      if (set == null) {
         return r;
      } else {
         Iterator var3 = m.entrySet().iterator();

         while(var3.hasNext()) {
            Entry a = (Entry)var3.next();
            String key = (String)a.getKey();
            Object value = null;
            if (key.startsWith("-")) {
               key = key.substring(1);
               value = true;
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

   static {
      m.put("directory", "minecraft.gamedir");
      m.put("profiles", "profiles");
      m.put("java-directory", "minecraft.javadir");
      m.put("version", "login.version");
      m.put("username", "login.account");
      m.put("usertype", "login.account.type");
      m.put("javaargs", "minecraft.javaargs");
      m.put("margs", "minecraft.args");
      m.put("window", "minecraft.size");
      m.put("background", "gui.background");
      m.put("fullscreen", "minecraft.fullscreen");
      m.put("-block-settings", "gui.settings.blocked");
      parser = new OptionParser();
      parser.accepts("help", "Shows this help");
      parser.accepts("nogui", "Starts minimal version");
      parser.accepts("directory", "Specifies Minecraft directory").withRequiredArg();
      parser.accepts("java-directory", "Specifies Java directory").withRequiredArg();
      parser.accepts("version", "Specifies version to run").withRequiredArg();
      parser.accepts("username", "Specifies username").withRequiredArg();
      parser.accepts("usertype", "Specifies user type (if multiple with the same name)").withRequiredArg();
      parser.accepts("javaargs", "Specifies JVM arguments").withRequiredArg();
      parser.accepts("margs", "Specifies Minecraft arguments").withRequiredArg();
      parser.accepts("window", "Specifies window size in format: width;height").withRequiredArg();
      parser.accepts("settings", "Specifies path to settings file").withRequiredArg();
      parser.accepts("background", "Specifies background image. URL links, JPEG and PNG formats are supported.").withRequiredArg();
      parser.accepts("fullscreen", "Specifies whether fullscreen mode enabled or not").withRequiredArg();
      parser.accepts("block-settings", "Disables settings and folder buttons");
   }
}

package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.util.OS;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.MapTokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

public final class CrashEntryList {
   private final List signatures;
   private final List _signatures;
   private int revision;
   private double required;

   private CrashEntryList() {
      this.signatures = new ArrayList();
      this._signatures = Collections.unmodifiableList(this.signatures);
   }

   public List getSignatures() {
      return this._signatures;
   }

   public int getRevision() {
      return this.revision;
   }

   public static String getLoc(JsonElement elem, JsonDeserializationContext context, ITokenResolver resolver) {
      if (elem == null) {
         throw new NullPointerException();
      } else if (elem.isJsonPrimitive()) {
         return TokenReplacingReader.resolveVars(elem.getAsString(), resolver);
      } else {
         JsonObject obj = elem.getAsJsonObject();
         Type type = (new TypeToken() {
         }).getType();
         Map map = (Map)context.deserialize(obj, type);
         Iterator var6 = map.entrySet().iterator();

         while(var6.hasNext()) {
            java.util.Map.Entry entry = (java.util.Map.Entry)var6.next();
            U.requireNotNull(entry.getKey());
            StringUtil.requireNotBlank((String)entry.getValue());
         }

         String locale = (TLauncher.getInstance() == null ? "en_US" : TLauncher.getInstance().getLang().getSelected()).toString();
         if (!map.containsKey(locale)) {
            if (Configuration.isUSSRLocale(locale) && map.containsKey("ru_RU")) {
               locale = "ru_RU";
            } else {
               locale = "en_US";
            }
         }

         return (String)map.get(locale);
      }
   }

   // $FF: synthetic method
   CrashEntryList(Object x0) {
      this();
   }

   public static class ListDeserializer implements JsonDeserializer {
      private final Map globalVars;
      private final Map vars;
      private final MapTokenResolver varsResolver;
      private final CrashManager manager;
      private final Button.Deserializer buttonDeserializer;

      ListDeserializer(CrashManager manager) {
         this.manager = (CrashManager)U.requireNotNull(manager);
         this.globalVars = new HashMap();
         this.globalVars.put("os", OS.CURRENT.toString());
         this.globalVars.put("arch", OS.Arch.CURRENT.toString());
         this.globalVars.put("locale", TLauncher.getInstance().getSettings().getLocale().toString());
         this.vars = new LinkedHashMap(this.globalVars);
         this.varsResolver = new MapTokenResolver(this.vars);
         this.buttonDeserializer = new Button.Deserializer(manager, this.varsResolver);
      }

      Map getVars() {
         return this.vars;
      }

      public String asString(JsonElement elem) {
         return TokenReplacingReader.resolveVars(elem.getAsString(), this.varsResolver);
      }

      public CrashEntryList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
         JsonObject root = json.getAsJsonObject();
         double required = root.get("required").getAsDouble();
         if (TLauncher.getVersion() < required) {
            throw new CrashEntryList.ListDeserializer.IncompatibleEntryList(required);
         } else {
            CrashEntryList entryList = new CrashEntryList();
            entryList.required = required;
            entryList.revision = root.get("revision").getAsInt();
            HashMap loadedVars = new HashMap();
            Iterator var9 = root.get("variables").getAsJsonObject().entrySet().iterator();

            while(var9.hasNext()) {
               java.util.Map.Entry entry = (java.util.Map.Entry)var9.next();
               String value;
               loadedVars.put(entry.getKey(), value = CrashEntryList.getLoc((JsonElement)entry.getValue(), context, this.varsResolver));
               this.log("Processing var:", entry.getKey(), value);
            }

            this.vars.putAll(loadedVars);
            Map buttonsMap = new HashMap();
            JsonArray buttons = root.get("buttons").getAsJsonArray();

            for(int i = 0; i < buttons.size(); ++i) {
               Button button = this.buttonDeserializer.deserialize(buttons.get(i), Button.class, context, true);
               buttonsMap.put(button.getName(), button);
            }

            CrashEntryList.ListDeserializer.EntryDeserializer entryDeserializer = new CrashEntryList.ListDeserializer.EntryDeserializer(buttonsMap, this.buttonDeserializer);
            JsonArray signatures = root.get("signatures").getAsJsonArray();

            for(int i = 0; i < signatures.size(); ++i) {
               CrashEntry entry = entryDeserializer.deserialize(signatures.get(i), CrashEntryList.ListDeserializer.EntryDeserializer.class, context);
               this.log("Entry parsed:", entry.getName());
               entryList.signatures.add(entry);
            }

            return entryList;
         }
      }

      private void log(Object... o) {
         U.log("[CrashEntryDeserializer]", o);
      }

      class IncompatibleEntryList extends JsonParseException {
         IncompatibleEntryList(double required) {
            super("required: " + required);
         }
      }

      public class EntryDeserializer implements JsonDeserializer {
         private final Map buttonMap;
         private final Button.Deserializer buttonDeserializer;

         private EntryDeserializer(Map buttonMap, Button.Deserializer buttonDeserializer) {
            this.buttonMap = buttonMap;
            this.buttonDeserializer = buttonDeserializer;
         }

         public CrashEntry deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject object = json.getAsJsonObject();
            String name = object.get("name").getAsString();
            Object entry;
            if (object.has("pattern")) {
               entry = new PatternEntry(ListDeserializer.this.manager, name, Pattern.compile(ListDeserializer.this.asString(object.get("pattern"))));
            } else {
               entry = new CrashEntry(ListDeserializer.this.manager, name);
            }

            if (object.has("fake")) {
               ((CrashEntry)entry).setFake(object.get("fake").getAsBoolean());
            }

            if (object.has("permitHelp")) {
               ((CrashEntry)entry).setPermitHelp(object.get("permitHelp").getAsBoolean());
            }

            if (object.has("exitCode")) {
               ((CrashEntry)entry).setExitCode(object.get("exitCode").getAsInt());
            }

            if (object.has("version")) {
               ((CrashEntry)entry).setVersionPattern(Pattern.compile(ListDeserializer.this.asString(object.get("version"))));
            }

            if (object.has("jre")) {
               ((CrashEntry)entry).setJrePattern(Pattern.compile(ListDeserializer.this.asString(object.get("jre"))));
            }

            if (object.has("archIssue")) {
               ((CrashEntry)entry).setArchIssue(object.get("archIssue").getAsBoolean());
            }

            if (object.has("graphicsCard")) {
               ((CrashEntry)entry).setGraphicsCardPattern(Pattern.compile(ListDeserializer.this.asString(object.get("graphicsCard"))));
            }

            if (object.has("title")) {
               ((CrashEntry)entry).setTitle(CrashEntryList.getLoc(object.get("title"), context, ListDeserializer.this.varsResolver));
            }

            if (object.has("loc")) {
               ((CrashEntry)entry).setLocalizable(object.get("loc").getAsBoolean());
            }

            if (object.has("body")) {
               ((CrashEntry)entry).setBody(CrashEntryList.getLoc(object.get("body"), context, ListDeserializer.this.varsResolver));
            }

            if (object.has("image")) {
               ((CrashEntry)entry).setImage(ListDeserializer.this.asString(object.get("image")));
            }

            if (object.has("os")) {
               ((CrashEntry)entry).setOS((OS[])((OS[])context.deserialize(object.get("os"), (new TypeToken() {
               }).getType())));
            }

            if (object.has("buttons")) {
               JsonArray buttons = object.getAsJsonArray("buttons");

               for(int i = 0; i < buttons.size(); ++i) {
                  JsonElement elem = buttons.get(i);
                  Button button;
                  if (elem.isJsonPrimitive()) {
                     button = (Button)this.buttonMap.get(elem.getAsString());
                  } else {
                     button = this.buttonDeserializer.deserialize(buttons.get(i), Button.class, context, false);
                  }

                  ((CrashEntry)entry).addButton(button);
               }
            }

            return (CrashEntry)entry;
         }

         // $FF: synthetic method
         EntryDeserializer(Map x1, Button.Deserializer x2, Object x3) {
            this(x1, x2);
         }
      }
   }
}

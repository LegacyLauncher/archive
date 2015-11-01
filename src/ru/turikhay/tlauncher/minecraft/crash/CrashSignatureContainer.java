package ru.turikhay.tlauncher.minecraft.crash;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import ru.turikhay.tlauncher.TLauncher;

public class CrashSignatureContainer {
   private static final int universalExitCode = 0;
   private Map variables = new LinkedHashMap();
   private List signatures = new ArrayList();

   public Map getVariables() {
      return this.variables;
   }

   public List getSignatures() {
      return this.signatures;
   }

   public String getVariable(String key) {
      return (String)this.variables.get(key);
   }

   public Pattern getPattern(String key) {
      return Pattern.compile((String)this.variables.get(key));
   }

   public String toString() {
      return this.getClass().getSimpleName() + "{\nvariables='" + this.variables + "',\nsignatures='" + this.signatures + "'}";
   }

   private static class CrashSignatureListSimpleDeserializer {
      private final Gson defaultContext;
      private Map variables;
      private String forgePrefix;

      private CrashSignatureListSimpleDeserializer() {
         this.defaultContext = TLauncher.getGson();
      }

      public void setVariables(Map vars) {
         this.variables = (Map)(vars == null ? new HashMap() : vars);
         this.forgePrefix = this.variables.containsKey("forge") ? (String)this.variables.get("forge") : "";
      }

      public List deserialize(JsonElement elem) throws JsonParseException {
         List signatureList = (List)this.defaultContext.fromJson(elem, (new TypeToken() {
         }).getType());
         Iterator var4 = signatureList.iterator();

         while(var4.hasNext()) {
            CrashSignatureContainer.CrashSignature signature = (CrashSignatureContainer.CrashSignature)var4.next();
            this.analyzeSignature(signature);
         }

         return signatureList;
      }

      private CrashSignatureContainer.CrashSignature analyzeSignature(CrashSignatureContainer.CrashSignature signature) {
         if (signature.name != null && !signature.name.isEmpty()) {
            String pattern;
            Entry en;
            Iterator var4;
            String varName;
            String varVal;
            if (signature.version != null) {
               pattern = signature.version;

               for(var4 = this.variables.entrySet().iterator(); var4.hasNext(); pattern = pattern.replace("${" + varName + "}", varVal)) {
                  en = (Entry)var4.next();
                  varName = (String)en.getKey();
                  varVal = (String)en.getValue();
               }

               signature.versionPattern = Pattern.compile(pattern);
            }

            if (signature.pattern != null) {
               pattern = signature.pattern;

               for(var4 = this.variables.entrySet().iterator(); var4.hasNext(); pattern = pattern.replace("${" + varName + "}", varVal)) {
                  en = (Entry)var4.next();
                  varName = (String)en.getKey();
                  varVal = (String)en.getValue();
               }

               if (signature.forge) {
                  pattern = this.forgePrefix + pattern;
               }

               signature.linePattern = Pattern.compile(pattern);
            }

            if (signature.versionPattern == null && signature.linePattern == null && signature.exit == 0) {
               throw new JsonParseException("Useless signature found: " + signature.name);
            } else {
               return signature;
            }
         } else {
            throw new JsonParseException("Invalid name: \"" + signature.name + "\"");
         }
      }

      // $FF: synthetic method
      CrashSignatureListSimpleDeserializer(Object x0) {
         this();
      }
   }

   static class CrashSignatureContainerDeserializer implements JsonDeserializer {
      private final CrashSignatureContainer.CrashSignatureListSimpleDeserializer listDeserializer = new CrashSignatureContainer.CrashSignatureListSimpleDeserializer();
      private final Gson defaultContext = TLauncher.getGson();

      public CrashSignatureContainer deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
         JsonObject object = element.getAsJsonObject();
         Map rawVariables = (Map)this.defaultContext.fromJson(object.get("variables"), (new TypeToken() {
         }).getType());
         LinkedHashMap variables = new LinkedHashMap();
         Iterator list = rawVariables.entrySet().iterator();

         while(list.hasNext()) {
            Entry signatures = (Entry)list.next();
            String varName = (String)signatures.getKey();
            String varVal = (String)signatures.getValue();

            String replaceName;
            String replaceVal;
            for(Iterator var12 = variables.entrySet().iterator(); var12.hasNext(); varVal = varVal.replace("${" + replaceName + "}", replaceVal)) {
               Entry en = (Entry)var12.next();
               replaceName = (String)en.getKey();
               replaceVal = (String)en.getValue();
            }

            variables.put(varName, varVal);
         }

         this.listDeserializer.setVariables(variables);
         List signatures1 = this.listDeserializer.deserialize(object.get("signatures"));
         CrashSignatureContainer list1 = new CrashSignatureContainer();
         list1.variables = variables;
         list1.signatures = signatures1;
         return list1;
      }
   }

   public class CrashSignature {
      private String name;
      private String version;
      private String path;
      private String pattern;
      private int exit;
      private boolean fake;
      private boolean forge;
      private Pattern versionPattern;
      private Pattern linePattern;

      public String getName() {
         return this.name;
      }

      public Pattern getVersion() {
         return this.versionPattern;
      }

      public boolean hasVersion() {
         return this.version != null;
      }

      public boolean isFake() {
         return this.fake;
      }

      public Pattern getPattern() {
         return this.linePattern;
      }

      public boolean hasPattern() {
         return this.pattern != null;
      }

      public String getPath() {
         return this.path;
      }

      public int getExitCode() {
         return this.exit;
      }

      public String toString() {
         return this.getClass().getSimpleName() + "{name='" + this.name + "', version='" + this.version + "', path='" + this.path + "', pattern='" + this.pattern + "', exitCode=" + this.exit + ", forge=" + this.forge + ", versionPattern='" + this.versionPattern + "', linePattern='" + this.linePattern + "'}";
      }
   }
}

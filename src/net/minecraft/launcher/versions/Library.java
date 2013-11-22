package net.minecraft.launcher.versions;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.launcher.OperatingSystem;
import org.apache.commons.lang3.text.StrSubstitutor;

public class Library {
   private static final String LIBRARY_DOWNLOAD_BASE = "https://s3.amazonaws.com/Minecraft.Download/libraries/";
   private static final StrSubstitutor SUBSTITUTOR = createSubstitutor();
   private String name;
   private List rules;
   private Map natives;
   private ExtractRules extract;
   private String url;
   private String exact_url;

   public Library() {
   }

   public Library(String name) {
      if (name != null && name.length() != 0) {
         this.name = name;
      } else {
         throw new IllegalArgumentException("Library name cannot be null or empty");
      }
   }

   public Library(Library library) {
      this.name = library.name;
      this.url = library.url;
      if (library.extract != null) {
         this.extract = new ExtractRules(library.extract);
      }

      Iterator var3;
      if (library.rules != null) {
         this.rules = new ArrayList();
         var3 = library.rules.iterator();

         while(var3.hasNext()) {
            Rule rule = (Rule)var3.next();
            this.rules.add(new Rule(rule));
         }
      }

      if (library.natives != null) {
         this.natives = new LinkedHashMap();
         var3 = library.getNatives().entrySet().iterator();

         while(var3.hasNext()) {
            Entry entry = (Entry)var3.next();
            this.natives.put((OperatingSystem)entry.getKey(), (String)entry.getValue());
         }
      }

   }

   public String getName() {
      return this.name;
   }

   public Library addNative(OperatingSystem operatingSystem, String name) {
      if (operatingSystem != null && operatingSystem.isSupported()) {
         if (name != null && name.length() != 0) {
            if (this.natives == null) {
               this.natives = new EnumMap(OperatingSystem.class);
            }

            this.natives.put(operatingSystem, name);
            return this;
         } else {
            throw new IllegalArgumentException("Cannot add native for null or empty name");
         }
      } else {
         throw new IllegalArgumentException("Cannot add native for unsupported OS");
      }
   }

   public List getRules() {
      return this.rules;
   }

   public boolean appliesToCurrentEnvironment() {
      if (this.rules == null) {
         return true;
      } else {
         Rule.Action lastAction = Rule.Action.DISALLOW;
         Iterator var3 = this.rules.iterator();

         while(var3.hasNext()) {
            Rule rule = (Rule)var3.next();
            Rule.Action action = rule.getAppliedAction();
            if (action != null) {
               lastAction = action;
            }
         }

         if (lastAction == Rule.Action.ALLOW) {
            return true;
         } else {
            return false;
         }
      }
   }

   public Map getNatives() {
      return this.natives;
   }

   public ExtractRules getExtractRules() {
      return this.extract;
   }

   public Library setExtractRules(ExtractRules rules) {
      this.extract = rules;
      return this;
   }

   public String getArtifactBaseDir() {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact dir of empty/blank artifact");
      } else {
         String[] parts = this.name.split(":", 3);
         return String.format("%s/%s/%s", parts[0].replaceAll("\\.", "/"), parts[1], parts[2]);
      }
   }

   public String getArtifactPath() {
      return this.getArtifactPath((String)null);
   }

   public String getArtifactPath(String classifier) {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
      } else {
         return String.format("%s/%s", this.getArtifactBaseDir(), this.getArtifactFilename(classifier));
      }
   }

   public String getArtifactFilename(String classifier) {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
      } else {
         String[] parts = this.name.split(":", 3);
         String result;
         if (classifier == null) {
            result = String.format("%s-%s.jar", parts[1], parts[2]);
         } else {
            result = String.format("%s-%s%s.jar", parts[1], parts[2], "-" + classifier);
         }

         return SUBSTITUTOR.replace(result);
      }
   }

   public String toString() {
      return "Library{name='" + this.name + '\'' + ", rules=" + this.rules + ", natives=" + this.natives + ", extract=" + this.extract + '}';
   }

   public boolean hasCustomUrl() {
      return this.url != null;
   }

   public boolean hasExactUrl() {
      return this.exact_url != null;
   }

   public String getExactDownloadUrl() {
      return this.exact_url;
   }

   public String getDownloadUrl() {
      return this.url != null ? this.url : "https://s3.amazonaws.com/Minecraft.Download/libraries/";
   }

   private static StrSubstitutor createSubstitutor() {
      HashMap map = new HashMap();
      OperatingSystem os = OperatingSystem.getCurrentPlatform();
      map.put("arch", os.getArch());
      return new StrSubstitutor(map);
   }
}
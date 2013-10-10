package net.minecraft.launcher_.versions;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.launcher_.OperatingSystem;

public class Library {
   private static final String LIBRARY_DOWNLOAD_BASE = "https://s3.amazonaws.com/Minecraft.Download/libraries/";
   private String name;
   private List rules;
   private Map natives;
   private ExtractRules extract;
   private String url;

   public Library() {
   }

   public Library(String name) {
      if (name != null && name.length() != 0) {
         this.name = name;
      } else {
         throw new IllegalArgumentException("Library name cannot be null or empty");
      }
   }

   public Library(String name, String url) {
      if (name != null && name.length() != 0) {
         if (url != null && url.length() != 0) {
            this.name = name;
            this.url = url;
         } else {
            throw new IllegalArgumentException("Library url cannot be null or empty");
         }
      } else {
         throw new IllegalArgumentException("Library name cannot be null or empty");
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
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
      } else {
         return String.format("%s/%s", this.getArtifactBaseDir(), this.getArtifactFilename());
      }
   }

   public String getArtifactPath(String classifier) {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact path of empty/blank artifact");
      } else {
         return String.format("%s/%s", this.getArtifactBaseDir(), this.getArtifactFilename(classifier));
      }
   }

   public String getArtifactFilename() {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
      } else {
         String[] parts = this.name.split(":", 3);
         return String.format("%s-%s.jar", parts[1], parts[2]);
      }
   }

   public String getArtifactFilename(String classifier) {
      if (this.name == null) {
         throw new IllegalStateException("Cannot get artifact filename of empty/blank artifact");
      } else {
         String[] parts = this.name.split(":", 3);
         return String.format("%s-%s-%s.jar", parts[1], parts[2], classifier);
      }
   }

   public String toString() {
      return "Library{name='" + this.name + '\'' + ", rules=" + this.rules + ", natives=" + this.natives + ", extract=" + this.extract + '}';
   }

   public boolean hasCustomUrl() {
      return this.url != null;
   }

   public String getDownloadUrl() {
      return this.url != null ? this.url : "https://s3.amazonaws.com/Minecraft.Download/libraries/";
   }
}

package net.minecraft.launcher.versions;

import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.repository.Repository;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.launcher.OperatingSystem;
import org.apache.commons.lang3.text.StrSubstitutor;

public class Library {
   private static final StrSubstitutor SUBSTITUTOR;
   private String name;
   private List rules;
   private Map natives;
   private ExtractRules extract;
   private String url;
   private String exact_url;

   static {
      HashMap map = new HashMap();
      OperatingSystem os = OperatingSystem.getCurrentPlatform();
      map.put("platform", os.getName());
      map.put("arch", os.getArch());
      SUBSTITUTOR = new StrSubstitutor(map);
   }

   public String getName() {
      return this.name;
   }

   public List getRules() {
      return Collections.unmodifiableList(this.rules);
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

   String getArtifactBaseDir() {
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

   String getArtifactFilename(String classifier) {
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

   public Downloadable getDownloadable(Repository versionSource, File file, OperatingSystem os) {
      if (this.exact_url != null) {
         return new Downloadable(this.exact_url, file);
      } else {
         String nativePath = this.natives != null && this.appliesToCurrentEnvironment() ? (String)this.natives.get(os) : null;
         String path = this.getArtifactPath(nativePath);
         Repository repo;
         if (this.url == null) {
            repo = Repository.LIBRARY_REPO;
         } else {
            repo = this.url.startsWith("/") ? versionSource : null;
            path = this.url.substring(1) + path;
         }

         return repo == null ? new Downloadable(path, file) : new Downloadable(repo, path, file);
      }
   }
}

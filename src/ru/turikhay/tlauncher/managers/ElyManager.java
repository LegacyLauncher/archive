package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.PatternTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

public class ElyManager extends InterruptibleComponent {
   private static final double VERSION = 1.0D;
   private final Gson gson = (new GsonBuilder()).registerTypeAdapter(Pattern.class, new PatternTypeAdapter()).create();
   private List authlib = nl();
   private List asm = nl();
   private List total = nl();
   private final List listeners = Collections.synchronizedList(new ArrayList());
   private boolean refreshAllowed;

   public ElyManager(ComponentManager manager) throws Exception {
      super(manager);
   }

   public List getLibraries(String version) {
      ArrayList libList = new ArrayList();
      Iterator var4 = this.total.iterator();

      while(var4.hasNext()) {
         ElyManager.ElyLib library = (ElyManager.ElyLib)var4.next();
         if (library.getSupportedList().contains(version)) {
            libList.add(library);
         }
      }

      return libList;
   }

   public boolean hasLibraries(String version) {
      Iterator var3 = this.total.iterator();

      while(var3.hasNext()) {
         ElyManager.ElyLib library = (ElyManager.ElyLib)var3.next();
         if (library.getSupportedList().contains(version)) {
            return true;
         }
      }

      return false;
   }

   public CompleteVersion elyficate(CompleteVersion original) {
      this.log(new Object[]{"Trying to elyficate version:", original.getID()});
      List libList = this.getLibraries(original.getID());
      if (libList.isEmpty()) {
         this.log(new Object[]{"No applicable library for this version"});
         return original;
      } else {
         CompleteVersion version = original.copyInto(new CompleteVersion());

         ElyManager.ElyLib lib;
         for(Iterator var5 = libList.iterator(); var5.hasNext(); version.getLibraries().add(lib)) {
            lib = (ElyManager.ElyLib)var5.next();
            this.log(new Object[]{"Processing Ely library:", lib.getName()});
            if (lib.getReplacementPattern() != null) {
               Pattern pattern = lib.getReplacementPattern();
               this.log(new Object[]{"This library replaces another library:", pattern});
               Iterator iter = version.getLibraries().iterator();

               while(iter.hasNext()) {
                  Library current = (Library)iter.next();
                  if (pattern.matcher(current.getName()).matches()) {
                     this.log(new Object[]{"Remove", current.getName()});
                     iter.remove();
                  }
               }
            }

            if (StringUtils.isNotBlank(lib.getArgs())) {
               String args = version.getMinecraftArguments();
               if (StringUtils.isBlank(args)) {
                  args = lib.getArgs();
               } else {
                  args = args + ' ' + lib.getArgs();
               }

               version.setMinecraftArguments(args);
            }

            if (StringUtils.isNotBlank(lib.getMainClass())) {
               version.setMainClass(lib.getMainClass());
            }

            if (lib.getRequirementList() != null) {
               List add = new ArrayList(lib.getRequirementList());
               Iterator var14 = add.iterator();

               while(var14.hasNext()) {
                  Library current = (Library)var14.next();
                  Iterator it = version.getLibraries().iterator();

                  while(it.hasNext()) {
                     Library compare = (Library)it.next();
                     if (current.getPlainName().equals(compare.getPlainName())) {
                        this.log(new Object[]{"Version library list already contains:", compare.getName()});
                        it.remove();
                     }
                  }
               }

               version.getLibraries().addAll(add);
            }
         }

         return version;
      }
   }

   public boolean hasLibraries(VersionSyncInfo version) {
      return version.isInstalled() && this.hasLibraries(version.getLocal()) || version.hasRemote() && this.hasLibraries(version.getRemote());
   }

   public boolean hasLibraries(Version version) {
      return this.hasLibraries(version.getID());
   }

   public boolean refreshComponent() {
      return true;
   }

   protected boolean refresh(int refreshID) {
      if (!this.refreshAllowed) {
         this.log(new Object[]{"Refresh is not allowed."});
         this.clearAll();
         return false;
      } else {
         this.log(new Object[]{"Refreshing Ely..."});

         try {
            this.refreshDirectly();
         } catch (Exception var3) {
            this.log(new Object[]{"Failed to refresh Ely", var3});
            return false;
         }

         this.log(new Object[]{"Refreshed successfully!"});
         return true;
      }
   }

   private void refreshDirectly() throws Exception {
      String content = Repository.EXTRA_VERSION_REPO.getUrl("libraries/by/ely/libraries.json");
      ElyManager.RawResponse response = (ElyManager.RawResponse)this.gson.fromJson(content, ElyManager.RawResponse.class);
      if (response.version != 1.0D) {
         throw new RuntimeException("incompatible ely summary info version. required: " + response.version + "; have: " + 1.0D);
      } else if (response.authlib == null) {
         throw new NullPointerException("no authlib");
      } else if (response.asm == null) {
         throw new NullPointerException("no asm");
      } else {
         synchronized(this.total) {
            synchronized(this.authlib) {
               this.authlib.clear();
               this.authlib.addAll(response.authlib);
            }

            synchronized(this.asm) {
               this.asm.clear();
               this.asm.addAll(response.asm);
            }

            this.total.clear();
            this.total.addAll(this.authlib);
            this.total.addAll(this.asm);
         }

         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            ElyManagerListener listener = (ElyManagerListener)var4.next();
            listener.onElyUpdated(this);
         }

      }
   }

   private void clearAll() {
      synchronized(this.total) {
         this.authlib.clear();
         this.asm.clear();
         this.total.clear();
      }
   }

   public void addListener(ElyManagerListener listener) {
      this.listeners.add(listener);
   }

   private static List nl() {
      return Collections.synchronizedList(new ArrayList());
   }

   public boolean getRefreshAllowed() {
      return this.refreshAllowed;
   }

   public void setRefreshAllowed(boolean allowed) {
      this.log(new Object[]{"Refresh allowed:", allowed});
      this.refreshAllowed = true;
   }

   public static class ElyLib extends Library {
      private Pattern replaces;
      private String args;
      private String mainClass;
      private List requires = new ArrayList();
      private List supports = new ArrayList();

      public ElyLib() {
         this.url = "/libraries/";
      }

      public Pattern getReplacementPattern() {
         return this.replaces;
      }

      public String getArgs() {
         return this.args;
      }

      public String getMainClass() {
         return this.mainClass;
      }

      public List getRequirementList() {
         return this.requires;
      }

      public List getSupportedList() {
         return this.supports;
      }

      public Downloadable getDownloadable(Repository versionSource, File file, OS os) {
         U.log("getting downloadable", this.getName(), versionSource, file, os);
         return super.getDownloadable(Repository.EXTRA_VERSION_REPO, file, os);
      }

      public String toString() {
         return "ElyLib{name='" + this.name + '\'' + ", replaces='" + this.replaces + "', args='" + this.args + "', requires=" + this.requires + ", supports=" + this.supports + "}";
      }
   }

   private static class RawResponse {
      private double version;
      private List authlib;
      private List asm;
   }
}

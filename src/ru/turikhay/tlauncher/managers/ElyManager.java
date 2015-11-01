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

public class ElyManager extends InterruptibleComponent {
   private static final double VERSION = 1.0D;
   private final Gson gson = (new GsonBuilder()).registerTypeAdapter(Pattern.class, new PatternTypeAdapter()).create();
   private List authlib = nl();
   private List asm = nl();
   private List total = nl();
   private boolean refreshedOnce;
   private final List listeners = Collections.synchronizedList(new ArrayList());

   public ElyManager(ComponentManager manager) throws Exception {
      super(manager);
   }

   public boolean hasLibraries(VersionSyncInfo version) {
      return version.isInstalled() && this.hasLibraries(version.getLocal()) || version.hasRemote() && this.hasLibraries(version.getRemote());
   }

   public boolean hasLibraries(Version version) {
      if (this.hasLibraries(version.getID())) {
         return true;
      } else if (!(version instanceof CompleteVersion)) {
         return false;
      } else {
         CompleteVersion complete = (CompleteVersion)version;
         Iterator var4 = complete.getLibraries().iterator();

         while(var4.hasNext()) {
            Library library = (Library)var4.next();
            Iterator var6 = this.total.iterator();

            while(var6.hasNext()) {
               ElyManager.ElyLib elyLib = (ElyManager.ElyLib)var6.next();
               if (elyLib.replaces(library)) {
                  return true;
               }
            }
         }

         return false;
      }
   }

   public boolean hasLibraries(String version) {
      Iterator var3 = this.total.iterator();

      ElyManager.ElyLib library;
      do {
         if (!var3.hasNext()) {
            return false;
         }

         library = (ElyManager.ElyLib)var3.next();
      } while(!library.getSupportedList().contains(version));

      return true;
   }

   public List getLibraries(CompleteVersion complete) {
      String id = complete.getID();
      ArrayList libList = new ArrayList();
      Iterator var5 = this.total.iterator();

      while(true) {
         while(var5.hasNext()) {
            ElyManager.ElyLib elyLib = (ElyManager.ElyLib)var5.next();
            if (elyLib.supports(id)) {
               libList.add(elyLib);
            } else {
               Iterator var7 = complete.getLibraries().iterator();

               while(var7.hasNext()) {
                  Library library = (Library)var7.next();
                  if (elyLib.replaces(library)) {
                     libList.add(elyLib);
                  }
               }
            }
         }

         return libList;
      }
   }

   public CompleteVersion elyficate(CompleteVersion original) {
      this.log(new Object[]{"Trying to elyficate version:", original.getID()});
      List libList = this.getLibraries(original);
      CompleteVersion complete = original.copyInto(new CompleteVersion());

      ElyManager.ElyLib lib;
      for(Iterator var5 = libList.iterator(); var5.hasNext(); complete.getLibraries().add(lib)) {
         lib = (ElyManager.ElyLib)var5.next();
         this.log(new Object[]{"Processing Ely library:", lib.getName()});
         Iterator current3;
         Library current2;
         if (lib.getPattern() != null) {
            Pattern add = lib.getPattern();
            current3 = complete.getLibraries().iterator();

            while(current3.hasNext()) {
               current2 = (Library)current3.next();
               if (add.matcher(current2.getName()).matches()) {
                  this.log(new Object[]{"Remove", current2.getName()});
                  current3.remove();
               }
            }
         }

         if (StringUtils.isNotBlank(lib.getArgs())) {
            String add1 = complete.getMinecraftArguments();
            if (StringUtils.isBlank(add1)) {
               add1 = lib.getArgs();
            } else {
               add1 = add1 + ' ' + lib.getArgs();
            }

            complete.setMinecraftArguments(add1);
         }

         if (StringUtils.isNotBlank(lib.getMainClass())) {
            complete.setMainClass(lib.getMainClass());
         }

         if (lib.getRequirementList() != null) {
            ArrayList add2 = new ArrayList(lib.getRequirementList());
            current3 = add2.iterator();

            while(current3.hasNext()) {
               current2 = (Library)current3.next();
               Iterator it = complete.getLibraries().iterator();

               while(it.hasNext()) {
                  Library compare = (Library)it.next();
                  if (current2.getPlainName().equals(compare.getPlainName())) {
                     this.log(new Object[]{"Version library list already contains:", compare.getName()});
                     it.remove();
                  }
               }
            }

            complete.getLibraries().addAll(add2);
         }
      }

      return complete;
   }

   public boolean refreshComponent() {
      return true;
   }

   protected boolean refresh(int refreshID) {
      this.log(new Object[]{"Refreshing Ely..."});
      Iterator var3 = this.listeners.iterator();

      while(var3.hasNext()) {
         ElyManagerListener e = (ElyManagerListener)var3.next();
         e.onElyUpdating(this);
      }

      boolean var10 = false;

      ElyManagerListener listener;
      Iterator var5;
      label114: {
         try {
            var10 = true;
            this.refreshDirectly();
            var10 = false;
            break label114;
         } catch (Exception var11) {
            this.log(new Object[]{"Failed to refresh Ely", var11});
            var10 = false;
         } finally {
            if (var10) {
               Iterator var5 = this.listeners.iterator();

               while(var5.hasNext()) {
                  ElyManagerListener listener = (ElyManagerListener)var5.next();
                  listener.onElyUpdated(this);
               }

            }
         }

         var5 = this.listeners.iterator();

         while(var5.hasNext()) {
            listener = (ElyManagerListener)var5.next();
            listener.onElyUpdated(this);
         }

         return false;
      }

      var5 = this.listeners.iterator();

      while(var5.hasNext()) {
         listener = (ElyManagerListener)var5.next();
         listener.onElyUpdated(this);
      }

      this.log(new Object[]{"Refreshed successfully!"});
      return true;
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
         List var3 = this.total;
         synchronized(this.total) {
            List var4 = this.authlib;
            synchronized(this.authlib) {
               this.authlib.clear();
               this.authlib.addAll(response.authlib);
            }

            var4 = this.asm;
            synchronized(this.asm) {
               this.asm.clear();
               this.asm.addAll(response.asm);
            }

            this.total.clear();
            this.total.addAll(this.authlib);
            this.total.addAll(this.asm);
         }

         this.refreshedOnce = true;
      }
   }

   public void refreshOnce() {
      if (!this.refreshedOnce) {
         this.asyncRefresh();
      }

   }

   private void clearAll() {
      List var1 = this.total;
      synchronized(this.total) {
         this.authlib.clear();
         this.asm.clear();
         this.total.clear();
      }
   }

   public void addListener(ElyManagerListener listener) {
      this.listeners.add(listener);
   }

   void removeListener(ElyManagerListener listener) {
      this.listeners.remove(listener);
   }

   private static List nl() {
      return Collections.synchronizedList(new ArrayList());
   }

   private static class RawResponse {
      private double version;
      private List authlib;
      private List asm;
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

      public Pattern getPattern() {
         return this.replaces;
      }

      public boolean replaces(Library lib) {
         return this.replaces != null && this.replaces.matcher(lib.getName()).matches();
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

      public boolean supports(String version) {
         return this.supports != null && this.supports.contains(version);
      }

      public Downloadable getDownloadable(Repository versionSource, File file, OS os) {
         return super.getDownloadable(Repository.EXTRA_VERSION_REPO, file, os);
      }

      public String toString() {
         return "ElyLib{name='" + this.name + '\'' + ", replaces='" + this.replaces + "', args='" + this.args + "', requires=" + this.requires + ", supports=" + this.supports + "}";
      }
   }
}

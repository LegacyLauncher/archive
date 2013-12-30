package com.turikhay.tlauncher.minecraft.profiles;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.events.ProfileListener;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ProfileLoader {
   public static final File DEFAULT_PROFILE_STORAGE = MinecraftUtil.getSystemRelatedFile("tlauncher.profiles.list");
   public static final String DEFAULT_PROFILE_FILENAME = "launcher_profiles.json";
   private final List managers;
   private final List listeners;
   private ProfileManager selected;
   private File storageFile;

   public ProfileLoader() {
      this.managers = Collections.synchronizedList(new ArrayList());
      this.listeners = Collections.synchronizedList(new ArrayList());
   }

   public ProfileLoader(File storage) throws IOException {
      this();
      this.createInto(storage);
   }

   public ProfileLoader(int select, File... managerFiles) {
      this();
      this.createFrom(select, managerFiles);
   }

   public void createFrom() {
      this.createFrom(0, new File(MinecraftUtil.getWorkingDirectory(), "launcher_profiles.json"));
   }

   public void createFrom(int select, File... managerFiles) {
      this.managers.clear();
      File[] var6 = managerFiles;
      int var5 = managerFiles.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         File manager = var6[var4];
         this.managers.add(new ProfileManager(this, TLauncher.getClientToken(), manager, true));
      }

      this.selected = (ProfileManager)this.managers.get(select);
      this.onRefresh(this.selected);
   }

   public void createInto() throws IOException {
      this.createInto(DEFAULT_PROFILE_STORAGE);
   }

   public void createInto(File storage) throws IOException {
      this.log("Creating ProfileLoader from file:", storage);
      if (storage == null) {
         throw new NullPointerException("File is NULL!");
      } else {
         this.storageFile = storage;
         if (FileUtil.createFile(storage)) {
            this.log("File created!");
         }

         String content = FileUtil.readFile(storage);
         if (content == null || content.length() == 0) {
            content = this.writeDefault();
         }

         List files = new ArrayList();
         String[] lines = content.split("\n");
         int select = -1;

         int i;
         for(i = 0; i < lines.length; ++i) {
            String line = lines[i];
            boolean selected = false;
            if (line.startsWith(">")) {
               selected = true;
               line = line.substring(1);
            }

            if (line.trim().isEmpty()) {
               this.log("Empty line:", i + 1);
            } else {
               File dir = new File(line);
               if (dir.isDirectory()) {
                  this.log("Found directory:", dir);
                  files.add(new File(dir, "launcher_profiles.json"));
                  if (selected) {
                     select = i;
                  }
               }
            }
         }

         for(i = 0; i < files.size(); ++i) {
            this.managers.add(new ProfileManager(this, TLauncher.getClientToken(), (File)files.get(i), true));
         }

         this.selected = this.managers.size() > 0 ? (ProfileManager)this.managers.get(select == -1 ? 0 : select) : null;
         this.onRefresh(this.selected);
      }
   }

   public ProfileManager getSelected() {
      return this.selected;
   }

   public boolean setSelected(ProfileManager pm) {
      Iterator var3 = this.managers.iterator();

      while(var3.hasNext()) {
         ProfileManager cpm = (ProfileManager)var3.next();
         if (cpm.equals(pm)) {
            this.selected = pm;
            return true;
         }
      }

      return false;
   }

   public List getManagers() {
      return Collections.unmodifiableList(this.managers);
   }

   public void add(ProfileManager... pms) {
      ProfileManager[] var5 = pms;
      int var4 = pms.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         ProfileManager pm = var5[var3];
         if (this.managers.contains(pm)) {
            throw new IllegalArgumentException("ProfileLoader already contains specified manager: " + pm);
         }

         this.managers.add(pm);
      }

   }

   public void remove(ProfileManager... pms) {
      ProfileManager[] var5 = pms;
      int var4 = pms.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         ProfileManager pm = var5[var3];
         if (!this.managers.contains(pm)) {
            throw new IllegalArgumentException("ProfileLoader does not contain specified manager: " + pm);
         }

         this.managers.add(pm);
      }

   }

   public void removeAll() {
      this.managers.clear();
   }

   public void addListener(ProfileListener l) {
      this.listeners.add(l);
   }

   public void removeListener(ProfileListener l) {
      this.listeners.remove(l);
   }

   public void save() throws IOException {
      if (this.storageFile == null) {
         throw new IllegalStateException("Storage file is not set!");
      } else {
         StringBuilder s = new StringBuilder();
         boolean first = true;
         synchronized(this.managers) {
            Iterator var5 = this.managers.iterator();

            while(true) {
               if (!var5.hasNext()) {
                  break;
               }

               ProfileManager pm = (ProfileManager)var5.next();
               if (!first) {
                  s.append("\n");
               } else {
                  first = false;
               }

               if (pm.equals(this.selected)) {
                  s.append(">");
               }

               s.append(pm.getFile().getAbsolutePath());
            }
         }

         FileUtil.writeFile(this.storageFile, s.toString());
      }
   }

   public void loadProfiles() throws IOException {
      synchronized(this.managers) {
         Iterator var3 = this.managers.iterator();

         while(var3.hasNext()) {
            ProfileManager pm = (ProfileManager)var3.next();
            pm.loadProfiles();
         }

      }
   }

   public void saveProfiles() throws IOException {
      synchronized(this.managers) {
         Iterator var3 = this.managers.iterator();

         while(var3.hasNext()) {
            ProfileManager pm = (ProfileManager)var3.next();
            pm.saveProfiles();
         }

      }
   }

   private String writeDefault() throws IOException {
      String path = MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath();
      FileUtil.writeFile(this.storageFile, ">" + path);
      return path;
   }

   void onRefresh(ProfileManager pm) {
      synchronized(this.listeners) {
         Iterator var4 = this.listeners.iterator();

         while(var4.hasNext()) {
            ProfileListener pl = (ProfileListener)var4.next();
            pl.onProfilesRefreshed(pm);
         }

      }
   }

   private void log(Object... o) {
      U.log("[ProfileLoader]", o);
   }
}

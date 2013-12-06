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
   private final TLauncher t;
   private File storageFile;
   private List managers;
   private final List listeners;
   private ProfileManager selected;

   public ProfileLoader(TLauncher t, File storage) throws IOException {
      this.managers = new ArrayList();
      this.listeners = Collections.synchronizedList(new ArrayList());
      this.t = t;
      this.create(storage);
   }

   public ProfileLoader(TLauncher t) throws IOException {
      this(t, DEFAULT_PROFILE_STORAGE);
   }

   private void create(File storage) throws IOException {
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
            this.managers.add(new ProfileManager(this, this.t.getClientToken(), (File)files.get(i), true));
         }

         this.selected = this.managers.size() > 0 ? (ProfileManager)this.managers.get(select == -1 ? 0 : select) : null;
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
      StringBuilder s = new StringBuilder();
      boolean first = true;

      ProfileManager pm;
      for(Iterator var4 = this.managers.iterator(); var4.hasNext(); s.append(pm.getFile().getAbsolutePath())) {
         pm = (ProfileManager)var4.next();
         if (!first) {
            s.append("\n");
         } else {
            first = false;
         }

         if (pm.equals(this.selected)) {
            s.append(">");
         }
      }

      FileUtil.writeFile(this.storageFile, s.toString());
   }

   public void loadProfiles() throws IOException {
      Iterator var2 = this.managers.iterator();

      while(var2.hasNext()) {
         ProfileManager pm = (ProfileManager)var2.next();
         pm.loadProfiles();
      }

   }

   public void saveProfiles() throws IOException {
      Iterator var2 = this.managers.iterator();

      while(var2.hasNext()) {
         ProfileManager pm = (ProfileManager)var2.next();
         pm.saveProfiles();
      }

   }

   private String writeDefault() throws IOException {
      String path = MinecraftUtil.getDefaultWorkingDirectory().getAbsolutePath();
      FileUtil.writeFile(this.storageFile, ">" + path);
      return path;
   }

   void onRefresh(ProfileManager pm) {
      List listeners = new ArrayList(this.listeners);
      Iterator iterator = listeners.iterator();

      while(iterator.hasNext()) {
         ProfileListener listener = (ProfileListener)iterator.next();
         listener.onProfilesRefreshed(pm);
         iterator.remove();
      }

   }

   private void log(Object... o) {
      U.log("[ProfileLoader]", o);
   }
}

package com.turikhay.tlauncher.minecraft.profiles;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.turikhay.tlauncher.minecraft.auth.AuthenticationDatabase;
import com.turikhay.util.FileUtil;
import com.turikhay.util.U;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.launcher.updater.versions.json.DateTypeAdapter;
import net.minecraft.launcher.updater.versions.json.FileTypeAdapter;
import net.minecraft.launcher.updater.versions.json.LowerCaseEnumTypeAdapterFactory;

public class ProfileManager {
   public static final String DEFAULT_PROFILE_NAME = "TLauncher";
   private final JsonParser parser;
   private final Gson gson;
   private final Map profiles;
   private final File profileFile;
   private final ProfileLoader loader;
   private String selectedProfile;
   private UUID clientToken;
   private AuthenticationDatabase authDatabase;

   public ProfileManager(ProfileLoader loader, UUID clientToken, File file) {
      this(loader, clientToken, file, false);
   }

   public ProfileManager(ProfileLoader loader, UUID clientToken, File file, boolean load) {
      this.parser = new JsonParser();
      this.profiles = new HashMap();
      this.loader = loader;
      this.profileFile = file;
      this.clientToken = clientToken;
      GsonBuilder builder = new GsonBuilder();
      builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
      builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
      builder.registerTypeAdapter(File.class, new FileTypeAdapter());
      builder.registerTypeAdapter(AuthenticationDatabase.class, new AuthenticationDatabase.Serializer());
      builder.setPrettyPrinting();
      this.gson = builder.create();
      this.authDatabase = new AuthenticationDatabase();
      if (load) {
         try {
            this.loadProfiles();
            this.saveProfiles();
         } catch (Throwable var7) {
            U.log("Cannot load profiles! Ignoring, though.", var7);
         }
      }

   }

   public void saveProfiles() throws IOException {
      ProfileManager.RawProfileList rawProfileList = new ProfileManager.RawProfileList();
      rawProfileList.profiles = this.profiles;
      rawProfileList.selectedProfile = this.getSelectedProfile().getName();
      rawProfileList.clientToken = this.clientToken;
      rawProfileList.authenticationDatabase = this.authDatabase;
      FileUtil.writeFile(this.profileFile, this.gson.toJson((Object)rawProfileList));
   }

   public boolean loadProfiles() throws IOException {
      this.profiles.clear();
      this.selectedProfile = null;
      if (this.profileFile.isFile()) {
         JsonObject object = this.parser.parse(FileUtil.readFile(this.profileFile)).getAsJsonObject();
         if (object.has("clientToken")) {
            this.clientToken = (UUID)this.gson.fromJson(object.get("clientToken"), UUID.class);
         }

         ProfileManager.RawProfileList rawProfileList = (ProfileManager.RawProfileList)this.gson.fromJson((JsonElement)object, (Class)ProfileManager.RawProfileList.class);
         this.profiles.putAll(rawProfileList.profiles);
         this.selectedProfile = rawProfileList.selectedProfile;
         this.authDatabase = rawProfileList.authenticationDatabase;
         this.fireRefreshEvent();
         return true;
      } else {
         this.fireRefreshEvent();
         return false;
      }
   }

   public File getFile() {
      return this.profileFile;
   }

   public ProfileLoader getLoader() {
      return this.loader;
   }

   public void fireRefreshEvent() {
      this.loader.onRefresh(this);
   }

   public Profile getSelectedProfile() {
      if (this.selectedProfile == null || !this.profiles.containsKey(this.selectedProfile)) {
         if (this.profiles.get("TLauncher") != null) {
            this.selectedProfile = "TLauncher";
         } else if (this.profiles.size() > 0) {
            this.selectedProfile = ((Profile)this.profiles.values().iterator().next()).getName();
         } else {
            this.selectedProfile = "TLauncher";
            this.profiles.put("TLauncher", new Profile(this.selectedProfile));
         }
      }

      return (Profile)this.profiles.get(this.selectedProfile);
   }

   public Map getProfiles() {
      return this.profiles;
   }

   public void addProfile(String name, Profile profile) {
      this.profiles.put(name, profile);
   }

   public void removeProfile(String name) {
      this.profiles.remove(name);
   }

   public void setSelectedProfile(String selectedProfile) {
      boolean update = !this.selectedProfile.equals(selectedProfile);
      this.selectedProfile = selectedProfile;
      if (update) {
         this.fireRefreshEvent();
      }

   }

   public AuthenticationDatabase getAuthDatabase() {
      return this.authDatabase;
   }

   public void trimAuthDatabase() {
      Set uuids = new HashSet(this.authDatabase.getknownUUIDs());
      Iterator var3 = this.profiles.values().iterator();

      while(var3.hasNext()) {
         Profile profile = (Profile)var3.next();
         uuids.remove(profile.getPlayerUUID());
      }

      var3 = uuids.iterator();

      while(var3.hasNext()) {
         String uuid = (String)var3.next();
         this.authDatabase.removeUUID(uuid);
      }

   }

   static class RawProfileList {
      Map profiles = new HashMap();
      String selectedProfile;
      UUID clientToken = UUID.randomUUID();
      AuthenticationDatabase authenticationDatabase = new AuthenticationDatabase();
   }
}

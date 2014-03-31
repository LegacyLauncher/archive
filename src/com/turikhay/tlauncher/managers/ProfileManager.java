package com.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.turikhay.tlauncher.component.RefreshableComponent;
import com.turikhay.tlauncher.minecraft.auth.AccountListener;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import com.turikhay.util.FileUtil;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.U;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.FileTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;

public class ProfileManager extends RefreshableComponent {
   public static final String DEFAULT_PROFILE_NAME = "TLauncher";
   public static final String DEFAULT_PROFILE_FILENAME = "launcher_profiles.json";
   private final List listeners;
   private final AccountListener accountListener;
   private final JsonParser parser;
   private final Gson gson;
   private final Map profiles;
   private String selectedProfile;
   private File file;
   private UUID clientToken;
   private AuthenticatorDatabase authDatabase;

   public ProfileManager(ComponentManager manager, File file) throws Exception {
      super(manager);
      this.parser = new JsonParser();
      if (file == null) {
         throw new NullPointerException();
      } else {
         this.file = file;
         this.listeners = Collections.synchronizedList(new ArrayList());
         this.profiles = new HashMap();
         this.clientToken = UUID.randomUUID();
         this.accountListener = new AccountListener() {
            public void onAccountsRefreshed(AuthenticatorDatabase db) {
               Iterator var3 = ProfileManager.this.listeners.iterator();

               while(var3.hasNext()) {
                  AccountListener listener = (AccountListener)var3.next();
                  listener.onAccountsRefreshed(db);
               }

            }
         };
         this.authDatabase = new AuthenticatorDatabase();
         this.authDatabase.setListener(this.accountListener);
         GsonBuilder builder = new GsonBuilder();
         builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
         builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
         builder.registerTypeAdapter(File.class, new FileTypeAdapter());
         builder.registerTypeAdapter(AuthenticatorDatabase.class, new AuthenticatorDatabase.Serializer());
         builder.setPrettyPrinting();
         this.gson = builder.create();
      }
   }

   public ProfileManager(ComponentManager manager) throws Exception {
      this(manager, getDefaultFile());
   }

   public void recreate() {
      this.setFile(getDefaultFile());
      this.refresh();
   }

   public boolean refresh() {
      this.loadProfiles();
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         ProfileManagerListener listener = (ProfileManagerListener)var2.next();
         listener.onProfilesRefreshed(this);
      }

      return true;
   }

   private void loadProfiles() {
      this.selectedProfile = null;
      this.profiles.clear();
      if (this.file.isFile()) {
         ProfileManager.RawProfileList raw;
         try {
            raw = (ProfileManager.RawProfileList)this.gson.fromJson((JsonElement)this.parser.parse(FileUtil.readFile(this.file)).getAsJsonObject(), (Class)ProfileManager.RawProfileList.class);
         } catch (Exception var3) {
            U.log("Cannot parse profile list! Loading an empty one.", var3);
            raw = new ProfileManager.RawProfileList();
         }

         this.clientToken = raw.clientToken;
         this.selectedProfile = raw.selectedProfile;
         this.authDatabase = raw.authenticationDatabase;
         this.authDatabase.setListener(this.accountListener);
         this.profiles.putAll(raw.profiles);
      }
   }

   public void saveProfiles() throws IOException {
      ProfileManager.RawProfileList raw = new ProfileManager.RawProfileList();
      raw.clientToken = this.clientToken;
      raw.selectedProfile = this.selectedProfile;
      raw.profiles = this.profiles;
      raw.authenticationDatabase = this.authDatabase;
      FileUtil.writeFile(this.file, this.gson.toJson((Object)raw));
   }

   public AuthenticatorDatabase getAuthDatabase() {
      return this.authDatabase;
   }

   public File getFile() {
      return this.file;
   }

   public void setFile(File file) {
      if (file == null) {
         throw new NullPointerException();
      } else {
         this.file = file;
         Iterator var3 = this.listeners.iterator();

         while(var3.hasNext()) {
            ProfileManagerListener listener = (ProfileManagerListener)var3.next();
            listener.onProfileManagerChanged(this);
         }

      }
   }

   public UUID getClientToken() {
      return this.clientToken;
   }

   public void setClientToken(String uuid) {
      this.clientToken = UUID.fromString(uuid);
   }

   public void addListener(ProfileManagerListener listener) {
      if (listener == null) {
         throw new NullPointerException();
      } else {
         if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
         }

      }
   }

   private static File getDefaultFile() {
      return new File(MinecraftUtil.getWorkingDirectory(), "launcher_profiles.json");
   }

   static class RawProfileList {
      Map profiles = new HashMap();
      String selectedProfile;
      UUID clientToken = UUID.randomUUID();
      AuthenticatorDatabase authenticationDatabase = new AuthenticatorDatabase();
   }
}

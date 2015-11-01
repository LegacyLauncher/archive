package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.FileTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.component.RefreshableComponent;
import ru.turikhay.tlauncher.minecraft.auth.AccountListener;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.U;

public class ProfileManager extends RefreshableComponent {
   public static final String DEFAULT_PROFILE_NAME = "TLauncher";
   public static final String OLD_PROFILE_FILENAME = "launcher_profiles.json";
   public static final String DEFAULT_PROFILE_FILENAME = "tlauncher_profiles.json";
   private final List listeners;
   private final AccountListener accountListener;
   private final Gson gson;
   private File file;
   private UUID clientToken;
   private AuthenticatorDatabase authDatabase;

   public ProfileManager(ComponentManager manager, File file) throws Exception {
      super(manager);
      if (file == null) {
         throw new NullPointerException();
      } else {
         this.file = file;
         this.listeners = Collections.synchronizedList(new ArrayList());
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
         builder.registerTypeAdapter(UUIDTypeAdapter.class, new UUIDTypeAdapter());
         builder.setPrettyPrinting();
         this.gson = builder.create();
      }
   }

   public ProfileManager(ComponentManager manager) throws Exception {
      this(manager, getDefaultFile(manager));
   }

   public void recreate() {
      this.setFile(getDefaultFile(this.manager));
      this.refresh();
   }

   public boolean refresh() {
      this.loadProfiles();
      Iterator var2 = this.listeners.iterator();

      while(var2.hasNext()) {
         ProfileManagerListener e = (ProfileManagerListener)var2.next();
         e.onProfilesRefreshed(this);
      }

      try {
         this.saveProfiles();
         return true;
      } catch (IOException var3) {
         return false;
      }
   }

   private void loadProfiles() {
      this.log(new Object[]{"Refreshing profiles from:", this.file});
      File oldFile = new File(this.file.getParentFile(), "launcher_profiles.json");
      OutputStreamWriter writer = null;
      if (!oldFile.isFile()) {
         try {
            writer = new OutputStreamWriter(new FileOutputStream(oldFile), Charset.forName("UTF-8"));
            this.gson.toJson((Object)(new ProfileManager.OldProfileList()), (Appendable)writer);
            writer.close();
         } catch (Exception var17) {
            this.log(new Object[]{"Cannot write into", "launcher_profiles.json", var17});
         } finally {
            U.close(writer);
         }
      }

      ProfileManager.RawProfileList raw = null;
      InputStreamReader reader = null;

      try {
         reader = new InputStreamReader(new FileInputStream(this.file.isFile() ? this.file : oldFile), Charset.forName("UTF-8"));
         raw = (ProfileManager.RawProfileList)this.gson.fromJson((Reader)reader, (Class)ProfileManager.RawProfileList.class);
      } catch (Exception var15) {
         this.log(new Object[]{"Cannot read from", "tlauncher_profiles.json", var15});
      } finally {
         U.close(reader);
      }

      if (raw == null) {
         raw = new ProfileManager.RawProfileList();
      }

      this.clientToken = raw.clientToken;
      this.authDatabase = raw.authenticationDatabase;
      this.authDatabase.setListener(this.accountListener);
   }

   public void saveProfiles() throws IOException {
      ProfileManager.RawProfileList raw = new ProfileManager.RawProfileList();
      raw.clientToken = this.clientToken;
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

   private static File getDefaultFile(ComponentManager manager) {
      String profileFile = manager.getLauncher().getSettings().get("profiles");
      return StringUtils.isNotEmpty(profileFile) ? new File(profileFile) : new File(MinecraftUtil.getWorkingDirectory(), "tlauncher_profiles.json");
   }

   static class RawProfileList {
      UUID clientToken = UUID.randomUUID();
      AuthenticatorDatabase authenticationDatabase = new AuthenticatorDatabase();
   }

   static class OldProfileList {
      UUID clientToken = UUID.randomUUID();
      HashMap profiles = new HashMap();
   }
}

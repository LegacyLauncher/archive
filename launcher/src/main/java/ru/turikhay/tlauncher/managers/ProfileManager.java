package ru.turikhay.tlauncher.managers;

import com.getsentry.raven.util.Base64;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.FileTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.component.RefreshableComponent;
import ru.turikhay.tlauncher.minecraft.auth.AccountMigrator;
import ru.turikhay.tlauncher.minecraft.auth.AuthenticatorDatabase;
import ru.turikhay.tlauncher.minecraft.auth.LegacyAccount;
import ru.turikhay.tlauncher.minecraft.auth.UUIDTypeAdapter;
import ru.turikhay.tlauncher.sentry.Sentry;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.user.McleaksUser;
import ru.turikhay.tlauncher.user.User;
import ru.turikhay.tlauncher.user.UserSet;
import ru.turikhay.tlauncher.user.UserSetListener;
import ru.turikhay.util.DataBuilder;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.U;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ProfileManager extends RefreshableComponent {
    public static final String DEFAULT_PROFILE_NAME = "TLauncher";
    public static final String OLD_PROFILE_FILENAME = "launcher_profiles.json";
    public static final String DEFAULT_PROFILE_FILENAME = "tlauncher_profiles.json";

    private final AccountManager accountManager;

    private final List<ProfileManagerListener> listeners;
    private final Gson gson;
    private File file;
    private AuthenticatorDatabase authDatabase;

    public ProfileManager(ComponentManager manager, File file) throws Exception {
        super(manager);
        if (file == null) {
            throw new NullPointerException();
        } else {
            this.file = file;
            listeners = Collections.synchronizedList(new ArrayList());

            this.accountManager  = new AccountManager();
            authDatabase = new AuthenticatorDatabase(accountManager);

            accountManager.addListener(new UserSetListener() {
                @Override
                public void userSetChanged(UserSet set) {
                    for(ProfileManagerListener l : listeners) {
                        try {
                            l.onProfilesRefreshed(ProfileManager.this);
                        } catch(Exception e) {
                            Sentry.sendError(ProfileManager.class, "ProfileManagerListener error", e, null);
                            log(e);
                        }
                    }

                    try {
                        saveProfiles();
                    } catch (IOException var3) {
                        log(var3);
                    }
                }
            });

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
            builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
            builder.registerTypeAdapter(File.class, new FileTypeAdapter());
            builder.registerTypeAdapter(UserSet.class, accountManager.getTypeAdapter());
            builder.registerTypeAdapter(UUIDTypeAdapter.class, new UUIDTypeAdapter());
            builder.setPrettyPrinting();
            gson = builder.create();
        }
    }

    public ProfileManager(ComponentManager manager) throws Exception {
        this(manager, getDefaultFile(manager));
    }

    public void recreate() {
        setFile(getDefaultFile(manager));
        refresh();
    }

    public boolean refresh() {
        loadProfiles();
        Iterator var2 = listeners.iterator();

        while (var2.hasNext()) {
            ProfileManagerListener e = (ProfileManagerListener) var2.next();
            e.onProfilesRefreshed(this);
        }

        try {
            saveProfiles();
            return true;
        } catch (IOException var3) {
            Alert.showError("Could not save profiles!", var3);
            return false;
        }
    }

    private void loadProfiles() {
        log("Refreshing profiles from:", file);
        File oldFile = new File(file.getParentFile(), "launcher_profiles.json");
        OutputStreamWriter writer = null;
        if (!oldFile.isFile()) {
            try {
                writer = new OutputStreamWriter(new FileOutputStream(oldFile), Charset.forName("UTF-8"));
                gson.toJson(new OldProfileList(), writer);
                writer.close();
            } catch (Exception var17) {
                log("Cannot write into", "launcher_profiles.json", var17);
            } finally {
                U.close(writer);
            }
        }

        //ProfileManager.RawProfileList raw = null;
        InputStreamReader reader = null;
        JsonObject object = null;
        File readFile = file.isFile() ? file : oldFile;
        String saveBackup = null;

        try {
            reader = new InputStreamReader(new FileInputStream(readFile), Charset.forName("UTF-8"));
            object = gson.fromJson(reader, JsonObject.class);
        } catch (Exception var15) {
            log("Cannot read from", readFile, var15);
        } finally {
            U.close(reader);
        }

        String outputJson = gson.toJson(object);
        RawProfileList raw = new RawProfileList();

        try {
            if (object != null) {
                if (object.has("authenticationDatabase") && object.has("clientToken")) {
                    raw.userSet = accountManager.getUserSet();
                    AccountMigrator migrator = new AccountMigrator(object.get("clientToken").getAsString());
                    Map<String, LegacyAccount> accountMap = migrator.parse(object.getAsJsonObject("authenticationDatabase"));
                    for (User user : migrator.migrate(accountMap.values())) {
                        raw.userSet.add(user);
                    }
                    saveBackup = "migrated";
                } else {
                    raw.userSet = gson.fromJson(object.getAsJsonObject("userSet"), UserSet.class);
                }
            }
        } catch(Exception e) {
            log("Error parsing profile list", e);
            Sentry.sendError(ProfileManager.class, "error parsing profile list", e, DataBuilder.create("object", Base64.encodeToString(outputJson.getBytes(FileUtil.getCharset()), Base64.DEFAULT)));
            saveBackup = "errored";
        }

        if(saveBackup != null) {
            File backupFile = new File(readFile.getAbsolutePath() + "." + saveBackup + ".bak");
            try {
                FileUtil.createFile(backupFile);
                try(FileOutputStream backupOut = new FileOutputStream(backupFile)) {
                    IOUtils.write(outputJson, backupOut, FileUtil.getCharset());
                }
            } catch(Exception e) {
                log("Could not save backup!", e);
                Alert.showError("Could not save profile backup. Accounts will be lost :(", e);
            }
        }

        if(raw.userSet != null) {
            for(User user : raw.userSet.getSet()) {
                if(user.getType().equals(McleaksUser.TYPE)) {
                    McleaksManager.triggerConnection();
                }
            }
        }
        accountManager.setUserSet(raw.userSet);
    }

    public void saveProfiles() throws IOException {
        ProfileManager.RawProfileList raw = new ProfileManager.RawProfileList();
        raw.userSet = accountManager.getUserSet();
        FileUtil.writeFile(file, gson.toJson(raw));
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public AuthenticatorDatabase getAuthDatabase() {
        return authDatabase;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        if (file == null) {
            throw new NullPointerException();
        } else {
            this.file = file;
            Iterator var3 = listeners.iterator();

            while (var3.hasNext()) {
                ProfileManagerListener listener = (ProfileManagerListener) var3.next();
                listener.onProfileManagerChanged(this);
            }

        }
    }

    public void addListener(ProfileManagerListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        } else {
            if (!listeners.contains(listener)) {
                listeners.add(listener);
            }

        }
    }

    private static File getDefaultFile(ComponentManager manager) {
        String profileFile = manager.getLauncher().getSettings().get("profiles");
        return StringUtils.isNotEmpty(profileFile) ? new File(profileFile) : new File(MinecraftUtil.getWorkingDirectory(), "tlauncher_profiles.json");
    }

    static class OldProfileList {
        UUID clientToken = UUID.randomUUID();
        HashMap<Object, Object> profiles = new HashMap();
    }

    static class RawProfileList {
        UserSet userSet;
    }

    static class UnmigratedProfileList {
        UUID clientToken;
        Map<String, LegacyAccount> authenticationDatabase;
    }
}

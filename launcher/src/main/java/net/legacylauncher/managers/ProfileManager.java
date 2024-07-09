package net.legacylauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.component.RefreshableComponent;
import net.legacylauncher.minecraft.auth.AccountMigrator;
import net.legacylauncher.minecraft.auth.AuthenticatorDatabase;
import net.legacylauncher.minecraft.auth.LegacyAccount;
import net.legacylauncher.minecraft.auth.UUIDTypeAdapter;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.user.User;
import net.legacylauncher.user.UserSet;
import net.legacylauncher.util.FileUtil;
import net.legacylauncher.util.MinecraftUtil;
import net.legacylauncher.util.json.InstantAdapter;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.FileTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class ProfileManager extends RefreshableComponent {
    public static final String DEFAULT_PROFILE_FILENAME = "tlauncher_profiles.json";

    private final AccountManager accountManager;

    private final List<ProfileManagerListener> listeners;
    private final Gson gson;
    private File file;
    private final AuthenticatorDatabase authDatabase;

    public ProfileManager(ComponentManager manager, File file) {
        super(manager);
        Objects.requireNonNull(file);

        this.file = file;
        listeners = new CopyOnWriteArrayList<>();

        this.accountManager = new AccountManager();
        authDatabase = new AuthenticatorDatabase(accountManager);

        accountManager.addListener(set -> {
            for (ProfileManagerListener l : listeners) {
                try {
                    l.onProfilesRefreshed(ProfileManager.this);
                } catch (Exception e) {
                    log.warn("Caught exception on one of profile manager listeners", e);
                }
            }

            try {
                saveProfiles();
            } catch (IOException var3) {
                log.warn("Could not save profiles", var3);
            }
        });

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
        builder.registerTypeAdapter(Date.class, new DateTypeAdapter(true));
        builder.registerTypeAdapter(Instant.class, new InstantAdapter());
        builder.registerTypeAdapter(File.class, new FileTypeAdapter());
        builder.registerTypeAdapter(UserSet.class, accountManager.getTypeAdapter());
        builder.registerTypeAdapter(UUIDTypeAdapter.class, new UUIDTypeAdapter());
        builder.setPrettyPrinting();
        gson = builder.create();
    }

    public ProfileManager(ComponentManager manager) {
        this(manager, getDefaultFile(manager));
    }

    public void recreate() {
        setFile(getDefaultFile(manager));
        refresh();
    }

    public boolean refresh() {
        loadProfiles();

        for (ProfileManagerListener e : listeners) {
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
        log.debug("Refreshing profiles from: {}", file);
        File oldFile = new File(file.getParentFile(), "launcher_profiles.json");
        if (!oldFile.isFile()) {
            try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(oldFile.toPath()), StandardCharsets.UTF_8)) {
                gson.toJson(new OldProfileList(), writer);
            } catch (Exception var17) {
                log.warn("Cannot write into {}", oldFile, var17);
            }
        }

        //ProfileManager.RawProfileList raw = null;
        JsonObject object = null;
        File readFile = file.isFile() ? file : oldFile;
        String saveBackup = null;

        try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(readFile.toPath()), StandardCharsets.UTF_8)) {
            object = gson.fromJson(reader, JsonObject.class);
        } catch (Exception var15) {
            log.warn("Cannot read from {}", readFile, var15);
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
        } catch (Exception e) {
            log.error("Error parsing profile list: {}", readFile, e);
            saveBackup = "errored";
        }

        if (saveBackup != null) {
            File backupFile = new File(readFile.getAbsolutePath() + "." + saveBackup + ".bak");
            try {
                FileUtil.createFile(backupFile);
                try (FileOutputStream backupOut = new FileOutputStream(backupFile)) {
                    IOUtils.write(outputJson, backupOut, StandardCharsets.UTF_8);
                }
            } catch (Exception e) {
                log.error("Could not save backup into {}", backupFile, e);
                Alert.showError("Could not save profile backup. Accounts will be lost :(", e);
            }
        }

        accountManager.setUserSet(raw.userSet);
    }

    public void saveProfiles() throws IOException {
        ProfileManager.RawProfileList raw = new ProfileManager.RawProfileList();
        raw.userSet = accountManager.getUserSet();
        File tmpFile = new File(file.getAbsolutePath() + ".tmp");
        FileUtil.createFile(tmpFile);
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(tmpFile.toPath()), StandardCharsets.UTF_8)) {
            gson.toJson(raw, writer);
        }
        Files.move(tmpFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
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

            for (ProfileManagerListener listener : listeners) {
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
        Map<Object, Object> profiles = new HashMap<>();
    }

    static class RawProfileList {
        UserSet userSet;
    }
}

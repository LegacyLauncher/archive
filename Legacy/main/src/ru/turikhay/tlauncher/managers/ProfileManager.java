package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class ProfileManager extends RefreshableComponent {
    public static final String DEFAULT_PROFILE_NAME = "TLauncher";
    public static final String OLD_PROFILE_FILENAME = "launcher_profiles.json";
    public static final String DEFAULT_PROFILE_FILENAME = "tlauncher_profiles.json";
    private final List<ProfileManagerListener> listeners;
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
            listeners = Collections.synchronizedList(new ArrayList());
            clientToken = UUID.randomUUID();
            accountListener = new AccountListener() {
                public void onAccountsRefreshed(AuthenticatorDatabase db) {
                    Iterator var3 = listeners.iterator();

                    while (var3.hasNext()) {
                        AccountListener listener = (AccountListener) var3.next();
                        listener.onAccountsRefreshed(db);
                    }

                }
            };
            authDatabase = new AuthenticatorDatabase();
            authDatabase.setListener(accountListener);
            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory());
            builder.registerTypeAdapter(Date.class, new DateTypeAdapter());
            builder.registerTypeAdapter(File.class, new FileTypeAdapter());
            builder.registerTypeAdapter(AuthenticatorDatabase.class, new AuthenticatorDatabase.Serializer());
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

        ProfileManager.RawProfileList raw = null;
        InputStreamReader reader = null;

        try {
            reader = new InputStreamReader(new FileInputStream(file.isFile() ? file : oldFile), Charset.forName("UTF-8"));
            raw = (ProfileManager.RawProfileList) gson.fromJson(reader, (Class) ProfileManager.RawProfileList.class);
        } catch (Exception var15) {
            log("Cannot read from", "tlauncher_profiles.json", var15);
        } finally {
            U.close(reader);
        }

        if (raw == null) {
            raw = new ProfileManager.RawProfileList();
        }

        clientToken = raw.clientToken;
        authDatabase = raw.authenticationDatabase;
        authDatabase.setListener(accountListener);
    }

    public void saveProfiles() throws IOException {
        ProfileManager.RawProfileList raw = new ProfileManager.RawProfileList();
        raw.clientToken = clientToken;
        raw.authenticationDatabase = authDatabase;
        FileUtil.writeFile(file, gson.toJson(raw));
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

    public UUID getClientToken() {
        return clientToken;
    }

    public void setClientToken(String uuid) {
        clientToken = UUID.fromString(uuid);
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
        UUID clientToken = UUID.randomUUID();
        AuthenticatorDatabase authenticationDatabase = new AuthenticatorDatabase();
    }
}

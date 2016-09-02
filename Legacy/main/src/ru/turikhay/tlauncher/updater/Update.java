package ru.turikhay.tlauncher.updater;

import com.github.zafarkhaja.semver.Version;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.Bootstrapper;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.DownloadableHandler;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.json.LegacyVersionSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

public class Update {
    protected Version version;
    protected Map<String, String> description = new HashMap();
    protected Map<PackageType, String> downloads = new HashMap();
    @Expose(
            serialize = false,
            deserialize = false
    )
    protected Update.State state;
    @Expose(
            serialize = false,
            deserialize = false
    )
    protected Downloader downloader;
    @Expose(
            serialize = false,
            deserialize = false
    )
    private Downloadable download;
    @Expose(
            serialize = false,
            deserialize = false
    )
    private final List<UpdateListener> listeners;

    public Update() {
        state = Update.State.NONE;
        downloader = getDefaultDownloader();
        listeners = Collections.synchronizedList(new ArrayList());
    }

    public Update(Version version, Map<String, String> description, Map<PackageType, String> downloads) {
        state = Update.State.NONE;
        downloader = getDefaultDownloader();
        listeners = Collections.synchronizedList(new ArrayList());
        this.version = version;
        if (description != null) {
            this.description.putAll(description);
        }

        if (downloads != null) {
            this.downloads.putAll(downloads);
        }

    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public Map<String, String> getDescriptionMap() {
        return description;
    }

    public Map<PackageType, String> getDownloads() {
        return downloads;
    }

    public String getLink(PackageType packageType) {
        return downloads.get(packageType);
    }

    public String getLink() {
        return getLink(PackageType.CURRENT);
    }

    public Update.State getState() {
        return state;
    }

    protected void setState(Update.State newState) {
        if (newState.ordinal() <= state.ordinal() && state.ordinal() != Update.State.values().length - 1) {
            throw new IllegalStateException("tried to change from " + state + " to " + newState);
        } else {
            state = newState;
            log("Set state:", newState);
        }
    }

    public Downloader getDownloader() {
        return downloader;
    }

    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    public boolean isApplicable() {
        return StringUtils.isNotBlank(downloads.get(PackageType.CURRENT)) && TLauncher.getVersion().lessThan(version);
    }

    public boolean isRequired() {
        return version.greaterThan(TLauncher.getVersion()) && version.getMinorVersion() > TLauncher.getVersion().getMinorVersion();
    }

    public String getDescription(String key) {
        return description == null ? null : description.get(key);
    }

    public String getDescription() {
        return getDescription(TLauncher.getInstance().getSettings().getLocale().toString());
    }

    protected void download0(PackageType packageType, boolean async) throws Throwable {
        setState(Update.State.DOWNLOADING);

        File destination = new File(FileUtil.getRunningJar().getAbsolutePath() + ".update");
        String link = getLink(packageType);

        if (link.startsWith("/")) {
            download = new Downloadable(Repository.EXTRA_VERSION_REPO, link.substring(1), destination);
        } else {
            download = new Downloadable(new URL(link).toExternalForm(), destination);
        }

        download.setInsertUA(true);
        download.addHandler(new DownloadableHandler() {
            public void onStart(Downloadable d) {
            }

            public void onAbort(Downloadable d) {
                onUpdateDownloadError(d.getError());
            }

            public void onComplete(Downloadable d) {
                onUpdateReady();
            }

            public void onError(Downloadable d, Throwable e) {
                onUpdateDownloadError(e);
            }
        });
        downloader.add(download);
        onUpdateDownloading();
        if (async) {
            downloader.startDownload();
        } else {
            downloader.startDownloadAndWait();
        }
    }

    public void download(PackageType type, boolean async) {
        try {
            download0(type, async);
        } catch (Throwable var4) {
            onUpdateError(var4);
        }

    }

    public void download(boolean async) {
        download(PackageType.CURRENT, async);
    }

    public void download() {
        download(false);
    }

    public void asyncDownload() {
        download(true);
    }

    protected void apply0() throws Throwable {
        setState(Update.State.APPLYING);

        File replace = FileUtil.getRunningJar();
        File replaceWith = download.getDestination();
        replaceWith.deleteOnExit();

        String[] args = TLauncher.getInstance() != null ? TLauncher.getArgs() : new String[0];
        ProcessBuilder builder = Bootstrapper.createLauncher(args, false).createProcess();
        onUpdateApplying();
        FileInputStream in = new FileInputStream(replaceWith);
        FileOutputStream out = new FileOutputStream(replace);
        byte[] buffer = new byte[2048];

        for (int read = in.read(buffer); read > 0; read = in.read(buffer)) {
            out.write(buffer, 0, read);
        }

        try {
            in.close();
        } catch (IOException var12) {
        }

        try {
            out.close();
        } catch (IOException var11) {
        }

        try {
            builder.start();
        } catch (Throwable var10) {
        }

        System.exit(0);
    }

    public void apply() {
        try {
            apply0();
        } catch (Throwable var2) {
            onUpdateApplyError(var2);
        }

    }

    public void addListener(UpdateListener l) {
        listeners.add(l);
    }

    public void removeListener(UpdateListener l) {
        listeners.remove(l);
    }

    protected void onUpdateError(Throwable e) {
        setState(Update.State.ERRORED);
        List var2 = listeners;
        synchronized (listeners) {
            Iterator var4 = listeners.iterator();

            while (var4.hasNext()) {
                UpdateListener l = (UpdateListener) var4.next();
                l.onUpdateError(this, e);
            }

        }
    }

    protected void onUpdateDownloading() {
        List var1 = listeners;
        synchronized (listeners) {
            Iterator var3 = listeners.iterator();

            while (var3.hasNext()) {
                UpdateListener l = (UpdateListener) var3.next();
                l.onUpdateDownloading(this);
            }

        }
    }

    protected void onUpdateDownloadError(Throwable e) {
        setState(Update.State.ERRORED);
        List var2 = listeners;
        synchronized (listeners) {
            Iterator var4 = listeners.iterator();

            while (var4.hasNext()) {
                UpdateListener l = (UpdateListener) var4.next();
                l.onUpdateDownloadError(this, e);
            }

        }
    }

    protected void onUpdateReady() {
        setState(Update.State.READY);
        List var1 = listeners;
        synchronized (listeners) {
            Iterator var3 = listeners.iterator();

            while (var3.hasNext()) {
                UpdateListener l = (UpdateListener) var3.next();
                l.onUpdateReady(this);
            }

        }
    }

    protected void onUpdateApplying() {
        List var1 = listeners;
        synchronized (listeners) {
            Iterator var3 = listeners.iterator();

            while (var3.hasNext()) {
                UpdateListener l = (UpdateListener) var3.next();
                l.onUpdateApplying(this);
            }

        }
    }

    protected void onUpdateApplyError(Throwable e) {
        setState(Update.State.ERRORED);
        List var2 = listeners;
        synchronized (listeners) {
            Iterator var4 = listeners.iterator();

            while (var4.hasNext()) {
                UpdateListener l = (UpdateListener) var4.next();
                l.onUpdateApplyError(this, e);
            }

        }
    }

    protected Downloader getDefaultDownloader() {
        return TLauncher.getInstance().getDownloader();
    }

    protected void log(Object... o) {
        U.log("[Update:" + version + "]", o);
    }

    public String toString() {
        return "Update{version=" + version + "," + "description=" + description + "," + "downloads=" + downloads + "}";
    }

    public static class Deserializer implements JsonDeserializer<Update> {
        private final LegacyVersionSerializer versionSerializer = new LegacyVersionSerializer();

        public Update deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            try {
                return deserialize0(json, context);
            } catch (Exception e) {
                U.log("[Update] Could not parse update, backing up to default", e);
                return new Update();
            }
        }

        private Update deserialize0(JsonElement json, JsonDeserializationContext context) {
            JsonObject object = json.getAsJsonObject();
            Update update = new Update();
            update.version = versionSerializer.deserialize(object.get("version"), Version.class, context);
            Map<String, String> description = context.deserialize(object.get("description"), (new TypeToken<Map<String, String>>() {
            }).getType());

            if (description != null) {
                if (!TLauncher.getBrand().equals("Legacy") && description.containsKey("en_US")) {
                    String universalDescription = description.get("en_US");

                    for (Locale locale : TLauncher.getInstance().getLang().getLocales()) {
                        if (description.containsKey(locale.toString())) {
                            continue;
                        }
                        description.put(locale.toString(), universalDescription);
                    }
                } else {
                    if (description.containsKey("ru_RU") && !description.containsKey("uk_UA")) {
                        description.put("uk_UA", description.get("ru_RU"));
                    }
                }
                update.description.putAll(description);
            }

            Map links = context.deserialize(object.get("downloads"), (new TypeToken<Map<PackageType, String>>() {
            }).getType());
            if (links != null) {
                update.downloads.putAll(links);
            }

            return update;
        }
    }

    public enum State {
        NONE,
        DOWNLOADING,
        READY,
        APPLYING,
        ERRORED
    }
}

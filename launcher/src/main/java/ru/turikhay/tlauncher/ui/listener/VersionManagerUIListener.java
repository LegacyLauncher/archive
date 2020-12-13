package ru.turikhay.tlauncher.ui.listener;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import net.minecraft.launcher.updater.VersionFilter;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.DateTypeAdapter;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.managers.VersionManagerListener;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.MinecraftUtil;
import ru.turikhay.util.U;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;

public class VersionManagerUIListener implements VersionManagerListener {
    private static final Logger LOGGER = LogManager.getLogger(VersionManagerUIListener.class);

    private final TLauncher tl;
    private final Configuration settings;

    private final Gson gson;

    private boolean firstUpdate = true;
    private File listFile;
    private SimpleVersionList list;

    public VersionManagerUIListener(TLauncher tl) {
        this.tl = tl;
        settings = tl.getSettings();

        gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateTypeAdapter())
                .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
                .setPrettyPrinting().create();

        tl.getVersionManager().addListener(this);
    }

    @Override
    public void onVersionsRefreshing(VersionManager vm) {
    }

    @Override
    public void onVersionsRefreshingFailed(VersionManager vm) {
    }

    @Override
    public void onVersionsRefreshed(VersionManager vm) {
        listFile = new File(MinecraftUtil.getWorkingDirectory(), "versions/versions.json");

        boolean isFirstUpdate = firstUpdate;
        firstUpdate = false;

        boolean enabled = false;

        if (settings.getBoolean("minecraft.versions.sub.remote"))
            for (ReleaseType type : ReleaseType.values()) {
                enabled |= settings.getBoolean("gui.alerton." + type);
            }

        if (!enabled) {
            if (listFile.isFile()) {
                try {
                    FileUtil.deleteFile(listFile);
                } catch (Exception e) {
                    LOGGER.warn("Could not delete version index file: {}", listFile, e);
                }
            }
            return;
        }

        SimpleVersionList oldList = list == null ? fetchListFromFile() : list;
        if (oldList == null) {
            LOGGER.debug("Old list is empty, saving current one for the next time.");

            saveList(list = fetchListFromManager(vm));

            return;
        }

        SimpleVersionList newList = fetchListFromManager(vm);

        TreeMap<ReleaseType, List<SimpleVersion>> newVersions = new TreeMap<ReleaseType, List<SimpleVersion>>();

        SimpleVersion lastVersion = null;
        int i = 0;

        for (SimpleVersion version : newList.versions) {
            if (oldList.versions.contains(version)) {
                continue;
            }

            ++i;
            lastVersion = version;

            List<SimpleVersion> subVersionList;

            if (!settings.getBoolean("gui.alerton." + version.type)) {
                continue;
            }

            switch (version.type) {
                case RELEASE:
                case SNAPSHOT:
                    break;
                default:
                    version.type = ReleaseType.UNKNOWN;
            }

            if (newVersions.containsKey(version.type)) {
                subVersionList = newVersions.get(version.type);
            } else {
                subVersionList = new ArrayList<SimpleVersion>();
                newVersions.put(version.type, subVersionList);
            }

            subVersionList.add(version);
        }

        if (newVersions.isEmpty()) {
            return;
        }

        StringBuilder text = new StringBuilder(Localizable.get("version.manager.alert.header.found" + (isFirstUpdate ? ".welcome" : ""))).append(" ");

        if (i == 1) {
            text.append(Localizable.get("version.manager.alert.header.single." + lastVersion.type)).append("\n");
            add(text, lastVersion);
        } else {
            text.append(Localizable.get("version.manager.alert.header.multiple")).append("\n");

            List<SimpleVersion> unknownNew = newVersions.get(ReleaseType.UNKNOWN);

            if (newVersions.size() == 1 && unknownNew != null) {
                for (SimpleVersion version : newVersions.get(ReleaseType.UNKNOWN))
                    add(text, version);
            } else {
                for (Map.Entry<ReleaseType, List<SimpleVersion>> entry : newVersions.entrySet()) {
                    ReleaseType type = entry.getKey();
                    List<SimpleVersion> versionList = entry.getValue();

                    if (versionList.isEmpty())
                        continue;

                    text.append('\n').append(Localizable.get("version.manager.alert." + type + "." + (versionList.size() == 1 ? "single" : "multiple"))).append('\n');

                    int k = 0;
                    for (SimpleVersion version : versionList) {
                        LOGGER.debug("New version: {}", version);
                        if (++k == 5) {
                            LOGGER.debug("... and probably more new versions");
                            text.append(Localizable.get("version.manager.alert.more", versionList.size() - k + 1)).append('\n');
                            break;
                        }
                        add(text, version);
                    }
                }
            }
        }

        Alert.showMessage(Localizable.get("version.manager.alert.title"), text.toString());

        list = newList;
        saveList(newList);
    }

    private boolean saveList(SimpleVersionList versionList) {
        FileWriter writer = null;
        try {
            FileUtil.createFile(listFile);
            writer = new FileWriter(listFile);
            gson.toJson(versionList, writer);
            return true;
        } catch (Exception e) {
            LOGGER.error("Could not write version list file: {}", listFile, e);
            throw new RuntimeException(e);
        } finally {
            U.close(writer);
        }
    }

    private SimpleVersionList fetchListFromFile() {
        FileReader reader = null;
        try {
            reader = new FileReader(listFile);
            return gson.fromJson(reader, SimpleVersionList.class);
        } catch (Exception e) {
            LOGGER.error("Could not read version list from file: {}", listFile, e);
            return null;
        } finally {
            U.close(reader);
        }
    }

    private SimpleVersionList fetchListFromManager(VersionManager vm) {
        try {
            SimpleVersionList versionList = new SimpleVersionList();
            for (VersionSyncInfo syncInfo : vm.getVersions(new VersionFilter(), false)) {
                versionList.versions.add(new SimpleVersion(syncInfo));
            }
            return versionList;
        } catch (Exception e) {
            LOGGER.error("Could not fetch list from manager", e);
            throw new RuntimeException(e);
        }
    }

    private StringBuilder add(StringBuilder b, SimpleVersion version) {
        return b.append("– ").append(version.id).append(" (").append(getTimeDifference(version.time)).append(")").append('\n');
    }

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private static Calendar cal() {
        return Calendar.getInstance(UTC);
    }

    private static Calendar cal(Date date) {
        Calendar calendar = cal();
        calendar.setTime(date);
        return calendar;
    }

    private static Calendar cal(long time) {
        Calendar calendar = cal();
        calendar.setTimeInMillis(time);
        return calendar;
    }

    private String getTimeDifference(Date time) {
        if (time == null) {
            return Localizable.get("version.manager.alert.time.unknown");
        }

        // compare current time and release time
        Calendar
                currentTime = cal(),
                releaseTime = cal(time),
                difference = cal(currentTime.getTimeInMillis() - releaseTime.getTimeInMillis());

        String path = "version.manager.alert.time.";
        int field = -1;

        if (difference.get(Calendar.YEAR) > 1970) {
            path += "longtimeago";
        } else if (difference.get(Calendar.WEEK_OF_YEAR) > 2) {
            path += "week";
            field = difference.get(Calendar.WEEK_OF_YEAR) - 1;
        } else if (difference.get(Calendar.DAY_OF_YEAR) > 1) {
            path += "day";
            field = difference.get(Calendar.DAY_OF_YEAR);
        } else if (difference.get(Calendar.HOUR_OF_DAY) > 1) {
            path += "hour";
            field = difference.get(Calendar.HOUR_OF_DAY);
        } else if (difference.get(Calendar.MINUTE) > 1) {
            path += "minute";
            field = difference.get(Calendar.MINUTE);
        } else {
            path += "justnow";
        }

        return field == -1 ? Localizable.get(path) : Localizable.get(path, field);
    }

    private static class SimpleVersionList {
        String _notice = "Pretend that you are not reading this. And this file does not exist. It does not affect anything important. Just for indexing. Hvae fnu!";
        List<SimpleVersion> versions = new ArrayList<SimpleVersion>();

        boolean contains(SimpleVersion version) {
            return versions.contains(version);
        }

        public String toString() {
            return "SimpleVersionList[" + versions + "]";
        }
    }

    private static class SimpleVersion {
        private String id;
        private ReleaseType type;
        private Date releaseTime, time;

        @Expose
        private boolean hasRemote;

        SimpleVersion(VersionSyncInfo syncInfo) {
            id = syncInfo.getID();

            Version version = syncInfo.getAvailableVersion();

            type = version.getReleaseType();
            releaseTime = version.getReleaseTime();
            time = version.getUpdatedTime();

            hasRemote = syncInfo.hasRemote();
        }

        SimpleVersion(String id, ReleaseType type, Date date) {
            this.id = id;
            this.type = type;
            releaseTime = time = date;
        }

        public boolean equals(Object o) {
            if (o == null || !(o instanceof SimpleVersion)) {
                return false;
            }

            SimpleVersion v = (SimpleVersion) o;
            return id.equals(v.id);
        }

        public String toString() {
            return "SimpleVersion{id=" + id + ",type=" + type + ",releaseTime=" + releaseTime + ",time=" + time + "}";
        }
    }
}

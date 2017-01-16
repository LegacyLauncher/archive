package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.PatternTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class ElyManager extends InterruptibleComponent {
    private static final double VERSION = 1.0;

    private final List<ElyManagerListener> listeners = Collections.synchronizedList(new ArrayList<ElyManagerListener>());

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
            .create();

    private final List<ElyLib>
            authlib = nl(),
            asm = nl(),
            total = nl();

    private boolean refreshedOnce, allowedGlobally;

    public ElyManager(ComponentManager manager) throws Exception {
        super(manager);
        allowedGlobally = manager.getLauncher().getBootConfig().isElyAllowed();
    }

    public boolean isAllowedGlobally() {
        return allowedGlobally;
    }

    public boolean isUsingGlobally() {
        return isAllowedGlobally() && manager.getLauncher().getSettings().getBoolean("ely.globally");
    }

    public boolean hasLibraries(VersionSyncInfo version) {
        return version.isInstalled() && hasLibraries(version.getLocal()) || version.hasRemote() && hasLibraries(version.getRemote());
    }

    public boolean hasLibraries(Version version) {
        if (hasLibraries(version.getID())) {
            return true;
        }

        if (version instanceof CompleteVersion) {
            CompleteVersion complete = (CompleteVersion) version;

            for (ElyLib lib : total) {
                for (Library replacingLib : complete.getLibraries()) {
                    if (lib.replaces(replacingLib)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean hasLibraries(String version) {
        for (ElyLib lib : total) {
            if (lib.supports(version)) {
                return true;
            }
        }
        return false;
    }

    public List<ElyManager.ElyLib> getLibraries(CompleteVersion complete) {
        String id = complete.getID();
        ArrayList<ElyLib> libList = new ArrayList<ElyLib>();

        for (ElyLib lib : total) {
            add:
            {
                if (lib.supports(id)) {
                    break add;
                }

                for (Library replacingLib : complete.getLibraries()) {
                    if (lib.replaces(replacingLib)) {
                        break add;
                    }
                }

                continue;
            }
            libList.add(lib);
        }

        return libList;
    }

    public CompleteVersion elyficate(CompleteVersion original) {
        log("Processing version:", original.getID());

        if (original.isElyfied()) {
            log("... already Elyfied");
            return original;
        }

        CompleteVersion modified = original.copyInto(new CompleteVersion());
        modified.setElyfied(true);

        List<ElyManager.ElyLib> libraries = getLibraries(original);

        for (ElyLib lib : libraries) {
            log("Now processing:", lib.getName());

            if (modified.getLibraries().contains(lib)) {
                log("... already contains");
                continue;
            }

            if (lib.getPattern() != null) {
                Pattern pattern = lib.getPattern();

                Iterator<Library> i = modified.getLibraries().iterator();
                while (i.hasNext()) {
                    Library replaceable = i.next();

                    if (pattern.matcher(replaceable.getName()).matches()) {
                        log("... replacing", replaceable.getName());
                        i.remove();
                    }
                }
            }

            if (lib.getRequirementList() != null) {
                List<Library> versionLibraries = modified.getLibraries();
                Iterator<Library> i = versionLibraries.iterator();

                while (i.hasNext()) {
                    Library required = i.next();
                    String plainName = required.getPlainName();

                    for (Library compare : lib.getRequirementList()) {
                        if (plainName.equals(compare.getPlainName())) {
                            log("... required library", plainName, "exists, removing...");
                            i.remove();
                        }
                    }
                }

                modified.getLibraries().addAll(lib.getRequirementList());
            }

            if (StringUtils.isNotBlank(lib.getArgs())) {
                String args = modified.getMinecraftArguments();

                if (StringUtils.isBlank(args)) {
                    args = lib.getArgs();
                } else {
                    args = args + ' ' + lib.getArgs();
                }

                modified.setMinecraftArguments(args);
            }

            if (StringUtils.isNotBlank(lib.getMainClass())) {
                modified.setMainClass(lib.getMainClass());
            }

            modified.getLibraries().add(lib);
        }

        return modified;
    }

    public boolean refreshComponent() {
        return true;
    }

    protected boolean refresh(int refreshID) {
        log("Refreshing Ely...");

        for (ElyManagerListener l : listeners) {
            l.onElyUpdating(this);
        }

        try {
            refreshDirectly();
        } catch (Exception e) {
            log("Ely refresh failed", e);
            return false;
        } finally {
            for (ElyManagerListener l : listeners) {
                l.onElyUpdated(this);
            }
        }

        log("Refreshed successfully");
        return true;
    }

    private void refreshDirectly() throws Exception {
        RawResponse resp = gson.fromJson(Repository.EXTRA_VERSION_REPO.read("libraries/by/ely/libraries.json"), RawResponse.class);

        if (resp.version != VERSION) {
            throw new IllegalArgumentException("incompatible version; required: " + VERSION + ", got: " + resp.version);
        }

        U.requireNotNull(resp.authlib, "authlib");
        U.requireNotNull(resp.asm, "asm");

        synchronized (total) {
            authlib.clear();
            authlib.addAll(resp.authlib);

            asm.clear();
            asm.addAll(resp.asm);

            total.clear();
            total.addAll(authlib);
            total.addAll(asm);
        }

        refreshedOnce = true;
    }

    public void refreshOnceAsync() {
        if (!refreshedOnce) {
            asyncRefresh();
        }
    }

    public void refreshOnce() {
        if (!refreshedOnce) {
            refresh();
        }
    }

    public void addListener(ElyManagerListener listener) {
        listeners.add(listener);
    }

    private static List<ElyLib> nl() {
        return Collections.synchronizedList(new ArrayList<ElyLib>());
    }

    public static class ElyLib extends Library {
        private Pattern replaces;
        private String args;
        private String mainClass;
        private List<Library> requires = new ArrayList<Library>();
        private List<String> supports = new ArrayList<String>();

        public ElyLib() {
            url = "/libraries/";
        }

        public Pattern getPattern() {
            return replaces;
        }

        boolean replaces(Library lib) {
            return replaces != null && replaces.matcher(lib.getName()).matches();
        }

        String getArgs() {
            return args;
        }

        String getMainClass() {
            return mainClass;
        }

        List<Library> getRequirementList() {
            return requires;
        }

        List<String> getSupportedList() {
            return supports;
        }

        boolean supports(String version) {
            return supports != null && supports.contains(version);
        }

        public Downloadable getDownloadable(Repository versionSource, File file, OS os) {
            return super.getDownloadable(Repository.EXTRA_VERSION_REPO, file, os);
        }

        public String toString() {
            return "ElyLib{name=\'" + name + '\'' + ", replaces=\'" + replaces + "\', args=\'" + args + "\', requires=" + requires + ", supports=" + supports + "}";
        }
    }

    private static class RawResponse {
        private double version;
        private List<ElyManager.ElyLib> authlib;
        private List<ElyManager.ElyLib> asm;
    }
}

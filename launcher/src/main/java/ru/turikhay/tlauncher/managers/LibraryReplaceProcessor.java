package ru.turikhay.tlauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.LibraryReplace;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.launcher.versions.json.PatternTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.component.InterruptibleComponent;
import ru.turikhay.tlauncher.minecraft.auth.Account;
import ru.turikhay.tlauncher.repository.Repository;

import java.util.*;
import java.util.regex.Pattern;

public class LibraryReplaceProcessor extends InterruptibleComponent {
    private static final Logger LOGGER = LogManager.getLogger(LibraryReplaceProcessor.class);
    private static final String PATCHY_TYPE = "patchy";

    private final double VERSION = 1.0;

    private final List<LibraryReplaceProcessorListener> listeners = Collections.synchronizedList(new ArrayList<LibraryReplaceProcessorListener>());
    private final Map<String, List<LibraryReplace>> libraries = Collections.synchronizedMap(new HashMap<String, List<LibraryReplace>>());

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
            .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
            .create();

    private boolean refreshed, allowElyEverywhere;

    public LibraryReplaceProcessor(ComponentManager manager) throws Exception {
        super(manager);
    }

    public boolean isAllowElyEverywhere() {
        return allowElyEverywhere;
    }

    public void setAllowElyEverywhere(boolean allowElyEverywhere) {
        this.allowElyEverywhere = allowElyEverywhere;
    }

    public boolean hasLibraries(Version version, String type) {
        return hasLibrariesExplicitly(version, type) || hasLibrariesExplicitly(version, PATCHY_TYPE);
    }

    public boolean hasLibrariesExplicitly(VersionSyncInfo version, String type) {
        return version.isInstalled() && hasLibrariesExplicitly(version.getLocal(), type) || version.hasRemote() && hasLibrariesExplicitly(version.getRemote(), type);
    }

    public boolean hasLibrariesExplicitly(Version version, String type) {
        if (hasLibrariesExplicitly(version.getID(), type)) {
            return true;
        }
        List<LibraryReplace> list = forType(type);
        if(list == null) {
            return false;
        }
        if (version instanceof CompleteVersion) {
            CompleteVersion complete = (CompleteVersion) version;
            List<Library> libraries = complete.getLibraries();
            if(libraries != null) {
                for (LibraryReplace lib : list) {
                    for (Library replacingLib : libraries) {
                        if (lib.replaces(replacingLib)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean hasLibrariesExplicitly(String version, String type) {
        List<LibraryReplace> list = forType(type);
        if(list == null) {
            return false;
        }
        for (LibraryReplace lib : list) {
            if (lib.supports(version)) {
                return true;
            }
        }
        return false;
    }

    private List<LibraryReplace> forType(String type) {
        return libraries.get(type);
    }

    private List<LibraryReplace> getLibraries(CompleteVersion complete, String type) {
        String id = complete.getID();
        ArrayList<LibraryReplace> result = new ArrayList<LibraryReplace>();
        List<LibraryReplace> list = forType(type);
        if(list == null) {
            return result;
        }
        for (LibraryReplace lib : list) {
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
            result.add(lib);
        }
        return result;
    }

    public CompleteVersion process(CompleteVersion original, String type) {
        CompleteVersion result = processExplicitly(original, type);
        result = processExplicitly(result, PATCHY_TYPE);
        return result;
    }

    private CompleteVersion processExplicitly(CompleteVersion original, String type) {
        LOGGER.debug("Processing version {} for type {}", original.getID(), type);

        if (original.isProceededFor(type)) {
            LOGGER.debug("... already proceeded");
            return original;
        }

        CompleteVersion modified = original.copyInto(new CompleteVersion());
        modified.setProceededFor(type);

        List<LibraryReplace> libraries = getLibraries(original, type);
        for (LibraryReplace lib : libraries) {
            LOGGER.debug("Now processing: {}", lib.getName());

            if (modified.getLibraries().contains(lib)) {
                LOGGER.debug("... already contains");
                continue;
            }

            if (lib.getPattern() != null) {
                Pattern pattern = lib.getPattern();

                Iterator<Library> i = modified.getLibraries().iterator();
                while (i.hasNext()) {
                    Library replaceable = i.next();

                    if (pattern.matcher(replaceable.getName()).matches()) {
                        LOGGER.debug("... removing {}", replaceable.getName());
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
                            LOGGER.debug("... required library {} exists, removing", plainName);
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

    @Override
    protected boolean refresh(int session) {
        if(refreshed) {
            //log("Already refreshed");
            return true;
        }

        LOGGER.debug("Refreshing libraries...");

        for (LibraryReplaceProcessorListener l : listeners) {
            l.onLibraryReplaceRefreshing(this);
        }

        try {
            refreshDirectly();
        } catch (Exception e) {
            LOGGER.warn("Ely refresh failed", e);
            return false;
        } finally {
            for (LibraryReplaceProcessorListener l : listeners) {
                l.onLibraryReplaceRefreshed(this);
            }
        }

        LOGGER.debug("Refreshed successfully");
        return true;
    }

    private void refreshDirectly() throws Exception {
        //U.sleepFor(10 * 60 * 1000);
        Payload resp = gson.fromJson(Repository.EXTRA_VERSION_REPO.read("libraries/replace.json"), Payload.class);

        if (resp.version != VERSION) {
            throw new IllegalArgumentException("incompatible version; required: " + VERSION + ", got: " + resp.version);
        }

        synchronized (libraries) {
            libraries.clear();
            libraries.putAll(resp.libraries);
            if(!libraries.containsKey(Account.AccountType.ELY_LEGACY.toString()) && resp.libraries.containsKey(Account.AccountType.ELY.toString())) {
                libraries.put(Account.AccountType.ELY_LEGACY.toString(), resp.libraries.get(Account.AccountType.ELY.toString()));
            }
        }

        refreshed = true;
    }

    public void addListener(LibraryReplaceProcessorListener listener) {
        listeners.add(listener);
    }

    private static class Payload {
        private double version;
        private boolean allowElyEveywhere;
        private Map<String, List<LibraryReplace>> libraries;
    }
}

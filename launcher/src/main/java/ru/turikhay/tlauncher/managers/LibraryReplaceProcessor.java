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

    private final double VERSION = 1.0;

    private final List<LibraryReplaceProcessorListener> listeners = Collections.synchronizedList(new ArrayList<LibraryReplaceProcessorListener>());
    private final Map<Account.AccountType, List<LibraryReplace>> libraries = Collections.synchronizedMap(new HashMap<Account.AccountType, List<LibraryReplace>>());

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

    private List<LibraryReplace> forAccount(Account.AccountType type) {
        return libraries.get(type);
    }

    public boolean hasForAccount(Account.AccountType type) {
        return forAccount(type) != null;
    }

    public boolean hasLibraries(VersionSyncInfo version, Account.AccountType type) {
        return version.isInstalled() && hasLibraries(version.getLocal(), type) || version.hasRemote() && hasLibraries(version.getRemote(), type);
    }

    public boolean hasLibraries(Version version, Account.AccountType type) {
        if (hasLibraries(version.getID(), type)) {
            return true;
        }
        List<LibraryReplace> list = forAccount(type);
        if(list == null) {
            return false;
        }
        if (version instanceof CompleteVersion) {
            CompleteVersion complete = (CompleteVersion) version;

            for (LibraryReplace lib : list) {
                for (Library replacingLib : complete.getLibraries()) {
                    if (lib.replaces(replacingLib)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private boolean hasLibraries(String version, Account.AccountType type) {
        List<LibraryReplace> list = forAccount(type);
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

    private List<LibraryReplace> getLibraries(CompleteVersion complete, Account.AccountType type) {
        String id = complete.getID();
        ArrayList<LibraryReplace> result = new ArrayList<LibraryReplace>();
        List<LibraryReplace> list = forAccount(type);
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

    public CompleteVersion process(CompleteVersion original, Account.AccountType type) {
        LOGGER.debug("Processing version {} for account of type {}", original.getID(), type);

        if (original.isProceededFor(type, true)) {
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
            if(!libraries.containsKey(Account.AccountType.ELY_LEGACY) && resp.libraries.containsKey(Account.AccountType.ELY)) {
                libraries.put(Account.AccountType.ELY_LEGACY, resp.libraries.get(Account.AccountType.ELY));
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
        private Map<Account.AccountType, List<LibraryReplace>> libraries;
    }
}

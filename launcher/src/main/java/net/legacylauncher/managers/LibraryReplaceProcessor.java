package net.legacylauncher.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.component.InterruptibleComponent;
import net.legacylauncher.minecraft.auth.Account;
import net.legacylauncher.repository.Repository;
import net.legacylauncher.util.EHttpClient;
import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.CompleteVersion;
import net.minecraft.launcher.versions.Library;
import net.minecraft.launcher.versions.LibraryReplace;
import net.minecraft.launcher.versions.Version;
import net.minecraft.launcher.versions.json.LowerCaseEnumTypeAdapterFactory;
import net.minecraft.launcher.versions.json.PatternTypeAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.fluent.Request;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public class LibraryReplaceProcessor extends InterruptibleComponent {
    private static final String PATCHY_TYPE = "patchy";

    private final List<LibraryReplaceProcessorListener> listeners = Collections.synchronizedList(new ArrayList<>());
    private final Map<String, List<LibraryReplace>> libraries = Collections.synchronizedMap(new HashMap<>());

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Pattern.class, new PatternTypeAdapter())
            .registerTypeAdapterFactory(new LowerCaseEnumTypeAdapterFactory())
            .create();

    private boolean refreshed, allowElyEverywhere;

    public LibraryReplaceProcessor(ComponentManager manager) {
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
        if (list == null) {
            return false;
        }
        if (version instanceof CompleteVersion) {
            CompleteVersion complete = (CompleteVersion) version;
            List<Library> libraries = complete.getLibraries();
            if (libraries != null) {
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
        if (list == null) {
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
        List<LibraryReplace> list = forType(type);
        if (list == null) {
            return Collections.emptyList();
        }
        return list.stream()
                .filter(lib -> lib.supports(id) || complete.getLibraries().stream().anyMatch(lib::replaces))
                .collect(Collectors.toList());
    }

    public CompleteVersion process(CompleteVersion original, @Nonnull String... types) {
        CompleteVersion result = original;
        for (String type : types) {
            result = processExplicitly(result, type);
        }
        result = processExplicitly(result, PATCHY_TYPE);
        return result;
    }

    public CompleteVersion processExplicitly(CompleteVersion original, String type) {
        log.debug("Processing version {} for type {}", original.getID(), type);

        if (original.isProceededFor(type)) {
            log.debug("... already proceeded");
            return original;
        }

        CompleteVersion target = original.copyInto(new CompleteVersion());
        target.setProceededFor(type);

        List<LibraryReplace> libraries = getLibraries(original, type);
        for (LibraryReplace replacementLib : libraries) {
            log.debug("Now processing: {}", replacementLib.getName());

            if (target.getLibraries().contains(replacementLib)) {
                log.debug("... already contains");
                continue;
            }

            if (replacementLib.getRequirementList() != null) {
                List<Library> requiredLibs = new ArrayList<>(replacementLib.getRequirementList());
                for (int i = 0; i < target.getLibraries().size(); i++) {
                    Library library = target.getLibraries().get(i);
                    Iterator<Library> requiredIterator = requiredLibs.iterator();
                    while (requiredIterator.hasNext()) {
                        Library requiredLib = requiredIterator.next();
                        if (library.getPlainName().equals(requiredLib.getPlainName())) {
                            log.debug("... replacing required at index {}: {} -> {}",
                                    i, library.getName(), requiredLib.getName());
                            target.getLibraries().set(i, requiredLib);
                            requiredIterator.remove();
                        }
                    }
                }
                if (!requiredLibs.isEmpty()) {
                    log.debug("... prepending required: {}", requiredLibs.stream().map(Library::getName).collect(Collectors.joining(", ")));
                    target.getLibraries().addAll(0, requiredLibs);
                }
            }

            if (replacementLib.getPattern() != null) {
                Pattern pattern = replacementLib.getPattern();
                for (int i = 0; i < target.getLibraries().size(); i++) {
                    Library library = target.getLibraries().get(i);
                    if (pattern.matcher(library.getName()).matches()) {
                        log.debug("... replacing at index {}: {}", i, library.getName());
                        target.getLibraries().set(i, replacementLib);
                    }
                }
            } else {
                log.debug("... prepending: {}", replacementLib.getName());
                target.getLibraries().add(0, replacementLib);
            }

            if (StringUtils.isNotBlank(replacementLib.getArgs())) {
                String args = target.getMinecraftArguments();

                if (StringUtils.isBlank(args)) {
                    args = replacementLib.getArgs();
                } else {
                    args = args + ' ' + replacementLib.getArgs();
                }

                target.setMinecraftArguments(args);
            }

            if (StringUtils.isNotBlank(replacementLib.getMainClass())) {
                target.setMainClass(replacementLib.getMainClass());
            }
        }

        return target;
    }

    @Override
    protected boolean refresh(int refreshID) {
        if (refreshed) {
            //log("Already refreshed");
            return true;
        }

        log.debug("Refreshing libraries...");

        for (LibraryReplaceProcessorListener l : listeners) {
            l.onLibraryReplaceRefreshing(this);
        }

        try {
            refreshDirectly();
        } catch (Exception e) {
            log.warn("Ely refresh failed", e);
            return false;
        } finally {
            for (LibraryReplaceProcessorListener l : listeners) {
                l.onLibraryReplaceRefreshed(this);
            }
        }

        log.debug("Refreshed successfully");
        return true;
    }

    private void refreshDirectly() throws Exception {
        //U.sleepFor(10 * 60 * 1000);
        Payload resp = gson.fromJson(Repository.EXTRA_VERSION_REPO.read("libraries/replace.json"), Payload.class);

        double VERSION = 1.0;
        if (resp.version != VERSION) {
            throw new IllegalArgumentException("incompatible version; required: " + VERSION + ", got: " + resp.version);
        }

        synchronized (libraries) {
            libraries.clear();
            log.debug("Got replaces: " + String.join(", ", resp.libraries.keySet()));
            libraries.putAll(resp.libraries);
            if (!libraries.containsKey(Account.AccountType.ELY_LEGACY.toString()) && resp.libraries.containsKey(Account.AccountType.ELY.toString())) {
                libraries.put(Account.AccountType.ELY_LEGACY.toString(), resp.libraries.get(Account.AccountType.ELY.toString()));
            }
        }

        refreshed = true;
    }

    public void fetchSeparate(String type) throws IOException {
        synchronized (libraries) {
            if (libraries.containsKey(type)) {
                return;
            }
        }
        log.info("Requesting manifest exclusively (type: {})", type);
        String content = EHttpClient.toString(
                Request.get("https://libservice.llaun.ch/manifest/" + type)
                        .addHeader("Accept", "application/json")
        );
        SeparatePayload payload = gson.fromJson(content, SeparatePayload.class);
        synchronized (libraries) {
            if (libraries.containsKey(type)) {
                log.info("Ignoring manifest for {}", type);
                return;
            }
            libraries.put(type, payload.replacements);
            log.info("Received {} libraries from the manifest (type: {})", payload.replacements.size(), type);
        }
    }

    public void addListener(LibraryReplaceProcessorListener listener) {
        listeners.add(listener);
    }

    private static class Payload {
        private double version;
        private boolean allowElyEveywhere;
        private Map<String, List<LibraryReplace>> libraries;
    }

    private static class SeparatePayload {
        private List<LibraryReplace> replacements;
    }
}

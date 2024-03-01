package net.legacylauncher;

import net.legacylauncher.ipc.BootstrapIPC;
import net.legacylauncher.ipc.SystemDefaultResolverIPC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.turikhay.tlauncher.bootstrap.bridge.BootBridge;
import ru.turikhay.tlauncher.bootstrap.bridge.BootEventDispatcher;
import ru.turikhay.tlauncher.bootstrap.bridge.BootMessage;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Shim to run launcher with non-ipc bootstrap.
 * ALL bridge usage should be incorporated here
 */
public class LegacyLauncherBridged {
    private static final Logger LOGGER = LoggerFactory.getLogger(LegacyLauncher.class);

    public static void launch(BootBridge bridge) {
        LegacyLauncher.launch(new Shim(bridge), SystemDefaultResolverIPC.INSTANCE);
    }

    public static void main(String[] args) throws InterruptedException {
        launch(BootBridge.create(args));
    }

    static class Shim implements BootstrapIPC {
        private final BootBridge bridge;
        private final BootEventDispatcher bootEventDispatcher;
        private final boolean capabilitiesSupported;

        public Shim(BootBridge bridge) {
            this.bridge = bridge;
            this.bootEventDispatcher = bridge.setupDispatcher();

            boolean capabilitiesSupported = true;
            try {
                //noinspection ResultOfMethodCallIgnored
                bridge.getCapabilities();
            } catch (NoSuchMethodError e) {
                LOGGER.info("Bootstrap doesn't report capabilities");
                capabilitiesSupported = false;
            }
            this.capabilitiesSupported = capabilitiesSupported;
        }

        @Override
        public BootstrapRelease getBootstrapRelease() {
            String version = bridge.getBootstrapVersion();
            return new BootstrapRelease("bootstrap-java-bridge", version == null ? "unknown" : version);
        }

        @Override
        public List<String> getLauncherArguments() {
            return Collections.unmodifiableList(Arrays.asList(bridge.getArgs()));
        }

        @Override
        public String getLauncherConfiguration() {
            return bridge.getOptions();
        }

        @Override
        public Map<String, ReleaseNotes> getLauncherReleaseNotes(String launcherVersion) {
            class BootMessageWithLocale {
                final String locale;
                final BootMessage message;

                BootMessageWithLocale(String locale, BootMessage message) {
                    this.locale = locale;
                    this.message = message;
                }
            }

            return Stream.of("en_US", "ru_RU")
                    .map(locale -> new BootMessageWithLocale(locale, bootEventDispatcher.getBootMessage(locale)))
                    .filter(it -> it.message != null)
                    .collect(Collectors.toMap(it -> it.locale, it -> new ReleaseNotes(it.message.getTitle(), it.message.getBody())));
        }

        @Override
        public void onBootStarted() {
            try {
                bootEventDispatcher.onBootStarted();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onBootProgress(String stepName, double percentage) {
            try {
                bootEventDispatcher.onBootStateChanged(stepName, percentage);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onBootSucceeded() {
            try {
                bootEventDispatcher.onBootSucceeded();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onBootError(String message) {
            try {
                bootEventDispatcher.onBootErrored(new RuntimeException(message));
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onBootError(Throwable e) {
            try {
                bootEventDispatcher.onBootErrored(e);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        }

        @Override
        public void requestClose() {
            bootEventDispatcher.requestClose();
        }

        @Override
        public void setMetadata(String key, @Nullable Object value) {
            if ("client".equals(key)) {
                if (!(value instanceof String)) {
                    throw new IllegalArgumentException("client metadata should be a string");
                }
                bootEventDispatcher.passClient(UUID.fromString((String) value));
                return;
            }
            if (!capabilitiesSupported) return;
            bridge.addCapability(key, value);
        }

        @Nullable
        @Override
        public Object getMetadata(String key) {
            if ("client".equals(key)) {
                return bridge.getClient().toString();
            }
            if (!capabilitiesSupported) return null;
            return bridge.getCapabilities().get(key);
        }

        @Override
        public void close() throws IOException {
            bridge.setInterrupted();
        }
    }
}

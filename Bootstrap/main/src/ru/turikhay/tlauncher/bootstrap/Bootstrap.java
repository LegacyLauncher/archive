package ru.turikhay.tlauncher.bootstrap;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import ru.turikhay.tlauncher.bootstrap.json.Json;
import ru.turikhay.tlauncher.bootstrap.launcher.LaunchType;
import ru.turikhay.tlauncher.bootstrap.launcher.LocalLauncher;
import ru.turikhay.tlauncher.bootstrap.launcher.RemoteLauncher;
import ru.turikhay.tlauncher.bootstrap.meta.*;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.ui.Alert;
import ru.turikhay.tlauncher.bootstrap.util.FolderValueConverter;
import ru.turikhay.tlauncher.bootstrap.util.OS;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.stream.RedirectPrintStream;

import java.io.File;
import java.io.IOException;

public final class Bootstrap {

    public static void main(String[] args) {
        System.setOut(RedirectPrintStream.newRedirectorFor(System.out));
        System.setErr(RedirectPrintStream.newRedirectorFor(System.err));

        log("Starting bootstrap...");

        Bootstrap bootstrap = new Bootstrap();
        LocalBootstrapMeta meta = bootstrap.getMeta();

        log("Version: " + meta.getVersion());

        OptionParser parser = new OptionParser();
        ArgumentAcceptingOptionSpec<LaunchType> launchTypeParser =
                parser.accepts("launchType", "defines launch type").withRequiredArg().ofType(LaunchType.class).defaultsTo(U.requireNotNull(meta.getLaunchType(), "default LaunchType"));
        ArgumentAcceptingOptionSpec<File> fileTypeParser =
                parser.accepts("localFile", "points to the folder where to find jar").withRequiredArg().withValuesConvertedBy(new FolderValueConverter()).defaultsTo(U.requireNotNull(LocalLauncher.getDefaultLocation(meta.getShortBrand()), "default LocalLauncher file location"));
        ArgumentAcceptingOptionSpec<String> brandParser =
                parser.accepts("brand", "defines brand name").withRequiredArg().ofType(String.class).defaultsTo(U.requireNotNull(meta.getShortBrand(), "default shortBrand"));
        OptionSpecBuilder forceUpdateParser =
                parser.accepts("forceUpdate", "defines if bootstrap should update launcher on update found");

        OptionSet parsed = parser.parse(args);
        meta.setLaunchType(U.requireNotNull(launchTypeParser.value(parsed), "LaunchType"));
        meta.setLocalFile(U.requireNotNull(fileTypeParser.value(parsed), "LocalLauncher file location"));
        meta.setShortBrand(U.requireNotNull(brandParser.value(parsed), "shortBrand"));
        meta.setForceUpdate(parsed.has(forceUpdateParser));

        try {
            bootstrap.mainTask().call();
        } catch (Exception e) {
            e.printStackTrace();

            Alert.showError("Could not start TLauncher!\nPlease copy the log below and send it to <a href=\"http://tlauncher.ru/go/support\">our support</a>. Your help will be very appreciated.", RedirectPrintStream.getBuffer().toString());
            System.exit(-1);
        }

        System.exit(0);
    }

    private final LocalBootstrapMeta meta;

    public Bootstrap() {
        final String resourceName = "meta.json";
        try {
            meta = Json.parse(U.requireNotNull(getClass().getResourceAsStream(resourceName), resourceName), LocalBootstrapMeta.class);
        } catch (Exception e) {
            throw new Error("could not load meta", e);
        }
    }

    public LocalBootstrapMeta getMeta() {
        return meta;
    }

    public Task<Void> mainTask() {
        return new Task<Void>("mainTask") {
            @Override
            protected Void execute() throws Exception {
                updateProgress(0.);
                UpdateMeta updateMeta = Updater.getUpdate(meta.getShortBrand());
                updateProgress(.25);

                RemoteBootstrapMeta remoteMeta = U.requireNotNull(U.requireNotNull(updateMeta, "updateMeta").getBootstrap(), "RemoteBootstrapMeta");

                updateBootstrap:
                {
                    U.requireNotNull(remoteMeta.getChecksum(), "RemoteBootstrap checksum");
                    U.requireNotNull(remoteMeta.getDownload(), "RemoteBootstrap download URL");

                    log("Remote bootstrap version: " + remoteMeta.getVersion());
                    if (!meta.getVersion().equals(remoteMeta.getVersion())) {
                        return openUpdateLink(remoteMeta);
                    }

                    String localBootstrapChecksum;
                    try {
                        localBootstrapChecksum = U.getSHA256(U.getJar());
                    } catch (Exception e) {
                        log("Could not get local bootstrap checksum", e);
                        break updateBootstrap;
                    }

                    log("Local bootstrap checksum: " + localBootstrapChecksum);
                    log("Remote bootstrap checksum: " + remoteMeta.getChecksum());

                    if (localBootstrapChecksum.equalsIgnoreCase(remoteMeta.getChecksum())) {
                        break updateBootstrap;
                    }

                    return openUpdateLink(remoteMeta);
                }

                updateProgress(.5);

                /*RemoteLauncher remoteLauncher = new RemoteLauncher(updateMeta.getLauncher());

                log("Remote launcher: " + remoteLauncher);

                LocalLauncher localLauncher = bindTo(getLocalLauncher(remoteLauncher), .5, .9);*/

                LocalLauncher localLauncher = new LocalLauncher(meta.getLocalFile());
                log("Local launcher: " + localLauncher);

                return bindTo(meta.getLaunchType().getStarter().start(localLauncher), .5, 1.);
            }

            private Void openUpdateLink(RemoteBootstrapMeta remoteMeta) {
                if (!OS.openUrl(remoteMeta.getDownload())) {
                    Alert.showError("Could not open the update link. Please open it yourself:", String.valueOf(remoteMeta.getDownload()));
                }
                return null;
            }
        };
    }

    private Task<LocalLauncher> getLocalLauncher(final RemoteLauncher remote) {
        U.requireNotNull(remote, "RemoteLauncher");

        return new Task<LocalLauncher>("getLocalLauncher") {
            @Override
            protected LocalLauncher execute() throws Exception {
                updateProgress(0.);
                log("Getting local launcher...");

                RemoteLauncherMeta remoteLauncherMeta = U.requireNotNull(remote.getMeta(), "RemoteLauncherMeta");

                LocalLauncher local = new LocalLauncher(meta.getLocalFile());
                LocalLauncherMeta localLauncherMeta;

                try {
                    localLauncherMeta = U.requireNotNull(local.getMeta(), "LocalLauncherMeta");
                } catch (IOException ioE) {
                    localLauncherMeta = null;
                    log("Could not get local launcher meta:", ioE);
                }

                updateProgress(.2);

                replaceSelect:
                {
                    if (localLauncherMeta == null) {
                        break replaceSelect;
                    }

                    U.requireNotNull(localLauncherMeta.getShortBrand(), "LocalLauncher shortBrand");
                    U.requireNotNull(localLauncherMeta.getBrand(), "LocalLauncher brand");
                    U.requireNotNull(localLauncherMeta.getMainClass(), "LocalLauncher mainClass");

                    log("Local version: " + localLauncherMeta.getVersion());
                    log("Remote version: " + remoteLauncherMeta.getVersion());
                    if (!remoteLauncherMeta.getVersion().equals(localLauncherMeta.getVersion())) {
                        log("... versions are not the same");
                        break replaceSelect;
                    }

                    String localLauncherHash = U.getSHA256(local.getFile());
                    log("Local SHA256: " + localLauncherHash);
                    log("Remote SHA256: " + remoteLauncherMeta.getChecksum());

                    if (!localLauncherHash.equalsIgnoreCase(remoteLauncherMeta.getChecksum())) {
                        log("... local SHA256 checksum is not the same as remote");
                        break replaceSelect;
                    }

                    log("All done, local launcher is up to date.");

                    return local;
                }

                updateProgress(.5);

                return bindTo(remote.toLocalLauncher(local.getFile()), .5, 1.);
            }
        };
    }

    private static void log(Object... o) {
        U.log("[Bootstrap]", o);
    }
}

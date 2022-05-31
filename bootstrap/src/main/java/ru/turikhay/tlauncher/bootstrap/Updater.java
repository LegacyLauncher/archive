package ru.turikhay.tlauncher.bootstrap;

import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.bootstrap.exception.FileLockedException;
import ru.turikhay.tlauncher.bootstrap.meta.DownloadEntry;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.ui.UserInterface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class Updater extends Task<Void> {

    private final Path destFile;
    private final DownloadEntry entry;

    private FinishAction finishAction = FinishAction.EXIT;
    private String[] restartCmd;

    public Updater(String name, Path destFile, DownloadEntry entry) throws IOException {
        super(name);

        this.destFile = Objects.requireNonNull(destFile, "destFile");
        if (!Files.isRegularFile(destFile)) {
            throw new FileNotFoundException();
        }

        this.entry = Objects.requireNonNull(entry, "downloadEntry");
    }

    public void restartOnFinish(List<String> restartCmd) {
        this.finishAction = FinishAction.RESTART;
        this.restartCmd = restartCmd.toArray(new String[0]);
    }

    @Override
    protected Void execute() throws Exception {
        String randomUrl = getAnyDownloadUrl();
        final boolean restartOnFinish = finishAction == FinishAction.RESTART;

        showUIMessage(randomUrl, restartOnFinish);

        ProcessBuilder processBuilder = null;

        if (restartOnFinish) {
            processBuilder = new ProcessBuilder(this.restartCmd);
        }

        doWork(randomUrl);

        if (restartOnFinish) {
            processBuilder.start();
        }

        System.exit(0);

        return null;
    }

    private void doWork(String randomUrl) throws Exception {
        Path tempFile = Files.createTempFile("updater", null);

        try {
            bindTo(entry.toDownloadTask(getName(), tempFile), 0., .95);
        } catch (FileLockedException lockedException) {
            UserInterface.showError(
                    UserInterface.getLString("update.locked",
                            "Update file is locked by another process."),
                    randomUrl
            );
            Files.delete(tempFile);
            return;
        }

        try (InputStream input = Files.newInputStream(tempFile);
             OutputStream output = Files.newOutputStream(destFile)) {
            IOUtils.copy(input, output);
        } finally {
            Files.delete(tempFile);
        }
    }

    private String getAnyDownloadUrl() {
        return entry.getUrl().get(new Random().nextInt(entry.getUrl().size())).toString();
    }

    private void showUIMessage(String randomUrl, boolean restartOnFinish) {
        StringBuilder message = new StringBuilder();
        if (restartOnFinish) {
            message.append(UserInterface.getLString("update.restart.auto",
                    "Application is going to self-update and then restart automatically."));
        } else {
            message.append(UserInterface.getLString("update.restart.manual",
                    "Application is going to self-update. Please restart it manually."));
        }
        message.append("\n\n");
        message.append(UserInterface.getLString("update.link",
                "Please download and install the update manually if something goes wrong:"));
        UserInterface.showWarning(message.toString(), randomUrl);
    }

    private enum FinishAction {
        RESTART,
        EXIT,
    }
}

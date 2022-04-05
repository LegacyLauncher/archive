package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.exception.FileLockedException;
import ru.turikhay.tlauncher.bootstrap.meta.DownloadEntry;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.ui.UserInterface;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.*;
import java.util.Random;

public class Updater extends Task<Void> {

    private final File destFile;
    private final DownloadEntry entry;

    private FinishAction finishAction = FinishAction.EXIT;
    private String restartExec;

    public Updater(String name, File destFile, DownloadEntry entry) throws IOException {
        super(name);

        this.destFile = U.requireNotNull(destFile, "destFile");
        if(!destFile.isFile()) {
            throw new FileNotFoundException();
        }

        this.entry = U.requireNotNull(entry, "downloadEntry");
    }

    public void restartOnFinish(String restartExec) {
        this.finishAction = FinishAction.RESTART;
        this.restartExec = U.requireNotNull(restartExec, "restartExec");
    }

    @Override
    protected Void execute() throws Exception {
        String randomUrl = getAnyDownloadUrl();
        final boolean restartOnFinish = finishAction == FinishAction.RESTART;

        showUIMessage(randomUrl, restartOnFinish);

        ProcessBuilder processBuilder = null;

        if(restartOnFinish) {
            processBuilder = new ProcessBuilder(this.restartExec);
        }

        doWork:
        {
            File tempFile = File.createTempFile("updater", null);
            tempFile.deleteOnExit();

            try {
                bindTo(entry.toDownloadTask(getName(), tempFile), 0., .95);
            } catch (FileLockedException lockedException) {
                UserInterface.showError(
                        UserInterface.getLString("update.locked",
                                "Update file is locked by another process."),
                        randomUrl
                );
                break doWork;
            }

            byte[] buffer = new byte[8192];
            FileInputStream input = null;
            FileOutputStream output = null;
            try {
                input = new FileInputStream(tempFile);
                output = new FileOutputStream(destFile);

                int read;
                while ((read = input.read(buffer)) != -1) {
                    output.write(buffer, 0, read);
                }
            } finally {
                if (input != null) {
                    input.close();
                    tempFile.delete();
                }
                if (output != null) {
                    output.close();
                }
            }
        }

        if(restartOnFinish) {
            processBuilder.start();
        }

        System.exit(0);

        return null;
    }

    private String getAnyDownloadUrl() {
        return entry.getUrl().get(new Random().nextInt(entry.getUrl().size())).toString();
    }

    private void showUIMessage(String randomUrl, boolean restartOnFinish) {
        StringBuilder message = new StringBuilder();
        if(restartOnFinish) {
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

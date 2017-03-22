package ru.turikhay.tlauncher.bootstrap;

import ru.turikhay.tlauncher.bootstrap.meta.DownloadEntry;
import ru.turikhay.tlauncher.bootstrap.task.Task;
import ru.turikhay.tlauncher.bootstrap.ui.UserInterface;
import ru.turikhay.tlauncher.bootstrap.util.U;

import java.io.*;
import java.util.Random;

public class Updater extends Task<Void> {

    private final File destFile;
    private final DownloadEntry entry;
    private final boolean exitOnFinish;

    public Updater(String name, File destFile, DownloadEntry entry, boolean exitOnFinish) throws IOException {
        super(name);

        this.destFile = U.requireNotNull(destFile, "destFile");
        if(!destFile.isFile()) {
            throw new FileNotFoundException();
        }

        this.entry = U.requireNotNull(entry, "downloadEntry");
        this.exitOnFinish = exitOnFinish;


    }

    @Override
    protected Void execute() throws Exception {
        UserInterface.showWarning(
                UserInterface.getResourceBundle().getString("update"),
                entry.getUrl().get(new Random().nextInt(entry.getUrl().size()))
        );

        final boolean exitOnFinish = this.exitOnFinish;

        File tempFile = File.createTempFile("updater", null);
        tempFile.deleteOnExit();

        bindTo(entry.toDownloadTask(getName(), tempFile), 0., .95);

        byte[] buffer = new byte[8192];
        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            input = new FileInputStream(tempFile);
            output = new FileOutputStream(destFile);

            int read;
            while((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }
        } finally {
            if(input != null) {
                input.close();
                tempFile.delete();
            }
            if(output != null) {
                output.close();
            }
        }

        if(exitOnFinish) {
            System.exit(0);
        }

        return null;
    }
}

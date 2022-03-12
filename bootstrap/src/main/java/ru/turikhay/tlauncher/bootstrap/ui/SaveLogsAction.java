package ru.turikhay.tlauncher.bootstrap.ui;

import org.apache.commons.io.IOUtils;
import ru.turikhay.tlauncher.bootstrap.ui.message.Button;
import ru.turikhay.tlauncher.bootstrap.ui.message.MessageHost;
import ru.turikhay.tlauncher.bootstrap.ui.message.SingleButtonMessage;
import ru.turikhay.tlauncher.bootstrap.ui.message.TextAreaMessage;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.stream.OutputRedirectBuffer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

public class SaveLogsAction implements Runnable {
    private final ResourceBundle b = UserInterface.getResourceBundle();

    private final MessageHost host;

    public SaveLogsAction(MessageHost host) {
        this.host = host;
    }

    @Override
    public void run() {
        saveLogs();
    }

    private void saveLogs() {
        String content = getLogsContent();

        File logsDir = new File(System.getProperty("user.home", "."), "bootstrap_logs");
        File file = new File(logsDir, "TL_bootstrap.log");
        OutputStreamWriter writer = null;
        try {
            if (!logsDir.isDirectory() && !logsDir.mkdir()) {
                throw new IOException("could not create directory: " + logsDir.getAbsolutePath());
            }
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            IOUtils.copy(new StringReader(content), writer);
            writer.flush();
        } catch (IOException ioE) {
            U.log("[SaveLogs]", ioE);
            saveLogsFailed();
            return;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        host.showMessage(new SingleButtonMessage(
                b.getString("save-logs.success.text")
                        + "<br/>"
                        + b.getString("contacts"),
                Button.openFile(
                        b.getString("save-logs.success.button"),
                        file.getParentFile()
                )
        ));
    }

    private void saveLogsFailed() {
        host.showMessage(new TextAreaMessage(
                b.getString("save-logs.fail.text")
                        + "<br/>"
                        + b.getString("contacts"),
                getLogsContent()
        ));
    }

    private static String getLogsContent() {
        return OutputRedirectBuffer.getBuffer();
    }
}

package net.legacylauncher.bootstrap.ui;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.ui.message.Button;
import net.legacylauncher.bootstrap.ui.message.MessageHost;
import net.legacylauncher.bootstrap.ui.message.SingleButtonMessage;
import net.legacylauncher.bootstrap.ui.message.TextAreaMessage;
import net.legacylauncher.bootstrap.util.stream.OutputRedirectBuffer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ResourceBundle;

@Slf4j
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
            writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8);
            IOUtils.copy(new StringReader(content), writer);
            writer.flush();
        } catch (IOException e) {
            log.error("Saving logs failed", e);
            saveLogsFailed();
            return;
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                log.warn("Jezz, we don't event able to close writer, something gone hardly wrong", e);
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

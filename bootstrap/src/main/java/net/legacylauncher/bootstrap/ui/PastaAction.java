package net.legacylauncher.bootstrap.ui;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.bootstrap.pasta.Pasta;
import net.legacylauncher.bootstrap.pasta.PastaException;
import net.legacylauncher.bootstrap.pasta.PastaLink;
import net.legacylauncher.bootstrap.ui.message.Button;
import net.legacylauncher.bootstrap.ui.message.MessageHost;
import net.legacylauncher.bootstrap.ui.message.ProcessMessage;
import net.legacylauncher.bootstrap.ui.message.SingleButtonMessage;
import net.legacylauncher.bootstrap.util.stream.OutputRedirectBuffer;

import java.util.ResourceBundle;

@Slf4j
class PastaAction implements Runnable {
    private final ResourceBundle b = UserInterface.getResourceBundle();

    private final MessageHost host;

    PastaAction(MessageHost host) {
        this.host = host;
    }

    @Override
    public void run() {
        host.showMessage(new ProcessMessage(
                b.getString("pasta.sending"),
                () -> {
                    try {
                        sendPasta();
                    } catch (PastaException e) {
                        log.error("Pasta submission failed", e);
                        sendPastaFailed();
                    }
                }
        ));
    }

    private void sendPasta() throws PastaException {
        Pasta pasta = new Pasta(getLogsContent());
        PastaLink link = pasta.send();
        host.showMessage(new SingleButtonMessage(
                b.getString("pasta.success.text"),
                Button.openLink(b.getString("pasta.success.button"), link.getUrl())
        ));
    }

    private void sendPastaFailed() {
        host.showMessage(new SingleButtonMessage(
                b.getString("pasta.fail.text"),
                new Button(b.getString("pasta.fail.button"), this::saveLogs)
        ));
    }

    private void saveLogs() {
        new SaveLogsAction(host).run();
    }

    private static String getLogsContent() {
        return OutputRedirectBuffer.getBuffer();
    }
}

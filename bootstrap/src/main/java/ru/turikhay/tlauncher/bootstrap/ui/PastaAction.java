package ru.turikhay.tlauncher.bootstrap.ui;

import ru.turikhay.tlauncher.bootstrap.pasta.Pasta;
import ru.turikhay.tlauncher.bootstrap.pasta.PastaException;
import ru.turikhay.tlauncher.bootstrap.pasta.PastaLink;
import ru.turikhay.tlauncher.bootstrap.ui.message.*;
import ru.turikhay.tlauncher.bootstrap.util.U;
import ru.turikhay.tlauncher.bootstrap.util.stream.RedirectPrintStream;

import java.util.ResourceBundle;

class PastaAction implements Runnable {
    private final ResourceBundle b = UserInterface.getResourceBundle();

    private final MessageHost host;
    private final String clientId;

    PastaAction(MessageHost host, String clientId) {
        this.host = host;
        this.clientId = clientId;
    }

    @Override
    public void run() {
        host.showMessage(new ProcessMessage(
                b.getString("pasta.sending"),
                new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendPasta();
                        } catch(PastaException e) {
                            U.log("[Pasta]", e);
                            sendPastaFailed();
                        }
                    }
                }
        ));
    }

    private void sendPasta() throws PastaException {
        Pasta pasta = new Pasta(clientId, getLogsContent());
        PastaLink link = pasta.send();
        host.showMessage(new SingleButtonMessage(
                b.getString("pasta.success.text"),
                Button.openLink(b.getString("pasta.success.button"), link.getUrl())
        ));
    }

    private void sendPastaFailed() {
        host.showMessage(new SingleButtonMessage(
                b.getString("pasta.fail.text"),
                new Button(b.getString("pasta.fail.button"), new Runnable() {
                    @Override
                    public void run() {
                        saveLogs();
                    }
                })
        ));
    }

    private void saveLogs() {
        new SaveLogsAction(host).run();
    }

    private static String getLogsContent() {
        return RedirectPrintStream.getBuffer().toString();
    }
}

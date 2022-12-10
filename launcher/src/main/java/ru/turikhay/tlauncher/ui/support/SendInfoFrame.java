package ru.turikhay.tlauncher.ui.support;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.logger.Log4j2ContextHelper;
import ru.turikhay.tlauncher.pasta.Pasta;
import ru.turikhay.tlauncher.pasta.PastaResult;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.frames.ProcessFrame;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.sysinfo.SystemInfo;
import ru.turikhay.util.sysinfo.SystemInfoReporter;

import javax.swing.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class SendInfoFrame extends ProcessFrame<SendInfoFrame.SendInfoResponse> {

    private static final Logger LOGGER = LogManager.getLogger(SendInfoFrame.class);
    private final SystemInfoReporter systemInfoReporter;

    public static final class SendInfoResponse {
        private final String link;

        SendInfoResponse(String link) {
            this.link = StringUtil.requireNotBlank(link);
        }

        public String getLink() {
            return link;
        }
    }

    public SendInfoFrame(SystemInfoReporter systemInfoReporter) {
        this.systemInfoReporter = systemInfoReporter;
        setTitlePath("support.sending.title");
        getHead().setText("support.sending.head");
        setIcon("compress");
        pack();
    }

    public final void submit() {
        submit(new Process() {
            @Override
            protected SendInfoResponse get() throws Exception {
                if (systemInfoReporter != null) {
                    SystemInfo systemInfo;
                    try {
                        systemInfo = systemInfoReporter.getReport().get(5, TimeUnit.SECONDS);
                    } catch (Exception ignored) {
                        systemInfo = null;
                    }
                    if (systemInfo != null) {
                        systemInfo.getLines().forEach(LOGGER::info);
                    }
                }
                Pasta pasta = new Pasta();
                pasta.setData(Log4j2ContextHelper.getCurrentLogFile());

                PastaResult result = pasta.paste();

                if (result instanceof PastaResult.PastaUploaded) {
                    return new SendInfoResponse(((PastaResult.PastaUploaded) result).getURL().toString());
                } else if (result instanceof PastaResult.PastaFailed) {
                    throw new IOException(((PastaResult.PastaFailed) result).getError());
                }
                throw new InternalError("unknown result type");
            }
        });
    }

    @Override
    protected void onSucceeded(Process process, SendInfoResponse result) {
        super.onSucceeded(process, result);

        final String link = result.getLink();
        SwingUtilities.invokeLater(() -> new OpenLinkFrame(link).showAtCenter());
    }

    @Override
    protected void onFailed(Process process, Exception e) {
        super.onFailed(process, e);

        if (Alert.showLocQuestion("support.sending.error")) {
            FileExplorer explorer;
            try {
                explorer = FileExplorer.newExplorer();
            } catch (Exception error) {
                Alert.showLocError("support.saving.error", error);
                return;
            }

            explorer.setSelectedFile(new File("diagnostic.log"));

            if (explorer.showSaveDialog(this) != FileExplorer.APPROVE_OPTION) {
                return;
            }

            if (explorer.getSelectedFile() != null) {
                File file = explorer.getSelectedFile();
                try (InputStreamReader reader = Log4j2ContextHelper.getCurrentLogFile().read();
                     OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)
                ) {
                    IOUtils.copy(reader, writer);
                } catch (Exception error) {
                    Alert.showLocError("support.saving.error", error);
                    return;
                }

                Alert.showLocMessage("support.saving.success", file);
            }
        }
    }

    /*private final ExecutorService service = Executors.newCachedThreadPool();

    private SupportFrame helpFrame;
    private SendInfoCallable current;

    public SendInfoFrame() {
        setMinimumSize(SwingUtil.magnify(new Dimension(600, 1)));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                onSendCancelled();
            }
        });

        setTitlePath("support.sending.title");
        getHead().setText("support.sending.head");

        ProgressBar progress = new ProgressBar();
        progress.setPreferredSize(new Dimension(1, SwingUtil.magnify(32)));
        progress.setIndeterminate(true);
        getBody().setCenter(progress);
        getBody().setWest(Images.getIcon("communication.png", SwingUtil.magnify(32)));

        pack();
    }

    public final SupportFrame getSupportFrame() {
        return helpFrame;
    }

    public final void setFrame(SupportFrame frame) {
        helpFrame = U.requireNotNull(frame);
        service.submit(current = new SendInfoCallable(frame));
    }

    protected void onSending(SendInfoCallable callable) {
        checkIfCurrent(callable);
        showAtCenter();
    }

    protected void onSendSuccess(SendInfoCallable callable, SendInfoResponse response) {
        checkIfCurrent(callable);

        setVisible(false);

        helpFrame.setResponse(response);
    }

    protected void onSendFailed(SendInfoCallable callable, Throwable t) {
        checkIfCurrent(callable);
        setVisible(false);


    }

    protected void onSendCancelled() {
        current = null;
    }

    private void checkIfCurrent(SendInfoCallable callable) {
        if (callable != current) {
            throw new IllegalStateException();
        }
    }

    public final class SendInfoResponse {
        private final String pastebinLink;

        SendInfoResponse(String pastebinLink) {
            this.pastebinLink = StringUtil.requireNotBlank(pastebinLink);
        }

        public final String getPastebinLink() {
            return pastebinLink;
        }
    }

    public class SendInfoCallable implements Runnable {
        private final SupportFrame frame;

        SendInfoCallable(SupportFrame frame) {
            this.frame = frame;
        }

        @Override
        public void run() {
            onSending(this);

            SendInfoResponse response;

            try {
                response = sendInfo();
            } catch (Exception e) {
                onSendFailed(this, e);
                return;
            }

            onSendSuccess(this, response);
        }

        private SendInfoResponse sendInfo() throws Exception {
            Paste paste = new Paste();
            paste.setTitle("Diagnostic Log");
            paste.setContent(TLauncher.getLogger().getOutput());

            PasteResult result = paste.paste();

            if (result instanceof PasteResult.PasteUploaded) {
                return new SendInfoResponse(((PasteResult.PasteUploaded) result).getURL().toString());
            } else if (result instanceof PasteResult.PasteFailed) {
                throw new IOException(((PasteResult.PasteFailed) result).getError());
            }
            throw new InternalError("unknown result type");
        }
    }*/
}

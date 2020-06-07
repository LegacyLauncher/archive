package ru.turikhay.tlauncher.ui.support;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceInputStream;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.frames.ProcessFrame;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.pastebin.Paste;
import ru.turikhay.util.pastebin.PasteResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SendInfoFrame extends ProcessFrame<SendInfoFrame.SendInfoResponse> {

    public final class SendInfoResponse {
        private final String pastebinLink;

        SendInfoResponse(String pastebinLink) {
            this.pastebinLink = StringUtil.requireNotBlank(pastebinLink);
        }

        public final String getPastebinLink() {
            return pastebinLink;
        }
    }

    private SupportFrame helpFrame;

    public SendInfoFrame() {
        setTitlePath("support.sending.title");
        getHead().setText("support.sending.head");
        setIcon("compress.png");
        pack();
    }

    public final SupportFrame getSupportFrame() {
        return helpFrame;
    }

    public final void setFrame(SupportFrame frame) {
        helpFrame = U.requireNotNull(frame);

        submit(new Process() {
            @Override
            protected SendInfoResponse get() throws Exception {
                Paste paste = new Paste();
                paste.setTitle("Diagnostic Log");
                paste.setContent(TLauncher.getInstance().getLogger().getOutput());

                PasteResult result = paste.paste();

                if (result instanceof PasteResult.PasteUploaded) {
                    return new SendInfoResponse(((PasteResult.PasteUploaded) result).getURL().toString());
                } else if (result instanceof PasteResult.PasteFailed) {
                    throw new IOException(((PasteResult.PasteFailed) result).getError());
                }
                throw new InternalError("unknown result type");
            }
        });
    }

    @Override
    protected void onSucceeded(Process process, SendInfoResponse result) {
        super.onSucceeded(process, result);
        helpFrame.setResponse(result);
    }

    @Override
    protected void onFailed(Process process, Exception e) {
        super.onFailed(process, e);

        U.log("Error sending diagnostic data", e);

        if (Alert.showLocQuestion("support.sending.error")) {
            Exception error;

            savingFile:
            {
                FileExplorer explorer;
                try {
                    explorer = FileExplorer.newExplorer();
                } catch (Exception e0) {
                    error = e0;
                    break savingFile;
                }

                explorer.setSelectedFile(new File("diagnostic.log"));

                if (explorer.showSaveDialog(this) != FileExplorer.APPROVE_OPTION) {
                    return;
                }

                if (explorer.getSelectedFile() != null) {
                    File file = explorer.getSelectedFile();
                    FileOutputStream out = null;
                    try {
                        IOUtils.copy(new CharSequenceInputStream(TLauncher.getInstance().getLogger().getOutput(), FileUtil.DEFAULT_CHARSET), out = new FileOutputStream(file));
                    } catch (Exception e0) {
                        error = e0;
                        break savingFile;
                    } finally {
                        U.close(out);
                    }

                    Alert.showLocMessage("support.saving.success", file);
                }
                return;
            }
            Alert.showLocError("support.saving.error", error);
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

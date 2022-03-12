package ru.turikhay.tlauncher.ui.versions;

import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class VersionHandlerThread {
    public final String START_DOWNLOAD = "start-download";
    public final String STOP_DOWNLOAD = "stop-download";
    public final String DELETE_BLOCK = "deleting";
    private final VersionHandler handler;
    final VersionHandlerThread.StartDownloadThread startThread;
    final VersionHandlerThread.StopDownloadThread stopThread;
    final VersionHandlerThread.VersionDeleteThread deleteThread;

    VersionHandlerThread(VersionHandler handler) {
        this.handler = handler;
        startThread = new StartDownloadThread(this);
        stopThread = new StopDownloadThread(this);
        deleteThread = new VersionDeleteThread(this);
    }

    static class StartDownloadThread extends LoopedThread {
        private final VersionHandler handler;
        private final VersionDownloadButton button;

        StartDownloadThread(VersionHandlerThread parent) {
            super("StartDownloadThread");
            handler = parent.handler;
            button = handler.list.download;
            startAndWait();
        }

        protected void iterateOnce() {
            button.setState(VersionDownloadButton.ButtonState.STOP);
            Blocker.block(handler, "start-download");
            button.startDownload();
            Blocker.unblock(handler, "start-download");
            button.setState(VersionDownloadButton.ButtonState.DOWNLOAD);
        }
    }

    static class StopDownloadThread extends LoopedThread {
        private final VersionHandler handler;
        private final VersionDownloadButton button;

        StopDownloadThread(VersionHandlerThread parent) {
            super("StopDownloadThread");
            handler = parent.handler;
            button = handler.list.download;
            startAndWait();
        }

        protected void iterateOnce() {
            Blocker.block(button.blockable, "stop-download");

            while (!handler.downloader.isThreadLocked()) {
                U.sleepFor(1000L);
            }

            button.stopDownload();
        }
    }

    static class VersionDeleteThread extends LoopedThread {
        private final VersionHandler handler;
        private final VersionRemoveButton button;

        VersionDeleteThread(VersionHandlerThread parent) {
            super("VersionDeleteThread");
            handler = parent.handler;
            button = handler.list.remove;
            startAndWait();
        }

        protected void iterateOnce() {
            Blocker.block(handler, "deleting");
            button.delete();
            Blocker.unblock(handler, "deleting");
        }
    }
}

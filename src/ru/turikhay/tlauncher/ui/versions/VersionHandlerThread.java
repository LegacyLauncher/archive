package ru.turikhay.tlauncher.ui.versions;

import ru.turikhay.tlauncher.ui.block.Blockable;
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
      this.startThread = new VersionHandlerThread.StartDownloadThread(this);
      this.stopThread = new VersionHandlerThread.StopDownloadThread(this);
      this.deleteThread = new VersionHandlerThread.VersionDeleteThread(this);
   }

   class VersionDeleteThread extends LoopedThread {
      private final VersionHandler handler;
      private final VersionRemoveButton button;

      VersionDeleteThread(VersionHandlerThread parent) {
         super("VersionDeleteThread");
         this.handler = parent.handler;
         this.button = this.handler.list.remove;
         this.startAndWait();
      }

      protected void iterateOnce() {
         Blocker.block((Blockable)this.handler, (Object)"deleting");
         this.button.delete();
         Blocker.unblock((Blockable)this.handler, (Object)"deleting");
      }
   }

   class StopDownloadThread extends LoopedThread {
      private final VersionHandler handler;
      private final VersionDownloadButton button;

      StopDownloadThread(VersionHandlerThread parent) {
         super("StopDownloadThread");
         this.handler = parent.handler;
         this.button = this.handler.list.download;
         this.startAndWait();
      }

      protected void iterateOnce() {
         Blocker.block((Blockable)this.button.blockable, (Object)"stop-download");

         while(!this.handler.downloader.isThreadLocked()) {
            U.sleepFor(1000L);
         }

         this.button.stopDownload();
      }
   }

   class StartDownloadThread extends LoopedThread {
      private final VersionHandler handler;
      private final VersionDownloadButton button;

      StartDownloadThread(VersionHandlerThread parent) {
         super("StartDownloadThread");
         this.handler = parent.handler;
         this.button = this.handler.list.download;
         this.startAndWait();
      }

      protected void iterateOnce() {
         this.button.setState(VersionDownloadButton.ButtonState.STOP);
         Blocker.block((Blockable)this.handler, (Object)"start-download");
         this.button.startDownload();
         Blocker.unblock((Blockable)this.handler, (Object)"start-download");
         this.button.setState(VersionDownloadButton.ButtonState.DOWNLOAD);
      }
   }
}

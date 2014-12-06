package ru.turikhay.tlauncher.ui.versions;

import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.versions.VersionDownloadButton.ButtonState;
import ru.turikhay.util.U;
import ru.turikhay.util.async.LoopedThread;

public class VersionHandlerThread {
	public final String
		START_DOWNLOAD = VersionHandler.START_DOWNLOAD,
		STOP_DOWNLOAD = VersionHandler.STOP_DOWNLOAD,
		DELETE_BLOCK = VersionHandler.DELETE_BLOCK;
	
	private final VersionHandler handler;
	
	final StartDownloadThread startThread;
	final StopDownloadThread stopThread;
	final VersionDeleteThread deleteThread;
	
	VersionHandlerThread(VersionHandler handler) {
		this.handler = handler;
		
		this.startThread = new StartDownloadThread(this);
		this.stopThread = new StopDownloadThread(this);
		this.deleteThread = new VersionDeleteThread(this);
	}
	
	class StartDownloadThread extends LoopedThread {
		private final VersionHandler handler;
		private final VersionDownloadButton button;
		
		StartDownloadThread(VersionHandlerThread parent) {
			super("StartDownloadThread");
			
			this.handler = parent.handler;
			this.button = handler.list.download;
			
			this.startAndWait();
		}
		
		@Override
		protected void iterateOnce() {			
			button.setState(ButtonState.STOP);
			Blocker.block(handler, START_DOWNLOAD);
			
			button.startDownload();
			
			Blocker.unblock(handler, START_DOWNLOAD);
			
			button.setState(ButtonState.DOWNLOAD);
		}
	}
	
	class StopDownloadThread extends LoopedThread {
		private final VersionHandler handler;
		private final VersionDownloadButton button;
		
		StopDownloadThread(VersionHandlerThread parent) {
			super("StopDownloadThread");
			
			this.handler = parent.handler;
			this.button = handler.list.download;
			
			this.startAndWait();
		}
		
		@Override
		protected void iterateOnce() {
			Blocker.block(button.blockable, STOP_DOWNLOAD);
			
			while(!handler.downloader.isThreadLocked())
				U.sleepFor(1000);
			
			button.stopDownload();
		}
	}
	
	class VersionDeleteThread extends LoopedThread {
		private final VersionHandler handler;
		private final VersionRemoveButton button;
		
		VersionDeleteThread(VersionHandlerThread parent) {
			super("VersionDeleteThread");
			
			this.handler = parent.handler;
			this.button = handler.list.remove;
			
			this.startAndWait();
		}
		
		@Override
		protected void iterateOnce() {
			Blocker.block(handler, DELETE_BLOCK);
			
			button.delete();
			
			Blocker.unblock(handler, DELETE_BLOCK);
		}
	}
}

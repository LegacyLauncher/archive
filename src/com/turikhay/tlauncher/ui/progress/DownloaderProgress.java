package com.turikhay.tlauncher.ui.progress;

import java.awt.Component;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.downloader.DownloadListener;
import com.turikhay.tlauncher.downloader.Downloadable;
import com.turikhay.tlauncher.downloader.Downloader;
import com.turikhay.tlauncher.ui.loc.LocalizableProgressBar;

public class DownloaderProgress extends LocalizableProgressBar {
	private static final long serialVersionUID = -8382205925341380876L;
	
	private final DownloaderProgress instance;
	
	private DownloadListener listener;

	public DownloaderProgress(Component parentComp, Downloader downloader) {
		super(parentComp);
		
		if(downloader == null)
			throw new NullPointerException();
		
		this.instance = this;
		
		listener = new DownloadListener(){
			@Override
			public void onDownloaderStart(Downloader d, int files) {
				instance.startProgress();
			
				setIndeterminate(true);
			
				setCenterString("progressBar.init");
				setEastString("progressBar.downloading", files);
			}

			@Override
			public void onDownloaderAbort(Downloader d) {
				instance.stopProgress();
			}
		
			@Override
			public void onDownloaderComplete(Downloader d) {
				instance.stopProgress();
			}

			@Override
			public void onDownloaderError(Downloader d, Downloadable file, Throwable error) {}

			@Override
			public void onDownloaderProgress(Downloader d, int progress, double speed) {
				if(progress > 0){
					if(getValue() > progress) return; // Something from a "lazy" thread, ignore.
					
					setIndeterminate(false);
					setValue(progress);
					setCenterString(progress + "%");		
				}
			}
			
			@Override
			public void onDownloaderFileComplete(Downloader d, Downloadable file) {
				setIndeterminate(false);
				
				setWestString("progressBar.completed", file.getFilename());
				setEastString("progressBar.remaining", d.getRemaining());
			}
		};
		downloader.addListener(listener);
		
		stopProgress(); // Hide the bar
	}
	
	public DownloaderProgress(Component parentComp) {
		this(parentComp, TLauncher.getInstance().getDownloader());
	}
}

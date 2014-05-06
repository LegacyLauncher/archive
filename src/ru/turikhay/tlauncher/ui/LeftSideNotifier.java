package ru.turikhay.tlauncher.ui;

import java.awt.Image;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.listener.UpdateUIListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.ImagePanel;
import ru.turikhay.tlauncher.updater.Ad;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;

public class LeftSideNotifier extends ImagePanel implements UpdaterListener {
	private static final long serialVersionUID = 8089346864504410975L;
	private static final String LANG_PREFIX = "notifier.left.";
	
	private NotifierStatus status;
	private Update update;
	
	protected LeftSideNotifier() {
		super((Image) null, 1.0F, 0.75F, false, true);
		
		TLauncher.getInstance().getUpdater().addListener(this);
	}
	
	public NotifierStatus getStatus() {
		return status;
	}
	
	public void setStatus(NotifierStatus status) {
		if(status == null)
			throw new NullPointerException();
		
		this.status = status;
		
		this.setImage(status.getImage());
		if(status == NotifierStatus.NONE) hide(); else show();
	}
	
	protected boolean onClick() {
		boolean result = processClick();
		
		if(result)
			hide();
		
		return result;
	}
	
	private boolean processClick() {
		if(!super.onClick())
			return false;
		
		switch(status) {
		case FAILED:
			Alert.showLocAsyncWarning(LANG_PREFIX + status.toString());
			
			break;			
		case FOUND:
			if(update == null)
				throw new IllegalStateException("Update is NULL!");
			
			String
				prefix = LANG_PREFIX + status.toString() +".",
				title = prefix + "title",
				question = prefix + "question";
			
			boolean ask = Alert.showQuestion(
				Localizable.get(title),
				Localizable.get( question, update.getVersion() ), update.getDescription()
			);
			
			if(!ask)
				return false;
			
			UpdateUIListener listener = new UpdateUIListener(update);
			listener.push();
			
			break;
		case NONE:
			break;
		default:
			throw new IllegalStateException("Unknown status: "+ status);
		}
		
		return true;		
	}

	@Override
	public void onUpdaterRequesting(Updater u) {
		setFoundUpdate(null);
	}

	@Override
	public void onUpdaterRequestError(Updater u) {
		setStatus(NotifierStatus.FAILED);
	}

	@Override
	public void onUpdateFound(Update upd) {
		if(upd.isRequired()) return; // Ingore required update, let it be processed by RequiredUpdateListener
		setFoundUpdate(upd);
	}

	@Override
	public void onUpdaterNotFoundUpdate(Updater u) {
		setFoundUpdate(null);
	}

	@Override
	public void onAdFound(Updater u, Ad ad) {
		setFoundUpdate(null);
	}
	
	private void setFoundUpdate(Update upd) {
		this.update = upd;
		
		setStatus(upd == null? NotifierStatus.NONE : NotifierStatus.FOUND);
	}
	
	public enum NotifierStatus {
		FAILED("warning.png"), FOUND("down32.png"), NONE;
		
		private final Image image;
		
		NotifierStatus(String imagePath) {
			this.image = imagePath == null? null : ImageCache.getImage(imagePath);
		}
		
		NotifierStatus() {
			this(null);
		}
		
		public Image getImage() {
			return image;
		}
		
		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}
}

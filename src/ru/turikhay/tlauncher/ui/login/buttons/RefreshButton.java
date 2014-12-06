package ru.turikhay.tlauncher.ui.login.buttons;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ComponentManager;
import ru.turikhay.tlauncher.managers.ComponentManagerListener;
import ru.turikhay.tlauncher.managers.ComponentManagerListenerHelper;
import ru.turikhay.tlauncher.ui.block.Blockable;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.swing.ImageButton;
import ru.turikhay.tlauncher.updater.AdParser.AdMap;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.async.AsyncThread;

public class RefreshButton extends ImageButton implements Blockable,
ComponentManagerListener, UpdaterListener {
	private static final long serialVersionUID = -1334187593288746348L;
	private final static int TYPE_REFRESH = 0;
	private final static int TYPE_CANCEL = 1;

	private LoginForm lf;
	private int type;
	private final Image refresh = loadImage("refresh.png"),
			cancel = loadImage("cancel.png");
	private Updater updaterFlag;

	private RefreshButton(LoginForm loginform, int type) {
		this.lf = loginform;

		this.rotation = ImageRotation.CENTER;
		this.setType(type, false);

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onPressButton();
			}
		});

		this.initImage();

		TLauncher.getInstance().getManager()
		.getComponent(ComponentManagerListenerHelper.class)
		.addListener(this);
		TLauncher.getInstance().getUpdater().addListener(this);
	}

	RefreshButton(LoginForm loginform) {
		this(loginform, TYPE_REFRESH);
	}

	private void onPressButton() {
		switch (type) {
		case TYPE_REFRESH:
			if(TLauncher.isBeta())
				TLauncher.getInstance().getUpdater().asyncFindUpdate();
			else if (updaterFlag != null)
				updaterFlag.asyncFindUpdate();
			else
				AsyncThread.execute(new Runnable() {
					@Override
					public void run() {
						lf.scene.infoPanel.updateAd(true);
					}
				});

			TLauncher.getInstance().getManager().startAsyncRefresh();
			break;
		case TYPE_CANCEL:
			TLauncher.getInstance().getManager().stopRefresh();
			break;
		default:
			throw new IllegalArgumentException("Unknown type: " + type
					+ ". Use RefreshButton.TYPE_* constants.");
		}

		lf.defocus();
	}

	void setType(int type) {
		this.setType(type, true);
	}

	void setType(int type, boolean repaint) {
		switch (type) {
		case TYPE_REFRESH:
			this.image = refresh;
			break;
		case TYPE_CANCEL:
			this.image = cancel;
			break;
		default:
			throw new IllegalArgumentException("Unknown type: " + type
					+ ". Use RefreshButton.TYPE_* constants.");
		}

		this.type = type;
	}

	@Override
	public void onUpdaterRequesting(Updater u) {
	}

	@Override
	public void onUpdaterRequestError(Updater u) {
		this.updaterFlag = u;
	}

	@Override
	public void onUpdateFound(Update upd) {
		this.updaterFlag = null;
	}

	@Override
	public void onUpdaterNotFoundUpdate(Updater u) {
		this.updaterFlag = null;
	}

	@Override
	public void onAdFound(Updater u, AdMap adMap) {
	}

	@Override
	public void onComponentsRefreshing(ComponentManager manager) {
		Blocker.block(this, LoginForm.REFRESH_BLOCK);
	}

	@Override
	public void onComponentsRefreshed(ComponentManager manager) {
		Blocker.unblock(this, LoginForm.REFRESH_BLOCK);
	}

	//

	@Override
	public void block(Object reason) {
		if (reason.equals(LoginForm.REFRESH_BLOCK))
			setType(TYPE_CANCEL);
		else
			setEnabled(false);
	}

	@Override
	public void unblock(Object reason) {
		if (reason.equals(LoginForm.REFRESH_BLOCK))
			setType(TYPE_REFRESH);
		setEnabled(true);
	}
}

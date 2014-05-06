package ru.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Point;

import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.tlauncher.ui.progress.LaunchProgress;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.scenes.PseudoScene;
import ru.turikhay.tlauncher.ui.scenes.VersionManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public class MainPane extends ExtendedLayeredPane {
	private static final long serialVersionUID = -8854598755786867602L;

	private final TLauncherFrame rootFrame;

	private PseudoScene scene;

	public final BackgroundHolder background;
	public final LaunchProgress progress;

	public final DefaultScene defaultScene;
	public final AccountEditorScene accountEditor;
	public final VersionManagerScene versionManager;

	public final LeftSideNotifier warning;

	MainPane(TLauncherFrame frame) {
		super(null); // TLauncherFrame will determine MainPane size with layout
						// manager

		this.rootFrame = frame;

		this.background = new BackgroundHolder(this);
		background.setBackground(background.SLIDE_BACKGROUND, false);
		this.add(background);

		this.defaultScene = new DefaultScene(this);
		this.add(defaultScene);

		this.accountEditor = new AccountEditorScene(this);
		this.add(accountEditor);
		
		this.versionManager = new VersionManagerScene(this);
		this.add(versionManager);

		this.progress = new LaunchProgress(frame);
		this.add(progress);

		this.warning = new LeftSideNotifier();
		warning.setLocation(10, 10);
		this.add(warning);

		this.setScene(defaultScene, false);
	}

	public PseudoScene getScene() {
		return scene;
	}

	public void setScene(PseudoScene scene) {
		this.setScene(scene, true);
	}

	public void setScene(PseudoScene scene, boolean animate) {
		if (scene == null)
			throw new NullPointerException();

		if (scene.equals(this.scene))
			return;

		for (Component comp : getComponents())
			if (!comp.equals(scene) && comp instanceof PseudoScene)
				((PseudoScene) comp).setShown(false, animate);

		this.scene = scene;
		this.scene.setShown(true);
	}

	public void openDefaultScene() {
		setScene(defaultScene);
	}

	public void openAccountEditor() {
		setScene(accountEditor);
	}
	
	public void openVersionManager() {
		setScene(versionManager);
	}

	public TLauncherFrame getRootFrame() {
		return rootFrame;
	}

	public LaunchProgress getProgress() {
		return progress;
	}

	@Override
	public void onResize() {
		super.onResize();

		progress.setBounds(0, getHeight() - ProgressBar.DEFAULT_HEIGHT + 1,
				getWidth(), ProgressBar.DEFAULT_HEIGHT);
	}

	/**
	 * Location of some components can be determined only with
	 * <code>getLocationOnScreen()</code> method. This method should help to
	 * find out the location of a <code>Component</code> on the
	 * <code>MainPane</code>.
	 * 
	 */
	public Point getLocationOf(Component comp) {
		Point compLocation = comp.getLocationOnScreen(), paneLocation = getLocationOnScreen();

		return new Point(compLocation.x - paneLocation.x, compLocation.y
				- paneLocation.y);
	}
}
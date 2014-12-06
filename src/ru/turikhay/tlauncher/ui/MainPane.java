package ru.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.tlauncher.ui.progress.LaunchProgress;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.scenes.PseudoScene;
import ru.turikhay.tlauncher.ui.scenes.VersionManagerScene;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;
import ru.turikhay.util.OS;

public class MainPane extends ExtendedLayeredPane {
	private static final long serialVersionUID = -8854598755786867602L;

	private final TLauncherFrame rootFrame;
	private final boolean repaintEveryTime;

	private PseudoScene scene;

	public final BackgroundHolder background;
	public final LaunchProgress progress;

	public final DefaultScene defaultScene;
	public final AccountEditorScene accountEditor;
	public final VersionManagerScene versionManager;

	public final SideNotifier notifier; // its location is defined from DefaultScene

	final ServicePanel service;

	MainPane(TLauncherFrame frame) {
		super(null);

		this.rootFrame = frame;
		this.repaintEveryTime = OS.LINUX.isCurrent(); // Yup, Swing under Linux doesn't work well.

		this.background = new BackgroundHolder(this);
		background.setBackground(background.SLIDE_BACKGROUND, false);
		this.add(background);

		this.service = new ServicePanel(this);

		this.notifier = new SideNotifier();
		notifier.setSize(32, 32);
		add(notifier);

		this.defaultScene = new DefaultScene(this);
		this.add(defaultScene);

		this.accountEditor = new AccountEditorScene(this);
		this.add(accountEditor);

		this.versionManager = new VersionManagerScene(this);
		this.add(versionManager);

		this.progress = new LaunchProgress(frame);
		this.add(progress);

		this.setScene(defaultScene, false);

		this.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				onResize(); // Fix
			}
		});
	}

	public PseudoScene getScene() {
		return scene;
	}

	public void setScene(PseudoScene scene) {
		this.setScene(scene, true);
	}

	public void setScene(PseudoScene newscene, boolean animate) {
		if (newscene == null)
			throw new NullPointerException();

		if (newscene.equals(this.scene))
			return;

		for (Component comp : getComponents())
			if (!comp.equals(newscene) && comp instanceof PseudoScene)
				((PseudoScene) comp).setShown(false, animate);

		this.scene = newscene;
		this.scene.setShown(true);

		if(repaintEveryTime)
			repaint();
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
		progress.setBounds(0, getHeight() - ProgressBar.DEFAULT_HEIGHT + 1,
				getWidth(), ProgressBar.DEFAULT_HEIGHT);
		service.onResize();
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

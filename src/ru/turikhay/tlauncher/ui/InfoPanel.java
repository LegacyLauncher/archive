package ru.turikhay.tlauncher.ui;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.minecraft.launcher.updater.VersionSyncInfo;

import org.apache.commons.lang3.StringUtils;

import ru.turikhay.exceptions.ParseException;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.ServerList.Server;
import ru.turikhay.tlauncher.managers.VersionManager;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.center.CenterPanel;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.login.LoginForm;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene;
import ru.turikhay.tlauncher.ui.scenes.DefaultScene.SidePanel;
import ru.turikhay.tlauncher.ui.swing.AnimatorAction;
import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.editor.ExtendedHTMLEditorKit;
import ru.turikhay.tlauncher.ui.swing.editor.HyperlinkProcessor;
import ru.turikhay.tlauncher.updater.AdParser.Ad;
import ru.turikhay.tlauncher.updater.AdParser.AdList;
import ru.turikhay.tlauncher.updater.AdParser.AdMap;
import ru.turikhay.tlauncher.updater.Update;
import ru.turikhay.tlauncher.updater.Updater;
import ru.turikhay.tlauncher.updater.UpdaterListener;
import ru.turikhay.util.U;
import ru.turikhay.util.async.ExtendedThread;

public class InfoPanel extends CenterPanel implements ResizeableComponent, UpdaterListener, LocalizableComponent {
	private static final int MARGIN = DefaultScene.EDGE_INSETS;
	private static final float FONT_SIZE = 12f;

	private final InfoPanelAnimator animator;

	private final EditorPane browser;

	private final DefaultScene parent;

	private final Object animationLock;

	private final int timeFrame;
	private float opacity;
	private boolean shown, canshow;

	private AdMap lastAd;
	private String content;
	private int width, height;

	public InfoPanel(DefaultScene p) {
		super(CenterPanel.tipTheme, new Insets(5, 10, 5, 10));

		this.animator = new InfoPanelAnimator();

		this.animationLock = new Object();
		this.timeFrame = 5;

		this.parent = p;

		this.browser = new EditorPane(getFont().deriveFont(FONT_SIZE));

		if(browser.getEditorKit() instanceof ExtendedHTMLEditorKit)
			((ExtendedHTMLEditorKit) browser.getEditorKit()).setHyperlinkProcessor(new HyperlinkProcessor() {
				@Override
				public void process(String link) {

					if(link != null && link.startsWith("server:")) {

						if(Blocker.isBlocked(parent.loginForm))
							return;

						// server:%ip%:%port%:%version%
						try {
							openServer(link);
							return;
						} catch(Exception e) {
							Alert.showLocError("ad.server.error", new RuntimeException("link: \""+link+"\"", e));
							return;
						}
					}

					ExtendedHTMLEditorKit.defaultHyperlinkProcessor.process(link);
				}

				private void openServer(String link) throws ParseException {
					String[] info = StringUtils.split(link.substring("server:".length()), ';');

					if(info.length != 4)
						throw new ParseException("split incorrectly");

					if(StringUtils.isEmpty(info[0]))
						throw new ParseException("ip is not defined");

					if(StringUtils.isEmpty(info[1]))
						throw new ParseException("port is not defined");

					Integer.parseInt(info[1]);

					if(StringUtils.isEmpty(info[2]))
						throw new ParseException("version is not defined");

					if(StringUtils.isEmpty(info[3]))
						throw new ParseException("name is not defined");

					VersionManager vm = TLauncher.getInstance().getVersionManager();
					VersionSyncInfo versionSync = vm.getVersionSyncInfo(info[2]);

					if(versionSync == null)
						throw new IllegalArgumentException("cannot find version: "+ info[2]);

					LoginForm lf = TLauncher.getInstance().getFrame().mp.defaultScene.loginForm;
					lf.versions.setSelectedValue(versionSync);

					if(!versionSync.equals(lf.versions.getSelectedItem()))
						throw new RuntimeException("cannot select version: "+ versionSync);

					Server server = new Server();
					server.setName(info[3]);
					server.setVersion(info[2]);
					server.setAddress(info[0] +':'+ info[1]);

					lf.startLauncher(server);
				}
			});

		browser.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!onClick())
					e.consume();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if(!isVisible())
					e.consume();
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if(!isVisible())
					e.consume();
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if(!isVisible())
					e.consume();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				if(!isVisible())
					e.consume();
			}
		});

		add(browser);

		this.shown = false;
		this.setVisible(false);

		TLauncher.getInstance().getUpdater().addListener(this);
	}

	void setContent(String text, int width, int height) {
		if (width < 1 || height < 1)
			throw new IllegalArgumentException();

		this.width = width;
		this.height = height;

		browser.setText(text);

		onResize();
	}

	@Override
	public void onResize() {
		Graphics g = getGraphics();
		if (g == null)
			return;

		Insets insets = getInsets();

		int
		compWidth = width + insets.left + insets.right,
		compHeight = height + insets.top + insets.bottom;

		Point loginFormLocation = parent.loginForm.getLocation();
		Dimension loginFormSize = parent.loginForm.getSize();

		int x = loginFormLocation.x + loginFormSize.width / 2 - compWidth / 2, y;

		if(x + compWidth > parent.getWidth() - MARGIN)
			x = parent.getWidth() - compWidth - MARGIN;

		if(x < MARGIN)
			x = MARGIN;

		switch(parent.getLoginFormDirection()) {
		case TOP_LEFT:
		case CENTER_LEFT:
		case TOP:
		case TOP_RIGHT:
		case CENTER:
		case CENTER_RIGHT:
			y = loginFormLocation.y + loginFormSize.height + MARGIN;
			break;
		case BOTTOM_LEFT:
		case BOTTOM:
		case BOTTOM_RIGHT:
			y = loginFormLocation.y - compHeight - MARGIN;
			break;
		default:
			throw new IllegalArgumentException();
		}

		if(y + compHeight > parent.getHeight() - MARGIN)
			y = parent.getHeight() - compHeight - MARGIN;

		if(y < MARGIN)
			y = MARGIN;

		setBounds(x, y, compWidth, compHeight);
	}

	@Override
	public void paint(Graphics g0) {
		Graphics2D g = (Graphics2D) g0;
		Composite oldComp = g.getComposite();

		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
				opacity));

		super.paint(g0);

		g.setComposite(oldComp);
	}

	@Override
	public void show(boolean animate) {
		if (!canshow)
			return;

		onResize();

		if (shown)
			return;

		synchronized (animationLock) {
			setVisible(true);
			browser.setVisible(true);
			opacity = 0.0F;

			float selectedOpacity = 1.0F;

			if (animate)
				while (opacity < selectedOpacity) {
					opacity += 0.01F;
					if (opacity > selectedOpacity)
						opacity = selectedOpacity;

					this.repaint();
					U.sleepFor(timeFrame);
				}
			else {
				opacity = selectedOpacity;
				this.repaint();
			}

			shown = true;
		}
	}

	@Override
	public void show() {
		animator.act(AnimatorAction.SHOW);
	}

	void hide(boolean animate) {
		if (!shown)
			return;

		synchronized (animationLock) {
			if (animate)
				while (opacity > 0.0F) {
					opacity -= 0.01F;
					if (opacity < 0.0F)
						opacity = 0.0F;

					this.repaint();
					U.sleepFor(timeFrame);
				}

			setVisible(false);
			browser.setVisible(false);
			if (!animate)
				opacity = 0.0F;
			shown = false;
		}
	}

	@Override
	public void hide() {
		animator.act(AnimatorAction.HIDE);
	}

	public void setShown(boolean shown, boolean animate) {
		if(animate) {
			if(shown) show();
			else hide();
		} else {
			if(shown) show(false);
			else hide(false);
		}
	}

	boolean onClick() {
		return isEnabled() && shown;
	}

	@Override
	public void onUpdaterRequesting(Updater u) {
		hide(true);
	}

	@Override
	public void onUpdaterRequestError(Updater u) {
	}

	@Override
	public void onUpdateFound(Update upd) {
	}

	@Override
	public void onUpdaterNotFoundUpdate(Updater u) {
	}

	@Override
	public void onAdFound(Updater u, AdMap adMap) {
		this.lastAd = adMap;
		updateAd(true);
	}

	@Override
	public void updateLocale() {
		updateAd();
	}

	public void updateAd(boolean animate) {
		hide(animate);

		canshow = prepareAd();

		if (parent.getSidePanel() != SidePanel.SETTINGS)
			show(animate);
	}

	public void updateAd() {
		updateAd(false);
	}

	private boolean prepareAd() {
		if(lastAd == null)
			return false;

		String locale = parent.getMainPane().getRootFrame().getLauncher().getSettings().getLocale().toString();
		AdList adList = lastAd.getByName(locale);

		if(adList == null || adList.getAds().isEmpty())
			return false;

		Ad ad = adList.getRandom();

		if(ad == null)
			return false;

		StringBuilder builder = new StringBuilder();

		builder
		.append("<table width=\"")
		.append(ad.getWidth())
		.append("\" height=\"")
		.append(ad.getHeight())
		.append("\"><tr><td align=\"center\" valign=\"center\">");

		if(ad.getImage() != null)
			builder
			.append("<img src=\"")
			.append(ad.getImage())
			.append("\" /></td><td align=\"center\" valign=\"center\" width=\"100%\">");

		builder
		.append(ad.getContent());

		builder
		.append("</td></tr></table>");

		this.content = builder.toString();
		setContent(this.content, ad.getWidth(), ad.getHeight());

		return true;
	}

	private class InfoPanelAnimator extends ExtendedThread {
		private AnimatorAction currentAction;

		InfoPanelAnimator() {
			startAndWait();
		}

		void act(AnimatorAction action) {
			if(action == null)
				throw new NullPointerException("action");

			this.currentAction = action;

			if(isThreadLocked())
				unlockThread("start");
		}

		@Override
		public void run() {
			lockThread("start");

			while(true) {
				while(currentAction == null)
					U.sleepFor(100);

				AnimatorAction action = currentAction;

				switch(action) {
				case SHOW:
					show(true);
					break;
				case HIDE:
					hide(true);
					break;
				default:
					throw new RuntimeException("unknown action: "+ currentAction);
				}

				if(currentAction == action)
					currentAction = null;
			}
		}
	}

	@Override
	public void block(Object reason) {
	}

	@Override
	public void unblock(Object reason) {
	}
}

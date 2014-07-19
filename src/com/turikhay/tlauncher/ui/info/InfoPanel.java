package com.turikhay.tlauncher.ui.info;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JEditorPane;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.swing.ResizeableComponent;
import com.turikhay.tlauncher.ui.swing.extended.EditorPane;
import com.turikhay.tlauncher.updater.Ad;
import com.turikhay.tlauncher.updater.Update;
import com.turikhay.tlauncher.updater.Updater;
import com.turikhay.tlauncher.updater.UpdaterListener;
import com.turikhay.util.U;

public class InfoPanel extends CenterPanel implements ResizeableComponent,
		UpdaterListener {
	private static final long serialVersionUID = 3310876991994323902L;

	private static final int MARGIN = 20;

	private final JEditorPane browser;

	private final DefaultScene parent;

	private final Object animationLock;

	private final int timeFrame;
	private float opacity;
	private boolean shown;

	private String content;
	private int width, height;

	public InfoPanel(DefaultScene parent) {
		super(CenterPanel.tipTheme, new Insets(5, 10, 5, 10));

		this.animationLock = new Object();
		this.timeFrame = 5;

		this.addMouseListener(new MouseListener() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!onClick())
					e.consume();
			}

			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

			@Override
			public void mouseExited(MouseEvent e) {
			}
		});

		this.parent = parent;
		this.browser = new EditorPane();

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

		int compWidth = width + insets.left + insets.right, compHeight = height
				+ insets.top + insets.bottom;

		Point loginFormLocation = parent.loginForm.getLocation();
		Dimension loginFormSize = parent.loginForm.getSize();

		int x = loginFormLocation.x + loginFormSize.width / 2 - compWidth / 2, y = loginFormLocation.y
				+ loginFormSize.height + MARGIN;

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
		if (content == null)
			return;

		onResize();

		if (shown)
			return;

		synchronized (animationLock) {
			this.setVisible(true);
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
		show(true);
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

			this.setVisible(false);
			if (!animate)
				opacity = 0.0F;
			shown = false;
		}
	}

	@Override
	public void hide() {
		hide(true);
	}

	public void setShown(boolean shown, boolean animate) {
		if (shown)
			show(animate);
		else
			hide(animate);
	}

	boolean onClick() {
		return shown;
	}

	@Override
	public void onUpdaterRequesting(Updater u) {
		hide();
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
	public void onAdFound(Updater u, Ad ad) {
		int[] size = ad.getSize();

		String content = "<table width=\"" + size[0] + "\" height=\"" + size[1]
				+ "\"" + "align=\"center\"><tr><td>";

		if (ad.getImage() != null)
			content += "<img src=\"" + ad.getImage().toExternalForm()
					+ "\" /></td><td>";

		content += ad.getContent();
		content += "</td></tr></table>";

		this.content = content;

		setContent(content, size[0], size[1]);
		if (!parent.isSettingsShown())
			show();
	}
}

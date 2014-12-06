package ru.turikhay.tlauncher.ui.progress;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JProgressBar;

import ru.turikhay.tlauncher.ui.TLauncherFrame;
import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

public class ProgressBar extends JProgressBar {
	public static int DEFAULT_HEIGHT = 20;
	private static int BORDER_SIZE = 10;
	private static int EDGE_CHARS = 50;
	private static int CENTER_CHARS = 30;
	private static float DEFAULT_FONT_SIZE = TLauncherFrame.fontSize;
	private static final long serialVersionUID = -8095192709934629794L;

	private final Object sync;
	private final Component parent;

	private String wS, cS, eS; // West string, center string, east string
	private boolean wS_changed, cS_changed, eS_changed;

	// Arrays containing bounds of strings: x, width, height
	private int wS_x, cS_x, eS_x;
	private int oldWidth;

	public ProgressBar(Component parentComp) {
		this.sync = new Object();

		this.parent = parentComp;
		if (parent != null)
			parent.addComponentListener(new ComponentListener() {
				@Override
				public void componentResized(ComponentEvent e) {
					updateSize();
				}

				@Override
				public void componentMoved(ComponentEvent e) {
				}

				@Override
				public void componentShown(ComponentEvent e) {
				}

				@Override
				public void componentHidden(ComponentEvent e) {
				}
			});

		setFont(getFont().deriveFont(DEFAULT_FONT_SIZE));
		setOpaque(false);
	}

	public ProgressBar() {
		this(null);
	}

	private void updateSize() {
		if (parent == null)
			return;
		setPreferredSize(new Dimension(parent.getWidth(), DEFAULT_HEIGHT));
	}

	public void setStrings(String west, String center, String east,
			boolean acceptNull, boolean repaint) {
		if (acceptNull || west != null)
			this.setWestString(west, false);
		if (acceptNull || center != null)
			this.setCenterString(center, false);
		if (acceptNull || east != null)
			this.setEastString(east, false);

		if (repaint)
			this.repaint();
	}

	public void setStrings(String west, String center, String east) {
		this.setStrings(west, center, east, true, true);
	}

	public void setWestString(String string, boolean update) {
		string = StringUtil.cut(string, EDGE_CHARS);

		this.wS_changed = wS != string;
		this.wS = string;

		if (wS_changed && update)
			this.repaint();
	}

	public void setWestString(String string) {
		this.setWestString(string, true);
	}

	public void setCenterString(String string, boolean update) {
		string = StringUtil.cut(string, CENTER_CHARS);

		this.cS_changed = cS != string;
		this.cS = string;

		if (cS_changed && update)
			this.repaint();
	}

	public void setCenterString(String string) {
		this.setCenterString(string, true);
	}

	public void setEastString(String string, boolean update) {
		string = StringUtil.cut(string, EDGE_CHARS);

		this.eS_changed = eS != string;
		this.eS = string;

		if (eS_changed && update)
			this.repaint();
	}

	public void setEastString(String string) {
		this.setEastString(string, true);
	}

	public void clearProgress() {
		this.setIndeterminate(false);
		this.setValue(0);
		this.setStrings(null, null, null, true, false);
	}

	public void startProgress() {
		this.clearProgress();

		this.updateSize();
		this.setVisible(true);
	}

	public void stopProgress() {
		this.setVisible(false);
		this.clearProgress();
	}

	private void draw(Graphics g) {
		boolean drawWest = wS != null, drawCenter = cS != null, drawEast = eS != null;

		if (!(drawWest || drawCenter || drawEast))
			return; // Nothing to draw.

		Font font = g.getFont();
		FontMetrics fm = g.getFontMetrics(font);
		int width = getWidth();

		boolean force = (width != oldWidth);
		this.oldWidth = width;

		if (drawCenter && (force || cS_changed)) {
			cS_x = (width / 2) - (fm.stringWidth(cS) / 2);
			cS_changed = false;
		}

		if (drawWest && (force || wS_changed)) {
			wS_x = BORDER_SIZE;
			wS_changed = false;
		}

		if (drawEast && (force || eS_changed)) {
			eS_x = width - fm.stringWidth(eS) - BORDER_SIZE;
			eS_changed = false;
		}

		Graphics2D g2D = (Graphics2D) g;

		g.setColor(Color.black);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setFont(font);

		drawString(g, wS, wS_x);
		drawString(g, cS, cS_x);
		drawString(g, eS, eS_x);
	}

	private void drawString(Graphics g, String s, int x) {
		if (s == null)
			return;

		int y = (getHeight() - g.getFontMetrics().getDescent() + g.getFontMetrics().getAscent()) / 2;

		g.setColor(Color.white);
		for (int borderX = -1; borderX < 2; borderX++)
			for (int borderY = -1; borderY< 2; borderY++)
				g.drawString(s, x + borderX, y + borderY);

		g.setColor(Color.black);
		g.drawString(s, x, y);
	}

	@Override
	public void update(Graphics g) {
		try {
			super.update(g);
		} catch (Exception e) {
			U.log("Error updating progress bar:", e.toString());
			return;
		}

		synchronized (sync) {
			draw(g);
		}
	}

	@Override
	public void paint(Graphics g) {
		try {
			super.paint(g);
		} catch (Exception e) {
			U.log("Error paining progress bar:", e.toString());
			return;
		}

		synchronized (sync) {
			this.draw(g);
		}
	}
}

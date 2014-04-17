package com.turikhay.tlauncher.ui.progress;

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
import java.awt.font.LineMetrics;

import javax.swing.JProgressBar;

import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.util.U;

public class ProgressBar extends JProgressBar {
	public static int DEFAULT_HEIGHT = 20;
	private static int BOUNDS_SIZE = 3;
	private static int BORDER_SIZE = 10;
	private static int EDGE_CHARS = 50;
	private static int CENTER_CHARS = 20;
	private static float DEFAULT_FONT_SIZE = TLauncherFrame.fontSize;
	private static final long serialVersionUID = -8095192709934629794L;

	private final Object sync;
	private final Component parent;

	private String wS, cS, eS; // West string, center string, east string
	private boolean wS_changed, cS_changed, eS_changed;

	// Arrays containing bounds of strings: zero elem: x, 1th: text width, 2nd:
	// text height (and y);
	private final int[] wS_bounds, cS_bounds, eS_bounds;

	private int oldWidth;

	public ProgressBar(Component parentComp) {
		this.sync = new Object();

		wS_bounds = new int[BOUNDS_SIZE];
		cS_bounds = new int[BOUNDS_SIZE];
		eS_bounds = new int[BOUNDS_SIZE];

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
		string = U.r(string, EDGE_CHARS);

		this.wS_changed = wS != string;
		this.wS = string;

		if (wS_changed && update)
			this.repaint();
	}

	public void setWestString(String string) {
		this.setWestString(string, true);
	}

	public void setCenterString(String string, boolean update) {
		string = U.r(string, CENTER_CHARS);

		this.cS_changed = cS != string;
		this.cS = string;

		if (cS_changed && update)
			this.repaint();
	}

	public void setCenterString(String string) {
		this.setCenterString(string, true);
	}

	public void setEastString(String string, boolean update) {
		string = U.r(string, EDGE_CHARS);

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
			LineMetrics lm = fm.getLineMetrics(cS, g);

			cS_bounds[1] = fm.stringWidth(cS);
			cS_bounds[2] = (int) lm.getHeight();

			cS_bounds[0] = (width / 2) - (cS_bounds[1] / 2);

			cS_changed = false;
		}

		if (drawWest && (force || wS_changed)) {
			LineMetrics lm = fm.getLineMetrics(wS, g);

			wS_bounds[1] = fm.stringWidth(wS);
			wS_bounds[2] = (int) lm.getHeight();

			wS_bounds[0] = BORDER_SIZE;

			wS_changed = false;
		}

		if (drawEast && (force || eS_changed)) {
			LineMetrics lm = fm.getLineMetrics(eS, g);

			eS_bounds[1] = fm.stringWidth(eS);
			eS_bounds[2] = (int) lm.getHeight();

			eS_bounds[0] = width - eS_bounds[1] - BORDER_SIZE;

			eS_changed = false;
		}

		Graphics2D g2D = (Graphics2D) g;

		g.setColor(Color.black);
		g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		g.setFont(font);
		this.drawString(g, wS, wS_bounds);
		this.drawString(g, cS, cS_bounds);
		this.drawString(g, eS, eS_bounds);
	}

	private void drawString(Graphics g, String s, int[] bounds) {
		if (s == null)
			return;

		g.setColor(Color.white);
		for (int x = -1; x < 2; x++)
			for (int y = -1; y < 2; y++)
				g.drawString(s, bounds[0] + x, bounds[2] + y);

		g.setColor(Color.black);
		g.drawString(s, bounds[0], bounds[2]);
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

package ru.turikhay.tlauncher.ui.background.legacy;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;

import ru.turikhay.tlauncher.ui.background.BackgroundHolder;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.util.U;

public class LegacyBackground extends PaintBackground {
	private static final long serialVersionUID = -3732711088655124975L;

	private Image grass = ImageCache.getImage("grass.png");
	private Image sun = ImageCache.getImage("sun.png");
	private Image glow = ImageCache.getImage("glow.png");

	private final int grassW;
	private final int grassH;
	private final int sunW;
	private final int sunH;
	private final int glowW;
	private final int glowH;
	double sunLocation;
	private Color backgroundColor = new Color(141, 189, 233);

	public LegacyBackground(BackgroundHolder holder, double loc) {
		super(holder);

		setBackground(backgroundColor);

		grassW = grass.getWidth(null);
		grassH = grass.getHeight(null);

		sunW = sun.getWidth(null);
		sunH = sun.getHeight(null);

		glowW = glow.getWidth(null);
		glowH = glow.getHeight(null);

		relativeSize = .5;

		sunLocation = (loc <= 0) ? U.doubleRandom() : loc;
		if (sunLocation <= 0)
			sunLocation += 0.5;
	}

	@Override
	protected void draw(Graphics2D g, boolean force) {
		drawGrass(g);
		drawGlow(g);
		drawSun(g);
	}

	void drawGrass(Graphics2D g) {
		for (int rw = 0; rw <= width;) {
			g.drawImage(grass, rw, height - grassH, null);

			rw += grassW;
		}
	}

	void drawSun(Graphics2D g) {
		int x = (int) (width * sunLocation - sunW / 2), // ((Math.abs((sunW -
														// width)) *
														// (sunLocation + 1)) /
														// 2), //(width / 2 -
														// sunW / 2 + (width / 2
														// - sunW / 2) *
														// sunLocation),
		y = height - grassH - sunH;
		g.drawImage(sun, x, y, sunW, sunH, null);

		g.setColor(backgroundColor);

		g.fillRect(0, 0, x, height - grassH - glowH);
		g.fillRect(0, 0, width, y);
		g.fillRect(x + sunW, y, sunW, sunH - glowH);
	}

	void drawGlow(Graphics2D g) {
		for (int rw = 0; rw <= width;) {
			g.drawImage(glow, rw, height - grassH - glowH, null);

			rw += glowW;
		}
	}
}

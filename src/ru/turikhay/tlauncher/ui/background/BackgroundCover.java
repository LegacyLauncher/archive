package ru.turikhay.tlauncher.ui.background;

import java.awt.Color;
import java.awt.Graphics;

import ru.turikhay.tlauncher.ui.swing.ResizeableComponent;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.U;

public class BackgroundCover extends ExtendedPanel implements
ResizeableComponent {
	private static final long serialVersionUID = -1801217638400760969L;

	private static final double opacityStep = 0.01;
	private static final int timeFrame = 5;

	private final BackgroundHolder parent;
	private final Object animationLock;

	private double opacity;
	private Color opacityColor;

	private Color color;

	BackgroundCover(BackgroundHolder parent, Color opacityColor, double opacity) {
		if (parent == null)
			throw new NullPointerException();

		this.parent = parent;

		this.setColor(opacityColor, false);
		this.setBgOpacity(opacity, false);

		this.animationLock = new Object();
	}

	BackgroundCover(BackgroundHolder parent) {
		this(parent, Color.white, 0.0);
	}

	public void makeCover(boolean animate) {
		synchronized (animationLock) {
			if (animate)
				while (opacity < 1) {
					setBgOpacity(opacity + opacityStep, true);
					U.sleepFor(timeFrame);
				}
			setBgOpacity(1.0, true);
		}
	}

	public void makeCover() {
		makeCover(true);
	}

	public void removeCover(boolean animate) {
		synchronized (animationLock) {
			if (animate)
				while (opacity > 0) {
					setBgOpacity(opacity - opacityStep, true);
					U.sleepFor(timeFrame);
				}
			setBgOpacity(0.0, true);
		}
	}

	public void removeCover() {
		removeCover(true);
	}

	public boolean isCovered() {
		return opacity == 1;
	}

	public void toggleCover(boolean animate) {
		if (isCovered())
			removeCover(animate);
		else
			makeCover(animate);
	}

	@Override
	public void paint(Graphics g) {
		g.setColor(opacityColor);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	public double getBgOpacity() {
		return opacity;
	}

	public void setBgOpacity(double opacity, boolean repaint) {
		if (opacity < 0)
			opacity = 0;
		else if (opacity > 1)
			opacity = 1;

		this.opacity = opacity;
		this.opacityColor = new Color(color.getRed(), color.getGreen(),
				color.getBlue(), (int) (255 * opacity));

		if (repaint)
			repaint();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color, boolean repaint) {
		if (color == null)
			throw new NullPointerException();

		this.color = color;

		if (repaint)
			repaint();
	}

	@Override
	public void onResize() {
		this.setSize(parent.getWidth(), parent.getHeight());
	}

}

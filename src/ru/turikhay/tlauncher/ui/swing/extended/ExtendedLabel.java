package ru.turikhay.tlauncher.ui.swing.extended;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;
import javax.swing.JLabel;

import ru.turikhay.tlauncher.ui.TLauncherFrame;

public class ExtendedLabel extends JLabel {
	private static final AlphaComposite disabledAlphaComposite =
			AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);

	public ExtendedLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		setFont(getFont().deriveFont(TLauncherFrame.fontSize));
		setOpaque(false);
	}

	public ExtendedLabel(String text, int horizontalAlignment) {
		this(text, null, horizontalAlignment);
	}

	public ExtendedLabel(String text) {
		this(text, null, LEADING);
	}

	public ExtendedLabel(Icon image, int horizontalAlignment) {
		this(null, image, horizontalAlignment);
	}

	public ExtendedLabel(Icon image) {
		this(null, image, CENTER);
	}

	public ExtendedLabel() {
		this(null, null, LEADING);
	}

	@Override
	public void paintComponent(Graphics g0) {
		if(isEnabled()) {
			super.paintComponent(g0);
			return;
		}

		Graphics2D g = (Graphics2D) g0;
		Composite oldComposite = g.getComposite();

		g.setComposite(disabledAlphaComposite);
		super.paintComponent(g);
		g.setComposite(oldComposite);
	}

}

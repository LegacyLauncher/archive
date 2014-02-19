package com.turikhay.tlauncher.ui.swing.extended;

import javax.swing.Icon;
import javax.swing.JLabel;

public class ExtendedLabel extends JLabel {
	private static final long serialVersionUID = -758117308854118352L;
	
    public ExtendedLabel(String text, Icon icon, int horizontalAlignment) {
    	super(text, icon, horizontalAlignment);
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

}

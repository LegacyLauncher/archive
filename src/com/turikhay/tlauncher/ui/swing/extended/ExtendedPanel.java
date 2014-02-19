package com.turikhay.tlauncher.ui.swing.extended;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class ExtendedPanel extends JPanel {
	private static final long serialVersionUID = 873670863629293560L;
	
    public ExtendedPanel(LayoutManager layout, boolean isDoubleBuffered) {
    	super(layout, isDoubleBuffered);
    	setOpaque(false);
    }

    public ExtendedPanel(LayoutManager layout) {
        this(layout, true);
    }

    public ExtendedPanel(boolean isDoubleBuffered) {
        this(new FlowLayout(), isDoubleBuffered);
    }
    
    public ExtendedPanel() {
        this(true);
    }
	
	public void add(Component... components){
		if(components == null)
			throw new NullPointerException();
		
		for(Component comp : components)
			add(comp);
	}
	
	public void add(Component component0, Component component1){
		add(new Component[]{component0, component1});
	}

}

package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.ButtonModel;
import javax.swing.JButton;
public class TransparentButton extends JButton {
	private static final long serialVersionUID = -5329305793566047719L;
	
	public TransparentButton(){
        setBorderPainted(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setOpaque(false);
        setForeground(Color.white);
        setPreferredSize(new Dimension(27, 27));
	    setCursor(Cursor.getPredefinedCursor(12));
	}
	
	public TransparentButton(String text) {
		this();
		this.setText(text);
    } 
	
    protected void paintComponent(Graphics g) {
        ButtonModel buttonModel = getModel();
        Graphics2D gd = (Graphics2D) g.create();
        
        gd.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        gd.setPaint(new GradientPaint(0, 0, Color.decode("#67c7f4"), 0, getHeight(), Color.decode("#379fc9")));
       
        if (buttonModel.isRollover()) {
        	gd.setPaint(new GradientPaint(0, 0, Color.decode("#7bd2f6"), 0, getHeight(), Color.decode("#43b3d5")));
        	if (buttonModel.isPressed()) {
        		gd.setPaint(new GradientPaint(0, 0, Color.decode("#379fc9"), 0, getHeight(), Color.decode("#4fb2dd")));
        	} else {
        		setForeground(Color.white);
        	}
        }
        gd.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
        gd.dispose();
        super.paintComponent(g);
    }
}
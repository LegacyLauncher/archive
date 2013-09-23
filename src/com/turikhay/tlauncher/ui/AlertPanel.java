package com.turikhay.tlauncher.ui;

import java.awt.LayoutManager;
import java.awt.Panel;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class AlertPanel extends Panel {
	private static final long serialVersionUID = -8032765825488193573L;
	
	public AlertPanel(String message){
		LayoutManager lm = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(lm);
		
		JLabel text = new JLabel(message);
		text.setAlignmentY(TOP_ALIGNMENT);
		text.setAlignmentX(LEFT_ALIGNMENT);
		this.add(text);
	}
	
	public void addTextArea(String text){
		JTextArea area = new JTextArea(text);
		area.setFont(getFont());
		area.setCaretPosition(0);
		area.setAlignmentX(LEFT_ALIGNMENT);
		
		this.add(area);
	}
}

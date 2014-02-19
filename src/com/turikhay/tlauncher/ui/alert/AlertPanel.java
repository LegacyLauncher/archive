package com.turikhay.tlauncher.ui.alert;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Panel;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.turikhay.tlauncher.ui.swing.TextPopup;
import com.turikhay.util.StringUtil;

public class AlertPanel extends Panel {
	private static final long serialVersionUID = -8032765825488193573L;
	
	private final JLabel label;
	
	public AlertPanel(String message){
		LayoutManager lm = new BoxLayout(this, BoxLayout.Y_AXIS);
		this.setLayout(lm);
		
		label = new JLabel(message);
		label.setAlignmentX(CENTER_ALIGNMENT);
		this.add(label);
	}
	
	public void addTextArea(String text){		
		JTextArea area = new JTextArea(text);
		
		area.setFont(getFont());
		area.setLineWrap(true);
		area.setMargin(new Insets(0, 0, 0, 0));
		area.setCaretPosition(0);
		area.setAlignmentX(CENTER_ALIGNMENT);
		area.setEditable(false);
		area.addMouseListener(new TextPopup());
		
		JScrollPane scroll = new JScrollPane(area);
	    scroll.setBorder(null);
	    scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    int lineHeight = (area.getFont() != null)? getFontMetrics(area.getFont()).getHeight() : 15;
	    int height = StringUtil.countLines(text) * lineHeight;
	    
	    int width = 300;
	    if(height > 150){ width = 400; height = 200; }
	    
	    label.setMinimumSize(new Dimension(width, 0));
	    scroll.setPreferredSize(new Dimension(width, height));
		
		this.add(scroll);
	}
}

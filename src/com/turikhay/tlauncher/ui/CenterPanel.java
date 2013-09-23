package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Label;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.GlobalSettings;
import com.turikhay.tlauncher.settings.Settings;

public abstract class CenterPanel extends BlockablePanel implements LocalizableComponent {
	private static final long serialVersionUID = 1L;
	private static List<CenterPanel> panels = new ArrayList<CenterPanel>();
	
	protected Insets insets = new Insets(5, 24, 18, 24);
	
	final CenterPanel instance = this;
	final TLauncher t;
	final TLauncherFrame f;
	final GlobalSettings s;
	final Settings l;
	
	Color
		panelColor = Color.getHSBColor(0, 0, (float) 0.40),
		delPanelColor = Color.getHSBColor(0, 0, (float) 0.25),
		successColor = Color.getHSBColor(0.25F, (float) 0.66, (float) 0.66),
		errorColor = Color.getHSBColor(0, (float) 0.66, (float) 0.66),
		borderColor = successColor,
		textBackground = Color.white,
		textForeground = Color.black,
		wrongColor = Color.pink;
	
	Font font, font_italic, font_bold, font_error, font_small;
	int fontsize;
	String fontname;
	
	LocalizableLabel
		error;
	
	public CenterPanel(TLauncherFrame f){
		LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		this.setLayout(layout);
		
		this.setBackground(panelColor);
		
		this.f = f; this.t = this.f.t;
		this.s = t.getSettings(); this.l = f.lang;
		
		font = getFont();
		
		if(font == null) font = new Font("", Font.PLAIN, 12);
		
		fontsize = font.getSize();
		fontname = font.getName();
		
		font_italic = new Font(fontname, Font.ITALIC, fontsize);
		font_bold = new Font(fontname, Font.BOLD, fontsize);
		font_small = new Font(fontname, Font.PLAIN, (fontsize > 5)? fontsize-2 : fontsize);
		
		error = new LocalizableLabel("");
		error.setFont(font_bold);
		error.setAlignment(Label.CENTER); error.setForeground(new Color(8388608));
		
		panels.add(this);
	}
	
	protected FlowLayout getDefaultFlowLayout(int aligment){
		FlowLayout t = new FlowLayout();
		
		t.setVgap(0); t.setHgap(0);
		t.setAlignment(aligment);
		
		return t;
	}
	
	public void update(Graphics g) {
		super.update(g);
		paint(g);
	}

	public void paint(Graphics g) {
		super.paint(g);
		
		g.setColor(borderColor);
		
		for(int x=1;x<4;x++)
			g.drawRect(x-1, x-1, getWidth()-2*x, getHeight()-2*x);
	}
	
	public Insets getInsets() {
		return this.insets;
	}
	
	public boolean setError(String message){
		boolean repaint = false;
		if(message == null){
			if(borderColor != successColor) repaint = true; borderColor = successColor;
			this.error.setText("");
		} else {
			if(borderColor != errorColor) repaint = true; borderColor = errorColor;
			this.error.setText(message);
		}
		
		if(repaint) this.repaint();
		return false;
	}
	
	protected Del del(int aligment){
		return new Del(2, aligment, delPanelColor);
	}
	
	protected Del cdel(int aligment, int width, int height){
		Del del = del(aligment);
		del.setPreferredSize(new Dimension(width, height));
		
		return del;
	}
	
	protected void defocus(){
		this.requestFocusInWindow();
	}
	
	public void updateLocale(){
		this.error.setText("");
	}
}

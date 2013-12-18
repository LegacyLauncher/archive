package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
		panelColor = Color.getHSBColor(0, 0, (float) 0.50),
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
	
	JPanel error = new JPanel();
	LocalizableLabel error_l;
	
	public CenterPanel(TLauncherFrame f){
		LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		this.setLayout(layout);
		
		//this.setBackground(panelColor);
		this.setBackground(new Color(255, 255, 255, 128));
		this.setOpaque(false);
		
		this.f = f; this.t = this.f.t;
		this.s = t.getSettings(); this.l = f.lang;
		
		font = getFont();
		
		if(font == null) font = new Font("", Font.PLAIN, 15);
		this.setFont(font);
		
		fontsize = font.getSize();
		fontname = font.getName();
		
		font_italic = new Font(fontname, Font.ITALIC, fontsize);
		font_bold = new Font(fontname, Font.BOLD, fontsize);
		font_small = new Font(fontname, Font.PLAIN, (fontsize > 5)? fontsize-2 : fontsize);
		
		error_l = new LocalizableLabel(" ");
		error_l.setFont(font_bold);
		error_l.setForeground(new Color(8388608));
		error_l.setVerticalAlignment(SwingConstants.CENTER);
		
		error.setOpaque(false);
		error.add(error_l);
		
		panels.add(this);
		
		this.add(Box.createVerticalGlue());
	}
	
    public void paintComponent(Graphics g0) {
    	Graphics2D g = (Graphics2D) g0;
    	int arc = 32;
    	
    	g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	
        g.setColor(getBackground());
        g.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
        
        g.setColor(borderColor);		
		for(int x=1;x<2;x++){
			g.drawRoundRect(x-1, x-1, getWidth()-2*x+1, getHeight()-2*x+1, arc, arc);
		}
		
        super.paintComponent(g);
    }
	
	protected FlowLayout getDefaultFlowLayout(int aligment){
		FlowLayout t = new FlowLayout();
		
		t.setVgap(0); t.setHgap(0);
		t.setAlignment(aligment);
		
		return t;
	}
	
	public Insets getInsets() {
		return this.insets;
	}
	
	public boolean setError(String message){
		this.error_l.setHorizontalAlignment(SwingConstants.CENTER);
		this.error_l.setHorizontalTextPosition(SwingConstants.CENTER);
		boolean repaint = false;
		if(message == null){
			if(borderColor != successColor) repaint = true; borderColor = successColor;
			this.error_l.setText(" ");
		} else {
			if(borderColor != errorColor) repaint = true; borderColor = errorColor;
			this.error_l.setText(message);
		}
		
		if(repaint) this.repaint();
		return false;
	}
	
	protected Del del(int aligment){
		return new Del(1, aligment, borderColor);
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
		this.error_l.setText("");
	}
	
	protected void blockElement(Object reason){
		handleComponents(this, false);
	}
	
	protected void unblockElement(Object reason){
		handleComponents(this, true);
	}
	
	protected static void handleComponents(Container container, boolean setEnabled){
		Component[] components = container.getComponents();
		for (Component component : components) {
			component.setEnabled(setEnabled);
			if (component instanceof Container) {
				handleComponents((Container)component, setEnabled);
			}
		}
	}
	
	protected static JPanel createSeparateJPanel(Component... components){
		
		BlockablePanel panel = new BlockablePanel(){
			private static final long serialVersionUID = 1L;
			
			protected void blockElement(Object reason) {
				handleComponents(this, false);
			}
			protected void unblockElement(Object reason) {
				handleComponents(this, true);
			}
		};
		LayoutManager lm = new GridLayout(0, 1);
		
		panel.setLayout(lm);
		panel.setOpaque(false);
		for(Component comp : components) panel.add(comp);
		
		return panel;
	}
	
	protected static JPanel sepPan(Component...components){
		return createSeparateJPanel(components);
	}
}

package com.turikhay.tlauncher.ui.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.TLauncherFrame;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.swing.TextPopup;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;

public class ConsoleFrame extends JFrame implements LocalizableComponent {
	private static final long serialVersionUID = 5667131709333334581L;
	public static final int minWidth = 620, minHeight = 400;
	private Font font;
	
	private int w = minWidth, h = minHeight, v = 0;
	private Dimension sizes = new Dimension(w, h);
	boolean update = true;
	
	private Object busy = new Object();
	
	private final JPanel panel;
	private final SearchPanel sp;
	private final ExitCancelPanel ecp;
	
	private final TextPopup textpopup;
	
	final JTextArea textArea;
	private final JScrollPane scrollPane;
	private final JScrollBar scrollBar;
	private final BoundedRangeModel scrollBarModel;
	
	final Console c;
	
	boolean hiding = false;
	
	public ConsoleFrame(Console c, Configuration s, String name){
		super(name);
		
		if(c == null)
			throw new NullPointerException("Console can't be NULL!");
		
		this.c = c;
		
		LayoutManager layout = new BorderLayout();
		panel = new JPanel(layout);
		panel.setAlignmentX(CENTER_ALIGNMENT);
		panel.setAlignmentY(CENTER_ALIGNMENT);
		
		this.font = new Font("DialogInput", 0, 14);
		
		this.textpopup = new TextPopup();
		
	    this.textArea = new JTextArea();
	    this.textArea.setLineWrap(true);
	    this.textArea.setEditable(false);
	    this.textArea.setMargin(new Insets(0, 0, 0, 0));
	    this.textArea.setFont(font);
	    this.textArea.setForeground(Color.white);
	    this.textArea.setCaretColor(Color.white);
	    this.textArea.setBackground(Color.black);
	    this.textArea.setSelectionColor(Color.gray);
	    this.textArea.setAutoscrolls(true);
	    ((DefaultCaret) this.textArea.getCaret()).setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);	  
	    this.textArea.addMouseListener(textpopup);
	    
	    this.scrollPane = new JScrollPane(this.textArea);
	    this.scrollPane.setBorder(null);
	    this.scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    this.scrollBar = this.scrollPane.getVerticalScrollBar();
	    this.scrollBarModel = this.scrollBar.getModel();
	    this.scrollBar.addAdjustmentListener(new AdjustmentListener(){
			public void adjustmentValueChanged(AdjustmentEvent e) {				
				boolean resizing = getWidth() != w;
				if(resizing) return;
				
				int nv = e.getValue();
				
				if(nv < v) // Scrolling up
					update = false; // Turn off auto-scroll
				else {
					// Scrolling down
					int max = scrollBarModel.getMaximum() - scrollBarModel.getExtent();
					if(nv == max) update = true; // Turn on auto-scroll
				}
				
				v = nv;
			}
	    });
	    
	    this.sp = new SearchPanel(this);
	    this.ecp = new ExitCancelPanel(this);
	    
	    this.addComponentListener(new ComponentListener(){
			public void componentResized(ComponentEvent e) {
				w = getWidth();
				h = getHeight();
				
				sp.repaint();
			}
			public void componentMoved(ComponentEvent e) {}
			public void componentShown(ComponentEvent e) {}
			public void componentHidden(ComponentEvent e) {}
	    });
	    
	    setFont(font);
	    setBackground(Color.black);
		setSize(sizes);
		setMinimumSize(sizes);
		setLocation(0, 0);
		
		this.setIconImages(TLauncherFrame.getFavicons());
		
		panel.add("Center", this.scrollPane);
		panel.add("South", this.sp);
		
		add(panel);
	}
	
	public void println(String string) {
		print(new StringBuilder().append(string).append('\n').toString());
	}

	public void print(String string) {
		synchronized(busy){
			
			Document document = this.textArea.getDocument();
		
			try{ document.insertString(document.getLength(), string, null); }
			catch (Throwable ignored) {}
			
			if(update) scrollBottom();
		}
	}
	
	public void scrollBottom(){		
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				scrollBar.setValue(scrollBar.getMaximum());
			}
		});
	}
	
	public String getOutput(){
		return c.getOutput();
	}
	
	public SearchPrefs getSearchPrefs(){
		return sp.prefs;
	}
	
	public void hideIn(final long millis){
		this.hiding = true;
		this.showTimer();
		
		AsyncThread.execute(new Runnable(){
			long remaining = millis;
			public void run(){
				ecp.setTimeout((int) remaining / 1000);
				U.sleepFor(1000);
				
				while(hiding && remaining > 1999){ remaining -= 1000;
					ecp.setTimeout((int) remaining / 1000);
					
					U.sleepFor(1000);
				}
				
				if(hiding)
					setVisible(false);
			}
		});
	}
	
	private void showTimer(){
		panel.remove(sp);
		panel.remove(ecp);
		
		panel.add("South", ecp);
		
		this.validate(); panel.repaint();
		this.scrollBottom();
		this.toFront();
	}
	
	private void showSearch(){
		panel.remove(sp);
		panel.remove(ecp);
		
		panel.add("South", sp);
		
		this.validate(); panel.repaint();
		this.scrollBottom();
	}
	
	public void cancelHiding(){
		this.hiding = false;
		this.showSearch();
	}
	
	public void clear(){
		this.textArea.setText("");
	}
	
	public void selectAll(){
		this.textArea.requestFocusInWindow();
		this.textArea.selectAll();
	}

	public void updateLocale() {
		TLauncherFrame.updateContainer(this, true);
		this.textpopup.updateLocale();
	}

}
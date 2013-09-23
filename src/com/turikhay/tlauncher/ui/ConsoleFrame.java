package com.turikhay.tlauncher.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.BoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DefaultCaret;

import com.turikhay.tlauncher.TLauncher;

public class ConsoleFrame extends JFrame {
	private static final long serialVersionUID = 5667131709333334581L;
	private Font font;
	
	private int w = 620, h = 400, v = 0;
	private Dimension sizes = new Dimension(w, h);
	boolean update = true;
	
	private final JTextArea textArea;
	private final JScrollPane scrollPane;
	private final JScrollBar scrollBar;
	private final BoundedRangeModel scrollBarModel;
	
	private final Image favicon = TLauncher.getInstance().getFrame().favicon;
	
	private final StringBuilder output = new StringBuilder();
	
	public ConsoleFrame(String name){
		super(name);
		
		this.font = new Font("DialogInput", 0, 14);
		
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
					update = false;
				else {
					// Scrolling down
					int max = scrollBarModel.getMaximum() - scrollBarModel.getExtent();
					if(nv == max) update = true;
				}
				
				v = nv;
			}
	    });
	    
	    this.addComponentListener(new ComponentListener(){
			public void componentResized(ComponentEvent e) {
				w = getWidth();
				h = getHeight();
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
		if(favicon != null) this.setIconImage(favicon);
		
	    add(this.scrollPane);
	}
	
	public void println(String string) {
		print(string + "\n");
	}

	public void print(String string) {		
		this.output.append(string);

		Document document = this.textArea.getDocument(); if(update) scrollBottom();
		
		try{ document.insertString(document.getLength(), string, null); }
		catch (BadLocationException ignored) {}
	}
	
	public void scrollBottom(){		
		SwingUtilities.invokeLater(new Runnable(){
			public void run(){
				scrollBar.setValue(scrollBar.getMaximum());
			}
		});
	}
	
	public String getOutput(){
		return output.toString();
	}

}
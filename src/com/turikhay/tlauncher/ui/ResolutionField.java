package com.turikhay.tlauncher.ui;

import java.awt.LayoutManager;
import java.awt.TextField;

import javax.swing.BoxLayout;

public class ResolutionField extends BlockablePanel {
	private static final long serialVersionUID = 2840605102354193923L;
	
	TextField w, h;
	
	ResolutionField(SettingsForm sf){
		LayoutManager layout = new BoxLayout(this, 0);
		this.setLayout(layout);
		
		this.w = new TextField();
		this.h = new TextField();
		
		this.w.addFocusListener(sf.restart);
		this.h.addFocusListener(sf.restart);
		
		this.add(this.w);
		this.add(this.h);
	}
	
	public void setValues(int width, int height){
		if(width < 1) width = 925;
		if(height < 1) height = 525;
		
		this.w.setText(String.valueOf(width));
		this.h.setText(String.valueOf(height));
		
	}
	
	public int[] getValues(){
		int[] i = new int[2];
		
		try{ i[0] = Integer.parseInt(w.getText()); }catch(Exception e){ return null; }
		try{ i[1] = Integer.parseInt(h.getText()); }catch(Exception e){ return null; }
		
		return i;
	}

	protected void blockElement(Object reason) {
		w.setEnabled(false);
		h.setEnabled(false);
	}

	protected void unblockElement(Object reason) {
		w.setEnabled(true);
		h.setEnabled(true);
	}
}

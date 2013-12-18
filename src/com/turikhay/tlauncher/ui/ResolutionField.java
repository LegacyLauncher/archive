package com.turikhay.tlauncher.ui;

import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JTextField;

import com.turikhay.util.IntegerArray;

public class ResolutionField extends BlockablePanel implements SettingsField {
	private static final long serialVersionUID = 2840605102354193923L;
	
	JTextField w, h;
	
	private boolean saveable;
	
	ResolutionField(SettingsForm sf){
		LayoutManager layout = new BoxLayout(this, 0);
		this.setLayout(layout);
		
		this.w = new JTextField();
		this.h = new JTextField();
		
		this.w.addFocusListener(sf.restart);
		this.h.addFocusListener(sf.restart);
		
		this.add(this.w);
		this.add(this.h);
	}
	
	private void setValues(int width, int height){
		if(width < 1) width = 925;
		if(height < 1) height = 525;
		
		this.w.setText(String.valueOf(width));
		this.h.setText(String.valueOf(height));
	}
	
	private int[] getValues(){
		int[] i = new int[2];
		
		try{ i[0] = Integer.parseInt(w.getText()); }catch(Exception e){ return null; }
		try{ i[1] = Integer.parseInt(h.getText()); }catch(Exception e){ return null; }
		
		return i;
	}
	
	public void setEnabled(boolean b){
		if(b) unblockElement(null);
		else blockElement(null);
	}

	protected void blockElement(Object reason) {
		w.setEnabled(false);
		h.setEnabled(false);
	}

	protected void unblockElement(Object reason) {
		w.setEnabled(true);
		h.setEnabled(true);
	}

	public String getSettingsPath() {
		return "minecraft.size";
	}

	public String getValue() {
		int[] v = getValues();
		if(v == null) return null;
		
		return v[0] + ";" + v[1];
	}

	public boolean isValueValid() {
		return (getValues() != null);
	}

	public void setValue(String value) {
		int w, h;
		try{
			IntegerArray values = IntegerArray.parseIntegerArray(value);
			w = values.get(0);
			h = values.get(1);
		}
		catch(Exception e){ w = h = -1; }
		
		setValues(w, h);
	}
	
	public void setToDefault() {
		setValue(null);
	}
	
	public boolean isSaveable() {
		return saveable;
	}

	public void setSaveable(boolean val) {
		this.saveable = val;
	}
}

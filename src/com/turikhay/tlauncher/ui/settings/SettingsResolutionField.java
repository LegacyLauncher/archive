package com.turikhay.tlauncher.ui.settings;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import com.turikhay.tlauncher.configuration.Configuration;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import com.turikhay.util.IntegerArray;

public class SettingsResolutionField extends ExtendedPanel implements SettingsField {
	private static final long serialVersionUID = -5565607141889620750L;

	SettingsNaturalIntegerField w, h;
	ExtendedLabel x;
	
	private final int[] defaults;
	
	SettingsResolutionField(){
		this(null, null, new int[]{0, 0});
	}
	
	SettingsResolutionField(String promptW, String promptH, Configuration settings){
		this(promptW, promptH, settings.getDefaultWindowSize());
	}
	
	SettingsResolutionField(String promptW, String promptH, int[] defaults){
		if(defaults == null)
			throw new NullPointerException();
		if(defaults.length != 2)
			throw new IllegalArgumentException("Illegal array size");
		
		this.defaults = defaults;
		
		this.setAlignmentX(CENTER_ALIGNMENT);
		this.setAlignmentY(CENTER_ALIGNMENT);
		
		this.w = new SettingsNaturalIntegerField(promptW);
		this.h = new SettingsNaturalIntegerField(promptH);
		this.x = new ExtendedLabel("X");
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.CENTER;
		
		c.gridx = 0;
		c.weightx = 1;
		c.insets.set(0, 0, 0, 0);
		c.fill = GridBagConstraints.BOTH;
		add(w, c);
		
		c.gridx = 1;
		c.weightx = 0;
		c.insets.set(0, 5, 0, 5);
		c.fill = GridBagConstraints.VERTICAL;
		add(x, c);
		
		c.gridx = 2;
		c.weightx = 1.25;
		c.insets.set(0, 0, 0, 0);
		c.fill = GridBagConstraints.BOTH;
		add(h, c);
	}

	public String getSettingsValue() {
		return w.getSettingsValue() + IntegerArray.defaultDelimiter + h.getSettingsValue();
	}
	
	public int[] getResolution(){
		try {
			IntegerArray arr = IntegerArray.parseIntegerArray(getSettingsValue());
			return arr.toArray();
		} catch(Exception e){
			return new int[2];
		}
	}

	public boolean isValueValid() {
		int[] size = getResolution();
		
		return size[0] >= defaults[0] && size[1] >= defaults[1];
	}

	public void setSettingsValue(String value) {
		String width, height;
		
		try{
			IntegerArray arr = IntegerArray.parseIntegerArray(value);
			width = String.valueOf(arr.get(0));
			height = String.valueOf(arr.get(1));
		}
		catch(Exception e){
			width = ""; height = "";
		}
		
		w.setText(width);
		h.setText(height);
	}
	
	@Override
	public void setBackground(Color bg) {
		if(w != null) w.setBackground(bg);
		if(h != null) h.setBackground(bg);
	}

	@Override
	public void block(Object reason) {
		Blocker.blockComponents(reason, w, h);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblockComponents(reason, w, h);
	}

}

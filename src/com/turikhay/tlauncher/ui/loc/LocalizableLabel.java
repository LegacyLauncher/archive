package com.turikhay.tlauncher.ui.loc;

import com.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;

public class LocalizableLabel extends ExtendedLabel implements LocalizableComponent {
	private static final long serialVersionUID = 7628068160047735335L;
	
	private String path;
	private String[] variables;
	
	public LocalizableLabel(String path, Object...vars){
		setText(path, vars);
	}
	
	public LocalizableLabel(String path){
		this(path, Localizable.EMPTY_VARS);
	}
	
	public LocalizableLabel(){
		this(null);
	}
	
	public LocalizableLabel(int horizontalAlignment){
		this(null);
		this.setHorizontalAlignment(horizontalAlignment);
	}
	
	public void setText(String path, Object...vars){
		this.path = path;
		this.variables = Localizable.checkVariables(vars);
		
		String value = Localizable.get(path);		
		for(int i=0;i<variables.length;i++) value = value.replace("%" + i, variables[i]);
		
		super.setText(value);
	}
	
	public void setText(String path){
		setText(path, Localizable.EMPTY_VARS);
	}
	
	@Override
	public void updateLocale() {
		setText(path, (Object[]) variables);
	}
}

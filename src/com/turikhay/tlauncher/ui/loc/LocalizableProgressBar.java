package com.turikhay.tlauncher.ui.loc;

import java.awt.Component;

import com.turikhay.tlauncher.ui.progress.ProgressBar;

public class LocalizableProgressBar extends ProgressBar implements LocalizableComponent {
	private static final long serialVersionUID = 7393243528402135898L;
	
	private String westPath, centerPath, eastPath;
	private String[] westVars, centerVars, eastVars;

	public LocalizableProgressBar(Component parentComp) {
		super(parentComp);
	}
	
	public LocalizableProgressBar() {
		this(null);
	}
	
	public void setWestString(String path, boolean update, Object...vars){
		this.westPath = path;
		this.westVars = Localizable.checkVariables(vars);
		
		String value = Localizable.get(path);
		for(int i=0;i<westVars.length;i++) value = value.replace("%" + i, westVars[i]);
		
		super.setWestString(value, update);
	}
	
	@Override
	public void setWestString(String path, boolean update){
		this.setWestString(path, update, Localizable.EMPTY_VARS);
	}
	
	public void setWestString(String path, Object...vars){
		this.setWestString(path, true, vars);
	}
	
	public void setCenterString(String path, boolean update, Object...vars){
		this.centerPath = path;
		this.centerVars = Localizable.checkVariables(vars);
		
		String value = Localizable.get(path);
		for(int i=0;i<centerVars.length;i++) value = value.replace("%" + i, centerVars[i]);
		
		super.setCenterString(value, update);
	}
	
	@Override
	public void setCenterString(String path, boolean update){
		this.setCenterString(path, update, Localizable.EMPTY_VARS);
	}
	
	public void setCenterString(String path, Object...vars){
		this.setCenterString(path, true, vars);
	}
	
	public void setEastString(String path, boolean update, Object...vars){
		this.eastPath = path;
		this.eastVars = Localizable.checkVariables(vars);
		
		String value = Localizable.get(path);
		for(int i=0;i<eastVars.length;i++) value = value.replace("%" + i, eastVars[i]);
		
		super.setEastString(value, update);
	}
	
	@Override
	public void setEastString(String path, boolean update){
		this.setEastString(path, update, Localizable.EMPTY_VARS);
	}
	
	public void setEastString(String path, Object...vars){
		this.setEastString(path, true, vars);
	}
	
	public void setStrings(String west, String center, String east, boolean acceptNull, boolean repaint, boolean saveVars){
		if(acceptNull || west != null) this.setWestString(west, false, saveVars? westVars : Localizable.EMPTY_VARS);
		if(acceptNull || center != null) this.setCenterString(center, false, saveVars? centerVars : Localizable.EMPTY_VARS);
		if(acceptNull || east != null) this.setEastString(east, false, saveVars? eastVars : Localizable.EMPTY_VARS);
		
		this.repaint();
	}
	
	@Override
	public void setStrings(String west, String center, String east, boolean acceptNull, boolean repaint){
		this.setStrings(west, center, east, acceptNull, repaint, false);
	}

	@Override
	public void updateLocale() {
		setStrings(westPath, centerPath, eastPath, true, true);
	}

}

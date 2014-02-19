package com.turikhay.tlauncher.ui.loc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JMenuItem;

public class LocalizableMenuItem extends JMenuItem implements LocalizableComponent {
	private static final long serialVersionUID = 1364363532569997394L;
	private static List<LocalizableMenuItem> items = Collections.synchronizedList(new ArrayList<LocalizableMenuItem>());
	
	private String path;
	
	public LocalizableMenuItem(String path) {
		super();
		init(path);
	}
	
	private void init(String path){
		this.path = path;
		this.setText(path);
		
		items.add(this);
	}
	
	public void setText(String path){
		this.path = path;
		super.setText(Localizable.exists()? Localizable.get(path) : path);
	}
	
	@Override
	public void updateLocale() {
		setText(path);
	}

	public static void updateLocales() {
		for(LocalizableMenuItem item : items)
			if(item == null) continue;
			else item.updateLocale();
	}

}

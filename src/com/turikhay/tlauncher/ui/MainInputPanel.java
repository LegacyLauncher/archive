package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;

public class MainInputPanel extends BlockablePanel implements LocalizableComponent {
	private static final long serialVersionUID = 296073104610204659L;
	
	private final LoginForm lf;
	
	UsernameField field;
	
	MainInputPanel(LoginForm loginform, String username){
		this.lf = loginform;
		
		LayoutManager lm = new GridLayout(1, 1);
		this.setLayout(lm);
		
		field = new UsernameField(lf, username, "loginform.username", 20);
		this.add(field);
	}

	protected void blockElement(Object reason) {
		field.setEnabled(false);
	}

	protected void unblockElement(Object reason) {
		field.setEnabled(true);
	}
	
	public void updateLocale(){
		field.setPlaceholder("loginform.username");
	}

}

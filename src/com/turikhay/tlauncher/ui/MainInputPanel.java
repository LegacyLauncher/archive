package com.turikhay.tlauncher.ui;

import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.turikhay.tlauncher.ui.UsernameField.UsernameState;

public class MainInputPanel extends BlockablePanel implements LocalizableComponent, LoginListener {
	private static final long serialVersionUID = 296073104610204659L;
	
	private final LoginForm lf;
	private boolean saveable;
	
	UsernameField field;
	//PasswordField password;
	
	MainInputPanel(LoginForm loginform, String username){
		this.lf = loginform;
		
		LayoutManager lm = new GridLayout(0, 1);
		this.setLayout(lm);
		this.setOpaque(false);
		
		field = new UsernameField(lf, UsernameState.USERNAME);
		field.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { lf.callLogin(); }
		});
		field.setValue(username);
		
		saveable = lf.s.isSaveable("login.username");
		
		//password = new PasswordField(lf, hastoken);
		
		field.setEnabled(saveable);
		this.add(field);
	}

	protected void blockElement(Object reason) {
		if(saveable)
			field.setEnabled(false);
	}

	protected void unblockElement(Object reason) {
		if(saveable)
			field.setEnabled(true);
	}
	
	public void updateLocale(){
		field.setPlaceholder("loginform.username");
	}

	public boolean onLogin() {
		return field.check(false);
	}
	public void onLoginFailed() {}
	public void onLoginSuccess() {}
}

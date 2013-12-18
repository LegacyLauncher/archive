package com.turikhay.tlauncher.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPasswordField;
import javax.swing.JProgressBar;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.auth.Authenticator;
import com.turikhay.tlauncher.minecraft.auth.AuthenticatorListener;
import com.turikhay.tlauncher.ui.UsernameField.UsernameState;
import com.turikhay.util.U;

public class ProfileCreatorForm extends CenterPanel implements AuthenticatorListener {
	private static final long serialVersionUID = -3246475706073813172L;
	
	UsernameField username;
	JPasswordField password;
	
	LocalizableCheckbox premiumBox;
	LocalizableButton next;
	
	JProgressBar progressBar;

	public ProfileCreatorForm(TLauncherFrame f) {
		super(f);
		
		this.username = new UsernameField(this, UsernameState.USERNAME);
		this.password = new JPasswordField();
		
		premiumBox = new LocalizableCheckbox("profile.premium");
		premiumBox.addItemListener(new CheckBoxListener(){
			public void itemStateChanged(boolean newstate) {
				password.setEnabled(newstate);
				username.setPlaceholder("profile." + (newstate? "e-mail" : "username"));
				username.setState(newstate? UsernameState.EMAIL : UsernameState.USERNAME);
				
				username.check(true);
				defocus();
			}
		});
		
		next = new LocalizableButton("profile.creator.next");
		next.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				doAuth();
				defocus();
			}
		});
		
		progressBar = new JProgressBar();
		progressBar.setPreferredSize(new Dimension(200, 20));
		
		this.add(error);
		this.add(del(Del.CENTER));
		this.add(sepPan(username));
		this.add(sepPan(premiumBox));
		this.add(sepPan(password));
		this.add(del(Del.CENTER));
		this.add(sepPan(next));
		this.add(sepPan(progressBar));
	}
	
	private void doAuth(){
		if(isBlocked()) return;
		
		if(!username.check(false)) return;
		if(password.getPassword().length == 0) return;
		
		Authenticator auth = new Authenticator();
		auth.setClientToken(TLauncher.getClientToken());
		auth.setUsername(username.getValue());
		auth.setPassword(password.getPassword());
		
		auth.asyncPass(this);
	}

	public void onAuthPassing(Authenticator auth) {
		this.block("AUTH");
		
		progressBar.setIndeterminate(true);
	}

	public void onAuthPassingError(Authenticator auth, Throwable e) {
		this.unblock("AUTH");
		
		progressBar.setIndeterminate(false);
	}

	public void onAuthPassed(Authenticator auth) {
		this.unblock("AUTH");
		
		progressBar.setIndeterminate(false);
		
		U.log(auth);
	}

}

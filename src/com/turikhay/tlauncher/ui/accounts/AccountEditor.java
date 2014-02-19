package com.turikhay.tlauncher.ui.accounts;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import com.turikhay.tlauncher.minecraft.auth.Account;
import com.turikhay.tlauncher.ui.accounts.UsernameField.UsernameState;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.loc.LocalizableButton;
import com.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import com.turikhay.tlauncher.ui.progress.ProgressBar;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.tlauncher.ui.swing.CheckBoxListener;
import com.turikhay.tlauncher.ui.swing.Del;
import com.turikhay.tlauncher.ui.text.ExtendedPasswordField;

public class AccountEditor extends CenterPanel {
	private static final long serialVersionUID = 7061277150214976212L;

	private final AccountEditorScene scene;
	
	public final UsernameField username;
	public final ExtendedPasswordField password;
	
	public final LocalizableCheckbox premiumBox;
	public final LocalizableButton save;
	
	public final ProgressBar progressBar;
	
	public AccountEditor(AccountEditorScene sc){
		super(squareInsets);
		
		this.scene = sc;
		
		this.username = new UsernameField(this, UsernameState.USERNAME);
		
		this.password = new ExtendedPasswordField();
		password.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				defocus();
				scene.handler.saveEditor();
			}
		});
		
		premiumBox = new LocalizableCheckbox("account.premium");
		premiumBox.addItemListener(new CheckBoxListener(){
			public void itemStateChanged(boolean newstate) {
				if(newstate && !password.hasPassword()) password.setText(null);
				
				password.setEnabled(newstate);
				username.setState(newstate? UsernameState.EMAIL : UsernameState.USERNAME);
				
				defocus();
			}
		});
		
		save = new LocalizableButton("account.save");
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				defocus();
				scene.handler.saveEditor();
			}
		});
		
		progressBar = new ProgressBar();
		progressBar.setPreferredSize(new Dimension(200, 20));
		
		this.add(del(Del.CENTER));
		this.add(sepPan(username));
		this.add(sepPan(premiumBox));
		this.add(sepPan(password));
		this.add(del(Del.CENTER));
		this.add(sepPan(save));
		this.add(sepPan(progressBar));
	}
	
	public void fill(Account account){
		this.premiumBox.setSelected(account.hasLicense());
		this.username.setText(account.getUsername());
		this.password.setText(null);
	}
	
	public void clear(){
		this.premiumBox.setSelected(false);
		this.username.setText(null);
		this.password.setText(null);
	}
	
	public Account get(){
		Account account = new Account();
		account.setUsername(username.getValue());
		
		if(premiumBox.isSelected()){
			account.setHasLicense(true);
			
			if(password.hasPassword())
				account.setPassword(password.getPassword());
		}
		
		return account;
	}
	
	public Insets getInsets() {
		return squareInsets;
	}
	
	public void block(Object reason){		
		super.block(reason);
		
		password.setEnabled(premiumBox.isSelected());
		
		if(!reason.equals("empty"))
			progressBar.setIndeterminate(true);
	}
	
	public void unblock(Object reason){		
		super.unblock(reason);
		
		password.setEnabled(premiumBox.isSelected());
		
		if(!reason.equals("empty"))
			progressBar.setIndeterminate(false);
	}
}

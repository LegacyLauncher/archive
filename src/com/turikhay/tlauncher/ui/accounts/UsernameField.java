package com.turikhay.tlauncher.ui.accounts;

import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.loc.LocalizableTextField;

public class UsernameField extends LocalizableTextField {
	private static final long serialVersionUID = -5813187607562947592L;
	
	UsernameState state;
	String username;
	
	public UsernameField(CenterPanel pan, UsernameState state){
		super(pan, "account.username");
		this.setState(state);
	}
	
	public UsernameState getState(){
		return state;
	}
	
	public void setState(UsernameState state){
		if(state == null)
			throw new NullPointerException();
		
		this.state = state;
		this.setPlaceholder(state.placeholder);
	}
	
	public enum UsernameState {
		USERNAME("account.username"), EMAIL("account.email");
		
		private final String placeholder;
		
		UsernameState(String placeholder){
			this.placeholder = placeholder;
		}
		
		public String getPlaceholder(){
			return placeholder;
		}
	}

}

package com.turikhay.tlauncher.ui;

public class UsernameField extends LocalizableTextField {
	private static final long serialVersionUID = -5813187607562947592L;
	UsernameState state;
	String username;
	
	UsernameField(CenterPanel pan, UsernameState state){
		super(pan, "profile.username", null, 20);
		this.setState(state);
	}
	
	public UsernameState getState(){
		return state;
	}
	
	public void setState(UsernameState state){
		if(state == null)
			throw new NullPointerException();
		
		this.state = state;
	}
	
	public boolean check(){
		return check(true);
	}
	
	protected boolean check(String text) {
		return check(text, true);
	}
	
	public boolean check(boolean canBeEmpty){
		String text = this.getValue();
		
		if(check(text, canBeEmpty))
			return ok();
		return wrong(l.get("username.incorrect"));
	}
	
	protected boolean check(String text, boolean canBeEmpty){
		if(text == null) return false;
		
		String regexp;
		
		switch(state){
		case EMAIL:
			regexp = "^.*$"; // LOL, any email
			break;
		case USERNAME:
			regexp= "^[A-Za-z0-9_|\\-|\\.]"+ ((canBeEmpty)? "*" : "+")  +"$";
			break;
		default:
			throw new IllegalArgumentException("Unknown field state!");
		}
		
		if(text.matches(regexp)){
			username = text;
			return true;
		}
		
		return false;
	}
	
	enum UsernameState {
		USERNAME, EMAIL;
	}

}

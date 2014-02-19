package com.turikhay.tlauncher.ui.scenes;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.accounts.AccountEditor;
import com.turikhay.tlauncher.ui.accounts.AccountHandler;
import com.turikhay.tlauncher.ui.accounts.AccountList;
import com.turikhay.tlauncher.ui.accounts.helper.AccountEditorHelper;
import com.turikhay.tlauncher.ui.accounts.helper.HelperState;

public class AccountEditorScene extends PseudoScene {
	private static final long serialVersionUID = -151325577614420989L;
	
	private final int ELEMENT_WIDTH = 225, ELEMENT_HEIGHT = ELEMENT_WIDTH, MARGIN = 10;
	
	public final AccountEditor editor;
	public final AccountList list;
	public final AccountEditorHelper helper;
	
	public final AccountHandler handler;

	public AccountEditorScene(MainPane main) {
		super(main);
		
		this.editor = new AccountEditor(this);
		this.editor.setSize(ELEMENT_WIDTH, ELEMENT_HEIGHT);
		this.add(editor);
		
		this.list = new AccountList(this);
		this.list.setSize(ELEMENT_WIDTH, ELEMENT_HEIGHT);
		this.add(list);
		
		this.handler = new AccountHandler(this);
		
		this.helper = new AccountEditorHelper(this);
		this.add(helper);
	}
	
	public void setShown(boolean shown, boolean animate){
		super.setShown(shown, animate);
		
		if(shown && list.model.isEmpty())
			helper.setState(HelperState.HELP);
		else
			helper.setState(HelperState.NONE);
	}
	
	public void onResize(){
		super.onResize();
		
		int
			hw = getWidth() / 2, hh = getHeight() / 2,
			heh = ELEMENT_HEIGHT / 2,
			y = hh - heh;
		
		this.editor.setLocation(hw - ELEMENT_WIDTH - MARGIN, y);
		this.list.setLocation(hw + MARGIN,  y);
	}

}

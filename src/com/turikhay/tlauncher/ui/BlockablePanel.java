package com.turikhay.tlauncher.ui;

import java.awt.Panel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class BlockablePanel extends Panel implements Blockable {
	private static final long serialVersionUID = 1L;
	
	public final static Object UNIVERSAL_UNBLOCK = "lol, nigga";
	private boolean blocked;
	private List<Object> reasons = Collections.synchronizedList(new ArrayList<Object>());
	
	public void block(Object reason){
		if(reason == null) throw new IllegalArgumentException("Reason cannot be NULL!");
		
		this.reasons.add(reason);
		if(blocked) return;
		
		this.blocked = true;
		blockElement(reason);
	}
	public void unblock(Object reason){
		if(!blocked || (!reasons.contains(reason) && !reason.equals(UNIVERSAL_UNBLOCK)) ){ return; }
		
		this.reasons.remove(reason);
		if(reason.equals(UNIVERSAL_UNBLOCK)) this.reasons.clear();
		if(!this.reasons.isEmpty()) return;
		
		this.blocked = false;
		unblockElement(reason);
	}
	public boolean isBlocked(){ return this.blocked; }
	
	protected abstract void blockElement(Object reason);
	protected abstract void unblockElement(Object reason);

}

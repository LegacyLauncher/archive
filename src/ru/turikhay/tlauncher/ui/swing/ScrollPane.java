package ru.turikhay.tlauncher.ui.swing;

import java.awt.Component;

import javax.swing.JScrollPane;

public class ScrollPane extends JScrollPane {
	private static final long serialVersionUID = -8296804097817210847L;
	
	public ScrollPane(Component view, ScrollBarPolicy vertical, ScrollBarPolicy horizontal) {
		super(view);
		
		setOpaque(false);
		getViewport().setOpaque(false);
		setBorder(null);
		
		setVBPolicy(vertical);
		setHBPolicy(horizontal);
	}
	
	public ScrollPane(Component view, ScrollBarPolicy generalPolicy) {
		this(view, generalPolicy, generalPolicy);
	}
	
	public ScrollPane(Component view) {
		this(view, ScrollBarPolicy.AS_NEEDED);
	}
	
	public void setVerticalScrollBarPolicy(ScrollBarPolicy policy) {
		int i_policy;
		
		switch(policy){
		case ALWAYS:
			i_policy = VERTICAL_SCROLLBAR_ALWAYS;
			break;
		case AS_NEEDED:
			i_policy = VERTICAL_SCROLLBAR_AS_NEEDED;
			break;
		case NEVER:
			i_policy = VERTICAL_SCROLLBAR_NEVER;
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		super.setVerticalScrollBarPolicy(i_policy);
	}
	
	public void setHorizontalScrollBarPolicy(ScrollBarPolicy policy) {
		int i_policy;
		
		switch(policy){
		case ALWAYS:
			i_policy = HORIZONTAL_SCROLLBAR_ALWAYS;
			break;
		case AS_NEEDED:
			i_policy = HORIZONTAL_SCROLLBAR_AS_NEEDED;
			break;
		case NEVER:
			i_policy = HORIZONTAL_SCROLLBAR_NEVER;
			break;
		default:
			throw new IllegalArgumentException();
		}
		
		super.setHorizontalScrollBarPolicy(i_policy);
	}
	
	public void setVBPolicy(ScrollBarPolicy policy) {
		this.setVerticalScrollBarPolicy(policy);
	}
	
	public void setHBPolicy(ScrollBarPolicy policy) {
		this.setHorizontalScrollBarPolicy(policy);
	}
	
	
	public enum ScrollBarPolicy {
		ALWAYS, AS_NEEDED, NEVER;
	}
}

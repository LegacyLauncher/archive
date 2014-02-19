package com.turikhay.tlauncher.ui.accounts.helper;

import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.util.Arrays;

import com.turikhay.tlauncher.ui.MainPane;
import com.turikhay.tlauncher.ui.accounts.AccountHandler;
import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import com.turikhay.tlauncher.ui.scenes.AccountEditorScene;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedLayeredPane;

public class AccountEditorHelper extends ExtendedLayeredPane {
	private static final int MARGIN = 5;
	public static final byte
		LEFT = 0, UP = 1, RIGHT = 2, DOWN = 3;
	
	private static final long serialVersionUID = -8240523754377261945L;
	
	private final AccountHandler handler;
	private final MainPane pane;
	
	private final HelperStep[] steps;
	
	private HelperState state;
	
	public AccountEditorHelper(AccountEditorScene scene){
		super(scene);
		
		this.handler = scene.handler;
		this.pane = scene.getMainPane();
		
		this.steps = new HelperStep[]{
			new HelperStep("add", handler.list.add, handler.list, DOWN,
					HelperState.LICENSE, HelperState.PIRATE),
			new HelperStep("username", handler.editor.username, handler.editor, LEFT,
					HelperState.LICENSE, HelperState.PIRATE),
			new HelperStep("checkbox", handler.editor.premiumBox, handler.editor, LEFT,
					HelperState.LICENSE, HelperState.PIRATE),
			new HelperStep("password", handler.editor.password, handler.editor, LEFT,
					HelperState.LICENSE),
			new HelperStep("button", handler.editor.save, handler.editor,  LEFT,
					HelperState.LICENSE, HelperState.PIRATE),
			new HelperStep("exit", handler.list.back, handler.list, RIGHT,
					HelperState.LICENSE, HelperState.PIRATE),
			new HelperStep("help", handler.list.help, handler.list, DOWN,
					HelperState.HELP)
		};
		
		add(steps);
		setState(HelperState.NONE);
	}
	
	public HelperState getState(){
		return state;
	}
	
	public void updateState(){
		setState(state);
	}
	
	public void setState(HelperState state){
		if(state == null)
			throw new NullPointerException();
		
		this.state = state;
		
		for(HelperState st : HelperState.values())
			st.item.setEnabled( !st.equals(state) );
		
		if(state == HelperState.NONE){
			for(HelperStep step : steps)
				if(step.isShowing())
					step.setVisible(false);
			return;
		}
		
		for(HelperStep step : steps){			
			if(Arrays.binarySearch(step.states, 0, step.states.length, state) < 0){
				step.setVisible(false);
				continue;
			}
			
			LocalizableLabel l = step.label;
			l.setText("auth.helper." + state.toString() + "." + step.name);
			
			Component c = step.component;
			int cWidth = c.getWidth(), cHeight = c.getHeight();
			Point cp = pane.getLocationOf(c);
			
			Component p = step.parent;
			int pWidth = p.getWidth(), pHeight = p.getHeight();
			Point pp = pane.getLocationOf(p);
			
			FontMetrics fm = l.getFontMetrics(l.getFont());
			Insets i = step.getInsets();
			
			int height = i.top + i.bottom + fm.getHeight();
			int width = i.left + i.right + fm.stringWidth(l.getText());
			
			int x, y;
			
			switch(step.alignment){
			case LEFT:
				x = pp.x - MARGIN - width;
				y = cp.y + cHeight / 2 - height / 2;
				break;
			case UP:
				x = cp.x + cWidth / 2 - width / 2;
				y = pp.y - MARGIN - height;
				break;
			case RIGHT:
				x = pp.x + pWidth + MARGIN;
				y = cp.y + cHeight / 2 - height / 2;
				break;
			case DOWN:
				x = cp.x + cWidth / 2 - width / 2;
				y = pp.y + pHeight + MARGIN;
				break;
			default:
				throw new IllegalArgumentException("Unknown alignment");
			}
			
			step.setVisible(true);
			step.setBounds(x, y, width, height);
		}
		
		this.setVisible(true);
	}
	
	public void onResize(){
		super.onResize();
		updateState();
	}
}

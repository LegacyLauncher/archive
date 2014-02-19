package com.turikhay.tlauncher.ui.console;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.turikhay.tlauncher.ui.block.BlockablePanel;
import com.turikhay.util.StringUtil;

public class SearchPanel extends BlockablePanel {
	private static final long serialVersionUID = -2659114952397165370L;
	protected Insets insets = new Insets(5, 10, 5, 10);
	
	ConsoleFrame cf;
	
	SearchField field;
	SearchPrefs prefs;
	SearchButton button;
	
	String regexp;
	Pattern pt;
	Matcher mt;
	int plastend, lastend;
	
	SearchPanel(ConsoleFrame cf){
		setOpaque(true);
		this.cf = cf;
		
		BorderLayout layout = new BorderLayout();
		layout.setVgap(2);
		layout.setHgap(5);
		this.setLayout(layout);
		
		this.setBackground(Color.black);
		this.setForeground(Color.white);
		
		add("Center", field = new SearchField(this));
		add("East", button = new SearchButton(this));
		add("South", prefs = new SearchPrefs(this));
	}
	
	public void search(){ focus();
		String c_regexp = (prefs.isRegExp())? field.getValue() : StringUtil.addSlashes(field.getValue(), StringUtil.EscapeGroup.REGEXP);
		if(c_regexp == null || c_regexp.trim().length() == 0) return;
		
		if(c_regexp.equalsIgnoreCase("fuck you")){
			log("No, fuck you! :C");
			cf.scrollBottom();
			return;
		}
		
		int flags = Pattern.MULTILINE;
		if(!prefs.isCaseSensetive()) flags |= Pattern.CASE_INSENSITIVE;
		if(prefs.isWordSearch()) c_regexp = "^[.]*(\\s){0,1}("+ c_regexp +")(?:\\1|[\\s]|[\\s]{0,1})";
		
		try{ pt = Pattern.compile(c_regexp, flags); }
		catch(PatternSyntaxException e){ log("Invalid pattern.\n", e.toString()); field.setInvalid(null); return; }
	
		if(!c_regexp.equals(regexp)){
			this.regexp = c_regexp;
			this.lastend = 0;
		}
	
		this.find();
	}
	
	private void find(){
		field.setValid();
		
		String text = cf.getOutput();
		
		this.mt = pt.matcher(text);
		
		if(!mt.find(lastend)){
			if(prefs.isCycled() && plastend != lastend){
				plastend = lastend = 0;
				
				search();
				return;
			}
			
			field.setInvalid(null);
			return;
		}
		
		int 
			group = prefs.isWordSearch()? 2 : 0,
			start = mt.start(group); lastend = mt.end(group);
		
		cf.update = false;
		cf.textArea.requestFocus();
		cf.textArea.select(start, lastend);
	}
	
	protected void focus(){
		field.requestFocusInWindow();
	}
	
	public Insets getInsets() {
		return insets;
	}
	
	private void log(Object... o){ cf.c.log("[CONSOLE]", o); cf.scrollBottom(); }

	public void block(Object reason) {
	}

	public void unblock(Object reason) {
	}

}

package com.turikhay.tlauncher.ui;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.LayoutManager;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import javax.swing.BoxLayout;

import net.minecraft.launcher_.OperatingSystem;
import net.minecraft.launcher_.events.RefreshedVersionsListener;
import net.minecraft.launcher_.updater.VersionFilter;
import net.minecraft.launcher_.updater.VersionManager;
import net.minecraft.launcher_.updater.VersionSyncInfo;
import net.minecraft.launcher_.versions.Version;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherException;
import com.turikhay.tlauncher.minecraft.MinecraftLauncherListener;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.MinecraftUtil;
import com.turikhay.tlauncher.util.U;

public class LoginForm extends Panel implements RefreshedVersionsListener, MinecraftLauncherListener {
	private static final long serialVersionUID = 6768252827144456302L;
	private Insets insets = new Insets(5, 24, 18, 24);
	
	final LoginForm instance = this;
	final TLauncher t;
	final TLauncherFrame f;
	final Settings s, l;
	final VersionManager vm;
	
	Color
		gray = Color.getHSBColor(0, 0, (float) 0.40),
		darkgray = Color.getHSBColor(0, 0, (float) 0.25),
		green = Color.getHSBColor((float) 0.25, (float) 0.66, (float) 0.66),
		red = Color.getHSBColor(0, (float) 0.66, (float) 0.66),
		border = Color.getHSBColor((float) 0.25, (float) 0.66, (float) 0.66); // green
	
	Font font, font_italic, font_bold, font_error, font_small;
	int fontsize;
	String fontname;
	
	Panel error, maininput, versionchoice, autologin, forceupdate, enter;
	
	LayoutManager g_zero = new GridLayout(0, 1);
	LayoutManager g_single = new GridLayout(1, 1);
	LayoutManager g_double = new GridLayout(1, 2);
	FlowLayout g_line = new FlowLayout();
	FlowLayout g_line_center = new FlowLayout();
	BorderLayout g_enter = new BorderLayout();
	
	Label error_l;
	
	TextField username_i;
	boolean username_i_edit;
	String username;
	
	Choice version_dm;
	boolean version_i;
	String version;
	
	// TODO autologin
	Checkbox forceupdate_f;
	boolean forceupdate_e;
	
	private boolean login_blocked;
	Button login_b, settings_b, cancelautologin_b;
	boolean settings_b_pressed;
	
	LoginForm(TLauncherFrame fd){
		this.f = fd; this.t = this.f.t;
		this.s = t.settings; this.l = f.lang;
		this.vm = t.vm;
		
		username = s.get("login.username");		
		version = s.get("login.version");
		
		this.init();
		
		LayoutManager layout = new BoxLayout(this, BoxLayout.PAGE_AXIS);
		this.setLayout(layout);
		
		this.setBackground(gray);
		
		// Error: error_l		
		error_l = new Label("");
		error_l.setFont(font_bold);
		error_l.setAlignment(Label.CENTER); error_l.setForeground(new Color(8388608));
		
		// Maininput: username_i
		maininput = new Panel(g_single); username_i_edit = (username != null);
		username_i = new TextField((username_i_edit)? username : l.get("username"), 20);
		username_i.setFont((username_i_edit)? font : font_italic);
		username_i.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent arg0) { editUsername(); }
			public void mouseEntered(MouseEvent arg0) {}
			public void mouseExited(MouseEvent arg0) {}
			public void mousePressed(MouseEvent arg0) {}
			public void mouseReleased(MouseEvent arg0) {}
		});
		username_i.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e){ }
			public void keyReleased(KeyEvent e) { editUsername(); checkUsername(); }
			public void keyTyped(KeyEvent e){ }
		});
		username_i.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { callLogin(); }
		});
		if(username_i_edit) checkUsername();
		
		maininput.add(username_i);
		
		// Versionchoose: version_l, version_dm
		versionchoice = new Panel(g_double);
		version_dm = new Choice();
		version_dm.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				version = e.getItem().toString();
				onVersionChanged();
			}
		});
		
		versionchoice.add(version_dm);
		
		// TODO autologin checkbox
		// Autologin: autologin_f, autologin_l
		autologin = new Panel(g_zero); /* autologin_save = autologin_enabled;
		autologin_f = new Checkbox(l.get("autologin")); autologin_f.setState(autologin_enabled);
		autologin_f.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				switch(e.getStateChange()){
				case ItemEvent.SELECTED: autologin_save = true; break;
				case ItemEvent.DESELECTED: autologin_save = false; cancelAutoLogin(); break;
				}				
			}
		});
		autologin.add(autologin_f);*/
		forceupdate_f = new Checkbox(l.get("forceupdate"));
		forceupdate_f.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				switch(e.getStateChange()){
				case ItemEvent.SELECTED: forceupdate_e = true; break;
				case ItemEvent.DESELECTED: forceupdate_e = false; break;
				}
			}
		});
		autologin.add(forceupdate_f);
		
		// Enter: login_b, settings_b
		enter = new Panel(g_enter);
		login_b = new Button(l.get("enter")); login_b.setFont(font_bold);
		login_b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { callLogin(); }
		});
		settings_b = new Button(){
			private static final long serialVersionUID = 8147812298929643824L;
			public void update(Graphics g){
				this.paint(g);
			}
			public void paint(Graphics g){
				int offset = (settings_b_pressed)? 1 : 0;
				Image img = instance.f.settings;
				int width = img.getWidth(null), height = img.getHeight(null),
					x = getWidth() / 2 - width / 2, y = getHeight() / 2 - height / 2;
				
				g.drawImage(img, x + offset, y + offset, null);
				settings_b_pressed = false;
			}
		}; settings_b.setPreferredSize(new Dimension(30, settings_b.getHeight()));
		settings_b.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mousePressed(MouseEvent e) { settings_b_pressed = true; }
			public void mouseReleased(MouseEvent e) {}
		});
		
		settings_b.addKeyListener(new KeyListener(){
			public void keyPressed(KeyEvent e) { if(e.getExtendedKeyCode() != 32) return; settings_b_pressed = true; }
			public void keyReleased(KeyEvent e) { settings_b_pressed = false; }
			public void keyTyped(KeyEvent e) {}
		});
		settings_b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				callSettings();
			}
		});
		// TODO cancelautologin
		/*cancelautologin_b = new Button(l.get("autologin.cancel", "i", 5));
		cancelautologin_b.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { cancelAutoLogin(); }
		});
		cancelautologin_b.setVisible(autologin_enabled);*/
		
		enter.add("Center", login_b); enter.add("East", settings_b);
		//enter.add("South", cancelautologin_b);		
		
		this.add(error_l);
		this.add(maininput);
		this.add(versionchoice);
		this.add(del(Del.BOTTOM));
		this.add(autologin);
		this.add(del(Del.TOP));
		this.add(enter);
	}
	
	private void save(){
		U.log("save");
		
		s.set("login.username", username);
		// TODO autologin save
		s.set("login.version", version);
	}
	
	private void editUsername(){
		if(username_i_edit) return;
		
		username_i.setText("");
		username_i.setFont(font);
		
		username_i_edit = true;
	}
	
	private boolean checkUsername(boolean notEmpty){
		String text = username_i.getText();
		String regexp = "^[A-Za-z0-9_-]"+( (notEmpty)? "+" : "*" )+"$";
		
		if(text.matches(regexp)){
			usernameOK();
			
			username = text;
			return true;
		}
		
		if(!username_i.hasFocus() && username != null) username_i.requestFocusInWindow();
		usernameWrong(l.get("username.incorrect"));
		return false;
	}
	
	private boolean checkUsername(){
		return checkUsername(false);
	}
	
	private void usernameWrong(String reason){
		username_i.setBackground(Color.pink);
		setError(reason);
	}
	
	private void usernameOK(){
		username_i.setBackground(Color.white);
		setError(null);
	}
	
	private void onVersionChanged(){
		VersionSyncInfo selected = vm.getVersionSyncInfo(version);
		String path = (selected.isInstalled())? "enter" : "enter.install";
		
		login_b.setLabel(l.get(path));
		version_dm.setEnabled(true);
	}
	
	private void init(){
		g_line.setVgap(0); g_line_center.setVgap(0);
		g_line.setHgap(0); g_line_center.setHgap(0);
		g_line.setAlignment(FlowLayout.LEADING); g_line_center.setAlignment(FlowLayout.CENTER);
		
		g_enter.setVgap(2);
		g_enter.setHgap(3);
		
		font = getFont();
		
		if(font == null) font = new Font("", Font.PLAIN, 12);
		
		fontsize = font.getSize();
		fontname = font.getName();
		
		font_italic = new Font(fontname, Font.ITALIC, fontsize);
		font_bold = new Font(fontname, Font.BOLD, fontsize);
		font_small = new Font(fontname, Font.PLAIN, (fontsize > 5)? fontsize-2 : fontsize);
	}
	
	public void callLogin(){ defocus();
		if(login_blocked) return;
		U.log("login");
		
		if(!checkUsername(true)) return;
		if(!version_i){
			blockLogin();
			vm.asyncRefresh();
			unblockLogin();
			if(!version_i) setError(l.get("versions.notfound"));
			
			return;
		}
		else setError(null);
		
		save();
		t.launch(this, username, version, forceupdate_e);
	}
	
	public void cancelLogin(){ defocus();
		U.log("cancellogin");
		
		unblock();
	}
	
	public void callSettings(){ defocus();
		U.log("settings");
		
		AsyncThread.run(new Runnable(){
			public void run(){
				if(!OperatingSystem.openFile(MinecraftUtil.getWorkingDirectory()))
					Alert.showError(l.get("settings.error.folder.title"), l.get("settings.error.folder", "d", MinecraftUtil.getWorkingDirectory() ));
			}
		});
	}
	
	void callAutoLogin(){
		// TODO callautologin
	}
	
	void cancelAutoLogin(){
		// TODO cancelautologin
	}
	void removeAutoLogin(){ cancelautologin_b.setVisible(false); }
	void setAutologinRemaining(int s){ cancelautologin_b.setLabel(l.get("autologin.cancel", "i", s)); }

	public Insets getInsets() {
		return this.insets;
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void paint(Graphics g) {
		super.paint(g);
		
		g.setColor(border);
		
		for(int x=1;x<4;x++)
			g.drawRect(x-1, x-1, getWidth()-2*x, getHeight()-2*x);
	}
	
	public void setError(String message){
		if(message == null){
			this.border = green; this.repaint();
			this.error_l.setText("");
			return;
		}
		
		this.border = red; this.repaint();
		this.error_l.setText(message);
	}
	
	private Panel del(int aligment){
		return new Del(2, aligment, darkgray);
	}
	
	private void defocus(){
		this.requestFocusInWindow();
	}
	
	private void block(){
		username_i.setEnabled(false);
		version_dm.setEnabled(false);
		//settings_b.setEnabled(false);
		
		blockLogin();
	}
	private void unblock(){
		username_i.setEnabled(true);
		version_dm.setEnabled(true);
		//settings_b.setEnabled(true);
		
		unblockLogin();
	}
	void blockLogin(){
		login_blocked = true;
		login_b.setEnabled(false);
	}
	void unblockLogin(){
		login_blocked = false;
		login_b.setEnabled(true);
	}
	
	public void onVersionsRefreshed(VersionManager vm) { refreshVersions(vm, false); }
	public void onVersionsRefreshingFailed(VersionManager vm){ refreshVersions(vm, true); }
	
	public void onVersionsRefreshing(VersionManager vm){
		version_dm.setEnabled(false);
		version_dm.removeAll();
		version_dm.add(l.get("versions.loading"));
		
		blockLogin();
	}
	
	private void refreshVersions(VersionManager vm, boolean local){ unblockLogin();
		version_dm.removeAll();
		
		VersionFilter vf = MinecraftUtil.getVersionFilter();
		List<VersionSyncInfo> listver = (local)? vm.getInstalledVersions(vf) : vm.getVersions(vf);
		
		for(VersionSyncInfo curv : listver){
			Version ver = curv.getLatestVersion();
			String toadd = ver.getId();
			
			version_dm.add(toadd);
			if(toadd.equals(version)) version_dm.select(toadd);
		}
		
		if(version_dm.getItemCount() != 0){
			if(version == null) version = version_dm.getItem(0);
			
			onVersionChanged();
			version_i = true;
			return;
		}
		version_dm.add(l.get("versions.notfound.tip"));
	}
	
	public void onMinecraftCheck() { block();
		
	}
	
	public void onMinecraftPrepare() { block();
	}

	public void onMinecraftLaunch() { unblock();
		vm.asyncRefresh();
		f.mc.sun.suspend();
	}
	
	public void onMinecraftClose() {
		unblock();
		f.mc.sun.resume();
	}

	public void onMinecraftError(Throwable e) { unblock();
		Alert.showError(l.get("launcher.error.title"), l.get("launcher.error.unknown"), e);
	}
	
	public void onMinecraftError(String message) { unblock();
		Alert.showError(l.get("launcher.error.title"), l.get(message));
	}

	public void onMinecraftError(MinecraftLauncherException knownError) { unblock();
		Alert.showError(l.get("launcher.error.title"), l.get(knownError.getLangpath(), "r", knownError.getReplace()) );
	}
	
	public void onMinecraftWarning(String message, String replace) {
		String prefix = "launcher.warning.";
		Alert.showWarning(l.get(prefix + "title"), l.get(prefix + message, "r", replace) );
	}
}

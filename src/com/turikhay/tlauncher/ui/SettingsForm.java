package com.turikhay.tlauncher.ui;

import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.turikhay.tlauncher.util.U;

public class SettingsForm extends CenterPanel {
	private static final long serialVersionUID = -4851979612103757573L;
	
	final GameDirectoryField gameDirField;
	final ResolutionField resolutionField;
	final JavaExecutableField pathCustomField;
	final AutologinTimeoutField autologinField;
	final LangChoice langChoice;
	//final MemoryField memoryCustomField;
	
	final LocalizableLabel gameDirCustom, resolutionCustom, pathCustom, argsCustom, tlauncherSettings, autologinCustom, langCustom; //memoryCustom;
	final ArgsField javaArgsField, minecraftArgsField;
	final LocalizableCheckbox snapshotsSelect, betaSelect, alphaSelect, consoleSelect, sunSelect;
	final LocalizableLabel versionChoice;
	final Button backButton, defButton;
	
	final SettingsPanel settingsPan;
	final VersionsPanel versionsPan;
	final TLauncherSettingsPanel tlauncherPan;
	final ArgsPanel argsPan;
	
	final FocusListener warner, restart;
	
	private boolean
		snapshot_old, snapshot_changed,
		beta_old, beta_changed,
		alpha_old, alpha_changed;

	public SettingsForm(TLauncherFrame tlauncher) {
		super(tlauncher);
		
		warner = new FocusListener(){
			public void focusGained(FocusEvent e) {
				error.setText("settings.warning");
			}
			public void focusLost(FocusEvent e) {
				error.setText("");
			}
		};
		
		restart = new FocusListener(){
			public void focusGained(FocusEvent e) {
				error.setText("settings.restart");
			}
			public void focusLost(FocusEvent e) {
				error.setText("");
			}
		};
		
		settingsPan = new SettingsPanel(this);
		
		gameDirField = new GameDirectoryField(this); gameDirField.addFocusListener(warner);
		resolutionField = new ResolutionField(this);
		pathCustomField = new JavaExecutableField(this); pathCustomField.addFocusListener(warner);
		autologinField = new AutologinTimeoutField(this);
		langChoice = new LangChoice(this);
		//memoryCustomField = new MemoryField(this); memoryCustomField.addFocusListener(warner);
		
		gameDirCustom = new LocalizableLabel("settings.client.gamedir.label");
		resolutionCustom = new LocalizableLabel("settings.client.resolution.label");
		
		versionChoice = new LocalizableLabel("settings.versions.label");
		
		snapshotsSelect = new LocalizableCheckbox("settings.versions.snapshots");
		snapshotsSelect.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				if(snapshot_old == selected) snapshot_changed = false;
				else snapshot_changed = true;
			}
		});
		betaSelect = new LocalizableCheckbox("settings.versions.beta");
		betaSelect.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				if(beta_old == selected) beta_changed = false;
				else beta_changed = true;
			}
		});
		alphaSelect = new LocalizableCheckbox("settings.versions.alpha");
		alphaSelect.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				if(alpha_old == selected) alpha_changed = false;
				else alpha_changed = false;
			}
		});
		versionsPan = new VersionsPanel(this);
		
		pathCustom = new LocalizableLabel("settings.java.path.label");
		argsCustom = new LocalizableLabel("settings.java.args.label");
		javaArgsField = new ArgsField(this, "settings.java.args.jvm"); javaArgsField.addFocusListener(warner);
		minecraftArgsField = new ArgsField(this, "settings.java.args.minecraft"); minecraftArgsField.addFocusListener(warner);
		argsPan = new ArgsPanel(this);
		tlauncherSettings = new LocalizableLabel("settings.tlauncher.label");
		consoleSelect = new LocalizableCheckbox("settings.tlauncher.console");
		sunSelect = new LocalizableCheckbox("settings.tlauncher.sun");
		sunSelect.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				switch(e.getStateChange()){
				case ItemEvent.SELECTED:
					f.mc.bg.start();
					break;
				case ItemEvent.DESELECTED:
					f.mc.bg.stop();
					break;
				}
			}
		});
		tlauncherPan = new TLauncherSettingsPanel(this);
		autologinCustom = new LocalizableLabel("settings.tlauncher.autologin.label");
		//memoryCustom = new Label(l.get("settings.java.memory.label"));
		
		langCustom = new LocalizableLabel("settings.lang.label");
		
		backButton = new LocalizableButton("settings.back"); backButton.setFont(font_bold);
		backButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(save())
					goBack();
			}
		});
		defButton = new LocalizableButton("settings.default");
		defButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(!Alert.showQuestion("settings.setdefault", true)) return;
				
				setToDefaults();
			}
		});
		 
		settingsPan.createInterface();
		
		add(error);
		add(settingsPan);
		add(backButton);
		add(defButton);
		
		updateValues();
	}
	
	public void updateValues(){
		String
			gamedir = s.get("minecraft.gamedir"),
			javadir = s.get("minecraft.javadir"),
			javaargs = s.get("minecraft.javaargs"),
			args = s.get("minecraft.args"),
			locale = s.get("locale");
			//memory = s.get("minecraft.memory");
		int
			resW = s.getInteger("minecraft.size.width"),
			resH = s.getInteger("minecraft.size.height"),
			autologin = s.getInteger("login.auto.timeout");
		boolean
			snapshots = snapshot_old = s.getBoolean("minecraft.versions.snapshots"),
			beta = beta_old = s.getBoolean("minecraft.versions.beta"),
			alpha = alpha_old = s.getBoolean("minecraft.versions.alpha"),
			console = s.getBoolean("gui.console"),
			sun = s.getBoolean("gui.sun");
		snapshot_changed = beta_changed = alpha_changed = false;
		
		gameDirField.setText(gamedir);
		resolutionField.setValues(resW, resH);
		pathCustomField.setText(javadir);
		javaArgsField.setText(javaargs);
		minecraftArgsField.setText(args);
		langChoice.selectValue(locale);
		//memoryCustomField.setText(memory);
		
		snapshotsSelect.setState(snapshots);
		betaSelect.setState(beta);
		alphaSelect.setState(alpha);
		
		consoleSelect.setState(console);
		sunSelect.setState(sun);
		
		autologinField.setText(autologin);
	}
	
	public void setToDefaults(){
		gameDirField.setText(null);
		resolutionField.setValues(0, 0);
		pathCustomField.setText(null);
		javaArgsField.setText(null);
		minecraftArgsField.setText(null);
		//memoryCustomField.setText(null);
		
		snapshotsSelect.setState(true);
		betaSelect.setState(true);
		alphaSelect.setState(true);
		
		consoleSelect.setState(false);
		sunSelect.setState(true);
		
		autologinField.setText(Autologin.DEFAULT_TIMEOUT);
	}
	
	public boolean save(){		
		U.log("Saving settings...");
		
		String
			gamedir = gameDirField.getValue(),
			javadir = pathCustomField.getValue(),
			javaargs = javaArgsField.getValue(),
			args = minecraftArgsField.getValue(),
			locale = langChoice.getValue();
			//memory = memoryCustomField.getSpecialValue();
		int
			autologin = autologinField.getSpecialValue();
		int[]
			size = resolutionField.getValues();
		boolean
			snapshots = snapshot_old = snapshotsSelect.getState(),
			beta = beta_old = betaSelect.getState(),
			alpha = alpha_old = alphaSelect.getState(),
			console = consoleSelect.getState(),
			sun = sunSelect.getState();
		
		if(gamedir == null) return setError(l.get("settings.client.gamedir.invalid"));
		if(javadir == null) return setError(l.get("settings.java.path.invalid"));
		//if(memory == -1) return setError(l.get("settings.java.memory.invalid"));
		if(size == null) return setError(l.get("settings.client.resolution.invalid"));
		
		s.set("minecraft.gamedir", gamedir, false);
		s.set("minecraft.javadir", javadir, false);
		s.set("minecraft.javaargs", javaargs, false);
		s.set("minecraft.args", args, false);
		s.set("locale", locale, false);
		//s.set("minecraft.memory", memory);
		
		s.set("minecraft.size.width", size[0], false);
		s.set("minecraft.size.height", size[1], false);
		
		s.set("minecraft.versions.snapshots", snapshots, false);
		s.set("minecraft.versions.beta", beta, false);
		s.set("minecraft.versions.alpha", alpha, false);
		
		s.set("gui.console", console, false);
		s.set("gui.sun", sun, false);
		
		s.set("login.auto.timeout", autologin, true);
		U.log("Settings saved!");
		
		if(langChoice.changed){
			langChoice.setCurrent();
			U.log("Language has been changed.");
			f.updateLocales();
		}
		
		if(snapshot_changed || beta_changed || alpha_changed) f.lf.versionchoice.asyncRefresh();
		snapshot_changed = beta_changed = alpha_changed = false;
		
		return true;
	}
	
	private void goBack(){
		f.mc.showLogin();
	}
	
	protected void blockElement(Object reason) {
	
	}

	protected void unblockElement(Object reason) {
	
	}
}
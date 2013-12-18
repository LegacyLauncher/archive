package com.turikhay.tlauncher.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import com.turikhay.tlauncher.ui.TimeoutField.FieldType;
import com.turikhay.util.U;

public class SettingsForm extends CenterPanel implements LoginListener {
	private static final long serialVersionUID = -4851979612103757573L;
	
	final GameDirectoryField gameDirField;
	final ResolutionField resolutionField;
	final JavaExecutableField pathCustomField;
	final AutologinTimeoutField autologinField;
	final LangChoice langChoice;
	final LaunchActionChoice launchActionChoice;
	final TimeoutField connectionField;
	//final MemoryField memoryCustomField;
	
	final LocalizableLabel gameDirCustom, resolutionCustom, pathCustom, argsCustom, tlauncherSettings, autologinCustom, launchActionCustom, connTimeoutLabel; //memoryCustom;
	final JLabel langCustom;
	final ArgsField javaArgsField, minecraftArgsField;
	final SettingsCheckbox snapshotsSelect, betaSelect, alphaSelect, cheatsSelect, consoleSelect, sunSelect;
	final LocalizableLabel versionChoice;

	final LocalizableButton defButton, saveButton;
	
	final SettingsPanel settingsPan;
	final VersionsPanel versionsPan;
	final TLauncherSettingsPanel tlauncherPan;
	final ArgsPanel argsPan;
	final SaveSettingsPanel savePan;
	
	final FocusListener warner, restart;
	
	
	private String oldDir;
	private boolean
		snapshot_old, snapshot_changed,
		beta_old, beta_changed,
		alpha_old, alpha_changed,
		cheats_old, cheats_changed;

	public SettingsForm(TLauncherFrame tlauncher) {
		super(tlauncher);
		
		warner = new FocusListener(){
			public void focusGained(FocusEvent e) {
				error_l.setText("settings.warning");
			}
			public void focusLost(FocusEvent e) {
				error_l.setText(" ");
			}
		};
		
		restart = new FocusListener(){
			public void focusGained(FocusEvent e) {
				error_l.setText("settings.restart");
			}
			public void focusLost(FocusEvent e) {
				error_l.setText(" ");
			}
		};
		
		settingsPan = new SettingsPanel(this);
		
		gameDirField = new GameDirectoryField(this); gameDirField.addFocusListener(warner);
		resolutionField = new ResolutionField(this);
		pathCustomField = new JavaExecutableField(this); pathCustomField.addFocusListener(warner);
		autologinField = new AutologinTimeoutField(this);
		langChoice = new LangChoice(this);
		launchActionChoice = new LaunchActionChoice(this);
		connectionField = new TimeoutField(this, FieldType.CONNECTION);
		//memoryCustomField = new MemoryField(this); memoryCustomField.addFocusListener(warner);
		
		gameDirCustom = new LocalizableLabel("settings.client.gamedir.label");
		resolutionCustom = new LocalizableLabel("settings.client.resolution.label");
		
		versionChoice = new LocalizableLabel("settings.versions.label");
		
		snapshotsSelect = new SettingsCheckbox(this, "settings.versions.snapshots", "minecraft.versions.snapshots");
		snapshotsSelect.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				snapshot_changed = (snapshot_old != selected);
			}
		});
		betaSelect = new SettingsCheckbox(this, "settings.versions.beta", "minecraft.versions.beta");
		betaSelect.addItemListener(new CheckBoxListener(){
			public void itemStateChanged(boolean newstate) {
				beta_changed = (beta_old != newstate);
			}
		});
		alphaSelect = new SettingsCheckbox(this, "settings.versions.alpha", "minecraft.versions.alpha");
		alphaSelect.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				alpha_changed = (alpha_old != selected);
			}
		});
		cheatsSelect = new SettingsCheckbox(this, "settings.versions.cheats", "minecraft.versions.cheats");
		cheatsSelect.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				boolean selected = e.getStateChange() == ItemEvent.SELECTED;
				cheats_changed = (cheats_old != selected);
			}
		});
		versionsPan = new VersionsPanel(this);
		
		pathCustom = new LocalizableLabel("settings.java.path.label");
		argsCustom = new LocalizableLabel("settings.java.args.label");
		javaArgsField = new ArgsField(this, "settings.java.args.jvm", "minecraft.javaargs"); javaArgsField.addFocusListener(warner);
		minecraftArgsField = new ArgsField(this, "settings.java.args.minecraft", "minecraft.args"); minecraftArgsField.addFocusListener(warner);
		argsPan = new ArgsPanel(this);
		tlauncherSettings = new LocalizableLabel("settings.tlauncher.label");
		consoleSelect = new SettingsCheckbox("settings.tlauncher.console", "gui.console", false);
		sunSelect = new SettingsCheckbox("settings.tlauncher.sun", "gui.sun", true);
		sunSelect.addItemListener(new ItemListener(){
			public void itemStateChanged(ItemEvent e) {
				if(f.mp == null) return; // TODO fix npe
				switch(e.getStateChange()){
				case ItemEvent.SELECTED:
					f.mp.startBackground();
					break;
				case ItemEvent.DESELECTED:
					f.mp.stopBackground();
					break;
				}
			}
		});
		tlauncherPan = new TLauncherSettingsPanel(this);
		autologinCustom = new LocalizableLabel("settings.tlauncher.autologin.label");
		//memoryCustom = new Label(l.get("settings.java.memory.label"));
		
		connTimeoutLabel = new LocalizableLabel("settings.timeouts.connection.label");
		
		langCustom = new JLabel("Language:");
		launchActionCustom = new LocalizableLabel("settings.launch-action.label");
		
		saveButton = new LocalizableButton("settings.save");
		saveButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { defocus();
				if(save())
					f.mp.toggleSettings();
			}
		});
		
		defButton = new LocalizableButton("settings.default");
		defButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) { defocus();
				if(!Alert.showQuestion("settings.setdefault", true)) return;
				
				setToDefaults();
			}
		});
		
		savePan = new SaveSettingsPanel(this);
		 
		settingsPan.createInterface();
		
		add(error);
		add(settingsPan);
		add(savePan);
		
		updateValues();
	}
	
	public void updateValues(){
		//U.log("Updating values...");
		
		SettingsField sf; String key, val;
		for(Component c : findFields())
			if(c instanceof SettingsField){
				//U.log("> Filling " + c.getClass().getSimpleName()+"...");
				
				sf = (SettingsField) c;
				key = sf.getSettingsPath();
				val = s.get(key);
				//U.log("Key:", key, "Value:", val);
				
				sf.setValue(val);
			}
		
		oldDir = gameDirField.getValue();
		
		snapshot_changed = beta_changed = alpha_changed = cheats_changed = false;
		
		snapshot_old = snapshotsSelect.getState();
		alpha_old = alphaSelect.getState();
		beta_old = betaSelect.getState();
		cheats_old = cheatsSelect.getState();
	}
	
	public void updateIfSaveable(){
		String key;
		
		for(Component c : findFields())
			if(c instanceof SettingsField){				
				key = ((SettingsField) c).getSettingsPath();
				
				if(s.isSaveable(key))
					continue;
				
				c.setEnabled(false);
			}
	}
	
	public void setToDefaults(){
		for(Component c : findFields())
			if(c instanceof SettingsField)
				((SettingsField) c).setToDefault();
	}
	
	public boolean save(){
		U.log("Saving...");
		
		SettingsField sf;
		for(Component c : findFields())
			if(c instanceof SettingsField){
				//U.log("> Checking " + c.getClass().getSimpleName()+"...");
				sf = (SettingsField) c;
				if(!sf.isValueValid()){
					//U.log("Invalid!");
					c.setBackground(wrongColor);
					return false;
				}
				
				if(!c.isEnabled()){
					//U.log("Is insaveable!");
					continue;
				}
				
				s.set(sf.getSettingsPath(), sf.getValue(), false);
				//U.log("Saved (", sf.getSettingsPath(), ":", sf.getValue(), ")");
			}
		
		try{
			s.save();
			U.log("Settings saved!");
		}catch(IOException e){
			U.log("Cannot save settings!");
			Alert.showError(e, false);
		}
		
		if(langChoice.changed){
			langChoice.setCurrent();
			U.log("Language has been changed.");
			f.updateLocales();
		}
		
		String gamedir = gameDirField.getValue();
		if(!gamedir.equals(oldDir)){ oldDir = gamedir;
			U.log("Game directory has been changed. Recreating Version Manager.");
			try {
				t.getVersionManager().recreate();
			} catch (IOException e) {
				Alert.showError(e, false);
			}
			return true;
		}
		
		if(snapshot_changed || beta_changed || alpha_changed || cheats_changed) f.lf.versionchoice.asyncRefresh();
		snapshot_changed = beta_changed = alpha_changed = cheats_changed = false;
		
		snapshot_old = snapshotsSelect.getState();
		alpha_old = alphaSelect.getState();
		beta_old = betaSelect.getState();
		cheats_old = cheatsSelect.getState();
		
		return true;
	}
	
	private List<Component> findFields(Container container){
		List<Component> f = new ArrayList<Component>();
		
		for(Component c : container.getComponents())
			if(c instanceof SettingsField) f.add(c);
			else if(c instanceof Container) f.addAll(findFields((Container) c));
		
		return f;
	}
	
	private List<Component> findFields(){ return findFields(this); }
	
	public boolean onLogin() {
		if(save()) return true;
		
		f.mp.setSettings(true);
		return false;
	}
	public void onLoginFailed() {}
	public void onLoginSuccess() {}
}
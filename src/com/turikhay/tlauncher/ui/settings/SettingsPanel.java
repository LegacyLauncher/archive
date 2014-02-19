package com.turikhay.tlauncher.ui.settings;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.JScrollPane;

import net.minecraft.launcher.versions.ReleaseType;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.Configuration.ActionOnLaunch;
import com.turikhay.tlauncher.configuration.Configuration.ConnectionQuality;
import com.turikhay.tlauncher.configuration.Configuration.ConsoleType;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blocker;
import com.turikhay.tlauncher.ui.center.CenterPanel;
import com.turikhay.tlauncher.ui.converter.ActionOnLaunchConverter;
import com.turikhay.tlauncher.ui.converter.ConnectionQualityConverter;
import com.turikhay.tlauncher.ui.converter.ConsoleTypeConverter;
import com.turikhay.tlauncher.ui.converter.LocaleConverter;
import com.turikhay.tlauncher.ui.loc.LocalizableButton;
import com.turikhay.tlauncher.ui.loc.LocalizableLabel;
import com.turikhay.tlauncher.ui.login.LoginException;
import com.turikhay.tlauncher.ui.login.LoginListener;
import com.turikhay.tlauncher.ui.scenes.DefaultScene;
import com.turikhay.tlauncher.ui.swing.Del;
import com.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import com.turikhay.util.U;

public class SettingsPanel extends CenterPanel implements LoginListener {
	private static final long serialVersionUID = 3896900830909661270L;
	private static final int PANELS = 4;
	
	public final DefaultScene scene;
	
	private final JScrollPane scrollPane;
	
	protected final ExtendedPanel container;
	protected final ExtendedPanel[] panels;
	protected final GridBagConstraints[] constraints;
	
	public final SettingsFieldHandler
		directory, resolution,
		javaPath, javaArgs, args,
		console, connection, action,
		lang;
	
	public final SettingsGroupHandler
		versionHandler;
	
	protected final LocalizableButton
		saveButton, defaultButton;
	
	protected final List<SettingsHandler> handlers;
	
	public SettingsPanel(DefaultScene sc){
		super(squareInsets);
		
		this.scene = sc;
		
		FocusListener
		warning = new FocusListener(){
			public void focusGained(FocusEvent e) {
				setMessage("settings.warning");
			}
			public void focusLost(FocusEvent e) {
				setMessage(null);
			}
		},		
		restart = new FocusListener(){
			public void focusGained(FocusEvent e) {
				setMessage("settings.restart");
			}
			public void focusLost(FocusEvent e) {
				setMessage(null);
			}
		};
		
		this.container = new ExtendedPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		
		this.panels = new ExtendedPanel[PANELS];
		this.constraints = new GridBagConstraints[PANELS];
		
		for(int i=0;i<PANELS;i++){
			panels[i] = new ExtendedPanel(new GridBagLayout());
			panels[i].getInsets().set(0, 0, 0, 0);
			
			constraints[i] = new GridBagConstraints();
			constraints[i].fill = GridBagConstraints.HORIZONTAL;
			
			container.add(panels[i], del(Del.CENTER));
		}
		
		this.handlers = new ArrayList<SettingsHandler>();
		
		byte pane = 0, row = 0;
		
		this.directory = new SettingsFieldHandler("minecraft.gamedir", new SettingsTextField("settings.client.gamedir.prompt"), warning);
		directory.addListener(new SettingsFieldChangeListener(){
			@Override
			protected void onChange(String oldValue, String newValue) {
				if(!tlauncher.isReady()) return;
				
				try {
					tlauncher.getManager().getVersionLists().updateLocal();
				} catch (IOException e) {
					Alert.showError("settings.client.gamedir.noaccess", e);
				}
				tlauncher.getVersionManager().asyncRefresh();
			}
		});
		add(pane, row++, new SettingsPair("settings.client.gamedir.label", directory));
		
		this.resolution = new SettingsFieldHandler("minecraft.size", new SettingsResolutionField("settings.client.resolution.width", "settings.client.resolution.height", global), restart);
		add(pane, row++, new SettingsPair("settings.client.resolution.label", resolution));
		
		++pane; row = 0;
		
		ReleaseType[] releaseTypes = ReleaseType.getDefinable();
		SettingsFieldHandler[] versions = new SettingsFieldHandler[releaseTypes.length];
		
		for(int i=0;i<releaseTypes.length;i++){
			ReleaseType releaseType = releaseTypes[i];
			versions[i] = new SettingsFieldHandler("minecraft.versions." + releaseType, new SettingsCheckBox("settings.versions." + releaseType));
		}
		
		this.versionHandler = new SettingsGroupHandler(versions);
		versionHandler.addListener(new SettingsFieldChangeListener(){
			protected void onChange(String oldvalue, String newvalue) {
				TLauncher.getInstance().getVersionManager().updateVersionList();
			}
		});
		
		add(pane, row++, new SettingsPair("settings.versions.label", versions));

		++pane; row = 0;
		
		this.javaArgs = new SettingsFieldHandler("minecraft.javaargs", new SettingsTextField("settings.java.args.jvm", true), warning);
		this.args = new SettingsFieldHandler("minecraft.args", new SettingsTextField("settings.java.args.minecraft", true), warning);
		add(pane, row++, new SettingsPair("settings.java.args.label", javaArgs, args));
		
		this.javaPath = new SettingsFieldHandler("minecraft.javadir", new SettingsTextField("settings.java.path.prompt", true), warning);
		add(pane, row++, new SettingsPair("settings.java.path.label", javaPath));
		
		++pane; row = 0;
		
		this.console = new SettingsFieldHandler("gui.console", new SettingsComboBox<ConsoleType>(new ConsoleTypeConverter(), ConsoleType.values()));
		console.addListener(new SettingsFieldChangeListener(){
			protected void onChange(String oldvalue, String newvalue) {
				if(newvalue == null) return;
				switch(ConsoleType.get(newvalue)){
				case GLOBAL:
					TLauncher.getConsole().show(false);
					break;
				case MINECRAFT:
				case NONE:
					TLauncher.getConsole().hide();
					break;
				default:
					throw new IllegalArgumentException("Unknown console type!");
				}
			}
		});
		add(pane, row++, new SettingsPair("settings.console.label", console));
		
		this.connection = new SettingsFieldHandler("connection", new SettingsComboBox<ConnectionQuality>(new ConnectionQualityConverter(), ConnectionQuality.values()));
		add(pane, row++, new SettingsPair("settings.connection.label", connection));
		
		this.action = new SettingsFieldHandler("minecraft.onlaunch", new SettingsComboBox<ActionOnLaunch>(new ActionOnLaunchConverter(), ActionOnLaunch.values()));
		add(pane, row++, new SettingsPair("settings.launch-action.label", action));
		
		this.lang = new SettingsFieldHandler("locale", new SettingsComboBox<Locale>(new LocaleConverter(), global.getLocales()));
		lang.addListener(new SettingsFieldChangeListener(){
			protected void onChange(String oldvalue, String newvalue) {
				if(tlauncher.getFrame() != null)
					tlauncher.getFrame().updateLocales();
			}
		});
		add(pane, row++, new SettingsPair("settings.lang.label", lang));
		
		this.saveButton = new LocalizableButton("settings.save");
		saveButton.setFont(saveButton.getFont().deriveFont(Font.BOLD));
		saveButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				if(saveValues())
					scene.setSettings(false);
			}
			
		});
		
		this.defaultButton = new LocalizableButton("settings.default");
		defaultButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				resetValues();
			}
		});
		
		container.add(sepPan(saveButton, defaultButton));
		
		this.scrollPane = new JScrollPane(container);
		scrollPane.setOpaque(false);
		scrollPane.getViewport().setOpaque(false);
		scrollPane.setBorder(null);
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	    scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
	    
	    add(messagePanel, scrollPane);
		
		this.updateValues();
	}
	
	protected void add(int pane, int row, SettingsPair pair){
		LocalizableLabel label = pair.getLabel();
		ExtendedPanel field = pair.getPanel();
		
		ExtendedPanel panel = this.panels[pane];
		GridBagConstraints c = this.constraints[pane];
		
		c.anchor = GridBagConstraints.WEST;
		c.gridy = row;
		c.gridx = 0;
		c.weightx = 0.1;
		panel.add(label, c);
		
		c.anchor = GridBagConstraints.EAST;
		c.gridy = row++;
		c.gridx = 1;
		c.weightx = 1;
		panel.add(field, c);
		
		Collections.addAll(this.handlers, pair.getHandlers());
	}
	
	protected boolean checkValues(){
		boolean allValid = true;
		
		for(SettingsHandler handler : handlers){
			boolean valid = handler.isValid();
			
			handler.getComponent().setBackground(valid? getTheme().getBackground() : getTheme().getFailure());
			if(!valid) allValid = false;
		}
		
		return allValid;
	}
	
	public void updateValues(){
		boolean globalUnSaveable = !global.isSaveable(); 
		for(SettingsHandler handler : handlers){
			String path = handler.getPath(), value = global.get(path);
			
			handler.setValue(value);
			
			if(globalUnSaveable || !global.isSaveable(path))
				Blocker.block(handler, "unsaveable");
		}
	}
	
	public boolean saveValues(){
		if(!checkValues()) return false;
		
		for(SettingsHandler handler : handlers){
			String path = handler.getPath(), value = handler.getValue();
			
			global.set(path, value, false);
			
			handler.onChange(value);
		}
		
		global.store();
		
		return true;
	}
	
	public void resetValues(){
		for(SettingsHandler handler : handlers){
			String path = handler.getPath(), value = global.getDefault(path);
			
			log("Resetting:", handler.getClass().getSimpleName(), path, value);
			
			if(value == null) continue;
			
			log("Reset!");
			
			handler.setValue(value);
		}
	}
	
	@Override
	public void block(Object reason) {
		Blocker.blockComponents(container, reason);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblockComponents(container, reason);
	}

	@Override
	public void onLogin() throws LoginException {
		if(checkValues()) return;
		
		scene.setSettings(true);		
		throw new LoginException("Invalid settings!");
	}

	@Override
	public void onLoginFailed() {}
	@Override
	public void onLoginSuccess() {}
	
	protected void log(Object...o){ U.log("[SettingsPanel]", o); }
}

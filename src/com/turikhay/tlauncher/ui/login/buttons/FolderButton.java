package com.turikhay.tlauncher.ui.login.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.alert.Alert;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.swing.ImageButton;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.async.AsyncThread;

public class FolderButton extends ImageButton implements Blockable {
	private static final long serialVersionUID = 1621745146166800209L;
	
	private final FolderButton instance = this;
	private final LoginForm lf;
	private final LangConfiguration l;
	
	FolderButton(LoginForm loginform){
		this.lf = loginform;
		this.l = lf.lang;
		
		this.image = loadImage("folder.png");
		this.rotation = ImageRotation.CENTER;
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				instance.openFolder();
				lf.defocus();
			}
		});
		
		this.initImage();
	}
	
	public void openFolder(){
		AsyncThread.execute(new Runnable(){
			public void run(){
				File dir = MinecraftUtil.getWorkingDirectory();
				if(!OperatingSystem.openFile(dir))
					Alert.showError(l.get("folder.error.title"), l.get("folder.error"), dir);
			}
		});
	}
	
	@Override
	public void block(Object reason) {
	}
	
	@Override
	public void unblock(Object reason) {
	}

}

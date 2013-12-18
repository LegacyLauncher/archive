package com.turikhay.tlauncher.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.util.AsyncThread;
import com.turikhay.util.MinecraftUtil;

public class FolderButton extends ImageButton {
	private static final long serialVersionUID = 1621745146166800209L;
	
	private final FolderButton instance = this;
	private final LoginForm lf;
	private final Settings l;
	
	FolderButton(LoginForm loginform){
		this.lf = loginform;
		this.l = lf.l;
		
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

}

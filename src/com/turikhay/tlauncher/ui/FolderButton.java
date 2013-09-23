package com.turikhay.tlauncher.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import net.minecraft.launcher_.OperatingSystem;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;

public class FolderButton extends ImageButton {
	private static final long serialVersionUID = 1621745146166800209L;
	
	private final FolderButton instance = this;
	private final LoginForm lf;
	private final Settings l;
	
	private final File dir;
	
	FolderButton(LoginForm loginform){
		this.lf = loginform;
		this.l = lf.l;
		
		this.image = loadImage("folder.png");
		this.rotation = ImageRotation.CENTER;
		
		this.dir = new File(lf.settings.gameDirField.getValue());
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				instance.openFolder();
				lf.defocus();
			}
		});
	}
	
	public void openFolder(){
		AsyncThread.execute(new Runnable(){
			public void run(){
				if(!OperatingSystem.openFile(dir))
					Alert.showError(l.get("support.error.title"), l.get("support.error"), dir);
			}
		});
	}

}

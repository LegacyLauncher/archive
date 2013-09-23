package com.turikhay.tlauncher.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;

import net.minecraft.launcher_.OperatingSystem;

import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.tlauncher.util.AsyncThread;
import com.turikhay.tlauncher.util.U;

public class SupportButton extends ImageButton {
	private static final long serialVersionUID = 7903730373496194592L;
	
	private final SupportButton instance = this;
	private final LoginForm lf;
	private final Settings l;
	
	private final String path;
	private final URL url;
	private final URI uri;
	
	SupportButton(LoginForm loginform){
		this.lf = loginform;
		this.l = lf.l;
		
		this.image = loadImage("vk.png");
		this.rotation = ImageRotation.CENTER;
		
		this.path = l.get("support.url");
		this.url = U.makeURL(path);
		this.uri = U.makeURI(url);
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				instance.openURL();
				lf.defocus();
			}
		});
	}
	
	public void openURL(){
		AsyncThread.execute(new Runnable(){
			public void run(){
				if(!OperatingSystem.openLink(uri))
					Alert.showError(l.get("support.error.title"), l.get("support.error"), path);
			}
		});
	}
}

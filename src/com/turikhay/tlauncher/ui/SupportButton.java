package com.turikhay.tlauncher.ui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.settings.Settings;
import com.turikhay.util.AsyncThread;
import com.turikhay.util.U;

public class SupportButton extends ImageButton implements LocalizableComponent {
	private static final long serialVersionUID = 7903730373496194592L;
	
	private final SupportButton instance = this;
	private final LoginForm lf;
	private final Settings l;
	
	private String path;
	private URL url;
	private URI uri;
	
	private final Image
		vk = loadImage("vk.png"),
		mail = loadImage("mail.png");
	
	SupportButton(LoginForm loginform){
		this.lf = loginform;
		this.l = lf.l;
		
		this.image = selectImage();
		this.rotation = ImageRotation.CENTER;		
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				instance.openURL();
				lf.defocus();
			}
		});
		
		this.updateURL();
		this.initImage();
	}
	
	public void openURL(){
		AsyncThread.execute(new Runnable(){
			public void run(){
				if(!OperatingSystem.openLink(uri))
					Alert.showError(l.get("support.error.title"), l.get("support.error"), path);
			}
		});
	}
	
	private Image selectImage(){
		String locale = TLauncher.getInstance().getSettings().getLocale().toString();
		
		if(locale.equals("ru_RU") || locale.equals("uk_UA"))
			return vk;
		
		return mail;
	}
	
	private void updateURL(){
		this.path = l.get("support.url");
		this.url = U.makeURL(path);
		this.uri = U.makeURI(url);
	}

	public void updateLocale() {
		this.image = selectImage(); updateURL();
	}
}

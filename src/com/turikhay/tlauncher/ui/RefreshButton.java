package com.turikhay.tlauncher.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RefreshButton extends ImageButton {
	private static final long serialVersionUID = -1334187593288746348L;
	
	private final LoginForm lf;
	
	RefreshButton(LoginForm loginform){
		this.lf = loginform;
		
		this.image = loadImage("refresh.png");
		this.rotation = ImageRotation.CENTER;
		
		this.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				lf.versionchoice.asyncRefresh();
				lf.defocus();
			}
		});
	}

}

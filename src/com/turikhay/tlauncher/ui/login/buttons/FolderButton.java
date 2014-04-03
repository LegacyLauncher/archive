package com.turikhay.tlauncher.ui.login.buttons;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.swing.ImageButton;
import com.turikhay.util.MinecraftUtil;
import com.turikhay.util.async.AsyncThread;

public class FolderButton extends ImageButton implements Blockable {
	private static final long serialVersionUID = 1621745146166800209L;

	private final FolderButton instance = this;
	private final LoginForm lf;

	FolderButton(LoginForm loginform) {
		this.lf = loginform;

		this.image = loadImage("folder.png");
		this.rotation = ImageRotation.CENTER;

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				instance.openFolder();
				lf.defocus();
			}
		});

		this.initImage();
	}

	void openFolder() {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				File dir = MinecraftUtil.getWorkingDirectory();
				OperatingSystem.openFile(dir);
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

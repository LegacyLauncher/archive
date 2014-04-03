package com.turikhay.tlauncher.ui.login.buttons;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.URL;

import net.minecraft.launcher.OperatingSystem;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.block.Blockable;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;
import com.turikhay.tlauncher.ui.login.LoginForm;
import com.turikhay.tlauncher.ui.swing.ImageButton;
import com.turikhay.util.U;
import com.turikhay.util.async.AsyncThread;

public class SupportButton extends ImageButton implements Blockable,
		LocalizableComponent {
	private static final long serialVersionUID = 7903730373496194592L;

	private final SupportButton instance = this;
	private final LoginForm lf;
	private final LangConfiguration l;

	private URI uri;

	private final Image vk = loadImage("vk.png"), mail = loadImage("mail.png");

	SupportButton(LoginForm loginform) {
		this.lf = loginform;
		this.l = lf.lang;

		this.image = selectImage();
		this.rotation = ImageRotation.CENTER;

		this.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				instance.openURL();
				lf.defocus();
			}
		});

		this.updateURL();
		this.initImage();
	}

	void openURL() {
		AsyncThread.execute(new Runnable() {
			@Override
			public void run() {
				OperatingSystem.openLink(uri);
			}
		});
	}

	private Image selectImage() {
		String locale = TLauncher.getInstance().getSettings().getLocale()
				.toString();

		if (locale.equals("ru_RU") || locale.equals("uk_UA"))
			return vk;

		return mail;
	}

	private void updateURL() {
		String path = l.nget("support.url");
		URL url = U.makeURL(path);
		this.uri = U.makeURI(url);
	}

	@Override
	public void updateLocale() {
		this.image = selectImage();
		updateURL();
	}

	@Override
	public void block(Object reason) {
	}

	@Override
	public void unblock(Object reason) {
	}
}

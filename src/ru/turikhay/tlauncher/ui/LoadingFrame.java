package ru.turikhay.tlauncher.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;

import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

public class LoadingFrame extends JFrame {
	private final ProgressBar progress;

	public LoadingFrame() {
		SwingUtil.initLookAndFeel();
		setLayout(new BorderLayout());

		this.progress = new ProgressBar();
		progress.setPreferredSize(new Dimension(250, 18));

		add(progress, BorderLayout.CENTER);
		add(new JLabel(ImageCache.getIcon("fav32.png")), BorderLayout.WEST);

		if(OS.JAVA_VERSION > 1.6)
			setType(Type.UTILITY);

		pack();
		setResizable(false);
		setAlwaysOnTop(true);
		setLocationRelativeTo(null);
	}

	public ProgressBar getProgressBar() {
		return progress;
	}

	public void setProgress(int percent) {
		progress.setIndeterminate(false);
		progress.setValue(percent);
		progress.setCenterString(String.valueOf(percent) +'%');
	}

}

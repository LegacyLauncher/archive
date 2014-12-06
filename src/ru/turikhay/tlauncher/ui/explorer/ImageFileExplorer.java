package ru.turikhay.tlauncher.ui.explorer;

public class ImageFileExplorer extends FileExplorer {
	private static final long serialVersionUID = -5906170445865689621L;

	public ImageFileExplorer(String directory) {
		super(directory);

		setAccessory(new ImageFilePreview(this));
		setFileFilter(new ImageFileFilter());
		setFileView(new ImageFileView());

		setAcceptAllFileFilterUsed(false);
	}

	public ImageFileExplorer() {
		this(null);
	}
}

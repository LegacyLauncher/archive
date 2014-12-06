package ru.turikhay.tlauncher.ui.explorer;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.filechooser.FileSystemView;

public class FileExplorer extends JFileChooser {
	private static final long serialVersionUID = 3826379908958645663L;

	public FileExplorer() {
		super();
	}

	public FileExplorer(String currentDirectoryPath) {
		super(currentDirectoryPath);
	}

	public FileExplorer(int selectionMode, boolean showHidden) {
		this();

		setFileSelectionMode(selectionMode);
		setFileHidingEnabled(!showHidden);
	}

	public FileExplorer(File currentDirectory) {
		super(currentDirectory);
	}

	public FileExplorer(FileSystemView fsv) {
		super(fsv);
	}

	protected FileExplorer(File currentDirectory, FileSystemView fsv) {
		super(currentDirectory);
	}

	public FileExplorer(String currentDirectoryPath, FileSystemView fsv) {
		super(currentDirectoryPath, fsv);
	}

	@Override
	public void setCurrentDirectory(File dir) {
		if (dir == null)
			dir = getFileSystemView().getDefaultDirectory();

		super.setCurrentDirectory(dir);
	}

	public void setCurrentDirectory(String sDir) {
		File dir = (sDir == null) ? null : new File(sDir);
		setCurrentDirectory(dir);
	}

	public int showDialog(Component parent) {
		return showDialog(parent,
				UIManager.getString("FileChooser.directoryOpenButtonText"));
	}

	@Override
	public File[] getSelectedFiles() {
		File[] selectedFiles = super.getSelectedFiles();

		if (selectedFiles.length > 0)
			return selectedFiles;

		File selectedFile = super.getSelectedFile();

		if (selectedFile == null)
			return null;

		return new File[] { selectedFile };
	}
}

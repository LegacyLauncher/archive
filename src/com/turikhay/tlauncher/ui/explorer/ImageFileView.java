package com.turikhay.tlauncher.ui.explorer;

import java.io.File;

import javax.swing.filechooser.FileView;

import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.util.FileUtil;

public class ImageFileView extends FileView {

	@Override
	public String getTypeDescription(File f) {
		String extension = FileUtil.getExtension(f), localized = Localizable
				.nget("explorer.extension." + extension);

		return localized;
	}

}

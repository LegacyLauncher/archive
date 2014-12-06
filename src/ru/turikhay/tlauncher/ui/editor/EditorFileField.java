package ru.turikhay.tlauncher.ui.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.regex.Pattern;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.util.U;

public class EditorFileField extends BorderPanel implements EditorField {
	private static final long serialVersionUID = 5136327098130653756L;
	public static final char DEFAULT_DELIMITER = ';';

	private final EditorTextField textField;
	private final LocalizableButton explorerButton;
	private final FileExplorer explorer;

	private final char delimiterChar;
	private final Pattern delimiterSplitter;

	public EditorFileField(String prompt, boolean canBeEmpty, String button,
			FileExplorer chooser, char delimiter) {
		if (chooser == null)
			throw new NullPointerException("FileExplorer should be defined!");

		this.textField = new EditorTextField(prompt, canBeEmpty);
		this.explorerButton = new LocalizableButton(button);
		this.explorer = chooser;

		this.delimiterChar = delimiter;
		this.delimiterSplitter = Pattern.compile(String.valueOf(delimiterChar),
				Pattern.LITERAL);

		this.explorerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				explorerButton.setEnabled(false);

				explorer.setCurrentDirectory(getFirstFile());
				int result = explorer.showDialog(EditorFileField.this);

				if (result == FileExplorer.APPROVE_OPTION)
					setRawValue(explorer.getSelectedFiles());

				explorerButton.setEnabled(true);
			}
		});

		this.add(textField, BorderLayout.CENTER);
		this.add(explorerButton, BorderLayout.EAST);
	}

	public EditorFileField(String prompt, boolean canBeEmpty, FileExplorer chooser) {
		this(prompt, canBeEmpty, "explorer.browse", chooser, DEFAULT_DELIMITER);
	}

	public EditorFileField(String prompt, FileExplorer chooser) {
		this(prompt, false, chooser);
	}

	@Override
	public String getSettingsValue() {
		return getValueFromRaw(getRawValues());
	}

	private File[] getRawValues() {
		String[] paths = getRawSplitValue();
		if (paths == null)
			return null;

		int len = paths.length;
		File[] files = new File[len];

		for (int i = 0; i < paths.length; i++)
			files[i] = new File(paths[i]);

		return files;
	}

	@Override
	public void setSettingsValue(String value) {
		this.textField.setSettingsValue(value);
	}

	private void setRawValue(File[] fileList) {
		setSettingsValue(getValueFromRaw(fileList));
	}

	private String[] getRawSplitValue() {
		return splitString(textField.getValue());
	}

	private String getValueFromRaw(File[] files) {
		if (files == null)
			return null;

		StringBuilder builder = new StringBuilder();

		for (File file : files) {
			String path = file.getAbsolutePath();
			builder.append(delimiterChar).append(path);
		}

		return builder.substring(1);
	}

	private String[] splitString(String s) {
		if (s == null)
			return null;

		String[] split = delimiterSplitter.split(s);
		if (split.length == 0)
			return null;

		return split;
	}

	private File getFirstFile() {
		File[] files = getRawValues();

		if (files == null || files.length == 0)
			return TLauncher.getDirectory();

		return files[0];
	}

	@Override
	public boolean isValueValid() {
		return textField.isValueValid();
	}

	@Override
	public void setBackground(Color bg) {
		if (textField != null)
			textField.setBackground(bg);
	}

	@Override
	public void block(Object reason) {
		Blocker.blockComponents(reason, textField, explorerButton);
	}

	@Override
	public void unblock(Object reason) {
		Blocker.unblockComponents(Blocker.UNIVERSAL_UNBLOCK, textField,
				explorerButton);
	}

	protected void log(Object... w) {
		U.log("[" + getClass().getSimpleName() + "]", w);
	}
}

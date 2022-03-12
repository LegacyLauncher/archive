package ru.turikhay.tlauncher.ui.editor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.swing.DocumentChangeListener;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;

import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.net.URL;
import java.util.function.Consumer;

public class EditorFileField extends BorderPanel implements EditorField {
    private static final Logger LOGGER = LogManager.getLogger(EditorFileField.class);

    private static final String DEFAULT_BUTTON_PATH = "explorer.browse";

    protected final EditorTextField textField;
    private final LocalizableButton explorerButton;
    private final FileExplorer explorer;

    private final boolean permitUrl;

    public EditorFileField(String prompt, String buttonPath, FileExplorer exp, boolean canBeEmpty, boolean permitUrl) {
        this.permitUrl = permitUrl;

        textField = new EditorTextField(prompt, canBeEmpty);

        explorerButton = new LocalizableButton(buttonPath);
        explorerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (explorer == null) {
                    return;
                }
                explorerButton.setEnabled(false);

                explorer.setCurrentDirectory(getSelectedFile());
                int result = explorer.showDialog(EditorFileField.this);
                if (result == 0) {
                    File selected = explorer.getSelectedFile();
                    String path;

                    if (selected == null) {
                        path = "";
                    } else {
                        try {
                            path = selected.getCanonicalPath();
                        } catch (Exception ex) {
                            path = selected.getAbsolutePath();
                            LOGGER.warn("Couldn't get canonical path. Will use absolute path: {} -> {}",
                                    selected, path, ex);
                        }
                    }

                    setSettingsValue(path);
                }

                explorerButton.setEnabled(true);
            }
        });

        explorer = exp;

        setCenter(textField);
        if (explorer != null)
            setEast(explorerButton);
    }

    public EditorFileField(String prompt, FileExplorer exp, boolean canBeEmpty, boolean permitUrl) {
        this(prompt, DEFAULT_BUTTON_PATH, exp, canBeEmpty, permitUrl);
    }

    public File getSelectedFile() {
        File selected = null;

        if (getSettingsValue() != null && parseUrl(getSettingsValue()) == null) {
            selected = new File(getSettingsValue());
        } else if (explorer != null) {
            selected = explorer.getSelectedFile();
        }

        return selected == null ? new File(".") : selected;
    }

    @Override
    public void setBackground(Color bg) {
        if (textField != null)
            textField.setBackground(bg);
    }

    @Override
    public String getSettingsValue() {
        String value = textField.getSettingsValue();
        URL testUrl;

        try {
            testUrl = new URL(value);
        } catch (Exception e) {
            return FileExplorer.normalize(value);
        }

        return testUrl.toString();
    }

    @Override
    public void setSettingsValue(String var) {
        textField.setSettingsValue(var);
    }

    @Override
    public boolean isValueValid() {
        if (!permitUrl && parseUrl(getSettingsValue()) != null)
            return false;

        return textField.isValueValid();
    }

    @Override
    public void block(Object reason) {
        Blocker.blockComponents(reason, textField, explorerButton);
    }

    @Override
    public void unblock(Object reason) {
        Blocker.unblockComponents(Blocker.UNIVERSAL_UNBLOCK, textField, explorerButton);
    }

    private static URL parseUrl(String s) {
        URL testUrl;

        try {
            testUrl = new URL(s);
        } catch (Exception e) {
            return null;
        }

        return testUrl;
    }

    public void addChangeListener(Consumer<String> listener) {
        textField.getDocument().addDocumentListener(new DocumentChangeListener() {
            @Override
            public void documentChanged(DocumentEvent e) {
                listener.accept(textField.getSettingsValue());
            }
        });
    }

    /*private EditorTextField textField;
    private final LocalizableButton explorerButton;
    private final FileExplorer explorer;
    private final char delimiterChar;
    private final Pattern delimiterSplitter;

    public EditorFileField(String prompt, boolean canBeEmpty, String button, FileExplorer chooser, char delimiter) {
        textField = new EditorTextField(prompt, canBeEmpty);
        explorerButton = new LocalizableButton(button);
        explorer = chooser;
        delimiterChar = delimiter;
        delimiterSplitter = Pattern.compile(String.valueOf(delimiterChar), 16);
        explorerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (explorer != null) {
                    explorerButton.setEnabled(false);
                    explorer.setCurrentDirectory(getFirstFile());
                    int result = explorer.showDialog(EditorFileField.this);
                    if (result == 0) {
                        setRawValue(explorer.getSelectedFiles());
                    }

                    explorerButton.setEnabled(true);
                }
            }
        });
        add(textField, "Center");
        if (explorer != null) {
            add(explorerButton, "East");
        }

    }

    public EditorFileField(String prompt, boolean canBeEmpty, FileExplorer chooser) {
        this(prompt, canBeEmpty, "explorer.browse", chooser, ';');
    }

    public EditorFileField(String prompt, FileExplorer chooser) {
        this(prompt, false, chooser);
    }

    public String getSettingsValue() {
        return getValueFromRaw(getRawValues());
    }

    private File[] getRawValues() {
        String[] paths = getRawSplitValue();
        if (paths == null) {
            return null;
        } else {
            int len = paths.length;
            File[] files = new File[len];

            for (int i = 0; i < paths.length; ++i) {
                files[i] = new File(paths[i]);
            }

            return files;
        }
    }

    public void setSettingsValue(String value) {
        textField.setSettingsValue(value);
    }

    private void setRawValue(File[] fileList) {
        setSettingsValue(getValueFromRaw(fileList));
    }

    private String[] getRawSplitValue() {
        return splitString(textField.textField.getValue());
    }

    private String getValueFromRaw(File[] files) {
        if (files == null) {
            return null;
        } else {
            StringBuilder builder = new StringBuilder();
            File[] var6 = files;
            int var5 = files.length;

            for (int var4 = 0; var4 < var5; ++var4) {
                File file = var6[var4];
                String path = file.getAbsolutePath();
                builder.append(delimiterChar).append(path);
            }

            return builder.substring(1);
        }
    }

    private String[] splitString(String s) {
        if (s == null) {
            return null;
        } else {
            String[] split = delimiterSplitter.split(s);
            return split.length == 0 ? null : split;
        }
    }

    private File getFirstFile() {
        File[] files = getRawValues();
        return files != null && files.length != 0 ? files[0] : TLauncher.getDirectory();
    }

    public boolean isValueValid() {
        return textField.isValueValid();
    }

    public void setBackground(Color bg) {
        if (textField != null)
            textField.setBackground(bg);
    }

    public void block(Object reason) {
        Blocker.blockComponents(reason, textField, explorerButton);
    }

    public void unblock(Object reason) {
        Blocker.unblockComponents(Blocker.UNIVERSAL_UNBLOCK, textField, explorerButton);
    }

    protected void log(Object... w) {
        U.log("[" + getClass().getSimpleName() + "]", w);
    }*/
}

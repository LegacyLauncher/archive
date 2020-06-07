package ru.turikhay.tlauncher.ui.explorer;

import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

import javax.swing.filechooser.FileFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class FilteredFileExplorer extends FileExplorer {
    private final List<String> extensionList = new ArrayList<String>();

    protected FilteredFileExplorer() {
        setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                String extension = FileUtil.getExtension(f);
                return extension == null || extensionList.contains(extension);
            }

            @Override
            public String getDescription() {
                return FilteredFileExplorer.this.getDescription();
            }
        });
        setAcceptAllFileFilterUsed(false);
    }

    protected abstract String getDescription();

    protected void addExtesion(String... ext) {
        Collections.addAll(extensionList, U.requireNotContainNull(ext, "ext"));
    }
}

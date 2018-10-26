package ru.turikhay.tlauncher.minecraft;

import net.minecraft.launcher.versions.Library;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ModList {
    private final String repositoryRoot;
    private final List<String> modRef;

    public ModList(File repositoryRoot, boolean absolutePrefix) {
        this.repositoryRoot = (absolutePrefix? "absolute:" : "") +  repositoryRoot.getAbsolutePath();
        this.modRef = new ArrayList<>();
    }

    public void addMod(Library library) {
        if(!U.requireNotNull(library).isMod()) {
            throw new IllegalArgumentException("not a mod: " + library);
        }
        modRef.add(library.getName());
    }

    public void save(File file) throws IOException {
        try(OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file), FileUtil.getCharset())) {
            U.getGson().toJson(this, writer);
        }
    }
}

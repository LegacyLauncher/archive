package ru.turikhay.tlauncher.minecraft.launcher;

import ru.turikhay.tlauncher.ui.loc.Localizable;

public class MinecraftException extends Exception {
    private final String langPath;
    private final String[] langVars;

    MinecraftException(boolean send, String message, String langPath, Throwable cause, Object... langVars) {
        super(message, cause);
        if (langPath == null) {
            throw new NullPointerException("Lang path required!");
        } else {

            if (langVars == null) {
                langVars = new Object[0];
            }

            this.langPath = langPath;
            this.langVars = Localizable.checkVariables(langVars);
        }
    }

    MinecraftException(boolean send, String message, String langPath, Throwable cause) {
        this(send, message, langPath, cause, new Object[0]);
    }

    MinecraftException(boolean send, String message, String langPath, Object... vars) {
        this(send, message, langPath, null, vars);
    }

    public String getLangPath() {
        return langPath;
    }

    public String[] getLangVars() {
        return langVars;
    }
}

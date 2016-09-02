package ru.turikhay.tlauncher.minecraft.crash;

import ru.turikhay.util.StringUtil;
import ru.turikhay.util.U;

public abstract class BindableAction {
    private final String name;
    private final String logPrefix;

    public BindableAction(String name) {
        this.name = StringUtil.requireNotBlank(name);
        logPrefix = "[BindAction][" + name + "]";
    }

    public final String getName() {
        return name;
    }

    public abstract void execute(String arg) throws Exception;

    public Binding bind(String arg) {
        return new Binding(this, arg);
    }

    class Binding implements Action {
        private final String arg;

        Binding(BindableAction action, String arg) {
            this.arg = U.requireNotNull(arg);
        }

        @Override
        public void execute() throws Exception {
            BindableAction.this.execute(arg);
        }
    }

    protected void log(Object... o) {
        U.log(logPrefix, o);
    }
}

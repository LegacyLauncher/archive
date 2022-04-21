package ru.turikhay.tlauncher.minecraft.crash;

import ru.turikhay.util.StringUtil;

import java.util.Objects;

public abstract class BindableAction {
    private final String name;

    public BindableAction(String name) {
        this.name = StringUtil.requireNotBlank(name);
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
            this.arg = Objects.requireNonNull(arg);
        }

        @Override
        public void execute() throws Exception {
            BindableAction.this.execute(arg);
        }
    }
}

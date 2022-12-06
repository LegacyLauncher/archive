package ru.turikhay.tlauncher.minecraft.launcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public interface ProcessHook {
    default void enrichProcess(ProcessBuilder process) {
    }

    default void processCreated(Process process) {
    }

    default void processDestroyed(Process process) {
    }

    default ProcessHook then(ProcessHook hook) {
        return new Merger(this, hook);
    }

    class Merger implements ProcessHook {
        private final Collection<ProcessHook> hooks;

        public Merger(Collection<ProcessHook> hooks) {
            this.hooks = hooks;
        }

        public Merger(ProcessHook... hooks) {
            this(Arrays.asList(hooks));
        }

        @Override
        public ProcessHook then(ProcessHook hook) {
            List<ProcessHook> hooks = new ArrayList<>(this.hooks.size() + 1);
            hooks.addAll(this.hooks);
            hooks.add(hook);
            return new Merger(hooks);
        }

        @Override
        public void enrichProcess(ProcessBuilder process) {
            hooks.forEach(hook -> hook.enrichProcess(process));
        }

        @Override
        public void processCreated(Process process) {
            hooks.forEach(hook -> hook.processCreated(process));
        }

        @Override
        public void processDestroyed(Process process) {
            hooks.forEach(hook -> hook.processDestroyed(process));
        }
    }

    class None implements ProcessHook {
        public static final ProcessHook INSTANCE = new None();

        private None() {
        }

        @Override
        public ProcessHook then(ProcessHook hook) {
            return hook;
        }
    }
}

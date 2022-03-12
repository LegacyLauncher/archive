package ru.turikhay.tlauncher.minecraft.crash;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.apache.commons.lang3.StringUtils;
import ru.turikhay.util.U;

public abstract class ArgsAction extends BindableAction {
    protected final OptionParser parser;

    ArgsAction(String name) {
        super(name);
        parser = new OptionParser();
    }

    ArgsAction(String name, String[] args) {
        this(name);

        if (U.requireNotNull(args).length == 0) {
            throw new IllegalArgumentException();
        }
        for (String arg : args) {
            parser.accepts(arg).withRequiredArg();
        }
    }

    @Override
    public void execute(String args) throws Exception {
        OptionSet set = parser.parse(StringUtils.split(args, ' '));
        execute(set);
    }

    abstract void execute(OptionSet args) throws Exception;
}

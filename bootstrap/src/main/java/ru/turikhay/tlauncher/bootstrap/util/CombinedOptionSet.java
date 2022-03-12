package ru.turikhay.tlauncher.bootstrap.util;

import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CombinedOptionSet {
    private final List<OptionSet> optionSetList;

    public CombinedOptionSet(OptionSet... optionSets) {
        this.optionSetList = Arrays.asList(optionSets);
    }

    public boolean has(OptionSpec<?> option) {
        return optionSetList.stream().anyMatch(o -> o.has(option));
    }

    public <V> V valueOf(OptionSpec<V> option) {
        return optionSetList.stream()
                .map(o -> o.valueOf(option))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

}

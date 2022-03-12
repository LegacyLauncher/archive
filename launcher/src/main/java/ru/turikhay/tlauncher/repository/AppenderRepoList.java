package ru.turikhay.tlauncher.repository;

import java.util.List;

public class AppenderRepoList extends RepoList {
    public AppenderRepoList(String name, String[] prefixList) {
        super(name);

        for (String prefix : prefixList) {
            add(new AppenderRepo(prefix));
        }
    }

    public AppenderRepoList(String name, List<String> prefixList) {
        super(name);

        for (String prefix : prefixList) {
            add(new AppenderRepo(prefix));
        }
    }
}

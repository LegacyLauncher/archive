package net.legacylauncher.repository;

import net.legacylauncher.util.U;
import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class AppenderRepo extends Repo {
    private final String prefix, suffix;

    public AppenderRepo(String prefix, String suffix) {
        super(prefix);

        if (StringUtils.isEmpty(prefix)) {
            throw new IllegalArgumentException("prefix is empty");
        }
        if (StringUtils.isEmpty(suffix)) {
            suffix = null;
        }

        this.prefix = prefix;
        this.suffix = suffix;
    }

    public AppenderRepo(String prefix) {
        this(prefix, null);
    }

    @Override
    protected URL makeUrl(String path) {
        String url;

        if (suffix == null) {
            url = prefix + path;
        } else {
            url = prefix + path + suffix;
        }

        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new RuntimeException(url, e);
        }
    }

    @Override
    public List<String> getHosts() {
        return Collections.singletonList(U.parseHost(prefix));
    }
}

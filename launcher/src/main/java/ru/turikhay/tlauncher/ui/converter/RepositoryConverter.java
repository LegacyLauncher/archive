package ru.turikhay.tlauncher.ui.converter;

import ru.turikhay.tlauncher.repository.Repository;
import ru.turikhay.tlauncher.ui.loc.LocalizableStringConverter;

public class RepositoryConverter extends LocalizableStringConverter<Repository> {
    public RepositoryConverter() {
        super("repo");
    }

    public Repository fromString(String from) {
        if (from == null) {
            return null;
        } else {
            Repository[] var5;
            int var4 = (var5 = Repository.values()).length;

            for (int var3 = 0; var3 < var4; ++var3) {
                Repository type = var5[var3];
                if (type.toString().equals(from)) {
                    return type;
                }
            }

            return null;
        }
    }

    public String toValue(Repository from) {
        return from.toString();
    }

    protected String toPath(Repository from) {
        return toValue(from);
    }

    public Class<Repository> getObjectClass() {
        return Repository.class;
    }
}

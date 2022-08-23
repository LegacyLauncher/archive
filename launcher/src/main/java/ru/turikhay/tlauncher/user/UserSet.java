package ru.turikhay.tlauncher.user;

import ru.turikhay.tlauncher.managed.ManagedListener;
import ru.turikhay.tlauncher.managed.ManagedSet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class UserSet extends ManagedSet<User> {
    private final Map<String, Auth<? extends User>> authMap;

    public UserSet(ManagedListener<User> listener) {
        super(listener);

        authMap = new HashMap<>();
        authMap.put(MojangUser.TYPE, new MojangAuth());
        authMap.put(PlainUser.TYPE, new PlainAuth());
    }

    public User getByUsername(String username, String type) {
        for (User user : getSet()) {
            if (user.getUsername().equals(username)) {
                if (type != null && !user.getType().equals(type)) {
                    continue;
                }
                return user;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <U extends User> void validate(U user) throws IOException, AuthException {
        String type = Objects.requireNonNull(user, "user").getType();
        Auth<U> auth = (Auth<U>) authMap.get(type);

        if (auth == null) {
            throw new IllegalArgumentException("auth not found: " + type);
        }

        auth.validate(user);
    }

    public Optional<? extends User> findByType(String type) {
        for (User user : getSet()) {
            if (user.getType().equals(type)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }
}

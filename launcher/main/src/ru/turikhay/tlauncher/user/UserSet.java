package ru.turikhay.tlauncher.user;

import ru.turikhay.tlauncher.managed.ManagedListener;
import ru.turikhay.tlauncher.managed.ManagedSet;
import ru.turikhay.util.U;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class UserSet extends ManagedSet<User> {
    private final MojangAuth mojangAuth;
    private final PlainAuth plainAuth;

    private final Map<String, Auth> authMap;

    public UserSet(ManagedListener listener) {
        super(listener);

        mojangAuth = new MojangAuth();
        plainAuth = new PlainAuth();

        authMap = new HashMap<String, Auth>() {
            {
                put(MojangUser.TYPE, mojangAuth);
                put(PlainUser.TYPE, plainAuth);
            }
        };
    }

    public User getByUsername(String username, String type) {
        for(User user : getSet()) {
            if(user.getUsername().equals(username)) {
                if(type != null && !user.getType().equals(type)) {
                    continue;
                }
                return user;
            }
        }
        return null;
    }

    public void validate(User user) throws IOException, AuthException {
        String type = U.requireNotNull(user, "user").getType();
        Auth auth = authMap.get(type);

        if(auth == null) {
            throw new IllegalArgumentException("auth not found: " + type);
        }

        auth.validate(user);
    }
}

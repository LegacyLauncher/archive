package ru.turikhay.tlauncher.user;

public interface MojangLikeUserFactory<M> {
    M createFromPayload(AuthlibUserPayload payload);
}

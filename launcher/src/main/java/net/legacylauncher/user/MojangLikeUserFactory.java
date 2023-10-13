package net.legacylauncher.user;

public interface MojangLikeUserFactory<M> {
    M createFromPayload(AuthlibUserPayload payload);
}

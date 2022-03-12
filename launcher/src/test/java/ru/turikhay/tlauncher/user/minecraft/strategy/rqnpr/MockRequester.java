package ru.turikhay.tlauncher.user.minecraft.strategy.rqnpr;

public class MockRequester {
    public static <A> Requester<A> returning(String result) {
        return new MockStringRequester<>(a -> result);
    }

    public static <A> Requester<A> throwing(InvalidResponseException e) {
        return new MockThrowingRequester.InvalidResponse<>(e);
    }
}

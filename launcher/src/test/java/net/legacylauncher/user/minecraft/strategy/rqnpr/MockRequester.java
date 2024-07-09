package net.legacylauncher.user.minecraft.strategy.rqnpr;

public class MockRequester {
    public static <A> Requester<A> returning(String result) {
        return (logger, argument) -> result;
    }

    public static <A> Requester<A> throwing(InvalidResponseException e) {
        return (logger, argument) -> {
            throw e;
        };
    }
}

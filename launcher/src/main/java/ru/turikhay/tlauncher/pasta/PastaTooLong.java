package ru.turikhay.tlauncher.pasta;

public class PastaTooLong extends PastaException {
    public PastaTooLong(long length) {
        super(String.valueOf(length));
    }
}

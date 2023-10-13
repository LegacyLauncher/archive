package net.legacylauncher.pasta;

public class PastaTooLong extends PastaException {
    public PastaTooLong(long length) {
        super(String.valueOf(length));
    }
}

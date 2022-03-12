package ru.turikhay.util.git;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

public class TokenReplacingReader extends Reader {

    protected PushbackReader pushbackReader;
    protected ITokenResolver tokenResolver;
    protected final StringBuilder tokenNameBuffer = new StringBuilder();
    protected String tokenValue = null;
    protected int tokenValueIndex = 0;

    public TokenReplacingReader(Reader source, ITokenResolver resolver) {
        pushbackReader = new PushbackReader(source, 2);
        tokenResolver = resolver;
    }

    public int read(@Nonnull CharBuffer target) throws IOException {
        int read;
        while ((read = read()) >= 0) {
            target.append((char) read);
        }
        return -1;
    }

    boolean unreadOnDollar;

    public int read() throws IOException {
        if (tokenValue != null) {
            if (tokenValueIndex < tokenValue.length()) {
                return tokenValue.charAt(tokenValueIndex++);
            }
            if (tokenValueIndex == tokenValue.length()) {
                tokenValue = null;
                tokenValueIndex = 0;
            }
        }

        int data = pushbackReader.read();
        if (unreadOnDollar) {
            if (data == 65535) {
                data = -1; // fix bug
            }
            unreadOnDollar = false;
            return data;
        }
        if (data != '$') return data;

        data = pushbackReader.read();
        if (data != '{') {
            unreadOnDollar = true;
            pushbackReader.unread(data);
            return '$';
        }
        tokenNameBuffer.delete(0, tokenNameBuffer.length());

        data = pushbackReader.read();
        while (data != '}') {
            tokenNameBuffer.append((char) data);
            data = pushbackReader.read();
        }

        tokenValue = tokenResolver
                .resolveToken(tokenNameBuffer.toString());

        if (tokenValue == null) {
            tokenValue = "${" + tokenNameBuffer + "}";
        }
        if (tokenValue.length() == 0) {
            return read();
        }
        return tokenValue.charAt(tokenValueIndex++);


    }

    public int read(char[] cbuf) throws IOException {
        return read(cbuf, 0, cbuf.length);
    }

    public int read(char[] cbuf, int off, int len) throws IOException {
        int charsRead = 0;
        for (int i = 0; i < len; i++) {
            int nextChar = read();
            if (nextChar == -1) {
                if (charsRead == 0) {
                    charsRead = -1;
                }
                break;
            }
            charsRead = i + 1;
            cbuf[off + i] = (char) nextChar;
        }
        return charsRead;
    }

    public void close() throws IOException {
        pushbackReader.close();
    }

    public long skip(long n) {
        throw new RuntimeException("Operation Not Supported");
    }

    public boolean ready() throws IOException {
        return pushbackReader.ready();
    }

    public boolean markSupported() {
        return false;
    }

    public void mark(int readAheadLimit) {
        throw new RuntimeException("Operation Not Supported");
    }

    public void reset() {
        throw new RuntimeException("Operation Not Supported");
    }

    public static String resolveVars(String str, ITokenResolver tokenResolver) {
        if (str == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        TokenReplacingReader reader = new TokenReplacingReader(new StringReader(str), tokenResolver);
        int read;

        try {
            while ((read = reader.read()) != -1) {
                builder.append((char) read);
            }
        } catch (IOException ioE) {
            throw new Error(ioE);
        }

        return builder.toString();
    }
}

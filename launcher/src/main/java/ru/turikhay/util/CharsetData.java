package ru.turikhay.util;

import java.io.IOException;
import java.io.Reader;

public interface CharsetData {
    Reader read() throws IOException;
}

package ru.turikhay.tlauncher.bootstrap.exception;

import org.junit.jupiter.api.Test;
import ru.turikhay.tlauncher.bootstrap.util.OS;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FatalExceptionTypeTest {

    @Test
    public void getTypeTest() {
        assertEquals(FatalExceptionType.getType(new UnknownHostException()), FatalExceptionType.INTERNET_CONNECTIVITY);
        assertEquals(FatalExceptionType.getType(new ClassNotFoundException()), FatalExceptionType.CORRUPTED_INSTALLATION);

        Exception e1 = new Exception();
        e1.addSuppressed(new SocketException());
        e1.addSuppressed(new FileNotFoundException());
        e1.addSuppressed(new UnknownHostException());
        assertEquals(FatalExceptionType.getType(e1), FatalExceptionType.INTERNET_CONNECTIVITY);

        if (OS.WINDOWS.isCurrent()) {
            Exception e2 = new Exception();
            e2.addSuppressed(new SocketException("Address family not supported by protocol family: connect"));

            assertEquals(FatalExceptionType.getType(e2), FatalExceptionType.INTERNET_CONNECTIVITY_BLOCKED);
        }

        Exception e3 = new Exception();
        e3.addSuppressed(new IOException());
        e3.addSuppressed(new Exception());

        assertEquals(FatalExceptionType.getType(e3), FatalExceptionType.UNKNOWN);
    }

}
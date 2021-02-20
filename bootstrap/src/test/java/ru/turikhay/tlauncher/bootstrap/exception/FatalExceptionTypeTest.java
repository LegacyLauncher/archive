package ru.turikhay.tlauncher.bootstrap.exception;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FatalExceptionTypeTest {

    @Test
    public void getTypeTest() {
        assertEquals(FatalExceptionType.getType(new UnknownHostException()), FatalExceptionType.INTERNET_CONNECTIVITY);
        assertEquals(FatalExceptionType.getType(new ClassNotFoundException()),FatalExceptionType.CORRUPTED_INSTALLATION);
        assertEquals(FatalExceptionType.getType(new ExceptionList(new ArrayList<Exception>() {
            {
                add(new SocketException());
                add(new FileNotFoundException());
                add(new UnknownHostException());
            }
        })), FatalExceptionType.INTERNET_CONNECTIVITY);
        assertEquals(FatalExceptionType.getType(new ExceptionList(new ArrayList<Exception>() {
            {
                add(new SocketException("Address family not supported by protocol family: connect"));
            }
        })), FatalExceptionType.INTERNET_CONNECTIVITY_BLOCKED);
        assertEquals(FatalExceptionType.getType(new ExceptionList(new ArrayList<Exception>() {
            {
                add(new IOException());
                add(new Exception());
            }
        })), FatalExceptionType.UNKNOWN);
    }

}
package ru.turikhay.tlauncher.bootstrap.exception;

import org.testng.annotations.Test;
import ru.turikhay.tlauncher.bootstrap.launcher.LauncherNotFoundException;

import java.io.FileNotFoundException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static org.testng.Assert.*;

public class FatalExceptionTypeTest {

    @Test
    public void getTypeTest() {
        assertTrue(FatalExceptionType.getType(new UnknownHostException()) == FatalExceptionType.INTERNET_CONNECTIVITY);
        assertTrue(FatalExceptionType.getType(new ClassNotFoundException()) == FatalExceptionType.CORRUPTED_INSTALLATION);
        assertTrue(FatalExceptionType.getType(new ExceptionList(new ArrayList<Exception>() {
            {
                add(new LauncherNotFoundException(""));
            }
        })) == FatalExceptionType.INTERNET_CONNECTIVITY);
    }

}
package ru.turikhay.util;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LazyTest {

    @Test
    void getTest() {
        Object o = new Object();
        Lazy<Object> lazy = Lazy.of(() -> o);
        assertEquals(o, lazy.get());
    }

    @Test
    void valueTest() {
        Object o = new Object();
        Lazy<Object> lazy = Lazy.of(() -> o);
        Optional<Object> optO = lazy.value();
        assertTrue(optO.isPresent());
        assertEquals(o, optO.get());
    }

    @Test
    void getNullTest() {
        Lazy<Object> lazy = Lazy.of(() -> null);
        assertNull(lazy.get());
    }

    @Test
    void valueNullTest() {
        Lazy<Object> lazy = Lazy.of(() -> null);
        Optional<Object> value = lazy.value();
        assertNotNull(value);
        assertFalse(value.isPresent());
    }

    @Test
    void getExceptionTest() {
        Lazy<Object> lazy = Lazy.of(() -> {
            throw new RuntimeException();
        });
        assertThrows(LazyInitException.class, lazy::get);
    }

    @Test
    void valueExceptionTest() {
        Lazy<Object> lazy = Lazy.of(() -> {
            throw new RuntimeException();
        });
        Optional<Object> value = lazy.value();
        assertFalse(value.isPresent());
    }

    @Test
    void getInitializeOnceTest() throws Exception {
        Callable<?> callableMock = Mockito.mock(Callable.class);
        Lazy<?> lazy = Lazy.of(callableMock);
        verifyNoInteractions(callableMock);
        lazy.get();
        verify(callableMock, only()).call();
        lazy.get();
        verifyNoMoreInteractions(callableMock);
    }

    @Test
    void valueInitializeOnceTest() throws Exception {
        Callable<?> callableMock = Mockito.mock(Callable.class);
        Lazy<?> lazy = Lazy.of(callableMock);
        verifyNoInteractions(callableMock);
        lazy.value();
        verify(callableMock, only()).call();
        lazy.value();
        verifyNoMoreInteractions(callableMock);
    }

}
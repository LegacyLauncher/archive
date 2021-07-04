package ru.turikhay.tlauncher.ui.images;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

class DuplicateAwareResourceLocatorTest {

    ImageResourceLocator mockLocator;
    DuplicateAwareResourceLocator locator;

    @BeforeEach
    void setUp() {
        mockLocator = Mockito.mock(ImageResourceLocator.class);
        locator = new DuplicateAwareResourceLocator(mockLocator);
    }

    @Test
    void dup1Test() {
        final String resourceName = "resource-1.png";
        locator.loadResource(resourceName);
        verify(mockLocator).loadResource(eq("resource.png"));
    }

    @Test
    void dup10Test() {
        final String resourceName = "resource-10.png";
        locator.loadResource(resourceName);
        verify(mockLocator).loadResource(eq("resource.png"));
    }

    @Test
    void noDupTest() {
        final String resourceName = "resource.png";
        locator.loadResource(resourceName);
        verify(mockLocator).loadResource(eq("resource.png"));
    }

}
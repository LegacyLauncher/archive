package ru.turikhay.util.logging;

import org.slf4j.spi.SLF4JServiceProvider;

public interface DelegateServiceProvider extends SLF4JServiceProvider {
    void setProvider(SLF4JServiceProvider provider);
}

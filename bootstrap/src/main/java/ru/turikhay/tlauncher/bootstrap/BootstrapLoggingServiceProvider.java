package ru.turikhay.tlauncher.bootstrap;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.Logger;
import org.slf4j.helpers.SubstituteLogger;
import org.slf4j.helpers.SubstituteLoggerFactory;
import org.slf4j.simple.SimpleServiceProvider;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;
import ru.turikhay.util.logging.DelegateServiceProvider;

import java.util.Objects;

public class BootstrapLoggingServiceProvider implements DelegateServiceProvider {
    public static BootstrapLoggingServiceProvider INSTANCE;

    private final Factory factory = new Factory();
    private volatile SLF4JServiceProvider provider;

    public BootstrapLoggingServiceProvider() {
        INSTANCE = this;
        this.provider = new SimpleServiceProvider();
    }

    @Override
    public void setProvider(SLF4JServiceProvider provider) {
        this.provider = Objects.requireNonNull(provider, "provider");
        provider.initialize();
        factory.updateLoggers();
    }

    @Override
    public Factory getLoggerFactory() {
        return factory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return provider.getMarkerFactory();
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return provider.getMDCAdapter();
    }

    @Override
    public String getRequestedApiVersion() {
        return provider.getRequestedApiVersion();
    }

    @Override
    public void initialize() {
        provider.initialize();
    }

    public class Factory implements ILoggerFactory {
        private final SubstituteLoggerFactory factory = new SubstituteLoggerFactory();

        private Factory() {
        }

        @Override
        public Logger getLogger(String name) {
            SubstituteLogger logger = (SubstituteLogger) factory.getLogger(name);
            setDelegateLoggerFor(logger);
            return logger;
        }

        public void updateLoggers() {
            factory.getLoggers().forEach(this::setDelegateLoggerFor);
        }

        private void setDelegateLoggerFor(SubstituteLogger logger) {
            Logger substituteLogger = provider.getLoggerFactory().getLogger(logger.getName());
            logger.setDelegate(substituteLogger);
        }
    }
}

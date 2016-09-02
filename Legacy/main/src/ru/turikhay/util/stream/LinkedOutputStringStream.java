package ru.turikhay.util.stream;

import ru.turikhay.tlauncher.ui.alert.Alert;

public class LinkedOutputStringStream extends BufferedOutputStringStream {
    private StreamLogger logger;

    public LinkedOutputStringStream() {
    }

    public StreamLogger getLogger() {
        return logger;
    }

    public void setLogger(StreamLogger logger) {
        this.logger = logger;
    }

    public synchronized void flush() {
        super.flush();
        if (logger != null) {
            logger.rawlog(bufferedLine);
        }
    }
}

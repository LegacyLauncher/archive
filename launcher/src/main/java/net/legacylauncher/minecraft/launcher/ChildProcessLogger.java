package net.legacylauncher.minecraft.launcher;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.logger.LogFile;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

@Slf4j
public class ChildProcessLogger implements Closeable {
    private final LogFile logFile;
    private Writer writer;

    public ChildProcessLogger(LogFile logFile) throws IOException {
        this.logFile = logFile;
        this.writer = logFile.write();
    }

    public LogFile getLogFile() {
        return logFile;
    }

    public void log(String line) {
        if (writer == null) {
            log.warn("Tried to log after closing: {}", line);
            return;
        }
        try {
            writer.write(line);
            writer.write('\n');
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("Couldn't write into logger", e);
            } else {
                log.warn("Couldn't write into logger: {}", e.toString());
            }
        }
    }

    public void close() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
    }

    public static ChildProcessLogger create(Charset charset) throws IOException {
        File tempFile = File.createTempFile("tl-logger", ".txt");
        tempFile.deleteOnExit();
        return new ChildProcessLogger(new LogFile(tempFile, charset));
    }
}

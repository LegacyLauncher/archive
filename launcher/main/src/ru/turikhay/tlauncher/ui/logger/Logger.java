package ru.turikhay.tlauncher.ui.logger;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.minecraft.launcher.MinecraftLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedComponentAdapter;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedTextArea;
import ru.turikhay.util.*;
import ru.turikhay.util.async.AsyncThread;
import ru.turikhay.util.pastebin.Paste;
import ru.turikhay.util.pastebin.PasteResult;
import ru.turikhay.util.stream.LinkedOutputStringStream;
import ru.turikhay.util.stream.BufferedOutputStringStream;
import ru.turikhay.util.stream.PrintLogger;
import ru.turikhay.util.stream.StreamLogger;

import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Logger implements StreamLogger {
    private static List<WeakReference<LoggerFrame>> frames = Collections.synchronizedList(new ArrayList());
    public final LoggerFrame frame;
    private final Configuration global;
    private String name;
    private LinkedOutputStringStream stream;
    private PrintLogger logger;
    private Logger.CloseAction close;
    private boolean killed;
    MinecraftLauncher launcher;
    private FileExplorer explorer;

    public Logger(Configuration global, PrintLogger logger, String name, boolean show) {
        this.global = global;
        this.name = name;
        frame = new LoggerFrame(this);
        frame.setTitle(name);
        frames.add(new WeakReference<LoggerFrame>(frame));
        update();
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onClose();
            }
            public void windowClosed(WindowEvent e) {
                U.log("Logger", Logger.this.name, "has been disposed.");
            }
        });
        frame.addComponentListener(new ExtendedComponentAdapter(frame) {
            public void componentShown(ComponentEvent e) {
                delayedSave();
            }
            public void componentHidden(ComponentEvent e) {
                delayedSave();
            }
            public void onComponentResized(ComponentEvent e) {
                delayedSave();
            }
            public void onComponentMoved(ComponentEvent e) {
                delayedSave();
            }
        });
        if (logger == null) {
            this.logger = null;
            stream = new LinkedOutputStringStream();
            stream.setLogger(this);
        } else {
            this.logger = logger;
            stream = logger.getStream();
        }

        if (show) {
            show();
        }

        stream.flush();
        if (logger != null) {
            logger.setMirror(this);
        }

    }

    public Logger(PrintLogger logger, String name) {
        this(null, logger, name, true);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        frame.setTitle(name);
    }

    public MinecraftLauncher getLauncher() {
        return launcher;
    }

    public void setLauncher(MinecraftLauncher launcher) {
        this.launcher = launcher;

        frame.bottom.folder.setEnabled(true);

        if (launcher != null) {
            frame.bottom.kill.setEnabled(true);
            frame.bottom.openFolder = launcher.getGameDir();
        } else {
            frame.bottom.kill.setEnabled(false);
            if (Localizable.get("logger").equals(name)) {
                frame.bottom.openFolder = MinecraftUtil.getWorkingDirectory();
            }
        }
    }

    public void log(String s) {
        if (logger != null) {
            logger.rawlog(s);
        } else {
            stream.write(s);
        }

    }

    public void log(Object... o) {
        log(U.toLog(o));
    }

    public void rawlog(String s) {
        if (StringUtil.lastChar(s) == 10) {
            frame.print(s);
        } else {
            frame.println(s);
        }

    }

    public void rawlog(Object... o) {
        rawlog(U.toLog(o));
    }

    public void rawlog(char[] c) {
        rawlog(new String(c));
    }

    public PrintLogger getLogger() {
        return logger;
    }

    public CharSequence getOutput() {
        Document d = frame.textarea.getDocument();
        if (d instanceof ExtendedTextArea.ContentDocument) {
            return ((ExtendedTextArea.ContentDocument) d).accessContent();
        }
        throw new IllegalArgumentException("document is not tweaked");
    }

    BufferedOutputStringStream getStream() {
        return stream;
    }

    void update() {
        check();
        if (global != null) {
            String prefix = "gui.logger.";
            int width = global.getInteger(prefix + "width", 670);
            int height = global.getInteger(prefix + "height", 500);
            int x = global.getInteger(prefix + "x", 0);
            int y = global.getInteger(prefix + "y", 0);
            frame.setSize(width, height);
            frame.setLocation(x, y);
        }
    }

    void save() {
        check();
        if (global != null) {
            String prefix = "gui.logger.";
            int[] size = getSize();
            int[] position = getPosition();
            global.set(prefix + "width", Integer.valueOf(size[0]), false);
            global.set(prefix + "height", Integer.valueOf(size[1]), false);
            global.set(prefix + "x", Integer.valueOf(position[0]), false);
            global.set(prefix + "y", Integer.valueOf(position[1]), false);
        }
    }

    void delayedSave() {
        if (killed) {
            return;
        }
        save();
    }

    private void check() {
        if (killed) {
            throw new IllegalStateException("Logger is already killed!");
        }
    }

    public void setShown(boolean shown) {
        if (shown) {
            show();
        } else {
            hide();
        }

    }

    public void show() {
        show(true);
    }

    public void show(boolean toFront) {
        check();
        frame.setVisible(true);
        frame.scrollDown();
        if (toFront) {
            frame.toFront();
        }

    }

    public void hide() {
        check();
        frame.setVisible(false);
    }

    public void clear() {
        check();
        stream.flush();
        frame.clear();
    }

    public void kill() {
        check();
        save();

        frame.dispose();
        frame.clear();

        killed = true;
    }

    public void killIn(long millis) {
        check();
        save();
        frame.hideIn(millis);
    }

    public void sendPaste() {
        if (!Alert.showLocQuestion("logger.pastebin.alert")) {
            return;
        }
        AsyncThread.execute(new Runnable() {
            public void run() {
                Paste paste = new Paste();
                paste.addListener(frame);
                paste.setTitle(frame.getTitle());
                paste.setContent(frame.logger.getOutput());
                PasteResult result = paste.paste();
                if (result instanceof PasteResult.PasteUploaded) {
                    PasteResult.PasteUploaded error = (PasteResult.PasteUploaded) result;
                    if (Alert.showLocQuestion("logger.pastebin.sent", error.getURL())) {
                        OS.openLink(error.getURL());
                    }
                } else if (result instanceof PasteResult.PasteFailed) {
                    Throwable error1 = ((PasteResult.PasteFailed) result).getError();
                    if (error1 instanceof RuntimeException) {
                        Alert.showLocError("logger.pastebin.invalid", error1);
                    } else if (error1 instanceof IOException) {
                        Alert.showLocError("logger.pastebin.failed", error1);
                    }
                }

            }
        });
    }

    public void saveAs() {
        if (explorer == null) {
            try {
                explorer = FileExplorer.newExplorer();
            } catch (Exception e) {
                Alert.showError(Localizable.get("explorer.unavailable.title"), Localizable.get("explorer.unvailable") + (OS.WINDOWS.isCurrent() ? "\n" + Localizable.get("explorer.unavailable.win") : ""));
                return;
            }
        }

        explorer.setSelectedFile(new File(getName() + ".log"));
        int result = explorer.showSaveDialog(frame);
        if (result == 0) {
            File file = explorer.getSelectedFile();
            if (file != null) {
                String path = file.getAbsolutePath();
                if (!path.endsWith(".log")) {
                    path = path + ".log";
                }

                file = new File(path);
                FileOutputStream output = null;

                try {
                    FileUtil.createFile(file);
                    IOUtils.copy(new CharSequenceReader(getOutput()), output = new FileOutputStream(file));
                } catch (Throwable var15) {
                    Alert.showLocError("logger.save.error", var15);
                } finally {
                    if (output != null) {
                        try {
                            output.close();
                        } catch (IOException var14) {
                            var14.printStackTrace();
                        }
                    }

                }

            }
        }
    }

    public boolean isKilled() {
        check();
        return killed;
    }

    boolean isHidden() {
        return !frame.isShowing();
    }

    Point getPositionPoint() {
        check();
        return frame.getLocation();
    }

    int[] getPosition() {
        check();
        Point p = getPositionPoint();
        return new int[]{p.x, p.y};
    }

    Dimension getDimension() {
        check();
        return frame.getSize();
    }

    int[] getSize() {
        check();
        Dimension d = getDimension();
        return new int[]{d.width, d.height};
    }

    public Logger.CloseAction getCloseAction() {
        return close;
    }

    public void setCloseAction(Logger.CloseAction action) {
        close = action;
    }

    private void onClose() {
        if (close != null) {
            switch (close) {
                case EXIT:
                    kill();
                case KILL:
                    TLauncher.kill();
                default:
            }
        }
    }

    public static void updateLocale() {
        for (WeakReference<LoggerFrame> ref : frames) {
            final LoggerFrame frame = ref.get();
            if (frame != null) {
                frame.updateLocale();
            }
        }
    }

    public static void wipeAll() {
        for (WeakReference<LoggerFrame> ref : frames) {
            final LoggerFrame frame = ref.get();
            if (frame != null) {
                frame.clear();
            }
        }
    }

    public enum CloseAction {
        KILL,
        EXIT
    }
}

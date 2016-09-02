package ru.turikhay.tlauncher.ui.crash;

import ru.turikhay.tlauncher.minecraft.crash.Crash;
import ru.turikhay.tlauncher.minecraft.crash.CrashManager;
import ru.turikhay.tlauncher.minecraft.crash.CrashManagerListener;
import ru.turikhay.tlauncher.ui.frames.BActionFrame;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.progress.ProgressBar;
import ru.turikhay.util.SwingUtil;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CrashProcessingFrame extends BActionFrame implements CrashManagerListener {
    private final CrashFrame frame;

    private CrashManager manager;

    public CrashProcessingFrame() {
        frame = new CrashFrame(this);

        setMinimumSize(SwingUtil.magnify(new Dimension(500, 150)));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (manager != null) {
                    manager.cancel();
                }
            }
        });

        setTitlePath("crash.loading.title");
        getHead().setText("crash.loading.head");

        ProgressBar progress = new ProgressBar();
        progress.setPreferredSize(new Dimension(1, SwingUtil.magnify(32)));
        progress.setIndeterminate(true);
        getBody().setCenter(progress);
        getBody().setWest(Images.getIcon("lightbulb.png"));
    }

    public CrashFrame getCrashFrame() {
        return frame;
    }

    @Override
    public void onCrashManagerProcessing(CrashManager manager) {
        this.manager = manager;
        showAtCenter();
    }

    @Override
    public void onCrashManagerComplete(CrashManager manager, Crash crash) {
        this.manager = null;
        frame.setCrash(crash);
        setVisible(false);
    }

    @Override
    public void onCrashManagerCancelled(CrashManager manager) {
        this.manager = null;
        setVisible(false);
    }

    @Override
    public void onCrashManagerFailed(CrashManager manager, Exception e) {
        this.manager = null;
        setVisible(false);
    }

    @Override
    public void updateLocale() {
        super.updateLocale();
        frame.updateLocale();
    }
}

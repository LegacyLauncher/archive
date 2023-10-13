package net.legacylauncher.ui.crash;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.configuration.Configuration;
import net.legacylauncher.minecraft.crash.Crash;
import net.legacylauncher.minecraft.crash.CrashManager;
import net.legacylauncher.minecraft.crash.CrashManagerListener;
import net.legacylauncher.ui.frames.BActionFrame;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.progress.ProgressBar;
import net.legacylauncher.util.SwingUtil;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class CrashProcessingFrame extends BActionFrame implements CrashManagerListener {
    private final CrashFrame frame;

    private CrashManager manager;

    public CrashProcessingFrame() {
        frame = new CrashFrame();

        setMinimumSize(SwingUtil.magnify(new Dimension(500, 150)));
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (manager != null) {
                    manager.cancel();
                }
            }
        });
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentHidden(ComponentEvent e) {
                if (manager != null && LegacyLauncher.getInstance().getSettings().getActionOnLaunch() ==
                        Configuration.ActionOnLaunch.EXIT) {
                    LegacyLauncher.kill();
                }
            }
        });

        setTitlePath("crash.loading.title");
        getHead().setText("crash.loading.head");

        ProgressBar progress = new ProgressBar();
        progress.setPreferredSize(new Dimension(1, SwingUtil.magnify(32)));
        progress.setIndeterminate(true);
        getBody().setCenter(progress);
        getBody().setWest(Images.getIcon32("lightbulb-o"));
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

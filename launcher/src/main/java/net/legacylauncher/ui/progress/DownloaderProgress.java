package net.legacylauncher.ui.progress;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.downloader.Downloadable;
import net.legacylauncher.downloader.Downloader;
import net.legacylauncher.downloader.DownloaderListener;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableProgressBar;

import java.awt.*;
import java.text.NumberFormat;

public class DownloaderProgress extends LocalizableProgressBar implements DownloaderListener {
    private static final long serialVersionUID = -8382205925341380876L;

    private NumberFormat percentFormat;

    private DownloaderProgress(Component parentComp, Downloader downloader) {
        super(parentComp);
        if (downloader == null) {
            throw new NullPointerException();
        } else {
            downloader.addListener(this);
            stopProgress();
        }
    }

    public DownloaderProgress(Component parentComp) {
        this(parentComp, LegacyLauncher.getInstance().getDownloader());
    }

    public void onDownloaderStart(Downloader d, int files) {
        startProgress();
        setIndeterminate(true);
        setCenterString("progressBar.init");
        setEastString("progressBar.downloading", files);
    }

    public void onDownloaderAbort(Downloader d) {
        stopProgress();
    }

    public void onDownloaderProgress(Downloader d, double dprogress, double speed) {
        if (dprogress > 0.0D) {
            double progress;

            if (d.getRemaining() == 1) {
                progress = d.getLastProgress() * 100.;
            } else {
                progress = dprogress * 100.;

                if (getValue() > progress) {
                    return;
                }
            }

            setIndeterminate(false);
            setValue((int) progress);

            setCenterString(getOrUpdatePercentFormat(false).format(dprogress));
        }
    }

    public void onDownloaderFileComplete(Downloader d, Downloadable file) {
        setIndeterminate(false);
        setEastString("progressBar.remaining", d.getRemaining());
    }

    public void onDownloaderComplete(Downloader d) {
        stopProgress();
    }

    @Override
    public void updateLocale() {
        super.updateLocale();
        getOrUpdatePercentFormat(true);
    }

    private NumberFormat getOrUpdatePercentFormat(boolean forceUpdate) {
        if (percentFormat == null || forceUpdate) {
            percentFormat = NumberFormat.getPercentInstance(Localizable.get().getLocale());
        }
        return percentFormat;
    }
}

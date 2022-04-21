package ru.turikhay.tlauncher.ui.progress;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.downloader.Downloadable;
import ru.turikhay.tlauncher.downloader.Downloader;
import ru.turikhay.tlauncher.downloader.DownloaderListener;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableProgressBar;

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
        this(parentComp, TLauncher.getInstance().getDownloader());
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

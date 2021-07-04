package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.swing.ImagePanel;

import java.awt.*;

public class SideNotifier extends ImagePanel {
    private static final String LANG_PREFIX = "notifier.";
    private SideNotifier.NotifierStatus status;

    public SideNotifier() {
        super((Image) null, 1.0F, 0.75F, false, true);
    }

    public SideNotifier.NotifierStatus getStatus() {
        return status;
    }

    public void setStatus(SideNotifier.NotifierStatus status) {
        if (status == null) {
            throw new NullPointerException();
        } else {
            this.status = status;
            setImage(status.getImage());
            if (status == SideNotifier.NotifierStatus.NONE) {
                hide();
            } else {
                show();
            }

        }
    }

    protected boolean onClick() {
        return false;
        /*boolean result = processClick();
        if (result) {
            hide();
        }

        return result;*/
    }

    /*private boolean processClick() {
        if (!super.onClick()) {
            return false;
        } else {
            switch (status) {
                case FAILED:
                    Alert.showWarning(Localizable.get("notifier.failed.title"), Localizable.get("notifier.failed"));
                    break;
                case FOUND:
                    if (update == null) {
                        throw new IllegalStateException("Update is NULL!");
                    }

                    String prefix = "notifier." + status + ".";
                    String title = prefix + "title";
                    String question = prefix + "question";
                    boolean ask = Alert.showQuestion(Localizable.get(title), Localizable.get(question, update.getVersion()), update.getDescription());
                    if (!ask) {
                        return false;
                    }

                    UpdateUIListener listener = new UpdateUIListener(update);
                    listener.push();
                case NONE:
                    break;
                default:
                    throw new IllegalStateException("Unknown status: " + status);
            }

            return true;
        }
    }

    public void onUpdaterRequesting(Updater u) {
        setFoundUpdate(null);
    }

    public void onUpdaterErrored(Updater.SearchFailed failed) {
        setStatus(SideNotifier.NotifierStatus.FAILED);
    }

    public void onUpdaterSucceeded(Updater.SearchSucceeded succeeded) {
        Update update = succeeded.getResponse().getUpdate();

        if (!update.isRequired()) {
            setFoundUpdate(update.isApplicable() ? update : null);
        }
    }

    private void setFoundUpdate(Update upd) {
        update = upd;
        setStatus(upd == null ? SideNotifier.NotifierStatus.NONE : SideNotifier.NotifierStatus.FOUND);
        if (upd != null && !TLauncher.getInstance().isMinecraftLauncherWorking() && TLauncher.getInstance().getSettings().getVersion("update.asked").equals(upd.getVersion())) {
            if (!update.isRequired()) {
                processClick();
            }

            TLauncher.getInstance().getSettings().set("update.asked", upd.getVersion());
        }
    }*/

    public enum NotifierStatus {
        FAILED("warning.png"),
        FOUND("down32.png"),
        NONE;

        private final Image image;

        NotifierStatus(String imagePath) {
            image = imagePath == null ? null : Images.getImage(imagePath);
        }

        NotifierStatus() {
            this(null);
        }

        public Image getImage() {
            return image;
        }

        public String toString() {
            return super.toString().toLowerCase(java.util.Locale.ROOT);
        }
    }
}

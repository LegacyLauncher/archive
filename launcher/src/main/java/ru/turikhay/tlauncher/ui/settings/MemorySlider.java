package ru.turikhay.tlauncher.ui.settings;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.Version;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.MemoryAllocationService;
import ru.turikhay.tlauncher.ui.block.Blocker;
import ru.turikhay.tlauncher.ui.editor.EditorField;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.LocalizableCheckbox;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableHTMLLabel;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.async.AsyncThread;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Hashtable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static ru.turikhay.tlauncher.managers.MemoryAllocationService.*;

public class MemorySlider extends ExtendedPanel implements EditorField, LocalizableComponent {
    private static final Logger LOGGER = LogManager.getLogger(MemorySlider.class);

    private final Lazy<ImageIcon> warningIcon = Images.getIcon16Lazy("warning");
    private final Lazy<ImageIcon> infoIcon = Images.getIcon16Lazy("info-circle");
    private final Lazy<ImageIcon> waitingIcon = Images.getIcon16Lazy("hourglass-start");
    private final Lazy<ImageIcon> questionIcon = Images.getIcon16Lazy("question");

    private final MemoryAllocationService service;
    private final JTextField inputField;
    private final Slider slider;
    private final LocalizableCheckbox autoCheckbox;
    private final LocalizableHTMLLabel statusLabel;

    private int value, savedManualValue;
    private boolean auto;

    public MemorySlider(MemoryAllocationService service) {
        this.service = service;

        setLayout(new GridBagLayout());

        this.inputField = new JTextField();
        inputField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (inputField.getSelectionStart() == 0 &&
                        inputField.getSelectionEnd() == inputField.getDocument().getLength()) {
                    inputField.setText("");
                }
                char c = e.getKeyChar();
                if (c != KeyEvent.CHAR_UNDEFINED) {
                    inputField.setCaretPosition(inputField.getDocument().getLength());
                    if (c < '0' || c > '9') {
                        UIManager.getLookAndFeel().provideErrorFeedback(inputField);
                        e.consume();
                    }
                }
            }
        });
        inputField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (inputField.isEditable()) {
                    inputField.selectAll();
                }
            }
        });
        this.autoCheckbox = new LocalizableCheckbox("settings.java.memory.auto");
        this.statusLabel = new LocalizableHTMLLabel();
        statusLabel.setIconTextGap(8);
        statusLabel.setLabelWidth(400);
        resetStatus();

        this.slider = new Slider();
        slider.addChangeListener(e -> {
            if (slider.getValue() == value) {
                return;
            }
            if (auto) {
                // rollback change
                slider.setValue(value);
                UIManager.getLookAndFeel().provideErrorFeedback(slider);
                autoCheckbox.requestFocusInWindow();
                makeFocusOnAutoCheckbox();
            } else {
                value = slider.getValue();
                inputField.setText(String.valueOf(value));
                updateManualStatus();
            }
        });
        slider.setOpaque(false);
        slider.setMinimum(service.getRange().getMin());
        slider.setMaximum(service.getRange().getMax());
        slider.setMajorTickSpacing(service.getMemoryInfo().getBase());
        slider.setSnapToTicks(true);
        slider.setPaintLabels(true);
        slider.setPaintTicks(true);
        slider.updateLocale();
        add(slider, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 8, 8, 12), 0, 0));

        ExtendedPanel sidePanel = new ExtendedPanel();
        sidePanel.setLayout(new GridBagLayout());
        ((GridBagLayout)sidePanel.getLayout()).columnWidths = new int[] {0, 0};
        ((GridBagLayout)sidePanel.getLayout()).rowHeights = new int[] {0, 0, 0};
        ((GridBagLayout)sidePanel.getLayout()).columnWeights = new double[] {0.0, 1.0E-4};
        ((GridBagLayout)sidePanel.getLayout()).rowWeights = new double[] {0.0, 0.0, 1.0E-4};

        ExtendedPanel inputPanel = new ExtendedPanel();
        inputPanel.setLayout(new FlowLayout());

        inputField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (auto) {
                    return;
                }
                int currentValue;
                try {
                    currentValue = Integer.parseInt(inputField.getText());
                } catch(NumberFormatException ignored) {
                    return;
                }
                if (currentValue == value) {
                    return;
                }
                value = currentValue;
                slider.setSnapToTicks(false);
                slider.setValue(value);
                updateManualStatus();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
        inputField.setColumns(4);
        inputPanel.add(inputField);

        inputPanel.add(new LocalizableLabel("settings.java.memory.unit.mib"));

        sidePanel.add(inputPanel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 5, 0), 0, 0));

        autoCheckbox.addActionListener(e -> {
            boolean active = autoCheckbox.getModel().isSelected();
            if (auto == active) {
                return;
            }
            if (active) {
                savedManualValue = value;
                setAuto();
            } else {
                if (savedManualValue == 0) {
                    savedManualValue = value;
                }
                setManual(savedManualValue);
            }
        });
        sidePanel.add(autoCheckbox, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 3, 0, 0), 0, 0));

        add(sidePanel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 8, 0), 0, 0));

        add(statusLabel, new GridBagConstraints(0, 1, 0, 1, 0.0, 0.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 8, 0, 0), 0, 0));
    }

    @Override
    public String getSettingsValue() {
        return auto ? "auto" : String.valueOf(inputField.getText());
    }

    @Override
    public void setSettingsValue(String value) {
        if ("auto".equals(value)) {
            setAuto();
            return;
        }
        int xmx;
        try {
            xmx = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            LOGGER.warn("Setting to auto mode. Bad value passed: {}", value, e);
            setAuto();
            return;
        }
        setManual(xmx);
    }

    private void setAuto() {
        requestId.incrementAndGet();
        auto = true;

        autoCheckbox.setSelected(true);
        inputField.setEditable(false);
        resetStatus();

        if (TLauncher.getInstance().isReady()) {
            updateForCurrentlySelectedVersion();
        }
    }

    private void updateManualStatus() {
        if (auto) {
            return;
        }
        int currentValue;
        try {
            currentValue = Integer.parseInt(inputField.getText());
        } catch(NumberFormatException ignored) {
            currentValue = -1;
        }
        if (currentValue < service.getRange().getMin() || currentValue > service.getRange().getMax()) {
            setStatus(warningIcon, "settings.java.memory.status.manual.invalid-input");
            return;
        }
        if (currentValue > service.getMemoryInfo().getSafeMax()) {
            setStatus(infoIcon, "settings.java.memory.status.manual.high-amount");
            return;
        }
        resetStatus();
    }

    private void setManual(int xmx) {
        requestId.incrementAndGet();
        auto = false;
        value = xmx;

        autoCheckbox.setSelected(false);
        inputField.setEditable(true);
        inputField.setText(String.valueOf(xmx));
        slider.setValue(xmx);
        resetStatus();
    }

    private final AtomicInteger requestId = new AtomicInteger();

    public void updateForCurrentlySelectedVersion() {
        if (!auto) {
            return;
        }
        int id = requestId.incrementAndGet();
        VersionSyncInfo version = TLauncher.getInstance().getFrame().mp.defaultScene.loginForm.versions.getVersion();
        if (version == null) {
            return;
        }
        AsyncThread.execute(() -> {
            Hint hint = null;
            Throwable error = null;
            Version availableVersion = version.getAvailableVersion();
//            Optional<CompleteVersion> completeVersion = availableVersion instanceof CompleteVersion ?
//                    Optional.of((CompleteVersion) availableVersion) : Optional.empty();
            String versionId = version.getAvailableVersion().getID();
            Future<Hint> hintFuture = service.queryHint(new VersionContext(
                    availableVersion,
                    null
//                    completeVersion
//                            .map(v -> new E<>(v, VersionFamily.guessFamilyOf(version)))
//                            .filter(e -> e.getValue() != null && e.getValue().isConfident())
//                            .map(e -> MinecraftLauncher.getGameDir(
//                                    new File(TLauncher.getInstance().getSettings().get("minecraft.gamedir")),
//                                    e.getValue().getFamily(),
//                                    availableVersion.getID(),
//                                    TLauncher.getInstance().getSettings().getSeparateDirs()
//                            ).toPath())
//                            .orElse(null)
            ));
            try {
                hint = hintFuture.get(500, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                boolean shouldContinue = SwingUtil.waitAndReturn(() -> {
                    if (requestId.get() != id) {
                        return false;
                    }
                    setStatus(waitingIcon, "settings.java.memory.status.querying", versionId);
                    return true;
                });
                if (!shouldContinue) {
                    hintFuture.cancel(true);
                    return;
                }
            } catch (ExecutionException e) {
                error = e;
            } catch (InterruptedException e) {
                return;
            }
            if (hint == null && error == null) {
                try {
                    hint = hintFuture.get();
                } catch (InterruptedException e) {
                    return;
                } catch (ExecutionException e) {
                    error = e;
                }
            }
            final Hint _hint = hint;
            final Throwable _error = error;
            SwingUtil.later(() -> hintCallback(id, versionId, _hint, _error));
        });
    }

    private void hintCallback(int id, String versionId, Hint hint, Throwable error) {
        if (requestId.get() != id) {
            return;
        }
        if (error != null) {
            LOGGER.error("Couldn't request memory hint", error);
            setStatus(warningIcon, "settings.java.memory.status.error");
            return;
        }
        value = hint.getActual();
        slider.setValue(hint.getActual());
        inputField.setText(String.valueOf(hint.getActual()));
        if (hint.isUnderAllocation()) {
            setStatus(warningIcon, "settings.java.memory.status.result.under-allocation", versionId, hint.getDesired());
            return;
        }
        setStatus(
                hint.isConfident() ? infoIcon : questionIcon,
                "settings.java.memory.status.result." + (hint.isConfident() ? "" : "un") + "confident",
                versionId
        );
    }

    @Override
    public boolean isValueValid() {
        if (autoCheckbox.isSelected()) {
            return true;
        }
        try {
            Integer.parseInt(inputField.getText());
        } catch(NumberFormatException ignored) {
            return false;
        }
        return true;
    }

    @Override
    public void block(Object var1) {
        Blocker.blockComponents(this, var1);
    }

    @Override
    public void unblock(Object var1) {
        Blocker.unblockComponents(this, var1);
    }

    private boolean isFocusingOnAutoCheckbox;

    private void makeFocusOnAutoCheckbox() {
        if (isFocusingOnAutoCheckbox) {
            return;
        }
        isFocusingOnAutoCheckbox = true;
        Font normalFont = autoCheckbox.getFont();
        AsyncThread.execute(() -> {
            for (int i = 0; i < 4; i++) {
                SwingUtilities.invokeLater(() -> autoCheckbox.setFont(normalFont.deriveFont(Font.BOLD)));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                SwingUtilities.invokeLater(() -> autoCheckbox.setFont(normalFont));
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            SwingUtilities.invokeLater(() -> isFocusingOnAutoCheckbox = false);
        });
    }

    private void setStatus(Lazy<ImageIcon> icon, String text, Object... vars) {
        statusLabel.setIcon(icon == null ? null : icon.get());
        if (text == null) {
            statusLabel.setText("<br/><br/>");
        } else {
            statusLabel.setText(text, vars);
        }
    }

    private void resetStatus() {
        setStatus(null, null);
    }

    @Override
    public void updateLocale() {
        slider.updateLocale();
    }

    private static class Slider extends JSlider implements LocalizableComponent {
        private Hashtable<Integer, JComponent> createLabelTable() {
            int step = getMaximum() >= 4000 ? (int) (Math.round(getMaximum() / 4. / 1024.) * 1024) : 512;
            Hashtable<Integer, JComponent> table = new Hashtable<>();
            table.put(getMinimum(), createLabel(getMinimum()));
            for (int i = step; i <= getMaximum() - step/2; i += step) {
                table.put(i, createLabel(i));
            }
            table.put(getMaximum(), createLabel(getMaximum()));
            return table;
        }

        private JLabel createLabel(int unit) {
            if (unit >= 1000 && unit % 1024 != 512) {
                return new LocalizableLabel("settings.java.memory.unit.gib.tick", String.valueOf(Math.round(unit / 1024.)));
            }
            return new LocalizableLabel("settings.java.memory.unit.mib.tick", String.valueOf(unit));
        }

        @Override
        public void updateLocale() {
            setLabelTable(createLabelTable());
        }
    }

}

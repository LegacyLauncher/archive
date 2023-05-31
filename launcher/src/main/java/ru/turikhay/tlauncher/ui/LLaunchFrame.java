package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.ui.images.FixedSizeImage;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.loc.LocalizableLabel;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;

public class LLaunchFrame extends ExtendedFrame implements LocalizableComponent {
    private final static int SIZE = SwingUtil.magnify(500);
    private final static int IMAGE_HEIGHT = SIZE / 2;
    private final static int BORDER = SwingUtil.magnify(20);
    private final static int HALF_BORDER = BORDER / 2;
    private final static int WIDTH_BORDERED = SIZE - BORDER * 2;

    private final Runnable disposeListener;
    private final EditorPane explanationLabel;

    public LLaunchFrame(Runnable disposeListener) {
        this.disposeListener = disposeListener;
        getContentPane().setLayout(new GridBagLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImages(SwingUtil.getFavicons());
        setResizable(false);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.weightx = 1.0;

        FixedSizeImage image = new FixedSizeImage(Images.loadImageByName("llaunch.jpg"));
        image.setPreferredSize(new Dimension(SIZE, IMAGE_HEIGHT));
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1.0;
        c.gridy++;
        add(image, c);

        ExtendedPanel container = new ExtendedPanel();
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 0.0;
        c.gridy++;
        add(container, c);

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBorder(BorderFactory.createEmptyBorder(
                BORDER,
                BORDER,
                BORDER,
                BORDER
        ));

        LocalizableLabel head = new LocalizableLabel("llaunch.head");
        head.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.setAlignmentY(Component.TOP_ALIGNMENT);
        head.setFont(head.getFont().deriveFont(Font.BOLD, head.getFont().getSize2D() + 3.f));
        head.setForeground(Theme.getTheme().getSemiForeground());
        container.add(head);
        container.add(Box.createRigidArea(new Dimension(1, HALF_BORDER)));

        this.explanationLabel = new EditorPane();
        this.explanationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.explanationLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        container.add(this.explanationLabel);
        container.add(Box.createRigidArea(new Dimension(1, BORDER)));

        ExtendedPanel buttonsPanel = new ExtendedPanel();
        buttonsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        buttonsPanel.setAlignmentY(Component.TOP_ALIGNMENT);
        GridBagLayout bl = new GridBagLayout();
        bl.columnWeights = new double[] {0.0, 1.0, 0.0};
        bl.rowHeights = new int[] {30};
        buttonsPanel.setLayout(bl);
        LocalizableButton learnMore = new LocalizableButton("llaunch.btn.learn-more");
        learnMore.addActionListener(e -> OS.openLink("https://llaun.ch/ex-tl-legacy"));
        buttonsPanel.add(learnMore,
                new GridBagConstraints(0, 0, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0)
        );
        LocalizableButton ok = new LocalizableButton("llaunch.btn.ok");
        ok.addActionListener(e -> dispose());
        buttonsPanel.add(ok,
                new GridBagConstraints(2, 0, 1, 1, 0, 0,
                        GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                        new Insets(0, 0, 0, 0), 0, 0)
        );
        container.add(buttonsPanel);

        getRootPane().setDefaultButton(ok);
        updateLocale();
        pack();
    }

    public LLaunchFrame() {
        this(null);
    }

    @Override
    public void updateLocale() {
        Localizable.updateContainer(getContentPane());

        setTitle(Localizable.get("llaunch.title"));

        explanationLabel.setText(Localizable.get("llaunch.body.0") +
                "<br/><br/>" +
                Localizable.get("llaunch.body.1") +
                "<br/><br/>" +
                Localizable.get("llaunch.body.2"));
        explanationLabel.setPreferredSize(new Dimension(WIDTH_BORDERED, SwingUtil.getPrefHeight(explanationLabel, WIDTH_BORDERED)));

        revalidate();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (disposeListener != null) {
            disposeListener.run();
        }
    }

    private static LLaunchFrame INSTANCE;

    public static void showInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LLaunchFrame(() -> INSTANCE = null);
        }
        INSTANCE.showAtCenter();
    }

    public static boolean isNotPostTlaunchEra() {
        return Instant.now().isBefore(Instant.parse("2023-08-01T00:00:00Z"));
    }
}

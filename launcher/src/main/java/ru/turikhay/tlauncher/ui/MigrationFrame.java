package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.FixedSizeImage;
import ru.turikhay.tlauncher.ui.images.ImageIcon;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.*;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.tlauncher.user.MojangUser;
import ru.turikhay.tlauncher.user.MojangUserMigrationStatus;
import ru.turikhay.util.Lazy;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MigrationFrame extends ExtendedFrame implements LocalizableComponent {
    private final static int SIZE = SwingUtil.magnify(500);
    private final static int IMAGE_HEIGHT = SIZE / 2;
    private final static int BORDER = SwingUtil.magnify(20);
    private final static int HALF_BORDER = BORDER / 2;
    private final static int QUARTER_BORDER = BORDER / 4;
    private final static int WIDTH_BORDERED = SIZE - BORDER * 2;

    private final EditorPane explanationLabel;
    private final LocalizableHTMLLabel accountsCount;
    private final ExtendedPanel accountsList;

    public MigrationFrame() {
        getContentPane().setLayout(new GridBagLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImages(SwingUtil.createFaviconList("migration-icon"));
        setMinimumSize(new Dimension(SIZE, SIZE));

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = -1;
        c.weightx = 1.0;

        FixedSizeImage image = new FixedSizeImage(Images.loadImageByName("migration-banner.jpg"));
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

        LocalizableLabel head = new LocalizableLabel("mojang-migration.head");
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

        LocalizableLabel yourAccountsHead = new LocalizableLabel("mojang-migration.your-accounts.title");
        yourAccountsHead.setIcon(Images.getIcon24("user-circle-o").getDisabledInstance());
        yourAccountsHead.setIconTextGap(HALF_BORDER);
        yourAccountsHead.setAlignmentX(Component.LEFT_ALIGNMENT);
        yourAccountsHead.setAlignmentY(Component.TOP_ALIGNMENT);
        yourAccountsHead.setFont(yourAccountsHead.getFont().deriveFont(Font.BOLD, yourAccountsHead.getFont().getSize2D() + 3.f));
        yourAccountsHead.setForeground(Theme.getTheme().getSemiForeground());
        container.add(yourAccountsHead);
        container.add(Box.createRigidArea(new Dimension(1, HALF_BORDER)));

        this.accountsCount = new LocalizableHTMLLabel();
        this.accountsCount.setLabelWidth(WIDTH_BORDERED);
        this.accountsCount.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.accountsCount.setAlignmentY(Component.TOP_ALIGNMENT);
        container.add(accountsCount);

        this.accountsList = new ExtendedPanel();
        this.accountsList.setLayout(new BoxLayout(accountsList, BoxLayout.Y_AXIS));
        this.accountsList.setAlignmentX(Component.LEFT_ALIGNMENT);
        this.accountsList.setAlignmentY(Component.TOP_ALIGNMENT);
        this.accountsList.add(new ExtendedLabel("..."));
        container.add(accountsList);
        container.add(Box.createRigidArea(new Dimension(1, BORDER)));

        LocalizableLabel notificationTitle = new LocalizableLabel("mojang-migration.notifications.title");
        notificationTitle.setIcon(Images.getIcon24("info-circle-2").getDisabledInstance());
        notificationTitle.setIconTextGap(HALF_BORDER);
        notificationTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        notificationTitle.setAlignmentY(Component.TOP_ALIGNMENT);
        notificationTitle.setFont(notificationTitle.getFont().deriveFont(Font.BOLD, notificationTitle.getFont().getSize2D() + 3.f));
        notificationTitle.setForeground(Theme.getTheme().getSemiForeground());
        container.add(notificationTitle);
        container.add(Box.createRigidArea(new Dimension(1, HALF_BORDER)));

        LocalizableHTMLLabel notificationLabel = new LocalizableHTMLLabel("mojang-migration.notifications.body");
        notificationLabel.setLabelWidth(WIDTH_BORDERED);
        notificationLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        notificationLabel.setAlignmentY(Component.TOP_ALIGNMENT);
        container.add(notificationLabel);

        updateLocale();
    }

    private Set<MojangUser> accounts;
    private Instant startDate, endDate;

    public void updateUsers(Set<MojangUser> users, Instant startDate, Instant endDate) {
        this.accounts = new LinkedHashSet<>(users);
        this.startDate = startDate;
        this.endDate = endDate;
        updateUserJobs();
        updateCallback();
        updateLocale();
    }

    private void updateUserJobs() {
        if (startDate == null) {
            this.accounts.forEach(u -> u.isReadyToMigrate().get().thenAccept(b -> updateCallback()));
        }
    }

    private void updateCallback() {
        SwingUtil.later(() -> {
            updateAccountsCount();
            accountsList.removeAll();
            if (!this.accounts.isEmpty()) {
                JSeparator s = new JSeparator();
                s.setAlignmentX(Component.LEFT_ALIGNMENT);
                s.setAlignmentY(Component.TOP_ALIGNMENT);
                accountsList.add(Box.createRigidArea(new Dimension(1, HALF_BORDER)));
                accountsList.add(s);
            }
            this.accounts.stream().sorted((ac1, ac0) -> {
                if (startDate != null) {
                    return 0;
                }
                int s = ac0.getMigrationStatusNow().compareTo(ac1.getMigrationStatusNow());
                if (s == 0) {
                    return ac0.getDisplayName().compareTo(ac1.getDisplayName());
                } else {
                    return s;
                }
            }).forEach(u -> {
                BorderPanel p = new BorderPanel();
                p.setBorder(BorderFactory.createEmptyBorder(
                        HALF_BORDER,
                        0,
                        HALF_BORDER,
                        0
                ));
                p.setAlignmentX(Component.LEFT_ALIGNMENT);
                p.setAlignmentY(Component.TOP_ALIGNMENT);

                ExtendedLabel l = new ExtendedLabel(u.getDisplayName());
                l.setIconTextGap(HALF_BORDER);
                ImageIcon.setup(l, Images.getIcon16("logo-mojang"));
                p.setWest(l);

                MojangUserMigrationStatus status = null;
                boolean canMigrateNow = false;
                boolean forcedMigrationStarted = startDate != null && Instant.now().isAfter(startDate);
                if (forcedMigrationStarted) {
                    canMigrateNow = true;
                } else {
                    if (startDate != null) {
                        status = new MojangUserMigrationStatus(true);
                    } else {
                        CompletableFuture<MojangUserMigrationStatus> f = u.isReadyToMigrate().get();
                        if (f.isDone()) {
                            try {
                                status = f.get();
                            } catch (ExecutionException | InterruptedException ignored) {
                            }
                        } else {
                            p.setEast(new LocalizableLabel("mojang-migration.your-accounts.list.waiting"));
                        }
                    }
                }
                if (status != null) {
                    if (status.getError() == null) {
                        if (status.canMigrate()) {
                            canMigrateNow = true;
                        } else {
                            LocalizableHTMLLabel nl = new LocalizableHTMLLabel("mojang-migration.your-accounts.list.not-eligible");
                            nl.setToolTipText(Localizable.get("mojang-migration.your-accounts.list.not-eligible.hint"));
                            p.setEast(nl);
                        }
                    } else {
                        ExtendedPanel bp = new ExtendedPanel();
                        bp.setAlignmentX(Component.RIGHT_ALIGNMENT);
                        bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
                        bp.add(new LocalizableHTMLLabel("mojang-migration.your-accounts.list.probably-migrated"));
                        bp.add(Box.createRigidArea(new Dimension(QUARTER_BORDER, 0)));
                        LocalizableButton yes = new LocalizableButton("mojang-migration.your-accounts.list.probably-migrated.yes");
                        yes.addActionListener(e -> {
                            if (Alert.showLocQuestion("mojang-migration.your-accounts.list.probably-migrated.yes.remove")) {
                                TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(u);
                            }
                        });
                        bp.add(yes);
                        bp.add(Box.createRigidArea(new Dimension(QUARTER_BORDER, 0)));
                        LocalizableButton no = new LocalizableButton("mojang-migration.your-accounts.list.probably-migrated.no");
                        no.addActionListener(e -> {
                            if (Alert.showLocQuestion("mojang-migration.your-accounts.list.probably-migrated.no.open-manager")) {
                                TLauncher.getInstance().getFrame().mp.openAccountEditor();
                                TLauncher.getInstance().getFrame().mp.accountManager.get().multipane.showTip("add-account-mojang");
                                MigrationFrame.this.dispose();
                            }
                        });
                        bp.add(no);
                        p.setEast(bp);
                    }
                }
                if (canMigrateNow) {
                    l.setFont(l.getFont().deriveFont(Font.BOLD));
                    ExtendedPanel bp = new ExtendedPanel();
                    bp.setAlignmentX(Component.RIGHT_ALIGNMENT);
                    bp.setLayout(new BoxLayout(bp, BoxLayout.X_AXIS));
                    LocalizableButton eb = new LocalizableButton("mojang-migration.your-accounts.list.migrate.show-email");
                    eb.addActionListener(e -> Alert.showMessage("", "Email:", u.getUsername()));
                    bp.add(eb);
                    bp.add(Box.createRigidArea(new Dimension(QUARTER_BORDER, 0)));
                    LocalizableButton mb = new LocalizableButton("mojang-migration.your-accounts.list.migrate");
                    mb.addActionListener(e -> OS.openLink("https://aka.ms/MinecraftMigration"));
                    mb.setFont(mb.getFont().deriveFont(Font.BOLD));
                    bp.add(mb);
                    if (forcedMigrationStarted) {
                        LocalizableButton db = new LocalizableButton("mojang-migration.your-accounts.list.remove.button");
                        db.addActionListener(e -> {
                            if (Alert.showLocQuestion("mojang-migration.your-accounts.list.remove")) {
                                TLauncher.getInstance().getProfileManager().getAccountManager().getUserSet().remove(u);
                            }
                        });
                        bp.add(Box.createRigidArea(new Dimension(QUARTER_BORDER, 0)));
                        bp.add(db);
                    }
                    p.setEast(bp);
                }
                accountsList.add(p);
                JSeparator s = new JSeparator();
                s.setAlignmentX(Component.LEFT_ALIGNMENT);
                s.setAlignmentY(Component.TOP_ALIGNMENT);
                accountsList.add(s);
            });
            if (!this.accounts.isEmpty()) {
                accountsList.add(Box.createRigidArea(new Dimension(0, HALF_BORDER)));
                LocalizableButton helpButton = new LocalizableButton("mojang-migration.help.button");
                helpButton.addActionListener(e -> OS.openLink(TLauncher.getInstance().getSettings().isLikelyRussianSpeakingLocale() ?
                        "https://llaun.ch/movehelpru" : "https://llaun.ch/movehelp")
                );
                LocalizableButton nadoButton = new LocalizableButton("mojang-migration.nado.button");
                nadoButton.addActionListener(e -> Alert.showLocMessage("mojang-migration.nado"));
                BorderPanel bp = new BorderPanel();
                bp.setAlignmentX(Component.LEFT_ALIGNMENT);
                bp.setAlignmentY(Component.TOP_ALIGNMENT);
                bp.setWest(nadoButton);
                bp.setEast(helpButton);
                accountsList.add(bp);
            }
            accountsList.revalidate();
        });
    }

    private void updateAccountsCount() {
        if (this.accounts == null) {
            accountsCount.setText(null);
        } else if (this.accounts.isEmpty()) {
            accountsCount.setText("mojang-migration.your-accounts.count.none");
        } else {
            accountsCount.setText("mojang-migration.your-accounts.count.some", this.accounts.size());
        }
    }

    private final Lazy<DateTimeFormatter> dateFormatter = Lazy.of(() ->
            DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
                    .withLocale(TLauncher.getInstance().getSettings().getLocale())
                    .withZone(ZoneId.systemDefault())
    );

    @Override
    public void updateLocale() {
        Localizable.updateContainer(getContentPane());

        setTitle(Localizable.get("mojang-migration.title"));

        final String explanationLink = TLauncher.getInstance().getSettings().isLikelyRussianSpeakingLocale() ?
                "https://llaun.ch/movefaqru" : "https://llaun.ch/movefaq";
        StringBuilder explanation = new StringBuilder();
        explanation.append(Localizable.get("mojang-migration.body.explanation.text"));
        if (startDate != null) {
            explanation.append("<br/><br/>").append(Localizable.get(
                    "mojang-migration.body.explanation.with-start-date.v1.past",
                    dateFormatter.get().format(startDate)
            ));
        }
        if (endDate != null) {
            explanation.append("<br/><br/>").append(Localizable.get(
                    "mojang-migration.body.explanation.with-end-date.v1." +
                            (Instant.now().isBefore(endDate) ? "future" : "past"),
                    dateFormatter.get().format(endDate)
            ));
        }
        explanation.append(" <a href=\"").append(explanationLink).append("\">");
        explanation.append(Localizable.get("mojang-migration.body.explanation.link"));
        explanation.append("</a>");
        explanationLabel.setText(explanation.toString());
        explanationLabel.setPreferredSize(new Dimension(WIDTH_BORDERED, SwingUtil.getPrefHeight(explanationLabel, WIDTH_BORDERED)));
        updateAccountsCount();
    }
}

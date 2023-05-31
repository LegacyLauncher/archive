package ru.turikhay.tlauncher.ui;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.managers.AuthServerChecker;
import ru.turikhay.tlauncher.managers.ConnectivityManager;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.*;
import ru.turikhay.tlauncher.ui.swing.ScrollPane;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedFrame;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedLabel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.util.SwingUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ConnectivityWarning extends ExtendedFrame implements LocalizableComponent {
    private final static int WIDTH = SwingUtil.magnify(500);
    private static final int BORDER = SwingUtil.magnify(20);
    private final static int WIDTH_BORDERED = WIDTH - 2 * BORDER;
    private static final int HALF_BORDER = BORDER / 2;

    private final EditorPane body;
    private final ExtendedPanel entriesPanel;

    private boolean tlaunchNotAvailable, noConnection;

    public ConnectivityWarning() {
        setIconImages(SwingUtil.createFaviconList("warning"));
        setMaximumSize(new Dimension(WIDTH, Integer.MAX_VALUE));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        //setResizable(false);

        ExtendedPanel p = new ExtendedPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(
                BORDER,
                BORDER,
                BORDER,
                BORDER
        ));
        setContentPane(p);

        LocalizableLabel title = new LocalizableLabel("connectivity.warning.title");
        title.setFont(title.getFont().deriveFont(Font.BOLD, title.getFont().getSize2D() + 3.f));
        title.setIconTextGap(HALF_BORDER);
        title.setIcon(Images.getIcon24("plug-1"));
        add(title);
        add(Box.createRigidArea(new Dimension(1, BORDER)));

        body = new EditorPane();
        body.setContentType("text/html");
        body.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.setAlignmentY(Component.TOP_ALIGNMENT);
        add(body);
        add(Box.createRigidArea(new Dimension(1, BORDER)));

        entriesPanel = new ExtendedPanel();
        entriesPanel.setLayout(new GridBagLayout());
        entriesPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        entriesPanel.setAlignmentY(Component.TOP_ALIGNMENT);

        ScrollPane scrollPane = new ScrollPane(entriesPanel);
        scrollPane.getViewport().setAlignmentY(Component.TOP_ALIGNMENT);
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);
        scrollPane.setHBPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVBPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        add(scrollPane);

        updateLocale();
    }

    public void updateEntries(List<ConnectivityManager.Entry> entries) {
        entriesPanel.removeAll();

        List<String> unavailableHosts = entries.stream()
                .filter(ConnectivityManager.Entry::isQueued)
                .filter(e -> !e.isReachable())
                .flatMap(e -> e.getHosts().stream())
                .sorted()
                .collect(Collectors.toList());

        tlaunchNotAvailable = entries.stream().anyMatch(e -> e.getName().equals("llaun.ch") && !e.isReachable());
        noConnection = entries.stream().allMatch(e -> e.isDone() && !e.isReachable());
        List<ConnectivityManager.Entry> unreachableEntries = entries.stream()
                .filter(ConnectivityManager.Entry::isQueued)
                .filter(e -> !e.isReachable())
                .sorted(
                        Comparator.comparing(ConnectivityManager.Entry::isDone, Boolean::compareTo)
                                .reversed()
                                .thenComparing(
                                        Comparator.comparing(ConnectivityManager.Entry::getPriority).reversed()
                                )
                                .thenComparing(ConnectivityManager.Entry::getName)
                )
                .collect(Collectors.toList());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        if (!noConnection && !unreachableEntries.isEmpty()) {
            c.gridy++;
            entriesPanel.add(new JSeparator(), c);
            c.gridy++;
            entriesPanel.add(Box.createRigidArea(new Dimension(1, HALF_BORDER)), c);
            boolean officialRepoUnavailable = unreachableEntries.stream()
                    .filter(ConnectivityManager.Entry::isDone)
                    .anyMatch(e -> e.getName().equals("official_repo"));
            boolean officialRepoMirrorUnavailable = unreachableEntries.stream()
                    .filter(ConnectivityManager.Entry::isDone)
                    .anyMatch(e -> e.getName().equals("official_repo_proxy"));
            for (ConnectivityManager.Entry entry : unreachableEntries) {
                if (entry.isReachable()) {
                    continue;
                }
                BorderPanel panel = new BorderPanel();
                panel.setVgap(SwingUtil.magnify(HALF_BORDER));
                panel.setAlignmentX(Component.LEFT_ALIGNMENT);
                panel.setAlignmentY(Component.TOP_ALIGNMENT);
                ExtendedLabel name;
                if (entry.getName().startsWith("repo_")) {
                    name = new LocalizableLabel(
                            "connectivity.warning.list.name.repo",
                            entry.getName().substring("repo_".length())
                    );
                } else {
                    String path = "connectivity.warning.list.name." + entry.getName();
                    if (Localizable.nget(path) != null) {
                        name = new LocalizableLabel(path);
                    } else {
                        name = new LocalizableLabel("connectivity.warning.list.name.web", entry.getName());
                    }
                }
                if (entry.isDone()) {
                    name.setFont(name.getFont().deriveFont(Font.BOLD));

                    String path;
                    Object[] vars = new Object[0];
                    if (entry.getName().equals("official_repo")) {
                        path = "connectivity.warning.list.hint.official_repo." +
                                (officialRepoMirrorUnavailable ? "not_ok" : "ok");
                    } else if (entry.getName().equals("official_repo_proxy")) {
                        path = "connectivity.warning.list.hint.official_repo_proxy." +
                                (officialRepoUnavailable ? "not_ok" : "ok");
                    } else {
                        path = "connectivity.warning.list.hint." + entry.getName();
                        if (entry.getChecker() instanceof AuthServerChecker &&
                                ((AuthServerChecker) entry.getChecker()).getDetectedThirdPartyAuthenticator() != null) {
                            path += ".third_party";
                            String thirdPartyAuthenticatorName =
                                    ((AuthServerChecker) entry.getChecker()).getDetectedThirdPartyAuthenticator().getName();
                            if (thirdPartyAuthenticatorName == null) {
                                path += ".unknown";
                            } else {
                                vars = new Object[]{thirdPartyAuthenticatorName};
                            }
                        }
                    }
                    if (Localizable.nget(path) != null) {
                        LocalizableHTMLLabel hint = new LocalizableHTMLLabel(path, vars);
                        hint.setLabelWidth(WIDTH_BORDERED);
                        panel.setSouth(hint);
                    }
                }
                panel.setWest(name);
                LocalizableLabel status = new LocalizableLabel(
                        "connectivity.warning.list.status." + (entry.isDone() ? "unreachable" : "waiting"));
                if (entry.isDone()) {
                    status.setIcon(Images.getIcon16("warning"));
                }
                panel.setEast(status);
                c.gridy++;
                entriesPanel.add(panel, c);
                c.gridy++;
                entriesPanel.add(Box.createRigidArea(new Dimension(1, HALF_BORDER)), c);
                JSeparator s = new JSeparator();
                s.setAlignmentX(Component.LEFT_ALIGNMENT);
                s.setAlignmentY(Component.TOP_ALIGNMENT);
                c.gridy++;
                entriesPanel.add(s, c);
                c.gridy++;
                entriesPanel.add(Box.createRigidArea(new Dimension(1, HALF_BORDER)), c);
            }
            // make the contents stick to the top
            c.weighty = 1.0;
            c.gridy++;
            if (unavailableHosts.isEmpty()) {
                entriesPanel.add(Box.createRigidArea(new Dimension(1, 1)), c);
            } else {
                entriesPanel.add(Box.createRigidArea(new Dimension(1, BORDER)), c);
                c.weighty = 0.0;
                c.gridy++;

                BorderPanel hostsPanel = new BorderPanel();
                LocalizableButton hostsButton = new LocalizableButton("connectivity.warning.hosts.button");
                hostsButton.addActionListener(e -> Alert.showMessage("", "", String.join("\n", unavailableHosts)));
                hostsPanel.setEast(hostsButton);
                entriesPanel.add(hostsPanel, c);
            }
        }
        updateLocale();
        SwingUtil.later(() -> {
            revalidate();
            repaint();
        });
    }

    @Override
    public void updateLocale() {
        setTitle(Localizable.get("connectivity.warning.title"));

        final ConnectivityType type = noConnection ? ConnectivityType.NONE : ConnectivityType.SOME;
        final String bodySuffix = noConnection ? "empty" : "text";

        body.setText(String.format(Locale.ROOT, "%s <a href=\"%s\">%s</a>",
                Localizable.get("connectivity.warning.body." + bodySuffix),
                generateConnectivityLink(type),
                Localizable.get("connectivity.warning.body.link")
        ));
        body.setPreferredSize(new Dimension(WIDTH_BORDERED, SwingUtil.getPrefHeight(body, WIDTH_BORDERED)));
        body.setMaximumSize(new Dimension(WIDTH_BORDERED, SwingUtil.getPrefHeight(body, WIDTH_BORDERED)));
    }

    private enum ConnectivityType {SOME, NONE}

    private String generateConnectivityLink(ConnectivityType type) {
        final String wikiLangPrefix = TLauncher.getInstance().getSettings().isLikelyRussianSpeakingLocale() ? "" : "en:";
        final String linkPrefix = tlaunchNotAvailable ? "https://web.archive.org/web/" : "";
        return String.format(Locale.ROOT,
                "%shttps://wiki.llaun.ch/%sconnectivity:%s",
                linkPrefix,
                wikiLangPrefix,
                type.name().toLowerCase(Locale.ROOT)
        );
    }
}

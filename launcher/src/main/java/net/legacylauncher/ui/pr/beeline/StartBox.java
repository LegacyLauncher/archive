/*
 * Created by JFormDesigner on Thu Jul 03 16:47:06 MSK 2025
 */

package net.legacylauncher.ui.pr.beeline;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.jre.JavaRuntimeLocalDiscoverer;
import net.legacylauncher.stats.Stats;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.swing.editor.*;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.async.AsyncThread;

import java.awt.*;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import javax.swing.*;
import javax.swing.border.*;

@Slf4j
class StartBox extends JPanel {
    static final String LINK = "https://lln4.site/7490lv4i4vtryij5?erid=2Vtzqutuqw5";

    private final FraudHuntersTask task;
    private boolean startRequested, compatible, linkClicked, initialized;
    private Instant userWaitingSince;

    public StartBox(JavaRuntimeLocalDiscoverer discoverer) {
        this.task = new FraudHuntersTask(discoverer);
        initComponents();
    }

    public void init() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        boolean compatible = task.isLauncherCompatible();
        Stats.fraudHuntersLandingOpened(compatible);
        if (!compatible) {
            log.info("Current platform is not compatible with FraudHunters launcher");
            setCard("manual");
            return;
        }
        this.compatible = true;
        task.prepareLauncher((ok) -> SwingUtil.later(() -> {
            if (startRequested || (!ok && linkClicked)) {
                doStart(ok);
            } else if (!ok) {
                handleError();
            }
        }));
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        logo = new JLabel();
        panel1 = new JPanel();
        textPane1 = new EditorPane();
        panel2 = new JPanel();
        button = new JPanel();
        start = new JButton();
        progress = new JPanel();
        progressLabel = new JLabel();
        progressBar = new JProgressBar();
        ok = new JPanel();
        progressLabel2 = new JLabel();
        manual = new JButton();
        panel3 = new JPanel();
        textPane2 = new EditorPane();

        //======== this ========
        setBackground(Color.white);
        setForeground(new Color(0x010101));
        setBorder(new EmptyBorder(16, 16, 16, 16));
        setLayout(new GridBagLayout());

        //---- logo ----
        logo.setIcon(new ImageIcon(getClass().getResource("/net/legacylauncher/ui/images/beeline-logo.png")));
        logo.setToolTipText("\u0411\u0438\u043b\u0430\u0439\u043d");
        add(logo, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 12, 32), 0, 0));

        //======== panel1 ========
        {
            panel1.setLayout(new GridBagLayout());

            //---- textPane1 ----
            textPane1.setText("<html><head><style>html, body { background: #fff; } a { color: inherit; }</style></head><body>\u0422\u0435\u0431\u044f \u0436\u0434\u0451\u0442 \u0441\u0430\u043c\u0430\u044f \u043c\u0430\u0441\u0448\u0442\u0430\u0431\u043d\u0430\u044f \u0438\u0433\u0440\u0430 \u0431\u0438\u043b\u0430\u0439\u043d\u0430 \u0441 \u0438\u043d\u0442\u0435\u0433\u0440\u0430\u0446\u0438\u0435\u0439 \u0438\u0441\u043a\u0443\u0441\u0441\u0442\u0432\u0435\u043d\u043d\u043e\u0433\u043e \u0438\u043d\u0442\u0435\u043b\u043b\u0435\u043a\u0442\u0430. \u041b\u043e\u0432\u0438 \u043c\u043e\u0448\u0435\u043d\u043d\u0438\u043a\u043e\u0432 \u0438 \u043f\u043e\u043b\u0443\u0447\u0430\u0439 \u044d\u043a\u0441\u043a\u043b\u044e\u0437\u0438\u0432\u043d\u044b\u0435 \u043f\u0440\u0438\u0437\u044b!<br/>\u041f\u043e\u0434\u0440\u043e\u0431\u043d\u0435\u0435 \u043d\u0430 <b><a href=\"https://xn--80aeikackepfddaeahf2dzczd.xn--p1ai/\">\u043e\u0445\u043e\u0442\u043d\u0438\u043a\u0438\u043d\u0430\u043c\u043e\u0448\u0435\u043d\u043d\u0438\u043a\u043e\u0432.\u0440\u0444 \u00bb</a></b></body></html>");
            textPane1.setEditable(false);
            textPane1.setBorder(null);
            textPane1.setMargin(new Insets(0, 0, 0, 0));
            textPane1.setBackground(Color.white);
            textPane1.setFocusable(false);
            textPane1.setForeground(new Color(0x010101));
            panel1.add(textPane1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel1, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 12, 32), 0, 0));

        //======== panel2 ========
        {
            panel2.setPreferredSize(null);
            panel2.setMinimumSize(null);
            panel2.setMaximumSize(null);
            panel2.setLayout(new BorderLayout());

            //======== button ========
            {
                button.setMinimumSize(null);
                button.setMaximumSize(null);
                button.setLayout(new CardLayout());

                //---- start ----
                start.setText("\u0418\u0433\u0440\u0430\u0442\u044c");
                start.setPreferredSize(new Dimension(130, 34));
                start.setMaximumSize(null);
                start.setMinimumSize(null);
                button.add(start, "play");

                //======== progress ========
                {
                    progress.setPreferredSize(null);
                    progress.setMinimumSize(null);
                    progress.setMaximumSize(null);
                    progress.setLayout(new BorderLayout(3, 3));

                    //---- progressLabel ----
                    progressLabel.setText("\u0423\u0441\u0442\u0430\u043d\u0430\u0432\u043b\u0438\u0432\u0430\u0435\u043c...");
                    progressLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    progressLabel.setMaximumSize(null);
                    progressLabel.setMinimumSize(null);
                    progressLabel.setPreferredSize(null);
                    progress.add(progressLabel, BorderLayout.CENTER);

                    //---- progressBar ----
                    progressBar.setIndeterminate(true);
                    progressBar.setPreferredSize(null);
                    progressBar.setMaximumSize(null);
                    progressBar.setMinimumSize(null);
                    progress.add(progressBar, BorderLayout.SOUTH);
                }
                button.add(progress, "progress");

                //======== ok ========
                {
                    ok.setPreferredSize(null);
                    ok.setMinimumSize(null);
                    ok.setMaximumSize(null);
                    ok.setLayout(new BorderLayout(3, 3));

                    //---- progressLabel2 ----
                    progressLabel2.setText("\u0417\u0430\u043f\u0443\u0441\u0442\u0438\u043b\u0438!");
                    progressLabel2.setHorizontalAlignment(SwingConstants.CENTER);
                    progressLabel2.setMaximumSize(null);
                    progressLabel2.setMinimumSize(null);
                    progressLabel2.setPreferredSize(null);
                    ok.add(progressLabel2, BorderLayout.CENTER);
                }
                button.add(ok, "ok");

                //---- manual ----
                manual.setText("\u041e\u0442\u043a\u0440\u044b\u0442\u044c \u0441\u0430\u0439\u0442");
                manual.setForeground(new Color(0x010101));
                manual.setPreferredSize(new Dimension(130, 34));
                manual.setMaximumSize(null);
                manual.setMinimumSize(null);
                button.add(manual, "manual");
            }
            panel2.add(button, BorderLayout.CENTER);
        }
        add(panel2, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 12, 0), 0, 0));

        //======== panel3 ========
        {
            panel3.setLayout(new GridBagLayout());

            //---- textPane2 ----
            textPane2.setText("<html><head><style>html, body { background: #fff;  font-size: 9pt; } a { color: inherit; }</style></head><body>\u0420\u0435\u043a\u043b\u0430\u043c\u0430. \u0420\u0435\u043a\u043b\u0430\u043c\u043e\u0434\u0430\u0442\u0435\u043b\u044c \u041f\u0410\u041e \u00ab\u0412\u044b\u043c\u043f\u0435\u043b\u041a\u043e\u043c\u00bb, \u041e\u0413\u0420\u041d 1027700166636, \u041c\u043e\u0441\u043a\u0432\u0430, \u0443\u043b. 8 \u041c\u0430\u0440\u0442\u0430, 10, \u0441\u0442\u0440.14,127083, beeline.ru. \u0421\u043a\u0430\u043c\u0441\u0438\u0442\u0438 \u2013 \u0438\u0433\u0440\u0430 \u0431\u0438\u043b\u0430\u0439\u043d\u0430 \u0434\u043b\u044f \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u043d\u044b\u0445 \u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u0435\u043b\u0435\u0439, 18+. \u0421\u0440\u043e\u043a \u043f\u0440\u043e\u0432\u0435\u0434\u0435\u043d\u0438\u044f \u0430\u043a\u0446\u0438\u0438 13.05.25 - 20.09.25. \u041a\u043e\u043b-\u0432\u043e \u043f\u0440\u0438\u0437\u043e\u0432 \u043e\u0433\u0440\u0430\u043d\u0438\u0447\u0435\u043d\u043e. \u0422\u0435\u0440\u0440\u0438\u0442\u043e\u0440\u0438\u044f \u0434\u0435\u0439\u0441\u0442\u0432\u0438\u044f, \u043e\u0440\u0433\u0430\u043d\u0438\u0437\u0430\u0442\u043e\u0440, \u0441\u0440\u043e\u043a \u0438 \u043f\u043e\u0440\u044f\u0434\u043e\u043a \u043f\u043e\u043b\u0443\u0447\u0435\u043d\u0438\u044f \u043f\u0440\u0438\u0437\u043e\u0432 \u0438 \u043f\u043e\u0434\u0440\u043e\u0431\u043d\u0435\u0435: <a href=\"https://xn--80aeikackepfddaeahf2dzczd.xn--p1ai/\">\u043e\u0445\u043e\u0442\u043d\u0438\u043a\u0438\u043d\u0430\u043c\u043e\u0448\u0435\u043d\u043d\u0438\u043a\u043e\u0432.\u0440\u0444</a>. Erid: <a href=\"https://erid.ads.llaun.ch/#erid=2Vtzqutuqw5\">2Vtzqutuqw5</a></body></html>");
            textPane2.setEditable(false);
            textPane2.setBorder(null);
            textPane2.setMargin(new Insets(0, 0, 0, 0));
            textPane2.setBackground(Color.white);
            textPane2.setFocusable(false);
            textPane2.setForeground(new Color(0x010101));
            panel3.add(textPane2, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
                GridBagConstraints.CENTER, GridBagConstraints.BOTH,
                new Insets(0, 0, 0, 0), 0, 0));
        }
        add(panel3, new GridBagConstraints(0, 1, 3, 1, 1.0, 0.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH,
            new Insets(0, 0, 0, 0), 0, 0));
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on

        ((ExtendedHTMLEditorKit) textPane1.getEditorKit()).setHyperlinkProcessor(new HyperlinkProcessor() {
            @Override
            public JPopupMenu process(String link) {
                if (compatible && !startRequested && link != null) {
                    Alert.showMessage("Мы сами!", "Сейчас мы откроем сайт охотникинамошенников.рф\n\nВам не нужно ничего скачивать с сайта. Мы сами всё уже (почти) установили. Просто вернитесь в лаунчер, когда решите поиграть!");
                    linkClicked = true;
                    link = LINK;
                }
                return HyperlinkProcessor.defaultProcessor.process(link);
            }
        });
        start.addActionListener((__) -> handleStart());
        manual.addActionListener((__) -> OS.openLink(LINK));
        setCard("play");
    }

    private void handleStart() {
        setCard("progress");
        init();
        Future<Void> f = task.getFuture();
        if (f.isDone()) {
            try {
                f.get();
            } catch (InterruptedException | ExecutionException e) {
                doStart(false);
                return;
            }
            doStart(true);
        } else {
            startRequested = true;
            this.userWaitingSince = Instant.now();
        }
    }

    private void doStart(boolean ok) {
        if (ok) {
            AsyncThread.execute(() -> {
                try {
                    task.startLauncher();
                } catch (Exception e) {
                    log.error("Failed to start launcher", e);
                    SwingUtil.later(this::handleAndShowError);
                    return;
                }
                Stats.fraudHuntersLauncherStarted(this.userWaitingSince);
                SwingUtil.later(() -> {
                    setCard("ok");
                    AsyncThread.afterSeconds(5, () -> {
                        SwingUtil.later(() -> setCard("play"));
                    });
                });
            });
        } else {
            handleAndShowError();
        }
    }

    private void setCard(String name) {
        ((CardLayout) button.getLayout()).show(button, name);
    }

    private void handleAndShowError() {
        handleError();
        Alert.showError("Что-то пошло не так", "Нам очень жаль, но мы не смогли запустить лаунчер, который привёл бы вас в мир Охотников на мошенников.<br/><br/>Попробуйте загрузить лаунчер самостоятельно с <b><a href=\"https://xn--80aeikackepfddaeahf2dzczd.xn--p1ai/\">охотникинамошенников.рф</a></b>.");
    }

    private void handleError() {
        setCard("manual");
        Stats.fraudHuntersLauncherFailed(this.userWaitingSince);
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JLabel logo;
    private JPanel panel1;
    private EditorPane textPane1;
    private JPanel panel2;
    private JPanel button;
    private JButton start;
    private JPanel progress;
    private JLabel progressLabel;
    private JProgressBar progressBar;
    private JPanel ok;
    private JLabel progressLabel2;
    private JButton manual;
    private JPanel panel3;
    private EditorPane textPane2;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}

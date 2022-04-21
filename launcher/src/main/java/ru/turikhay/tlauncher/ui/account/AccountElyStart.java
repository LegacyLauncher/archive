package ru.turikhay.tlauncher.ui.account;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableButton;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.scenes.AccountManagerScene;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.editor.ExtendedHTMLEditorKit;
import ru.turikhay.tlauncher.ui.swing.editor.HyperlinkProcessor;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.swing.extended.ExtendedPanel;
import ru.turikhay.tlauncher.ui.swing.extended.HtmlSubstitutor;
import ru.turikhay.util.git.TokenReplacingReader;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class AccountElyStart extends BorderPanel implements AccountMultipaneCompCloseable, LocalizableComponent {
    private final String LOC_PREFIX = AccountMultipaneComp.LOC_PREFIX_PATH + multipaneName() + ".";

    private final LocalizableButton button/*, extraButton*/;
    private final EditorPane content;

    private StartState state;

    public AccountElyStart(final AccountManagerScene scene) {

        this.content = new EditorPane();
        ((ExtendedHTMLEditorKit) content.getEditorKit()).setHyperlinkProcessor(new HyperlinkProcessor() {
            @Override
            public JPopupMenu process(String link) {
                if (link != null && link.startsWith("internal:tip:")) {
                    String tip = link.substring("internal:tip:".length());
                    scene.multipane.showTip(tip);
                    return null;
                }
                return HyperlinkProcessor.defaultProcessor.process(link);
            }
        });
        setCenter(content);

        ExtendedPanel panel = new ExtendedPanel();
        panel.setLayout(new GridBagLayout());

        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridy = -1;

        button = new LocalizableButton();
        button.addActionListener(e -> {
            switch (state) {
                case DESCRIPTION:
                    setState(StartState.GET_READY);
                    break;
                case GET_READY:
                    scene.multipane.showTip("process-account-ely");
                    break;
            }
        });
        button.setIcon(Images.getIcon24("logo-ely"));
        button.setFont(button.getFont().deriveFont(Font.BOLD));
        c.gridy++;
        panel.add(button, c);

        /*extraButton = new LocalizableButton();
        extraButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switch(state) {
                    case DESCRIPTION:
                        break;
                    case GET_READY:
                        scene.multipane.showTip("add-account-ely_legacy");
                        break;
                }
            }
        });
        c.gridy++;
        panel.add(extraButton, c);*/

        setSouth(panel);

        setState(StartState.DESCRIPTION);
    }

    private void setState(StartState state) {
        this.state = Objects.requireNonNull(state, "state");
        button.setText(LOC_PREFIX + state.toString().toLowerCase(java.util.Locale.ROOT) + ".button");
        //extraButton.setText(LOC_PREFIX + state.toString().toLowerCase(java.util.Locale.ROOT) + ".bottom-button");
        content.setText(TokenReplacingReader.resolveVars(Localizable.get(LOC_PREFIX + state.toString().toLowerCase(java.util.Locale.ROOT) + ".body"), new HtmlSubstitutor()));
    }

    @Override
    public void setMaximumSize(Dimension maximumSize) {
        super.setMaximumSize(maximumSize);
        content.setPreferredSize(new Dimension(0, 0));
    }

    @Override
    public void multipaneClosed() {

    }

    @Override
    public Component multipaneComp() {
        return this;
    }

    @Override
    public String multipaneName() {
        return "add-account-ely";
    }

    @Override
    public boolean multipaneLocksView() {
        return true;
    }

    @Override
    public void multipaneShown(boolean gotBack) {
        setState(StartState.DESCRIPTION);
    }

    @Override
    public void updateLocale() {
        setState(state);
    }

    private enum StartState {
        DESCRIPTION,
        GET_READY
    }
}

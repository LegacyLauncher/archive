package net.legacylauncher.ui.pr.beeline;

import net.legacylauncher.ui.MainPane;
import net.legacylauncher.ui.images.ImageIcon;
import net.legacylauncher.ui.images.Images;
import net.legacylauncher.ui.scenes.PseudoScene;
import net.legacylauncher.ui.swing.extended.ExtendedButton;

public class FraudHuntersScene extends PseudoScene {
    private static final int MARGIN = 16, START_BOX_HEIGHT = 112;

    private final StartBox startBox;

    public FraudHuntersScene(MainPane main) {
        super(main);

        ExtendedButton goBack = new ExtendedButton();
        goBack.addActionListener(e -> getMainPane().setScene(getMainPane().defaultScene));
        goBack.setBounds(MARGIN, MARGIN, 48, 48);
        ImageIcon.setup(goBack, Images.getIcon32("arrow-left"));
        add(goBack);

        startBox = new StartBox();
        add(startBox);

        updateStartBoxPos();
    }

    @Override
    public void setShown(boolean shown, boolean animate) {
        super.setShown(shown, animate);

        getMainPane().background.setBeeline(shown);
        if (shown) {
            startBox.init();
        }
    }

    @Override
    public void onResize() {
        super.onResize();
        updateStartBoxPos();
    }

    private void updateStartBoxPos() {
        startBox.setBounds(MARGIN, getHeight() - MARGIN - START_BOX_HEIGHT, getWidth() - MARGIN - MARGIN, START_BOX_HEIGHT);
    }
}

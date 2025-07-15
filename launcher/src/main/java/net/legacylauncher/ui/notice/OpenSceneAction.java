package net.legacylauncher.ui.notice;

import net.legacylauncher.LegacyLauncher;
import net.legacylauncher.ui.MainPane;
import net.legacylauncher.ui.alert.Alert;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableMenuItem;
import net.legacylauncher.ui.scenes.PseudoScene;
import net.legacylauncher.ui.swing.DelayedComponent;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OpenSceneAction extends NoticeAction {
    private final String sceneName;
    private final Map<String, String> translations;

    OpenSceneAction(String sceneName, Map<String, String> translations) {
        super("open_scene");
        this.sceneName = sceneName;
        this.translations = translations;
    }

    @Override
    Runnable getRunnable() {
        return this::openScene;
    }

    @Override
    List<? extends JMenuItem> getMenuItemList() {
        List<JMenuItem> list = new ArrayList<>();

        JMenuItem item = new LocalizableMenuItem(translations.getOrDefault(Localizable.get().getLocale().toString(), "en_US"));
        item.addActionListener(e -> openScene());
        list.add(item);

        return list;
    }

    private void openScene() {
        MainPane mp = LegacyLauncher.getInstance().getFrame().mp;
        DelayedComponent<? extends PseudoScene> scene = mp.scenes.get(sceneName);
        if (scene == null) {
            Alert.showError("Scene not found", null);
            return;
        }
        mp.setScene(scene);
    }
}

package net.legacylauncher.ui.settings;

import lombok.extern.slf4j.Slf4j;
import net.legacylauncher.ui.loc.Localizable;
import net.legacylauncher.ui.loc.LocalizableComponent;
import net.legacylauncher.ui.swing.MagnifiedInsets;
import net.legacylauncher.ui.swing.editor.EditorPane;
import net.legacylauncher.ui.swing.extended.BorderPanel;
import net.legacylauncher.ui.theme.Theme;
import net.legacylauncher.util.FileUtil;
import net.legacylauncher.util.OS;
import net.legacylauncher.util.SwingUtil;
import net.legacylauncher.util.git.ITokenResolver;
import net.legacylauncher.util.git.TokenReplacingReader;
import org.apache.commons.io.IOUtils;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

@Slf4j
public class HTMLPage extends BorderPanel implements LocalizableComponent {
    private final HTMLPage.AboutPageTokenResolver resolver;
    private final String source;
    private final EditorPane editor;

    HTMLPage(String resourceName) {
        String tempSource;
        try {
            tempSource = FileUtil.getResource(getClass().getResource(resourceName));
        } catch (Exception var4) {
            log.warn("Could not load HTML page resource from {}", resourceName, var4);
            tempSource = null;
        }

        source = tempSource;
        resolver = new HTMLPage.AboutPageTokenResolver();
        editor = new EditorPane();

        if (!OS.WINDOWS.isCurrent())
            editor.setMargin(new MagnifiedInsets(10, 0, 5, 0));

        updateLocale();
        setCenter(editor);
    }

    public EditorPane getEditor() {
        return editor;
    }

    public String getSource() {
        return source;
    }

    @Override
    public void updateLocale() {
        updateHtml();
    }

    @Override
    public void updateUI() {
        updateHtml();
        super.updateUI();
    }

    private void updateHtml() {
        if (source == null) {
            return;
        }
        try {
            String string = IOUtils.toString(new TokenReplacingReader(new StringReader(source), resolver));
            editor.setText(string);
        } catch (IOException e) {
            log.warn("Unable to update html content", e);
        }
    }

    private class AboutPageTokenResolver implements ITokenResolver {
        private AboutPageTokenResolver() {
        }

        public String resolveToken(String token) {
            if (token.equals("width")) {
                return String.valueOf(SwingUtil.magnify(555));
            }
            if (token.startsWith("image:")) {
                return token.substring("image:".length());
            }
            if (token.startsWith("loc:")) {
                return Localizable.get(token.substring("loc:".length()));
            }
            if (token.equals("color")) {
                Color color = Theme.getTheme().getForeground();
                return String.format(java.util.Locale.ROOT, "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
            }
            return token;
        }
    }
}

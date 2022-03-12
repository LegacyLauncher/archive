package ru.turikhay.tlauncher.ui.settings;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

public class HTMLPage extends BorderPanel implements LocalizableComponent {
    private static final Logger LOGGER = LogManager.getLogger(HTMLPage.class);

    private final String textColor;

    {
        Color color = Theme.getTheme().getForeground();
        textColor = String.format(java.util.Locale.ROOT, "#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private final HTMLPage.AboutPageTokenResolver resolver;
    private final String source;
    private final EditorPane editor;

    HTMLPage(String resourceName) {
        String tempSource;
        try {
            tempSource = FileUtil.getResource(getClass().getResource(resourceName));
        } catch (Exception var4) {
            LOGGER.warn("Could not load HTML page resource from {}", resourceName, var4);
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

    public void updateLocale() {
        if (source == null) {
            return;
        }
        try {
            String string = IOUtils.toString(new TokenReplacingReader(new StringReader(source), resolver));
            editor.setText(string);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class AboutPageTokenResolver implements ITokenResolver {
        private AboutPageTokenResolver() {
        }

        public String resolveToken(String token) {
            if (token.equals("width")) {
                return String.valueOf(SwingUtil.magnify(525));
            }
            if (token.startsWith("image:")) {
                return token.substring("image:".length());
            }
            if (token.startsWith("loc:")) {
                return Localizable.get(token.substring("loc:".length()));
            }
            if (token.equals("color")) {
                return textColor;
            }
            return token;
        }
    }
}

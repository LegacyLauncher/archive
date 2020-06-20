package ru.turikhay.tlauncher.ui.settings;

import ru.turikhay.tlauncher.ui.images.Images;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.MagnifiedInsets;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.tlauncher.ui.theme.Theme;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

import java.awt.*;
import java.io.IOException;
import java.io.StringReader;

public class HTMLPage extends BorderPanel implements LocalizableComponent {
    private final String textColor;

    {
        Color color = Theme.getTheme().getForeground();
        textColor = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    private final HTMLPage.AboutPageTokenResolver resolver;
    private final String source;
    private final EditorPane editor;

    HTMLPage(String resourceName) {
        String tempSource;
        try {
            tempSource = FileUtil.getResource(getClass().getResource(resourceName));
        } catch (Exception var4) {
            U.log(var4);
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
        if (source != null) {
            StringBuilder string = new StringBuilder();
            TokenReplacingReader replacer = new TokenReplacingReader(new StringReader(source), resolver);

            label54:
            {
                try {
                    while (true) {
                        int read;
                        if ((read = replacer.read()) <= 0) {
                            break label54;
                        }

                        string.append((char) read);
                    }
                } catch (IOException var8) {
                    var8.printStackTrace();
                } finally {
                    U.close(replacer);
                }

                return;
            }

            editor.setText(string.toString());
        }
    }

    private class AboutPageTokenResolver implements ITokenResolver {
        private static final String image = "image:";
        private static final String loc = "loc:";
        private static final String color = "color";

        private AboutPageTokenResolver() {
        }

        public String resolveToken(String token) {
            if (token.equals("width"))
                return String.valueOf(SwingUtil.magnify(525));
            return token.startsWith("image:") ? Images.getRes(token.substring("image:".length())).toExternalForm() : (token.startsWith("loc:") ? Localizable.get(token.substring("loc:".length())) : (token.equals("color") ? textColor : token));
        }
    }
}

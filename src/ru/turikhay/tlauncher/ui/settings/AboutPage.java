package ru.turikhay.tlauncher.ui.settings;

import java.io.IOException;
import java.io.StringReader;

import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableComponent;
import ru.turikhay.tlauncher.ui.swing.editor.EditorPane;
import ru.turikhay.tlauncher.ui.swing.extended.BorderPanel;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.U;
import ru.turikhay.util.git.ITokenResolver;
import ru.turikhay.util.git.TokenReplacingReader;

public class AboutPage extends BorderPanel implements LocalizableComponent {
	private final AboutPageTokenResolver resolver;

	private final String source;
	private final EditorPane editor;

	AboutPage() {
		String tempSource;

		try {
			tempSource = FileUtil.getResource(getClass().getResource("about.html"));
		} catch(Exception e) {
			U.log(e);
			tempSource = null;
		}

		this.source = tempSource;
		this.resolver = new AboutPageTokenResolver();
		this.editor = new EditorPane();

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
		if(source == null) return;

		StringBuilder string = new StringBuilder();
		TokenReplacingReader replacer = new TokenReplacingReader(new StringReader(source), resolver);
		int read;

		try {

			while((read = replacer.read()) > 0)
				string.append((char) read);

		} catch(IOException ioE) {
			ioE.printStackTrace();
			return;
		} finally {
			U.close(replacer);
		}

		editor.setText(string.toString());
	}

	private class AboutPageTokenResolver implements ITokenResolver {
		private static final String
		image = "image:", loc = "loc:", width = "width",
		color = "color";

		@Override
		public String resolveToken(String token) {
			if(token.startsWith(image))
				return ImageCache.getRes(token.substring(image.length())).toExternalForm();

			if(token.startsWith(loc))
				return Localizable.get(token.substring(loc.length()));

			if(token.equals(width))
				return "445";

			if(token.equals(color))
				return "black";

			return token;
		}
	}

}

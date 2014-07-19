package ru.turikhay.tlauncher.ui.swing.editor;

import javax.swing.text.Element;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.ImageView;

public class ExtendedHTMLEditorKit extends HTMLEditorKit {
	protected final static ExtendedHTMLFactory extendedFactory = new ExtendedHTMLFactory();

	@Override
	public ViewFactory getViewFactory() {
		return extendedFactory;
	}

	public static class ExtendedHTMLFactory extends HTMLFactory {
		@Override
		public View create(Element elem) {
			View view = super.create(elem);

			if(!(view instanceof ImageView))
				return view;

			return new ExtendedImageView(elem);
		}
	}
}

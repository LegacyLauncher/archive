package com.turikhay.tlauncher.ui.explorer;

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;

import com.turikhay.util.OS;

/**
 * 
 * 
 * @see <a
 *      href="http://docs.oracle.com/javase/tutorial/uiswing/components/filechooser.html">Taken
 *      from here</a>
 * 
 */
public class ImageFilePreview extends JComponent {
	private static final long serialVersionUID = -1465489971097254329L;
	private static final Cursor DEFAULT = Cursor.getDefaultCursor(),
			HAND = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

	ImageIcon thumbnail = null;
	File file = null;

	public ImageFilePreview(JFileChooser fc) {
		setCursor(DEFAULT);
		setPreferredSize(new Dimension(200, 100));

		fc.addPropertyChangeListener(new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent e) {
				boolean update = false;
				String prop = e.getPropertyName();

				// If the directory changed, don't show an image.
				if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
					file = null;
					update = true;

					// If a file became selected, find out which one.
				} else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY
						.equals(prop)) {
					file = (File) e.getNewValue();
					update = true;
				}

				// Update the preview accordingly.
				if (update) {
					thumbnail = null;
					if (isShowing()) {
						loadImage();
						repaint();
					}
				}
			}
		});

		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() != MouseEvent.BUTTON1)
					return;

				if (file != null)
					OS.openFile(file);
			}
		});
	}

	public void loadImage() {
		if (file == null) {
			thumbnail = null;
			
			setCursor(DEFAULT);
			return;
		}

		// Don't use createImageIcon (which is a wrapper for getResource)
		// because the image we're trying to load is probably not one
		// of this program's own resources.
		ImageIcon tmpIcon = new ImageIcon(file.getPath());
		setCursor(HAND);

		if (tmpIcon != null) {
			if (tmpIcon.getIconWidth() > 190) {
				thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(
						190, -1, Image.SCALE_DEFAULT));
			} else { // no need to miniaturize
				thumbnail = tmpIcon;
			}
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (thumbnail == null) {
			loadImage();
		}
		if (thumbnail != null) {
			int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
			int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;

			if (y < 0) {
				y = 0;
			}

			if (x < 10) {
				x = 10;
			}
			thumbnail.paintIcon(this, g, x, y);
		}
	}

}

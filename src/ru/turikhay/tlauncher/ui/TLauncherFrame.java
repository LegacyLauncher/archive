package ru.turikhay.tlauncher.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.console.Console;
import ru.turikhay.tlauncher.ui.images.ImageCache;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.util.U;

public class TLauncherFrame extends JFrame {
	private static final long serialVersionUID = 5077131443679431434L;
	public static final int[] maxSize = { 1920, 1080 };
	public static final float fontSize = 12F;

	private final static List<Image> favicons = new ArrayList<Image>();

	private final TLauncherFrame instance;

	private final TLauncher tlauncher;
	private final Configuration settings;
	private final LangConfiguration lang;
	//
	private final int[] windowSize;
	//
	public final MainPane mp;

	//

	public TLauncherFrame(TLauncher t) {
		this.instance = this;

		this.tlauncher = t;
		this.settings = t.getSettings();
		this.lang = t.getLang();

		this.windowSize = settings.getWindowSize();

		TLauncherFrame.initLookAndFeel();
		TLauncherFrame.initFontSize();

		this.setUILocale();
		this.setWindowSize();
		this.setWindowTitle();
		this.setIconImages(getFavicons());

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				instance.setVisible(false);
				TLauncher.kill();
			}
		});

		this.addComponentListener(new ComponentListener() {
			@Override
			public void componentResized(ComponentEvent e) {
				mp.onResize();
			}

			@Override
			public void componentShown(ComponentEvent e) {
				instance.validate();
				instance.repaint();
				instance.toFront();

				mp.background.startBackground();
			}

			@Override
			public void componentMoved(ComponentEvent e) {
			}

			@Override
			public void componentHidden(ComponentEvent e) {
				mp.background.suspendBackground();
			}
		});

		log("Preparing main pane...");
		mp = new MainPane(this);

		add(mp);

		log("Packing main frame...");
		pack();

		log("Resizing main pane...");
		mp.onResize();
		mp.background.startBackground();

		this.setVisible(true);

		if (settings.isFirstRun())
			Alert.showLocAsyncWarning("firstrun");
	}

	public void updateLocales() {
		try {
			tlauncher.reloadLocale();
		} catch (Exception e) {
			log("Cannot reload settings!", e);
			return;
		}

		Console.updateLocale();
		LocalizableMenuItem.updateLocales();

		setWindowTitle();
		setUILocale();

		Localizable.updateContainer(this);
	}

	public void setWindowTitle() {
		String translator = lang.nget("translator"), copyright = "(by " + TLauncher.getDeveloper()
				+ ((translator != null) ? ", translated by " + translator : "")
				+ ")", brand = TLauncher.getBrand() + " "
				+ TLauncher.getVersion();
		
		this.setTitle("TLauncher " + brand + " " + copyright);
	}

	private void setWindowSize() {
		int width = (windowSize[0] > maxSize[0]) ? maxSize[0] : windowSize[0];
		int height = (windowSize[1] > maxSize[1]) ? maxSize[1] : windowSize[1];

		Dimension size = new Dimension(width, height);

		this.setPreferredSize(size);
		this.setMinimumSize(size);
		this.setLocationRelativeTo(null);
	}

	private void setUILocale() {
		UIManager.put("OptionPane.yesButtonText", lang.nget("ui.yes"));
		UIManager.put("OptionPane.noButtonText", lang.nget("ui.no"));
		UIManager.put("OptionPane.cancelButtonText", lang.nget("ui.cancel"));

		// I know that I could place this into (Extended)ResourceBundle
		// But, you know...
		// NO.

		UIManager.put("FileChooser.acceptAllFileFilterText",
				lang.nget("explorer.extension.all"));

		UIManager.put("FileChooser.lookInLabelText",
				lang.nget("explorer.lookin"));
		UIManager.put("FileChooser.saveInLabelText",
				lang.nget("explorer.lookin"));

		UIManager.put("FileChooser.fileNameLabelText",
				lang.nget("explorer.input.filename"));
		UIManager.put("FileChooser.folderNameLabelText",
				lang.nget("explorer.input.foldername"));
		UIManager.put("FileChooser.filesOfTypeLabelText",
				lang.nget("explorer.input.type"));

		UIManager.put("FileChooser.upFolderToolTipText",
				lang.nget("explorer.button.up.tip"));
		UIManager.put("FileChooser.upFolderAccessibleName",
				lang.nget("explorer.button.up"));

		UIManager.put("FileChooser.newFolderToolTipText",
				lang.nget("explorer.button.newfolder.tip"));
		UIManager.put("FileChooser.newFolderAccessibleName",
				lang.nget("explorer.button.newfolder"));
		UIManager.put("FileChooser.newFolderButtonToolTipText",
				lang.nget("explorer.button.newfolder.tip"));
		UIManager.put("FileChooser.newFolderButtonText",
				lang.nget("explorer.button.newfolder"));

		UIManager.put("FileChooser.other.newFolder",
				lang.nget("explorer.button.newfolder.name"));
		UIManager.put("FileChooser.other.newFolder.subsequent",
				lang.nget("explorer.button.newfolder.name"));
		UIManager.put("FileChooser.win32.newFolder",
				lang.nget("explorer.button.newfolder.name"));
		UIManager.put("FileChooser.win32.newFolder.subsequent",
				lang.nget("explorer.button.newfolder.name"));

		UIManager.put("FileChooser.homeFolderToolTipText",
				lang.nget("explorer.button.home.tip"));
		UIManager.put("FileChooser.homeFolderAccessibleName",
				lang.nget("explorer.button.home"));

		UIManager.put("FileChooser.listViewButtonToolTipText",
				lang.nget("explorer.button.list.tip"));
		UIManager.put("FileChooser.listViewButtonAccessibleName",
				lang.nget("explorer.button.list"));

		UIManager.put("FileChooser.detailsViewButtonToolTipText",
				lang.nget("explorer.button.details.tip"));
		UIManager.put("FileChooser.detailsViewButtonAccessibleName",
				lang.nget("explorer.button.details"));

		UIManager.put("FileChooser.viewMenuButtonToolTipText",
				lang.nget("explorer.button.view.tip"));
		UIManager.put("FileChooser.viewMenuButtonAccessibleName",
				lang.nget("explorer.button.view"));

		UIManager.put("FileChooser.newFolderErrorText",
				lang.nget("explorer.error.newfolder"));
		UIManager.put("FileChooser.newFolderErrorSeparator", ": ");

		UIManager.put("FileChooser.newFolderParentDoesntExistTitleText",
				lang.nget("explorer.error.newfolder-nopath"));
		UIManager.put("FileChooser.newFolderParentDoesntExistText",
				lang.nget("explorer.error.newfolder-nopath"));

		UIManager.put("FileChooser.fileDescriptionText",
				lang.nget("explorer.details.file"));
		UIManager.put("FileChooser.directoryDescriptionText",
				lang.nget("explorer.details.dir"));

		UIManager.put("FileChooser.saveButtonText",
				lang.nget("explorer.button.save"));
		UIManager.put("FileChooser.openButtonText",
				lang.nget("explorer.button.open"));

		UIManager.put("FileChooser.saveDialogTitleText",
				lang.nget("explorer.title.save"));
		UIManager.put("FileChooser.openDialogTitleText",
				lang.nget("explorer.title.open"));
		UIManager.put("FileChooser.cancelButtonText",
				lang.nget("explorer.button.cancel"));
		UIManager.put("FileChooser.updateButtonText",
				lang.nget("explorer.button.update"));
		UIManager.put("FileChooser.helpButtonText",
				lang.nget("explorer.button.help"));
		UIManager.put("FileChooser.directoryOpenButtonText",
				lang.nget("explorer.button.open-dir"));

		UIManager.put("FileChooser.saveButtonToolTipText",
				lang.nget("explorer.title.save.tip"));
		UIManager.put("FileChooser.openButtonToolTipText",
				lang.nget("explorer.title.open.tip"));
		UIManager.put("FileChooser.cancelButtonToolTipText",
				lang.nget("explorer.button.cancel.tip"));
		UIManager.put("FileChooser.updateButtonToolTipText",
				lang.nget("explorer.button.update.tip"));
		UIManager.put("FileChooser.helpButtonToolTipText",
				lang.nget("explorer.title.help.tip"));
		UIManager.put("FileChooser.directoryOpenButtonToolTipText",
				lang.nget("explorer.button.open-dir.tip"));

		UIManager.put("FileChooser.viewMenuLabelText",
				lang.nget("explorer.button.view"));
		UIManager.put("FileChooser.refreshActionLabelText",
				lang.nget("explorer.context.refresh"));
		UIManager.put("FileChooser.newFolderActionLabelText",
				lang.nget("explorer.context.newfolder"));

		UIManager.put("FileChooser.listViewActionLabelText",
				lang.nget("explorer.view.list"));
		UIManager.put("FileChooser.detailsViewActionLabelText",
				lang.nget("explorer.view.details"));

		UIManager.put("FileChooser.filesListAccessibleName",
				lang.nget("explorer.view.list.name"));
		UIManager.put("FileChooser.filesDetailsAccessibleName",
				lang.nget("explorer.view.details.name"));

		UIManager.put("FileChooser.renameErrorTitleText",
				lang.nget("explorer.error.rename.title"));
		UIManager.put("FileChooser.renameErrorText",
				lang.nget("explorer.error.rename") + "\n{0}");
		UIManager.put("FileChooser.renameErrorFileExistsText",
				lang.nget("explorer.error.rename-exists"));

		UIManager.put("FileChooser.readOnly", Boolean.FALSE);
	}

	private static void initFontSize() {
		try {
			UIDefaults defaults = UIManager.getDefaults();

			int minSize = 12, maxSize = 12;

			for (Enumeration<?> e = defaults.keys(); e.hasMoreElements();) {
				Object key = e.nextElement();
				Object value = defaults.get(key);

				if (value instanceof Font) {
					Font font = (Font) value;
					int size = font.getSize();

					if (size < minSize)
						size = minSize;
					else if (size > maxSize)
						size = maxSize;

					if (value instanceof FontUIResource) {
						defaults.put(key, new FontUIResource(font.getName(),
								font.getStyle(), size));
					} else {
						defaults.put(key,
								new Font(font.getName(), font.getStyle(), size));
					}
				}
			}
		} catch (Exception e) {
			log("Cannot change font sizes!", e);
		}
	}

	public static void initLookAndFeel() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			log("Can't set system look and feel.");
			e.printStackTrace();
		}
	}

	public static List<Image> getFavicons() {
		if (!favicons.isEmpty())
			return Collections.unmodifiableList(favicons);

		int[] sizes = new int[] { 256, 128, 96, 64, 48, 32, 24, 16 };
		String loaded = "";

		for (int i : sizes) {
			Image image = ImageCache.getImage("fav" + i + ".png", false);
			if (image == null)
				continue;

			loaded += ", " + i + "px";
			favicons.add(image);
		}

		if (loaded.isEmpty())
			log("No favicon is loaded.");
		else
			log("Favicons loaded:", loaded.substring(2));

		return favicons;
	}

	public static URL getRes(String uri) {
		return TLauncherFrame.class.getResource(uri);
	}

	private static void log(Object... o) {
		U.log("[Frame]", o);
	}
}

package ru.turikhay.tlauncher.ui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.UIManager;

import ru.turikhay.tlauncher.TLauncher;
import ru.turikhay.tlauncher.configuration.Configuration;
import ru.turikhay.tlauncher.configuration.LangConfiguration;
import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.console.Console;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.loc.LocalizableMenuItem;
import ru.turikhay.util.OS;
import ru.turikhay.util.SwingUtil;
import ru.turikhay.util.U;

public class TLauncherFrame extends JFrame {
	private static final long serialVersionUID = 5077131443679431434L;
	public static final int[] maxSize = { 1920, 1080 };
	public static final float fontSize = OS.WINDOWS.isCurrent()? 12f : 14f;

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

		SwingUtil.initFontSize((int) fontSize);
		SwingUtil.setFavicons(this);

		this.setUILocale();
		this.setWindowSize();
		this.setWindowTitle();

		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				instance.setVisible(false);
				TLauncher.kill();
			}
		});
		this.setDefaultCloseOperation(EXIT_ON_CLOSE); // If code above isn't working under running os.

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

		this.addWindowStateListener(new WindowStateListener() {
			@Override
			public void windowStateChanged(WindowEvent e) {
				int newState = getExtendedStateFor(e.getNewState());
				if(newState == -1) return;

				settings.set("gui.window", newState);
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

		int windowState = getExtendedStateFor(settings.getInteger("gui.window"));

		if(windowState != -1)
			this.setExtendedState(windowState);

		setVisible(true);

		if(settings.isFirstRun())
			Alert.showLocAsyncWarning("firstrun");
	}

	public TLauncher getLauncher() {
		return tlauncher;
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
		UIManager.put("TabbedPane.contentOpaque", false);

		UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0)); 
		UIManager.put("TabbedPane.tabInsets", new Insets(0, 8, 6, 8));
	}

	private static int getExtendedStateFor(int state) {
		switch(state) {
		case MAXIMIZED_BOTH:
		case MAXIMIZED_HORIZ:
		case MAXIMIZED_VERT:
		case NORMAL:
			return state;
		}
		return -1;
	}

	public static URL getRes(String uri) {
		return TLauncherFrame.class.getResource(uri);
	}

	private static void log(Object... o) {
		U.log("[Frame]", o);
	}
}

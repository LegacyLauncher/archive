package ru.turikhay.tlauncher.ui.console;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.JTextComponent;

import ru.turikhay.tlauncher.ui.alert.Alert;
import ru.turikhay.tlauncher.ui.explorer.ExtensionFileFilter;
import ru.turikhay.tlauncher.ui.explorer.FileExplorer;
import ru.turikhay.tlauncher.ui.loc.Localizable;
import ru.turikhay.tlauncher.ui.swing.EmptyAction;
import ru.turikhay.tlauncher.ui.swing.TextPopup;
import ru.turikhay.util.FileUtil;
import ru.turikhay.util.OS;
import ru.turikhay.util.U;
import ru.turikhay.util.stream.StringStream;

public class ConsoleTextPopup extends TextPopup {
	private final Console console;
	
	private final FileExplorer explorer;
	private final Action saveAllAction;
	
	ConsoleTextPopup(Console console) {
		this.console = console;
		
		this.explorer = new FileExplorer();
		explorer.setFileFilter(new ExtensionFileFilter("log"));
		
		this.saveAllAction = new EmptyAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				onSavingCalled(e);
			}
		};
	}
	
	@Override
	protected JPopupMenu getPopup(MouseEvent e, final JTextComponent comp) {
		JPopupMenu menu = super.getPopup(e, comp);
		
		if(menu == null)
			return null;
		
		menu.addSeparator();
		menu.add(saveAllAction).setText(Localizable.get("console.save.popup"));
		
		return menu;
	}
	
	protected void onSavingCalled(ActionEvent e) {
		explorer.setSelectedFile(new File(console.getName() + ".log"));
		
		int result = explorer.showSaveDialog(console.frame);
		
		if(result != FileExplorer.APPROVE_OPTION)
			return;
		
		File file = explorer.getSelectedFile();
		
		if(file == null) {
			U.log("Returned NULL. Damn it!");
			return;
		}
		
		String path = file.getAbsolutePath();
		
		if(!path.endsWith(".log"))
			path += ".log";
		
		file = new File(path);
		
		OutputStream output = null;		
		try {
			FileUtil.createFile(file);
			
			StringStream input = console.getStream();
			output = new BufferedOutputStream(new FileOutputStream(file));
			
			boolean addR = OS.WINDOWS.isCurrent();
			int caret = -1;
			char current;
			
			while(++caret < input.getLength()) {
				current = input.getCharAt(caret);
				
				if(current == '\n' && addR)
					output.write('\r');
				
				output.write(current);
			}
			
			output.close();
		} catch(Throwable throwable) {
			Alert.showLocError("console.save.error", throwable);
		} finally {
			if(output != null)
				try { output.close(); }
				catch(IOException ignored) {
					ignored.printStackTrace();
				}
		}
	}
}
 
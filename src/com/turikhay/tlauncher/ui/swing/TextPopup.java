package com.turikhay.tlauncher.ui.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import com.turikhay.tlauncher.TLauncher;
import com.turikhay.tlauncher.configuration.LangConfiguration;
import com.turikhay.tlauncher.ui.loc.Localizable;
import com.turikhay.tlauncher.ui.loc.LocalizableComponent;

public class TextPopup extends MouseAdapter implements LocalizableComponent {
	@SuppressWarnings("unused")
	private static String SOURCE_CODE = "http://cloud-notes.blogspot.ru/2013/04/jtextcomponent-java.html";
	 
    private static String CUT, COPY, SELECTALL, PASTE;
    static LangConfiguration l;
    
    public TextPopup(){
    	updateLocale();
    }
 
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            if (!(e.getSource() instanceof JTextComponent)) {
                return;
            }
 
            JTextComponent textComponent = (JTextComponent) e.getSource();
            textComponent.requestFocus();
            boolean enabled = textComponent.isEnabled();
            boolean editable = textComponent.isEditable();
            boolean nonempty = !(textComponent.getText() == null || textComponent.getText().equals(""));
            boolean marked = textComponent.getSelectedText() != null;
 
            boolean pasteAvailable =
                    Toolkit.getDefaultToolkit().getSystemClipboard().
                    getContents(null).isDataFlavorSupported(
                    DataFlavor.stringFlavor);
 
            JPopupMenu popup = new JPopupMenu();
            Action cutAction, copyAction, pasteAction, selectAllAction;
 
            if (enabled && editable && marked) {
                cutAction = textComponent.getActionMap().get(DefaultEditorKit.cutAction);
                if (cutAction == null) {
                    cutAction = textComponent.getActionMap().get("cut");
                }
                if (cutAction != null) {
                    popup.add(cutAction).setText(CUT);
                }
            }
            if (enabled && marked) {
                copyAction = textComponent.getActionMap().get(DefaultEditorKit.copyAction);
                if (copyAction == null) {
                    copyAction = textComponent.getActionMap().get("copy");
                }
                if (copyAction != null) {
                    popup.add(copyAction).setText(COPY);
                }
            }
            if (enabled && editable && pasteAvailable) {
                pasteAction = textComponent.getActionMap().get(DefaultEditorKit.pasteAction);
                if (pasteAction == null) {
                    pasteAction = textComponent.getActionMap().get("paste");
                }
                if (pasteAction != null) {
                    popup.add(pasteAction).setText(PASTE);
                }
            }
 
            if (enabled && nonempty) {
                selectAllAction = textComponent.getActionMap().get(DefaultEditorKit.selectAllAction);
                if (selectAllAction == null) {
                    selectAllAction = textComponent.getActionMap().get("selectAll");
                }
                if (selectAllAction != null) {
                    if (popup.getComponentCount() > 0) {
                        if (!(popup.getComponent(popup.getComponentCount() - 1) instanceof JPopupMenu.Separator)) {
                            popup.addSeparator();
                        }
                    }
                    popup.add(selectAllAction).setText(SELECTALL);
                }
 
            }
 
            if (popup.getComponentCount() > 0) {
                if (popup.getComponent(0) instanceof JPopupMenu.Separator) {
                    popup.remove(0);
                }
                if (popup.getComponent(popup.getComponentCount() - 1) instanceof JPopupMenu.Separator) {
                    popup.remove(popup.getComponentCount() - 1);
                }
                
 
                popup.show(e.getComponent(), e.getX(), e.getY() - popup.getSize().height);
            }
        }
    }

	public void updateLocale(){
		if(l == null && TLauncher.getInstance() != null) l = Localizable.get();
		
		if(l == null){
			CUT = "Cut";
			COPY = "Copy";
			SELECTALL = "Select all";
			PASTE = "Paste";
		} else {
			CUT = l.get("popup.cut");
			COPY = l.get("popup.copy");
			SELECTALL = l.get("popup.selectall");
			PASTE = l.get("popup.paste");
		}
	}
}

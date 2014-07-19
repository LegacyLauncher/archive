package ru.turikhay.tlauncher.ui.swing;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import org.apache.commons.lang3.StringUtils;

import ru.turikhay.tlauncher.ui.loc.Localizable;

/**
 * @author turikhay
 * @see <a href="http://cloud-notes.blogspot.ru/2013/04/jtextcomponent-java.html">Based on this source</a>
 */
public class TextPopup extends MouseAdapter {
	
	@Override
	public void mouseClicked(MouseEvent e) {
		if(e.getModifiers() != InputEvent.BUTTON3_MASK) // Single right button click
			return;
		
		Object source = e.getSource();
		
		if(!(source instanceof JTextComponent))
			return;
		
		JPopupMenu popup = getPopup(e, (JTextComponent) source);
		
		if(popup == null)
			return;
		
		popup.show(e.getComponent(), e.getX(), e.getY() - popup.getSize().height);
	}
	
	protected JPopupMenu getPopup(MouseEvent e, final JTextComponent comp) {
		
		if(!comp.isEnabled())
			return null; // Component is disabled
		
		boolean
			isEditable = comp.isEditable(),
			isSelected = comp.getSelectedText() != null,
			hasValue = StringUtils.isNotEmpty(comp.getText()),
			
			pasteAvailable = isEditable && Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null).isDataFlavorSupported(DataFlavor.stringFlavor);
		
		JPopupMenu menu = new JPopupMenu();
		
		final Action
			cut = isEditable? selectAction(comp, DefaultEditorKit.cutAction, "cut") : null,
			copy = selectAction(comp, DefaultEditorKit.copyAction, "copy"),
			paste = pasteAvailable? selectAction(comp, DefaultEditorKit.pasteAction, "paste") : null,
			selectAll = hasValue? selectAction(comp, DefaultEditorKit.selectAllAction, "selectAll") : null,
			copyAll;
		
		if(selectAll != null && copy != null)
			copyAll = new EmptyAction() {
				@Override
				public void actionPerformed(ActionEvent e) {
					selectAll.actionPerformed(e);
					copy.actionPerformed(e);
					
					comp.setSelectionStart(comp.getSelectionEnd()); // Deselect copied text, fixing Swing bug under Windows
				}
			};
		else
			copyAll = null;
		
		if(cut != null)
			menu.add(cut).setText(Localizable.get("popup.cut"));
		
		if(isSelected && copy != null)
			menu.add(copy).setText(Localizable.get("popup.copy"));
		
		if(paste != null)
			menu.add(paste).setText(Localizable.get("popup.paste"));
		
		if(selectAll != null) {
			
			if (
				menu.getComponentCount() > 0
				&&
				!(menu.getComponent(menu.getComponentCount() - 1) instanceof JPopupMenu.Separator)
			)
				menu.addSeparator();
			
			menu.add(selectAll).setText(Localizable.get("popup.selectall"));
		}
		
		if(copyAll != null)
			menu.add(copyAll).setText(Localizable.get("popup.copyall"));
		
		if (menu.getComponentCount() == 0)
			return null;
		
		if (menu.getComponent(0) instanceof JPopupMenu.Separator)
			menu.remove(0);
		
		if (menu.getComponent(menu.getComponentCount() - 1) instanceof JPopupMenu.Separator)
			menu.remove(menu.getComponentCount() - 1);

		return menu;
		
	}
	
	protected static Action selectAction(JTextComponent comp, String general, String fallback) {
		Action action;
		
		action = comp.getActionMap().get(general);
		
		if (action == null)
			action = comp.getActionMap().get(fallback);
		
		return action;
	}
	
}

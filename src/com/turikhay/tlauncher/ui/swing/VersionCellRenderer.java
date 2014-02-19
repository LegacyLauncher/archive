package com.turikhay.tlauncher.ui.swing;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import net.minecraft.launcher.updater.VersionSyncInfo;
import net.minecraft.launcher.versions.ReleaseType;

import com.turikhay.tlauncher.ui.loc.Localizable;

public class VersionCellRenderer implements ListCellRenderer<VersionSyncInfo> {
	// Pseudo elements
	public static final VersionSyncInfo
		LOADING = VersionSyncInfo.createEmpty(),
		EMPTY = VersionSyncInfo.createEmpty();
	
	protected final DefaultListCellRenderer defaultRenderer;
	
	public VersionCellRenderer(){
		 this.defaultRenderer = new DefaultListCellRenderer();
	}
	
	@Override
	public Component getListCellRendererComponent(
			JList<? extends VersionSyncInfo> list, VersionSyncInfo value,
			int index, boolean isSelected, boolean cellHasFocus) {
		// Source: http://www.java2s.com/Tutorial/Java/0240__Swing/Comboboxcellrenderer.htm
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
		        isSelected, cellHasFocus);
		
		renderer.setAlignmentY(JLabel.CENTER_ALIGNMENT);
		
		if(value == null)
			renderer.setText("(null)");
		
		else if(value.equals(LOADING))
			renderer.setText(Localizable.get("versions.loading"));
		
		else if(value.equals(EMPTY))
			renderer.setText(Localizable.get("versions.notfound.tip"));
		
		else {
			ReleaseType type = value.getLatestVersion().getReleaseType();
			String id = value.getID(), label = Localizable.nget("version." + type);
			
			switch(value.getLatestVersion().getReleaseType()){
			case OLD_ALPHA:
				id = (id.startsWith("a"))? id.substring(1) : id;
				break;
			case OLD_BETA:
				id = id.substring(1);
				break;
			default: break;
			}
			
			String text = label != null? label + " " + id : id;
			renderer.setText(text);
		}
		
		return renderer;
	}
	
}

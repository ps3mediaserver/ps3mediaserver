package net.pms.newgui;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import net.pms.Messages;

public class FontFileFilter extends FileFilter {

	@Override
	public boolean accept(File f) {
		String name = f.getName().toUpperCase();
		if (name.endsWith("TTC") || name.endsWith("TTF") || name.endsWith(".DESC")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			return true;
		return f.isDirectory();
	}

	@Override
	public String getDescription() {
		return Messages.getString("FontFileFilter.3"); //$NON-NLS-1$
	}

}

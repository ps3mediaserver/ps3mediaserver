package net.pms.medialibrary.gui.dialogs.folderdialog;

import javax.swing.JMenuItem;

import net.pms.medialibrary.external.FileDetailPlugin;

public class FileEntryPluginMenuItem extends JMenuItem {
    private static final long serialVersionUID = -6237466696454071444L;
    private FileDetailPlugin plugin;

    public FileEntryPluginMenuItem(FileDetailPlugin plugin){
    	super(plugin.getName(), plugin.getTreeIcon());
    	setPlugin(plugin);
    }

	public void setPlugin(FileDetailPlugin plugin) {
	    this.plugin = plugin;
    }

	public FileDetailPlugin getPlugin() {
	    return plugin;
    }
}

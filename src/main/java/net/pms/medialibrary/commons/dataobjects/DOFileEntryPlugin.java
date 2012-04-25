package net.pms.medialibrary.commons.dataobjects;

import java.util.List;

import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.plugins.FileDetailPlugin;


public class DOFileEntryPlugin extends DOFileEntryBase {
	private FileDetailPlugin plugin;
	
	public DOFileEntryPlugin(long id, DOFileEntryFolder parent, int positionInParent, String displayNameMask, List<DOThumbnailPriority> thumbnailPrioritis, int maxLineLength, FileDetailPlugin plugin, String pluginConfigFilePath){
		super(id, parent, positionInParent, displayNameMask, thumbnailPrioritis, FileDisplayType.PLUGIN, maxLineLength, plugin == null ? null : plugin.getClass().getName(), pluginConfigFilePath);
		setPlugin(plugin);
	}

	public void setPlugin(FileDetailPlugin plugin) {
	    this.plugin = plugin;
    }

	public FileDetailPlugin getPlugin() {
	    return plugin;
    }
}

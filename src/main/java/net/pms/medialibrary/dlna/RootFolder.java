package net.pms.medialibrary.dlna;

import java.io.File;
import java.util.List;

import net.pms.configuration.MapFileConfiguration;
import net.pms.dlna.DLNAResource;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.external.AdditionalFoldersAtRoot;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.pms.plugins.PluginsFactory;

public class RootFolder extends MediaLibraryFolder{
	private MediaLibraryFolder rootFolder;
	
	public RootFolder(){
		super(MediaLibraryStorage.getInstance().getMediaLibraryFolder(MediaLibraryStorage.getInstance().getRootFolderId(), MediaLibraryStorage.ALL_CHILDREN));
		setId("0");
	}
	
	public DOMediaLibraryFolder getRootFolder(){
		return rootFolder.getFolder();
	}
	
	public void browse(File startFolders []) {		
		discoverChildren();
	}

	public void scan() {	
		//do nothing
	}

	public void stopscan() {	
		//do nothing
	}

	public void browse(List<MapFileConfiguration> parse) {
		//do nothing
	}
	
	@Override
	public boolean isRefreshNeeded() {
		return false;
	}
	
	@Override
	public void discoverChildren() {
		super.discoverChildren();
		addAdditionalFoldersAtRoot();
	}

	@Override
	public boolean refreshChildren() {
		super.refreshChildren();
		addAdditionalFoldersAtRoot();
		
		return true;
	}
	
	private void addAdditionalFoldersAtRoot() {
		for(AdditionalFolderAtRoot l : PluginsFactory.getAdditionalFolderAtRootList()) {
			addChild(l.getChild());			
		}
		for(AdditionalFoldersAtRoot l : PluginsFactory.getAdditionalFoldersAtRootList()) {
			DLNAResource child;
			while((child = l.getChildren().next()) != null){
				addChild(child);
			}
		}
	}
}

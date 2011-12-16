package net.pms.medialibrary.dlna;

import java.io.File;
import java.util.List;

import net.pms.configuration.MapFileConfiguration;
import net.pms.dlna.DLNAResource;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.external.AdditionalFoldersAtRoot;
import net.pms.external.ExternalFactory;
import net.pms.external.ExternalListener;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.storage.MediaLibraryStorage;

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
	public void refreshChildren() {
		super.refreshChildren();
		addAdditionalFoldersAtRoot();
	}
	
	private void addAdditionalFoldersAtRoot() {
		for(ExternalListener l : ExternalFactory.getExternalListeners()){
			if(l instanceof AdditionalFolderAtRoot){
				AdditionalFolderAtRoot nf = (AdditionalFolderAtRoot)l;
				addChild(nf.getChild());
			} else if(l instanceof AdditionalFoldersAtRoot){
				AdditionalFoldersAtRoot nfs = (AdditionalFoldersAtRoot)l;
				DLNAResource child;
				while((child = nfs.getChildren().next()) != null){
					addChild(child);
				}
			}
		}
	}
}

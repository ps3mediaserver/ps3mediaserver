/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.medialibrary.dlna;

import net.pms.dlna.DLNAResource;
import net.pms.external.AdditionalFolderAtRoot;
import net.pms.external.AdditionalFoldersAtRoot;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.pms.plugins.PluginsFactory;

/**
 * The dlna root folder is the entry point to create the dlna tree shown on the renderer
 */
@SuppressWarnings("deprecation")
public class RootFolder extends MediaLibraryFolder{
	private MediaLibraryFolder rootFolder;
	
	/**
	 * Instantiates a new root folder.
	 */
	public RootFolder(){
		super(MediaLibraryStorage.getInstance().getMediaLibraryFolder(MediaLibraryStorage.getInstance().getRootFolderId(), MediaLibraryStorage.ALL_CHILDREN));
		setId("0");
	}
	
	/**
	 * Gets the root folder.
	 *
	 * @return the root folder
	 */
	public DOMediaLibraryFolder getRootFolder(){
		return rootFolder.getFolder();
	}
	
	/* (non-Javadoc)
	 * @see net.pms.medialibrary.dlna.MediaLibraryFolder#isRefreshNeeded()
	 */
	@Override
	public boolean isRefreshNeeded() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see net.pms.medialibrary.dlna.MediaLibraryFolder#discoverChildren()
	 */
	@Override
	public void discoverChildren() {
		super.discoverChildren();
		addAdditionalFoldersAtRoot();
	}

	/* (non-Javadoc)
	 * @see net.pms.medialibrary.dlna.MediaLibraryFolder#refreshChildren()
	 */
	@Override
	public boolean refreshChildren() {
		super.refreshChildren();
		addAdditionalFoldersAtRoot();
		
		return true;
	}
	
	/**
	 * Adds the additional folders at root.
	 */
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

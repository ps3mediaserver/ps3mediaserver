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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.CueFolder;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.DVDISOFile;
import net.pms.dlna.PlaylistFolder;
import net.pms.dlna.RarredFile;
import net.pms.dlna.ZippedFile;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOFolder;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.dataobjects.DOSpecialFolder;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.dataobjects.FileDisplayProperties;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.storage.MediaLibraryStorage;

/**
 * When adding a file as a folder, this class is being used
 */
public class MediaLibraryFolder extends VirtualFolder {
	private static final Logger log = LoggerFactory.getLogger(MediaLibraryFolder.class);
	private DOMediaLibraryFolder folder;
	private boolean isUpdating = false;

	/**
	 * Instantiates a new media library folder.
	 *
	 * @param folder the folder
	 */
	public MediaLibraryFolder(DOMediaLibraryFolder folder) {
		super(folder.getName(), null);

		setFolder(folder);
	}
	
	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#isRefreshNeeded()
	 */
	@Override
	public boolean isRefreshNeeded() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#isTranscodeFolderAvailable()
	 */
	@Override
	public boolean isTranscodeFolderAvailable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.virtual.VirtualFolder#isFolder()
	 */
	@Override
	public boolean isFolder() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#discoverChildren()
	 */
	@Override
	public void discoverChildren() {
		refreshChildren();
	}

	/**
	 * Adds the child folder.
	 *
	 * @param f the f
	 */
	private void addChildFolder(DOFolder f) {
		if (f instanceof DOMediaLibraryFolder) {
			addChild(new MediaLibraryFolder((DOMediaLibraryFolder) f));
		} else if (f instanceof DOSpecialFolder) {
			addChild(((DOSpecialFolder) f).getSpecialFolderImplementation().getDLNAResource());
		}
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#refreshChildren()
	 */
	@Override
	public boolean refreshChildren() {
		if (isUpdating) return false;
		isUpdating = true;
		
		if(log.isDebugEnabled()) log.debug(String.format("Start refreshing children for folder '%s' (%s)", getName(), getId()));

		updateFolder();

		FileDisplayProperties fdp = getFolder().getDisplayProperties();
		short fileIndex = 0;
		short folderIndex = 0;
		boolean nodeRefreshed = false;
		boolean add = false;
		int pos = 0;

		// check if the folders have changed
		for (DOFolder f : getFolder().getChildFolders()) {
			if (getChildren().size() > pos) {
				switch (f.getFolderType()) {
				case MEDIALIBRARY:
					if (getChildren().get(pos) instanceof MediaLibraryFolder) {
						MediaLibraryFolder currNodeInTree = (MediaLibraryFolder) getChildren().get(pos);
						if (!currNodeInTree.getFolder().equals(f)) {
							// if this is a different media library folder node
							add = true;
						}
					} else {
						// if this is another type of node then a media library one
						add = true;
					}
					break;
				case SPECIAL:
					if (!getChildren().get(pos).getName().equals(((DOSpecialFolder) f).getName())) {
						// if the special folder has changed
						// TODO: Improve.. How to check if this is actually a special folder??
						add = true;
					}
					break;
				default:
					log.warn(String.format("Unhandled folder type received (%s). This should never happen!", f.getFolderType()));
					break;
				}
				if (getChildren().get(pos) instanceof MediaLibraryRealFile) {
					add = true;
				}
			} else {
				// If we have to add a new node
				add = true;
			}

			if (add) {
				break;
			} else {
				folderIndex++;
			}

			pos++;
		}

		List<DOFileInfo> files = new ArrayList<DOFileInfo>();
		if(getFolder().isDisplayItems() && getFolder().getFileType() == FileType.VIDEO){
			List<DOVideoFileInfo> videoFiles = MediaLibraryStorage.getInstance().getVideoFileInfo(getFolder().getInheritedFilter(), fdp.isSortAscending(), fdp.getSortType(), folder.getMaxFiles(), fdp.getSortOption(), true);
			files = Arrays.asList(videoFiles.toArray(new DOFileInfo[videoFiles.size()]));
		}
		
		if (!add) {
			// the folders haven't changed, check the files
			for (DOFileInfo child : files) {
				if (pos < getChildren().size()){
					if(getChildren().get(pos) instanceof MediaLibraryRealFile) {
    					MediaLibraryRealFile dlnaFile = (MediaLibraryRealFile) getChildren().get(pos);
    					if (!dlnaFile.equals(new MediaLibraryRealFile(child, getFolder().getDisplayProperties(), getFolder().getFileType()))) {
    						// a file has changed
    						add = true;
    						break;
    					}
    					fileIndex++;
					}
				}else {
					//there's a new file
					add = true;
					break;
				}
				pos++;
			}
		}

		// remove nodes if needed
		while (pos < getChildren().size()) {
			getChildren().remove(pos);
			nodeRefreshed = true;
		}

		if (add) {
			// add folders if needed
			if (pos < getFolder().getChildFolders().size()) {
				for (int i = folderIndex; i < getFolder().getChildFolders().size(); i++) {
					addChildFolder(getFolder().getChildFolders().get(i));
					nodeRefreshed = true;
					pos++;
				}
			}

			// add files if needed
			if (MediaLibraryConfiguration.getInstance().isMediaLibraryEnabled() && getFolder().isDisplayItems() && files != null) {
				if (pos < getFolder().getChildFolders().size() + files.size()) {
					for (int i = fileIndex; i < files.size(); i++) {
						manageFile(files.get(i));
						nodeRefreshed = true;
						pos++;
					}
				}
			}
		}

		isUpdating = false;
		
		if(log.isDebugEnabled()) log.debug(String.format("Finished refreshing children for folder '%s' (%s). Refreshed=%s", getName(), getId(), nodeRefreshed));
	
		return nodeRefreshed;
	}
	
	/**
	 * This method decides if a zip, rar, iso, m3u or cue folder should be added
	 *
	 * @param fileInfo the file info to check
	 */
	private void manageFile(DOFileInfo fileInfo) {
		File f = new File(fileInfo.getFilePath());
		if ((f.isFile() || f.isDirectory()) && !f.isHidden()) {
			DLNAResource fileToAdd = new MediaLibraryRealFile(fileInfo, getFolder().getDisplayProperties(), getFolder().getFileType());
			
			if(getFolder().getDisplayProperties().getFileDisplayType() == FileDisplayType.FOLDER){
				// add the child as a MediaLibraryRealFile anyway when it has to be displayed as a folder
				addChild(fileToAdd);			
			} else {
				if (f.getName().toLowerCase().endsWith(".zip") || f.getName().toLowerCase().endsWith(".cbz")) {
					addChild(new ZippedFile(f));
				} else if (f.getName().toLowerCase().endsWith(".rar") || f.getName().toLowerCase().endsWith(".cbr")) {
					addChild(new RarredFile(f));
				} else if ((f.getName().toLowerCase().endsWith(".iso") || f.getName().toLowerCase().endsWith(".img")) || (f.isDirectory() && f.getName().toUpperCase().equals("VIDEO_TS"))) {
					addChild(new DVDISOFile(f));
				} else if (f.getName().toLowerCase().endsWith(".m3u") || f.getName().toLowerCase().endsWith(".m3u8") || f.getName().toLowerCase().endsWith(".pls")) {
					addChild(new PlaylistFolder(f));
				} else if (f.getName().toLowerCase().endsWith(".cue")) {
					addChild(new CueFolder(f));
				} else {
					addChild(fileToAdd);
				}
			}			
		}
	}

	/**
	 * *
	 * Updates the folder if the one retrieved from the DB is more recent then
	 * the used one.
	 *
	 * @return true if the folder has been updated
	 */
	private void updateFolder() {
		DOMediaLibraryFolder newFolder = MediaLibraryStorage.getInstance().getMediaLibraryFolder(getFolder().getId(), MediaLibraryStorage.ALL_CHILDREN);
		if(getFolder().getParentFolder() != null
				&& getFolder().getParentFolder().getChildFolders() != null
				&& getFolder().getParentFolder().getChildFolders().contains(getFolder())){
			getFolder().getParentFolder().getChildFolders().remove(getFolder());				
		}
		newFolder.setParentFolder(getFolder().getParentFolder());
		setFolder(newFolder);
	}

	/**
	 * Sets the folder.
	 *
	 * @param folder the new folder
	 */
	public void setFolder(DOMediaLibraryFolder folder) {
		name = folder.getName();
		this.folder = folder;
	}

	/**
	 * Gets the folder.
	 *
	 * @return the folder
	 */
	public DOMediaLibraryFolder getFolder() {
		return folder;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MediaLibraryFolder)) {
			return false;
		}

		MediaLibraryFolder compObj = (MediaLibraryFolder) obj;
		if (getFolder().equals(compObj.getFolder())) {
			return true;
		}
		return false;
	}

}

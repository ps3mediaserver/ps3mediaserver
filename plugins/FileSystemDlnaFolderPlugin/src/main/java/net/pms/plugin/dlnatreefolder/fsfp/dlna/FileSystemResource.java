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
package net.pms.plugin.dlnatreefolder.fsfp.dlna;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.dlna.CueFolder;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.DVDISOFile;
import net.pms.dlna.PlaylistFolder;
import net.pms.dlna.RarredFile;
import net.pms.dlna.RealFile;
import net.pms.dlna.ZippedFile;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.FormatFactory;

/**
 * The FileSystemResource contains 0-n shared folders.<br>
 * If nbFolders == 0 all drives will be shared<br>
 * If nbFolders == 1 this folder will be shared<br>
 * If nbFolders > 1 all folders will be merged into one
 */
public class FileSystemResource extends VirtualFolder {
	private static final Logger log = LoggerFactory.getLogger(FileSystemResource.class);
	private List<File> discoverable;
	private List<String> folderPaths = new ArrayList<String>();
	private boolean isRefreshing = false;

	/**
	 * Instantiates a new file system resource.
	 *
	 * @param name the name that will show up on the renderer
	 * @param folderPaths the paths of the folders to share
	 */
	public FileSystemResource(String name, List<String> folderPaths) {
	    super(name, null);
	    setFolderPaths(folderPaths);
    }
	
	/**
	 * Gets the folder paths.
	 *
	 * @return the folder paths
	 */
	public List<String> getFolderPaths(){
		return folderPaths;
	}
	
	/**
	 * Adds the folder path.
	 *
	 * @param path the path
	 */
	public void addFolderPath(String path){
		if(!folderPaths.contains(path)){
			folderPaths.add(path);
		}
	}
	
	/**
	 * Sets the folder paths.
	 *
	 * @param folderPaths the new folder paths
	 */
	public void setFolderPaths(List<String> folderPaths){
		this.folderPaths = folderPaths;
	}
	
	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#discoverChildren()
	 */
	@Override
	public void discoverChildren() {
		if (discoverable == null){
			discoverable = new ArrayList<File>();			
		}
		else{
			return;			
		}
		
		refreshChildren(true);
	}
	
	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#isRefreshNeeded()
	 */
	@Override
	public boolean isRefreshNeeded() {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#refreshChildren()
	 */
	@Override
	public boolean refreshChildren() {
		return refreshChildren(false);
	}

	/**
	 * Refresh children. For the first use, the shared files and
	 * folders found will be added to the list discoverable to
	 * speed up the process and read the files later. When
	 * refreshing a {@link FileSystemResource}, files which have
	 * been removed from the file system will disappear and added
	 * ones show up at the bottom of the list
	 *
	 * @param isFirstUse true if it is the first use
	 * @return true, if children have been refreshed
	 */
	private boolean refreshChildren(boolean isFirstUse) {
		synchronized (this) {
			if(isRefreshing) return false;
			isRefreshing = true;			
		}

		if(folderPaths == null){
			folderPaths = new ArrayList<String>();
		}

		List<File> rootFolders = Arrays.asList(File.listRoots());

		//Get the list of files and folders contained in the configured folders
		Map<String, String> mergeFolderPaths = new HashMap<String, String>(); // value=path, key=Name
		Map<String, String> mergeFilePaths = new HashMap<String, String>(); // value=path, key=Name
		
		if(folderPaths.size() == 0){
			//add all disks if no folder has been configured
			for(File f : rootFolders){
				mergeFolderPaths.put(f.getAbsolutePath(), f.getAbsolutePath());
			}
			log.info(String.format("Added all disks (%s) because no folders were configured for file system folder %s", mergeFolderPaths.size(), getName()));
		} else {
			//add the configured folder(s)
			for (String folderPath : folderPaths) {
				File dir = new File(folderPath);
				if (dir.isDirectory()) {
					for (String s : dir.list()) {
						File child = new File(dir.getAbsolutePath() + File.separatorChar + s);
						if (child.isDirectory()) {
							mergeFolderPaths.put(child.getAbsolutePath(), child.getName());
						} else if (child.isFile()) {
							mergeFilePaths.put(child.getAbsolutePath(), child.getName()); 
						}
					}
				}
			}
		}
		
		//merge the sorted lists
		List<String> allPaths = new ArrayList<String>(getSortedPaths(mergeFolderPaths));
		allPaths.addAll(getSortedPaths(mergeFilePaths));
		
		//Use the same algo as in RealFile
		ArrayList<DLNAResource> removedFiles = new ArrayList<DLNAResource>();
		ArrayList<File> addedFiles = new ArrayList<File>();

		int i = 0;
		for(String s : allPaths) {
			File f = new File(s);
			if (!f.isHidden() || rootFolders.contains(f)) {
				boolean present = false;
				for(DLNAResource d : getChildren()) {
					if (i == 0 && (!(d instanceof VirtualFolder) || (d instanceof DVDISOFile))) // specific for video_ts, we need to refresh it
						removedFiles.add(d);
					boolean video_ts_hack = (d instanceof DVDISOFile) && d.getName().startsWith(DVDISOFile.PREFIX) && d.getName().substring(DVDISOFile.PREFIX.length()).equals(f.getName());
					if ((d.getName().equals(f.getName()) || video_ts_hack) && ((d instanceof RealFile && d.isFolder()) || d.getLastmodified() == f.lastModified())) {
						removedFiles.remove(d);
						present = true;
					}
				}
				if (!present && (f.isDirectory() || rootFolders.contains(f) || FormatFactory.getAssociatedExtension(f.getName()) != null))
					addedFiles.add(f);
			}
			i++;
		}
		
		
		for(DLNAResource f:removedFiles) {
			getChildren().remove(f);
		}
		for(File f:addedFiles) {
			if(isFirstUse){
				discoverable.add(f);
			} else {
				addChild(new RealFile(f));
			}
		}

		synchronized (this) {
			isRefreshing = false;
		}
		
		return isFirstUse ? false : removedFiles.size() > 0 || addedFiles.size() > 0;
	}
	
	
	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#analyzeChildren(int)
	 */
	@Override
	public boolean analyzeChildren(int count) {
		int currentChildrenCount = getChildren().size();
		while ((getChildren().size() - currentChildrenCount) < count || count == -1) {
			if (discoverable.size() == 0) {
				break;
			}
			manageFile(discoverable.remove(0));
		}
		return discoverable.size() == 0;
	}

	/**
	 * Gets the paths sorted by folder or file name
	 *
	 * @param mergePaths the merge paths
	 * @return the sorted paths
	 */
	private Collection<String> getSortedPaths(Map<String, String> mergePaths) {
		List<String> res = new ArrayList<String>();
		
		//Sort the lists by folder or file name
		Entry<String, String>[] sortedFolders = getSortedHashtableEntries(mergePaths);
		for(Entry<String, String> entry : sortedFolders){
			res.add(entry.getKey());
		}
		
	    return res;
    }
	
	/**
	 * Loads a specific resource if the file is of type
	 * .zip, .cbz, .rar, .cbr, .iso, .img etc.
	 *
	 * @param f the file
	 */
	private void manageFile(File f) {
		List<File> rootFolders = Arrays.asList(File.listRoots());
		if ((f.isFile() || f.isDirectory()) && (!f.isHidden() || rootFolders.contains(f))) {
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
				
				/* Optionally ignore empty directories */
				if (f.isDirectory() && PMS.getConfiguration().isHideEmptyFolders() && !isFolderRelevant(f)) {					
					if(log.isInfoEnabled()) log.info("Ignoring empty/non relevant directory: " + f.getName());
				}
				
				/* Otherwise add the file */
				else {
					RealFile file = new RealFile(f);
					addChild(file);
				}
			}
		}
	}
	
	/**
	 * Checks if a folder is relevant.
	 *
	 * @param f the f
	 * @return true, if f is a folder containing playable files
	 */
	private boolean isFolderRelevant(File f) {

		boolean excludeNonRelevantFolder = true;
		if (f.isDirectory() && PMS.getConfiguration().isHideEmptyFolders()) {
			File children[] = f.listFiles();
			if (children == null) {
				log.warn("access denied reading " + f.getAbsolutePath());
				return false;
			}
			for (File child : children) {
				if (child.isFile()) {
					if (FormatFactory.getAssociatedExtension(child.getName()) != null || isFileRelevant(child)) {
						excludeNonRelevantFolder = false;
						break;
					}
				} else {
					if (isFolderRelevant(child)) {
						excludeNonRelevantFolder = false;
						break;
					}
				}
			}
		}

		return !excludeNonRelevantFolder;
	}
	
	/**
	 * Checks if is file relevant.
	 *
	 * @param f the file to check
	 * @return true, if is file relevant
	 */
	private boolean isFileRelevant(File f) {
		String fileName = f.getName().toLowerCase();
		return (PMS.getConfiguration().isArchiveBrowsing() && (fileName.endsWith(".zip") || fileName.endsWith(".cbz") ||
			fileName.endsWith(".rar") || fileName.endsWith(".cbr"))) ||
			fileName.endsWith(".iso") || fileName.endsWith(".img") || 
			fileName.endsWith(".m3u") || fileName.endsWith(".m3u8") || fileName.endsWith(".pls") || fileName.endsWith(".cue");
	}

	/**
	 * Helper method used to sort paths
	 *
	 * @param h the h
	 * @return the sorted hashtable entries
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Entry<String, String>[] getSortedHashtableEntries(Map<String, String> h) {
		Set<?> set = h.entrySet();
		Map.Entry[] entries = (Map.Entry[]) set.toArray(new Map.Entry[set.size()]);
		Arrays.sort(entries, new Comparator<Object>() {
			public int compare(Object o1, Object o2) {
				Object v1 = ((Map.Entry) o1).getValue();
				Object v2 = ((Map.Entry) o2).getValue();
				return ((Comparable<Object>) v1).compareTo(v2);
			}
		});
		return entries;
	}

}

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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.InputFile;
import net.pms.dlna.RealFile;
import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFile;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFolder;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryInfo;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryPlugin;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOThumbnailPriority;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.dataobjects.FileDisplayProperties;
import net.pms.medialibrary.commons.enumarations.FileDisplayType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.helpers.DLNAHelper;
import net.pms.medialibrary.commons.helpers.ImageHelper;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.pms.plugins.FileDetailPlugin;

/**
 * Use this class to show a file in the dlna tree. It will either show a 
 * single file or folder depending on the configuration
 */
public class MediaLibraryRealFile extends RealFile {
	private static final Logger log = LoggerFactory.getLogger(MediaLibraryRealFile.class);
	private DOFileInfo            fileInfo;
	private FileDisplayProperties displayProperties;
	private FileType              fileType;
	private DOFileEntryBase     fileBase;
	private RealFile originalFile;

	/**
	 * Instantiates a new media library real file.
	 *
	 * @param fileInfo the file info
	 * @param displayProperties the display properties
	 * @param fileType the file type
	 */
	public MediaLibraryRealFile(DOFileInfo fileInfo, FileDisplayProperties displayProperties, FileType fileType) {
		super(new File(fileInfo.getFilePath()));

		setFileType(fileType);
		setFileInfo(fileInfo);
		setDisplayProperties(displayProperties);
		
		originalFile = this;

		//set the base file if we are a folder
		if (displayProperties.getFileDisplayType() == FileDisplayType.FOLDER) {
			fileBase = MediaLibraryStorage.getInstance().getFileFolder(displayProperties.getTemplate().getId());
			originalFile = new RealFile(new File(fileInfo.getFilePath()));
			handleFileFolderDisplayNameMasks(fileBase);
		}
	}

	/**
	 * Instantiates a new media library real file.
	 *
	 * @param fileBase the file base
	 * @param fileInfo the file info
	 * @param displayProperties the display properties
	 * @param fileType the file type
	 */
	private MediaLibraryRealFile(DOFileEntryBase fileBase, DOFileInfo fileInfo, FileDisplayProperties displayProperties, FileType fileType, RealFile originalFile) {
		super(new File(fileInfo.getFilePath()));

		setFileType(fileType);
		setFileInfo(fileInfo);
		setDisplayProperties(displayProperties);
		
		this.fileBase = fileBase;
		this.originalFile = originalFile;
	}
	
	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#isTranscodeFolderAvailable()
	 */
	@Override
	public boolean isTranscodeFolderAvailable() {
		return false;
	}

	/**
	 * This method will replace the configurable string values with their actual
	 * value in all file entries and sub folders.
	 *
	 * @param file the file
	 */
	private void handleFileFolderDisplayNameMasks(DOFileEntryBase file) {
		file.setDisplayNameMask(fileInfo.getDisplayString(file.getDisplayNameMask()));
		if (file instanceof DOFileEntryFolder) {
			for (DOFileEntryBase entry : ((DOFileEntryFolder) file).getChildren()) {
				handleFileFolderDisplayNameMasks(entry);
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.RealFile#getName()
	 */
	@Override
	public String getName() {
		return fileBase == null ? fileInfo.getDisplayString(displayProperties.getDisplayNameMask()) : fileBase.getDisplayNameMask();
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.RealFile#isFolder()
	 */
	@Override
	public boolean isFolder() {
		return fileBase == null ? displayProperties.getFileDisplayType() == FileDisplayType.FOLDER : fileBase.getFileEntryType() == FileDisplayType.FOLDER;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.RealFile#resolve()
	 */
	@Override
	public void resolve() {
		if (displayProperties.getFileDisplayType() == FileDisplayType.FILE
				|| fileBase instanceof DOFileEntryFile) {
			switch (fileType) {
				case AUDIO:
					break;
				case FILE:
					break;
				case PICTURES:
					break;
				case VIDEO:
					setMedia(DLNAHelper.getMedia((DOVideoFileInfo) fileInfo));
					break;
			default:
				log.warn(String.format("Unhandled file type received (%s). This should never happen!", fileType));
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.MapFile#analyzeChildren(int)
	 */
	@Override
	public boolean analyzeChildren(int count) {
		return true;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.MapFile#discoverChildren()
	 */
	@Override
	public void discoverChildren() {
		if (fileBase != null && fileBase instanceof DOFileEntryFolder) {
			//show all children if we are a DOFileEntryFolder
			for (DOFileEntryBase entry : ((DOFileEntryFolder) fileBase).getChildren()) {
				if (entry instanceof DOFileEntryFolder) {
					//add a DOFileEntryFolder as child
					addChild(new MediaLibraryRealFile(entry, getFileInfo(), getDisplayProperties(), getFileType(), originalFile));
				} else if (entry instanceof DOFileEntryFile) {
					//add a file (either as single entry or as a transcode file selection)
					DOFileEntryFile file = (DOFileEntryFile) entry;
					MediaLibraryRealFile newChild = new MediaLibraryRealFile(entry, getFileInfo(), getDisplayProperties(), getFileType(), originalFile);
					switch (file.getFileDisplayMode()) {
						case MULTIPLE:
							DLNAHelper.addMultipleFiles(this, newChild, originalFile);
							break;
						case SINGLE:
							addChild(newChild);
							break;
					default:
						log.warn(String.format("Unhandled file disply mode received (%s). This should never happen!", file.getFileDisplayMode()));
						break;
					}
				} else if (entry instanceof DOFileEntryInfo) {
					//add a DOFileEntryInfo
					String[] filesToDisplay = DLNAHelper.getSplitLines(fileInfo.getDisplayString(entry.getDisplayNameMask()), entry.getMaxLineLength());
					for(String f : filesToDisplay){
						addChild(new MediaLibraryFileInfo(f, getCoverPath(entry.getThumbnailPriorities(), getFileInfo())));						
					}
				}else if (entry instanceof DOFileEntryPlugin && fileInfo instanceof DOVideoFileInfo) {
					//add a FileDetailPlugin
					FileDetailPlugin pl = ((DOFileEntryPlugin) entry).getPlugin();
					if(pl.isInstanceAvailable()){
						pl.setVideo((DOVideoFileInfo) fileInfo);
						pl.setDisplayName(fileInfo.getDisplayString(entry.getDisplayNameMask()));
						addChild(pl.getResource());
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.RealFile#getThumbnailInputStream()
	 */
	@Override
	public InputStream getThumbnailInputStream() throws IOException {
		String thumbPath;
		
		//determine if the source is a file folder entry or an actual file
		if(fileBase == null){
			thumbPath= getCoverPath(displayProperties.getThumbnailPriorities(), getFileInfo()); 
		}else{
			thumbPath = getCoverPath(fileBase.getThumbnailPriorities(), getFileInfo());
		}
		
		//show the default icon if no thumbnail has been found
		if(thumbPath == null){
			return getResourceInputStream("images/icon-256.png");
		}
		
		//return the scaled input stream in order to reduce the memory footprint used by pms.
		return ImageHelper.getScaledInputStream(thumbPath, 320, 240);
	}
	
	/**
	 * Gets the cover path. Depending on the thumnail prios, either a thumbnail will be generated, or a picture used.
	 *
	 * @param thumnailPrios the thumnail prios
	 * @param fileInfo the file info
	 * @return the absolute cover path or null if no cover could be found
	 */
	private String getCoverPath(List<DOThumbnailPriority> thumnailPrios, DOFileInfo fileInfo) {
		File coverFile = null;
		for (DOThumbnailPriority prio : thumnailPrios) {
			switch (prio.getThumbnailPriorityType()) {
			case GENERATED:
				String picFolderPath = MediaLibraryConfiguration.getInstance().getPictureSaveFolderPath();
				if(!picFolderPath.endsWith(File.separator)){
					picFolderPath += File.separator;
				}
				picFolderPath += "generated" + File.separator;
				String picName = fileInfo.getFileName() + "_" + prio.getSeekPosition() + ".cover.jpg";
				File pic = new File(picFolderPath + picName);
				if(pic.exists()){
					coverFile = pic;
				} else {
					InputFile inputFile = new InputFile();
					inputFile.setFile(getFile());
					getMedia().setThumbnailSeekPos(prio.getSeekPosition());
					getMedia().generateThumbnail(inputFile, getFormat(), getType());
					try {
						File picFolder = new File(picFolderPath);
						if(!picFolder.isDirectory()){
							picFolder.mkdirs();							
						}
						pic.createNewFile();
						InputStream inputStream = getMedia().getThumbnailInputStream();
						OutputStream out = new FileOutputStream(pic);
						byte buf[] = new byte[4096];
						int len;
						while ((len = inputStream.read(buf)) > 0)
							out.write(buf, 0, len);
						out.close();
						inputStream.close();
						
						coverFile = pic;
					} catch (IOException e) {
						log.error("Failed to save generated thumbnail to file", e);
					}					
				}
				break;
			case PICTURE:
				File tmpf = new File(fileInfo.getDisplayString(prio.getPicturePath()));
				if(tmpf.exists()){
					coverFile = tmpf;
				}
				break;
			case THUMBNAIL:
				tmpf = new File(fileInfo.getThumbnailPath());
				if(tmpf.exists()){
					coverFile = tmpf;
				}
				break;
			default:
				log.warn(String.format("Unhandled thumbnail priority type received (%s). This should never happen!", prio.getThumbnailPriorityType()));
				break;
			}
			
			if (coverFile != null) {
				break;
			}
		}
		
		return coverFile == null ? null : coverFile.getAbsolutePath();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof MediaLibraryRealFile)) { 
			return false; 
		}

		MediaLibraryRealFile compObj = (MediaLibraryRealFile) obj;
		if (getFileInfo().equals(compObj.getFileInfo()) 
				&& getDisplayProperties().equals(compObj.getDisplayProperties()) 
				&& getFileType().equals(compObj.fileType)) { 
			return true; 
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.MapFile#toString()
	 */
	@Override
	public String toString() {
		return getName();
	}
	
	/**
	 * Sets the file type.
	 *
	 * @param fileType the new file type
	 */
	public void setFileType(FileType fileType) {
		this.fileType = fileType;
	}

	/**
	 * Gets the file type.
	 *
	 * @return the file type
	 */
	public FileType getFileType() {
		return fileType;
	}

	/**
	 * Sets the display properties.
	 *
	 * @param displayProperties the new display properties
	 */
	public void setDisplayProperties(FileDisplayProperties displayProperties) {
		this.displayProperties = displayProperties;
	}

	/**
	 * Gets the display properties.
	 *
	 * @return the display properties
	 */
	public FileDisplayProperties getDisplayProperties() {
		return displayProperties;
	}

	/**
	 * Sets the file info.
	 *
	 * @param fileInfo the new file info
	 */
	public void setFileInfo(DOFileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	/**
	 * Gets the file info.
	 *
	 * @return the file info
	 */
	public DOFileInfo getFileInfo() {
		return fileInfo;
	}
}

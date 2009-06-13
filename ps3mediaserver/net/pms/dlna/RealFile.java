/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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
package net.pms.dlna;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

import net.pms.PMS;
import net.pms.dlna.virtual.TranscodeVirtualFolder;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.formats.Format;
import net.pms.util.FileUtil;
import net.pms.util.ProcessUtil;

public class RealFile extends DLNAResource {
	
	private String driveName;
	
	
	@Override
	public boolean isValid() {
		checktype();
		if (getType() == Format.VIDEO && file.exists() && file.getName().length() > 4) {
			srtFile = FileUtil.doesSubtitlesExists(file, null);
		}
		return file.exists() && (ext != null || file.isDirectory());
	}

	@Override
	public void discoverChildren() {
		super.discoverChildren();
		File files [] = getFileList();
		if (PMS.getConfiguration().getSortMethod() > 0) {
			Arrays.sort( files, new Comparator<File>()
			{
				public int compare(File o1, File o2) {
					return new Long(o2.lastModified()).compareTo(new Long(o1.lastModified()));
				}
			}); 
		} else
			Arrays.sort(files);
		for(File f:files) {
			if (f.isDirectory())
				manageFile(f);
		}
		for(File f:files) {
			if (f.isFile())
				manageFile(f);
		}
	}
	
	private File potentialCover;
	
	private boolean isFileRelevant(File f) {
		String fileName = f.getName().toLowerCase();
		return (PMS.getConfiguration().isArchiveBrowsing() && (fileName.endsWith(".zip") || fileName.endsWith(".cbz") ||
			fileName.endsWith(".rar") || fileName.endsWith(".cbr"))) ||
			fileName.endsWith(".iso") || fileName.endsWith(".img") || 
			fileName.endsWith(".m3u") || fileName.endsWith(".m3u8") || fileName.endsWith(".pls") || fileName.endsWith(".cue");
	}
	
	private boolean isFolderRelevant(File f) {
	
		boolean excludeNonRelevantFolder = true;
		if (f.isDirectory() && PMS.getConfiguration().isHideEmptyFolders()) {
			File children [] = f.listFiles();
			for(File child:children) {
				if (child.isFile()) {
					if (PMS.get().getAssociatedExtension(child.getName()) != null || isFileRelevant(child)) {
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
	
	private void manageFile(File f) {
		if ((f.isFile() || f.isDirectory()) && !f.isHidden()) {
			if (PMS.getConfiguration().isArchiveBrowsing() && (f.getName().toLowerCase().endsWith(".zip") || f.getName().toLowerCase().endsWith(".cbz"))) {
				addChild(new ZippedFile(f));
			} else if (PMS.getConfiguration().isArchiveBrowsing() && (f.getName().toLowerCase().endsWith(".rar") || f.getName().toLowerCase().endsWith(".cbr"))) {
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
					PMS.info("Ignoring empty/non relevant directory: " + f.getName());
				}
				
				/* Otherwise add the file */
				else {
					RealFile file = new RealFile(f);
					addChild(file);
				}
			}
		}
		if (f.isFile()) {
			String fileName = f.getName().toLowerCase();
			if (fileName.equalsIgnoreCase("folder.jpg") || fileName.equalsIgnoreCase("folder.png") || (fileName.contains("albumart") && fileName.endsWith(".jpg")))
				potentialCover = f;
		}
	}
	
	@Override
	public boolean refreshChildren() {
		File files [] = getFileList();
		ArrayList<File> addedFiles = new ArrayList<File>();
		ArrayList<DLNAResource> removedFiles = new ArrayList<DLNAResource>();
		int i = 0;
		for(File f:files) {
			if (!f.isHidden()) {
				boolean present = false;
				for(DLNAResource d:children) {
					if (i == 0 && (!(d instanceof VirtualFolder) || (d instanceof DVDISOFile))) // specific for video_ts, we need to refresh it
						removedFiles.add(d);
					boolean video_ts_hack = (d instanceof DVDISOFile) && d.getName().startsWith(DVDISOFile.PREFIX) && d.getName().substring(DVDISOFile.PREFIX.length()).equals(f.getName());
					if ((d.getName().equals(f.getName()) || video_ts_hack) && ((d instanceof RealFile && d.isFolder()) || d.lastmodified == f.lastModified())) { // && (!addcheck || (addcheck && d.lastmodified == f.lastModified()))
						removedFiles.remove(d);
						present = true;
					}
				}
				if (!present && (f.isDirectory() || PMS.get().getAssociatedExtension(f.getName()) != null))
					addedFiles.add(f);
			}
			i++;
		}
		
		for(DLNAResource f:removedFiles) {
			PMS.info("File automatically removed: " + f.getName());
		}
		
		for(File f:addedFiles) {
			PMS.info("File automatically added: " + f.getName());
		}
		

		TranscodeVirtualFolder vf = null;
		if (!PMS.getConfiguration().getHideTranscodeEnabled())
		{
			for(DLNAResource r:children) {
				if (r instanceof TranscodeVirtualFolder) {
					vf = (TranscodeVirtualFolder) r;
					break;
				}
			}
		}
		
		for(DLNAResource f:removedFiles) {
			children.remove(f);
			if (vf != null) {
				for(int j=vf.children.size()-1;j>=0;j--) {
					if (vf.children.get(j).getName().equals(f.getName()))
						vf.children.remove(j);
				}
			}
		}
		for(File f:addedFiles) {
			manageFile(f);
		}
		return removedFiles.size() != 0 || addedFiles.size() != 0;
	}

	private File[] getFileList() {
		File[] out = file.listFiles();
		if (out == null) {
			out = new File[0];
		}
		return out;
	}

	
	protected File file;
	
	public RealFile(File file) {
		this.file = file;
		lastmodified = file.lastModified();
	}

	public InputStream getInputStream() {
		try {
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	public long length() {
		if (player != null && player.type() != Format.IMAGE)
			return DLNAMediaInfo.TRANS_SIZE;
		else if (media != null && media.mediaparsed)
			return media.size;
		return file.length();
	}

	public String getName() {
		String name = null;
		if (file.getName().trim().equals("")) {
			if (PMS.get().isWindows()) {
				if (driveName == null) {
					driveName = PMS.get().getRegistry().getDiskLabel(file);
				}
			} 
			if (driveName != null && driveName.length() > 0)
				name = file.getAbsolutePath().substring(0, 1) + ":\\ [" + driveName + "]";
			else
				name = file.getAbsolutePath().substring(0, 1);
		}
		else {
			name = file.getName();
		}

		return name;
	}

	public boolean isFolder() {
		return file.isDirectory();
	}

	public File getFile() {
		return file;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return ProcessUtil.getShortFileNameIfWideChars(file.getAbsolutePath());
	}

	@Override
	public void resolve() {
		if (file.isFile() && file.exists()) {
			boolean found = false;
			InputFile input = new InputFile();
			input.file = file;
			String fileName = file.getAbsolutePath();
			if (splitTrack > 0)
				fileName += "#SplitTrack" + splitTrack; 
			if (PMS.getConfiguration().getUseCache()) {
				ArrayList<DLNAMediaInfo> medias = PMS.get().getDatabase().getData(fileName, file.lastModified());
				if (medias != null && medias.size() == 1) {
					media = medias.get(0);
					media.finalize(getType(), input);
					found = true;
				}
			}
			
			if (!found) {
				if (media == null) {
					media = new DLNAMediaInfo();
				}
				found = !media.mediaparsed && !media.parsing;
				if (ext != null) 
					ext.parse(media, input, getType());
				else //don't think that will ever happen
					media.parse(input, ext, getType());
				if (found && PMS.getConfiguration().getUseCache()) {
					PMS.get().getDatabase().insertData(fileName, file.lastModified(), getType(), media);
				}
			}
		}
		super.resolve();
	}

	@Override
	public String getThumbnailContentType() {
		return super.getThumbnailContentType();
	}

	@Override
	public InputStream getThumbnailInputStream() throws IOException {
		File cachedThumbnail = null;
		if (getParent() != null && getParent() instanceof RealFile) {
			cachedThumbnail = ((RealFile) getParent()).potentialCover;
			File thumbFolder = null;
			boolean alternativeCheck = false;
			while (cachedThumbnail == null) {
				if (thumbFolder == null)
					thumbFolder = file.getParentFile();
				cachedThumbnail = FileUtil.getFileNameWitNewExtension(thumbFolder, file, "jpg");
				if (cachedThumbnail == null)
					cachedThumbnail = FileUtil.getFileNameWitNewExtension(thumbFolder, file, "png");
				if (cachedThumbnail == null)
					cachedThumbnail = FileUtil.getFileNameWitAddedExtension(thumbFolder, file, ".cover.jpg");
				if (cachedThumbnail == null)
					cachedThumbnail = FileUtil.getFileNameWitAddedExtension(thumbFolder, file, ".cover.png");
				if (alternativeCheck)
					break;
				if (StringUtils.isNotBlank(PMS.getConfiguration().getAlternateThumbFolder())) {
					thumbFolder = new File(PMS.getConfiguration().getAlternateThumbFolder());
					if (!thumbFolder.exists() || !thumbFolder.isDirectory()) {
						thumbFolder = null;
						break;
					}
				}
				alternativeCheck = true;
			}
		}
		boolean hasAlreadyEmbeddedCoverArt = getType() == Format.AUDIO && media != null && media.thumb != null;
		if (cachedThumbnail != null && !hasAlreadyEmbeddedCoverArt)
			return new FileInputStream(cachedThumbnail);
		else if (media != null && media.thumb != null)
			return media.getThumbnailInputStream();
		else return super.getThumbnailInputStream();
	}

	@Override
	protected String getThumbnailURL() {
		if (getType() == Format.IMAGE) // no thumbnail support for now for real based disk images
			return null;
		StringBuffer sb = new StringBuffer();
		sb.append(PMS.get().getServer().getURL());
		sb.append("/");
		if (media != null && media.thumb != null)
			return super.getThumbnailURL();
		else if (getType() == Format.AUDIO) {
			if (getParent() != null && getParent() instanceof RealFile && ((RealFile) getParent()).potentialCover != null)
				return super.getThumbnailURL();
			return null;
		}
		return super.getThumbnailURL();
	}

}

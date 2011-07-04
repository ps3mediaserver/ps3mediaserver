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

import net.pms.PMS;
import net.pms.formats.Format;
import net.pms.util.FileUtil;
import net.pms.util.ProcessUtil;

import org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RealFile extends MapFile {
	private static final Logger logger = LoggerFactory.getLogger(RealFile.class);

	public RealFile(File file) {
		conf.getFiles().add(file);
		lastmodified = file.lastModified();
	}

	public RealFile(File file, String name) {
		conf.getFiles().add(file);
		conf.setName(name);
		lastmodified = file.lastModified();
	}

	@Override
	public boolean isValid() {
		File file = this.getFile();
		checktype();
		if (getType() == Format.VIDEO && file.exists() && PMS.getConfiguration().getUseSubtitles() && file.getName().length() > 4) {
			srtFile = FileUtil.doesSubtitlesExists(file, null);
		}
		boolean valid = file.exists() && (ext != null || file.isDirectory());

		if (valid && parent != null && parent.defaultRenderer != null && parent.defaultRenderer.isMediaParserV2()) {
			// we need to resolve the dlna resource now
			run();
			if (media != null && media.thumb == null && getType() != Format.AUDIO) // MediaInfo retrieves cover art now
			{
				media.thumbready = false;
			}
			if (media != null && (media.encrypted || media.container == null || media.container.equals(DLNAMediaLang.UND))) {
				// fine tuning: bad parsing = no file !
				valid = false;
				if (media.encrypted) {
					logger.info("The file " + file.getAbsolutePath() + " is encrypted. It will be hidden");
				} else {
					logger.info("The file " + file.getAbsolutePath() + " was badly parsed. It will be hidden");
				}
			}
			if (parent.defaultRenderer.isMediaParserV2ThumbnailGeneration()) {
				checkThumbnail();
			}
		}
		return valid;
	}

	@Override
	public InputStream getInputStream() {
		try {
			return new FileInputStream(getFile());
		} catch (FileNotFoundException e) {
		}
		return null;
	}

	@Override
	public long length() {
		if (player != null && player.type() != Format.IMAGE) {
			return DLNAMediaInfo.TRANS_SIZE;
		} else if (media != null && media.mediaparsed) {
			return media.size;
		}
		return getFile().length();
	}

	public boolean isFolder() {
		return getFile().isDirectory();
	}

	public File getFile() {
		return conf.getFiles().get(0);
	}

	@Override
	public String getName() {
		if (this.conf.getName() == null) {
			String name = null;
			File file = getFile();
			if (file.getName().trim().equals("")) {
				if (PMS.get().isWindows()) {
					name = PMS.get().getRegistry().getDiskLabel(file);
				}
				if (name != null && name.length() > 0) {
					name = file.getAbsolutePath().substring(0, 1) + ":\\ [" + name + "]";
				} else {
					name = file.getAbsolutePath().substring(0, 1);
				}
			} else {
				name = file.getName();
			}
			this.conf.setName(name);
		}
		return this.conf.getName();
	}

	@Override
	protected void checktype() {
		if (ext == null) {
			ext = PMS.get().getAssociatedExtension(getFile().getAbsolutePath());
		}

		super.checktype();
	}

	@Override
	public String getSystemName() {
		return ProcessUtil.getShortFileNameIfWideChars(getFile().getAbsolutePath());
	}

	@Override
	public void resolve() {
		File file = getFile();
		if (file.isFile() && file.exists() && (media == null || !media.mediaparsed)) {
			boolean found = false;
			InputFile input = new InputFile();
			input.file = file;
			String fileName = file.getAbsolutePath();
			if (splitTrack > 0) {
				fileName += "#SplitTrack" + splitTrack;
			}
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
				if (ext != null) {
					ext.parse(media, input, getType(), parent == null ? defaultRenderer : parent.defaultRenderer);
				} else //don't think that will ever happen
				{
					media.parse(input, ext, getType(), false);
				}
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
		File file = getFile();
		File cachedThumbnail = null;
		if (getParent() != null && getParent() instanceof RealFile) {
			cachedThumbnail = ((RealFile) getParent()).potentialCover;
			File thumbFolder = null;
			boolean alternativeCheck = false;
			while (cachedThumbnail == null) {
				if (thumbFolder == null && getType() != Format.IMAGE) {
					thumbFolder = file.getParentFile();
				}
				cachedThumbnail = FileUtil.getFileNameWitNewExtension(thumbFolder, file, "jpg");
				if (cachedThumbnail == null) {
					cachedThumbnail = FileUtil.getFileNameWitNewExtension(thumbFolder, file, "png");
				}
				if (cachedThumbnail == null) {
					cachedThumbnail = FileUtil.getFileNameWitAddedExtension(thumbFolder, file, ".cover.jpg");
				}
				if (cachedThumbnail == null) {
					cachedThumbnail = FileUtil.getFileNameWitAddedExtension(thumbFolder, file, ".cover.png");
				}
				if (alternativeCheck) {
					break;
				}
				if (StringUtils.isNotBlank(PMS.getConfiguration().getAlternateThumbFolder())) {
					thumbFolder = new File(PMS.getConfiguration().getAlternateThumbFolder());
					if (!thumbFolder.exists() || !thumbFolder.isDirectory()) {
						thumbFolder = null;
						break;
					}
				}
				alternativeCheck = true;
			}
			if (file.isDirectory()) {
				cachedThumbnail = FileUtil.getFileNameWitNewExtension(file.getParentFile(), file, "/folder.jpg");
				if (cachedThumbnail == null) {
					cachedThumbnail = FileUtil.getFileNameWitNewExtension(file.getParentFile(), file, "/folder.png");
				}
			}

		}
		boolean hasAlreadyEmbeddedCoverArt = getType() == Format.AUDIO && media != null && media.thumb != null;
		if (cachedThumbnail != null && (!hasAlreadyEmbeddedCoverArt || file.isDirectory())) {
			return new FileInputStream(cachedThumbnail);
		} else if (media != null && media.thumb != null) {
			return media.getThumbnailInputStream();
		} else {
			return super.getThumbnailInputStream();
		}
	}

	@Override
	public void checkThumbnail() {
		InputFile input = new InputFile();
		input.file = getFile();
		checkThumbnail(input);
	}

	@Override
	protected String getThumbnailURL() {
		if (getType() == Format.IMAGE) // no thumbnail support for now for real based disk images
		{
			return null;
		}
		StringBuilder sb = new StringBuilder();
		sb.append(PMS.get().getServer().getURL());
		sb.append("/");
		if (media != null && media.thumb != null) {
			return super.getThumbnailURL();
		} else if (getType() == Format.AUDIO) {
			if (getParent() != null && getParent() instanceof RealFile && ((RealFile) getParent()).potentialCover != null) {
				return super.getThumbnailURL();
			}
			return null;
		}
		return super.getThumbnailURL();
	}
}

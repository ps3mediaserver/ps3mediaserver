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
package net.pms.medialibrary.commons.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAResource;
import net.pms.dlna.FileTranscodeVirtualFolder;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.dlna.MediaLibraryRealFile;

public class DLNAHelper {
	private static final Logger log = LoggerFactory.getLogger(DLNAHelper.class);

	public static DLNAMediaInfo getMedia(DOVideoFileInfo video) {		
		DLNAMediaInfo dbMedia = new DLNAMediaInfo();
		dbMedia.setDuration(video.getDurationSec());
		dbMedia.setBitrate(video.getBitrate());
		dbMedia.setWidth(video.getWidth());
		dbMedia.setHeight(video.getHeight());
		dbMedia.setSize(video.getSize());
		dbMedia.setCodecV(video.getCodecV());
		dbMedia.setFrameRate(video.getFrameRate());
		dbMedia.setAspect(video.getAspectRatio() == null || video.getAspectRatio().equals("") ? null : video.getAspectRatio());
		dbMedia.setBitsPerPixel(video.getBitsPerPixel());
		dbMedia.setThumb(getThumb(video.getThumbnailPath()));
		dbMedia.setContainer(video.getContainer());
		dbMedia.setModel(video.getModel());
		dbMedia.setMediaparsed(true);

		dbMedia.setAudioTracksList(video.getAudioCodes());
		dbMedia.setSubtitleTracksList(video.getSubtitlesCodes());

		dbMedia.setDvdtrack(video.getDvdtrack());
		dbMedia.setH264AnnexB(video.getH264_annexB());
		dbMedia.setMimeType(video.getMimeType());
//		dbMedia.muxable = video.isMuxable();
		dbMedia.setParsing(false);
		dbMedia.setSecondaryFormatValid(true);
		dbMedia.setMuxingMode(video.getMuxingMode());

		return dbMedia;
	}	
	
	public static String[] getSplitLines(String input, int maxLineLength) {
		List<String> lines = new ArrayList<String>();
		if (maxLineLength > 0 && input.length() > maxLineLength) {
			int cutPos;
			do {
				cutPos = getCutOffPosition(input, maxLineLength);
				String text;
				if (cutPos > 0) {
					text = input.substring(0, cutPos).trim();
					input = input.substring(cutPos).trim();
				} else {
					text = input.trim();
					input = "";
				}

				lines.add(text);

			} while (cutPos > 0);
		} else {
			lines.add(input);
		}
		return lines.toArray(new String[lines.size()]);
	}

	private static int getCutOffPosition(String convertedMask, int maxLineLength) {
		int cutOffPos = -1;
		if (maxLineLength > 0 && convertedMask.length() > maxLineLength) {
			cutOffPos = maxLineLength;
			while (cutOffPos > 0 && convertedMask.charAt(cutOffPos) != ' ') {
				cutOffPos--;
			}
			if (cutOffPos == 0) {
				cutOffPos = maxLineLength;
			}
		}
		return cutOffPos;
	}

	private static byte[] getThumb(String thumbnailPath) {
		byte[] bytePic = new byte[0];

		// try to load picture
		File pic = new File(thumbnailPath);
		if (pic.exists()) {
			InputStream is;
			try {
				is = new FileInputStream(pic);
				int sz = is.available();
				if (sz > 0) {
					bytePic = new byte[sz];
					is.read(bytePic);
				}
				is.close();
			} catch (Exception e) {
				log.error("Failed to read image with path '" + thumbnailPath + "'", e);
			}
		}

		return bytePic;
	}

	public static String formatSecToHHMMSS(int secsIn) {
		int hours = secsIn / 3600, 
		remainder = secsIn % 3600, 
		minutes = remainder / 60, 
		seconds = remainder % 60;

		return ((hours < 10 ? "0" : "") + hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds);
	}

	public static void addMultipleFiles(DLNAResource parent, MediaLibraryRealFile child) {
		FileTranscodeVirtualFolder rootFolder = new FileTranscodeVirtualFolder("", null, false);
		rootFolder.setParent(parent);
		rootFolder.addChild(child.clone());
		rootFolder.resolve();
		//get the transcode folder which is hidden a bit deeper. this could break at some point but is an easy solution..
		rootFolder.getChildren().get(1).getChildren().get(0).resolve();
		DLNAResource transcodeFolder = rootFolder.getChildren().get(1).getChildren().get(0);
		
		for(DLNAResource r : transcodeFolder.getChildren()) {
			parent.addChild(r);
		}
	}
}

package net.pms.medialibrary.commons.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;
import net.pms.dlna.DLNAResource;
import net.pms.encoders.MEncoderVideo;
import net.pms.encoders.Player;
import net.pms.encoders.TSMuxerVideo;
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

		dbMedia.setAudioCodes(video.getAudioCodes());
		dbMedia.setSubtitlesCodes(video.getSubtitlesCodes());

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
		child.setParent(parent);
		child.resolve();
		if (child.isValid() && child.getExt().getProfiles() != null) {
			DLNAResource ref = child;
			Player tsMuxer = null;
			for (int i = 0; i < child.getExt().getProfiles().size(); i++) {
				Player pl = PMS.get().getPlayer(child.getExt().getProfiles().get(i), child.getExt());
				if (pl != null && !child.getPlayer().equals(pl)) {
					DLNAResource avisnewChild = (DLNAResource) child.clone();
					avisnewChild.setId(null);
					avisnewChild.setPlayer(pl);
					avisnewChild.setNoName(true);
					avisnewChild.setMedia(child.getMedia());
					if (avisnewChild.getPlayer().id().equals(MEncoderVideo.ID)) {
						ref = avisnewChild;
					}
					if (avisnewChild.getPlayer().id().equals(TSMuxerVideo.ID)) {
						tsMuxer = pl;
					}
					avisnewChild.setParent(parent);
				}
			}
			for (int i = 0; i < child.getMedia().getAudioCodes().size(); i++) {
				DLNAResource newChildNoSub = (DLNAResource) ref.clone();
				newChildNoSub.setId(null);
				newChildNoSub.setPlayer(ref.getPlayer());
				newChildNoSub.setMedia(ref.getMedia());
				newChildNoSub.setNoName(true);
				newChildNoSub.setMediaAudio(ref.getMedia().getAudioCodes().get(i));
				newChildNoSub.setMediaSubtitle(new DLNAMediaSubtitle());
				newChildNoSub.getMediaSubtitle().setId(-1);
				newChildNoSub.setParent(parent);

				for (int j = 0; j < child.getMedia().getSubtitlesCodes().size(); j++) {
					DLNAResource newChild = (DLNAResource) ref.clone();
					newChild.setId(null);
					newChild.setPlayer(ref.getPlayer());
					newChild.setMedia(ref.getMedia());
					newChild.setNoName(true);
					newChild.setMediaAudio(ref.getMedia().getAudioCodes().get(i));
					newChild.setMediaSubtitle(ref.getMedia().getSubtitlesCodes().get(j));
					newChild.setParent(parent);
				}
			}

			if (tsMuxer != null) {
				for (int i = 0; i < child.getMedia().getAudioCodes().size(); i++) {
					DLNAResource newChildNoSub = (DLNAResource) ref.clone();
					newChildNoSub.setId(null);
					newChildNoSub.setPlayer(tsMuxer);
					newChildNoSub.setMedia(ref.getMedia());
					newChildNoSub.setNoName(true);
					newChildNoSub.setMediaAudio(ref.getMedia().getAudioCodes().get(i));
					newChildNoSub.setParent(parent);

				}
			}

			// meskibob: I think it'd be a good idea to add a "Stream" option (for PS3 compatible containers) to the #Transcode# folder in addition to the current options already in there.
			DLNAResource justStreamed = (DLNAResource) ref.clone();
			if (justStreamed.getExt() != null && (justStreamed.getExt().ps3compatible() || justStreamed.isSkipTranscode())) {
				justStreamed.setId(null);
				justStreamed.setPlayer(null);
				justStreamed.setMedia(ref.getMedia());
				justStreamed.setNoName(true);
				justStreamed.setParent(parent);
			}
			}
	}
}

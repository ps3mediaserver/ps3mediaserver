package net.pms.util;

import java.io.File;
import java.io.IOException;

import net.pms.PMS;
import net.pms.dlna.DLNAMediaInfo;
import net.pms.dlna.DLNAMediaSubtitle;


public class FileUtil {
	
	public static File isFileExists(String f, String ext) {
		return isFileExists(new File(f), ext);
	}
	
	public static String getExtension(String f) {
		int point = f.lastIndexOf(".");
		if (point == -1) {
			return null;
		}
		return f.substring(point+1);
	}

	public static String getFileNameWithoutExtension(String f) {
		int point = f.lastIndexOf(".");
		if (point == -1) {
			point = f.length();
		}
		return f.substring(0, point);
	}
	
	public static File getFileNameWitNewExtension(File parent, File f, String ext) {
		File ff = isFileExists(new File(parent, getFileNameWithoutExtension(f.getName())), ext);
		if (ff !=null && ff.exists())
			return ff;
		return null;
	}
	
	public static File getFileNameWitAddedExtension(File parent, File f, String ext) {
		File ff = new File(parent, f.getName() + ext);
		if (ff.exists())
			return ff;
		return null;
	}
	
	public static File isFileExists(File f, String ext) {
		int point = f.getName().lastIndexOf(".");
		if (point == -1) {
			point = f.getName().length();
		}
		File lowerCasedFilename = new File(f.getParentFile(), f.getName().substring(0, point) + "." + ext.toLowerCase());
		if (lowerCasedFilename.exists())
			return lowerCasedFilename;
		
		File upperCasedFilename = new File(f.getParentFile(), f.getName().substring(0, point) + "." + ext.toUpperCase());
		if (upperCasedFilename.exists())
			return upperCasedFilename;
		
		return null;
	}
	
	public static boolean doesSubtitlesExists(File file, DLNAMediaInfo media) {
		boolean found = browseFolderForSubtitles(file.getParentFile(), file, media);
		if (PMS.getConfiguration().getAlternateSubsFolder() != null) {
			String alternate = PMS.getConfiguration().getAlternateSubsFolder();
			File subFolder = new File(alternate);
			if (!subFolder.isAbsolute()) {
				subFolder = new File(file.getParent() + "/" + alternate);
				try {
					subFolder = subFolder.getCanonicalFile();
				} catch (IOException e) {}
			}
			if (subFolder.exists()) {
				found = found || browseFolderForSubtitles(subFolder, file, media);
			}
		}
		return found;
	}
	
	private synchronized static boolean browseFolderForSubtitles(File subFolder, File file, DLNAMediaInfo media) {
		boolean found = false;
		File allSubs [] = subFolder.listFiles();
		String fileName = getFileNameWithoutExtension(file.getName()).toLowerCase();
		for(File f:allSubs) {
			if (f.isFile() && !f.isHidden()) {
				String fName = f.getName().toLowerCase();
				for(int i=0;i<DLNAMediaSubtitle.subExtensions.length;i++) {
					String ext = DLNAMediaSubtitle.subExtensions[i];
					if (!fName.startsWith(".") && fName.length() > ext.length() && fName.startsWith(fileName) && fName.endsWith("." + ext)) {
						String code = fName.substring(fileName.length(), fName.length()-ext.length()-1);
						if (code.startsWith("."))
							code = code.substring(1);
						if (Iso639.getCodeList().contains(code) || code.length() == 0) {
							if (media != null) {
								boolean exists = false;
								for(DLNAMediaSubtitle sub:media.subtitlesCodes) {
									if (f.equals(sub.file))
										exists = true;
									else if (i == 4 && sub.type == DLNAMediaSubtitle.MICRODVD) { // VOBSUB
										sub.type = DLNAMediaSubtitle.VOBSUB;
										exists = true;
									} else if (i == 1 && sub.type == DLNAMediaSubtitle.VOBSUB) { // VOBSUB
										sub.file = f;
										exists = true;
									}
								}
								if (!exists) {
									DLNAMediaSubtitle sub = new DLNAMediaSubtitle();
									sub.id = 100 + media.subtitlesCodes.size(); // fake id, not used
									sub.file = f;
									sub.checkUnicode();
									if (code.length() == 0) {
										sub.lang = "und";
										sub.type = i+1;
									} else {
										sub.lang = code;
										sub.type = i+1;
									}
									found = true;
									media.subtitlesCodes.add(sub);
								}
							} else
								return true;
						}
					}
				}
			}
		}
		return found;
	}
	/*
	private static int doesSubtitlesExists(File parent, File file, String code) {
		if (parent != null) {
			file = new File(parent, file.getName());
		}
		File srt = FileUtil.isFileExists(file, code + "srt");
		if (srt.exists()) {
			return DLNAMediaSubtitle.SUBRIP;
			break;
		}
			srt = FileUtil.isFileExists(file, "sub");
			srtFile = srt != null;
			if (!srtFile) {
				srt = FileUtil.isFileExists(file, "ass");
				srtFile = srt != null;
				if (!srtFile) {
					srt = FileUtil.isFileExists(file, "smi");
					srtFile = srt != null;
				}
			}
		}
	}
*/
}

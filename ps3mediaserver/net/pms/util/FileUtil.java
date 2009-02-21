package net.pms.util;

import java.io.File;


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
	
	public static boolean doesSubtitlesExists(File file) {
		File srt = FileUtil.isFileExists(file, "srt");
		boolean srtFile = srt != null;
		if (!srtFile) {
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
		return srtFile;
	}

}

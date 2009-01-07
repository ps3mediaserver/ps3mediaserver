package net.pms.util;

import java.io.File;

public class FileUtil {
	
	public static File isFileExists(String f, String ext) {
		return isFileExists(new File(f), ext);
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

}

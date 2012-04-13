package net.pms.medialibrary.commons.helpers;

import java.io.File;

import net.pms.PMS;

public class ConfigurationHelper {
	
	public static String getApplicationRootPath(){
		return System.getProperty("user.dir") + File.separator;
	}

	public static String getDbDir() {
		return PMS.getConfiguration().getProfileDirectory() + File.separatorChar + "db" + File.separatorChar;
	}
}

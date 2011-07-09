package net.pms.configuration;

import com.sun.jna.Platform;

// a one-stop class for values and methods specific to custom PMS builds
public class Build {
	/**
	 * The URL of the properties file used by the {@link AutoUpdater} to announce PMS updates.
	 * Can be null if the build doesn't support updating on any platform.
	 */
	private static final String UPDATE_SERVER_URL = "http://ps3mediaserver.googlecode.com/svn/trunk/ps3mediaserver/update/update_1.properties"; //$NON-NLS-1$

	// if false, both manual and automatic update checks are disabled
	private static final boolean IS_UPDATABLE = true;

	/*
	 * the name of the subdirectory under which PMS config files are stored for this build.
	 * the default value is "PMS" e.g.
	 *
	 *     Windows:
	 *
	 *         %ALLUSERSPROFILE%\PMS
	 *
	 *     Mac OS X:
	 *
	 *         /home/<username>/Library/Application Support/PMS
	 *
	 *     Linux &c.
	 *
	 *         /home/<username>/.config/PMS
	 *
	 * a custom build can change this to avoid interfering with the config files of other
	 * builds e.g.:
	 *
	 *     PROFILE_DIRECTORY_NAME = "PMS Rendr Edition";
	 *     PROFILE_DIRECTORY_NAME = "pms-mlx";
	 *
	 * Note: custom Windows builds that change this value should change the corresponding "$ALLUSERSPROFILE\PMS"
	 * value in nsis/setup.nsi
	 */

	private static final String PROFILE_DIRECTORY_NAME = "PMS"; //$NON-NLS-1$

	public static boolean isUpdatable() {
		return IS_UPDATABLE && Platform.isWindows() && UPDATE_SERVER_URL != null;
	}

	public static String getUpdateServerURL() {
		return UPDATE_SERVER_URL;
	}

	public static String getProfileDirectoryName() {
		return PROFILE_DIRECTORY_NAME; 
	}
}

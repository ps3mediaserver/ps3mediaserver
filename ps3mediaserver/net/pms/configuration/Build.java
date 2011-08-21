package net.pms.configuration;

import com.sun.jna.Platform;

import org.apache.commons.lang.StringUtils;

// a one-stop class for values and methods specific to custom PMS builds
public class Build {
	private static final String REPO = "http://ps3mediaserver.googlecode.com/svn/trunk/ps3mediaserver"; //$NON-NLS-1$

	/**
	 * The URL of the properties file used by the {@link AutoUpdater} to announce PMS updates.
	 * Can be null/empty if not used. Not used if IS_UPDATABLE is set to false.
	 */

	private static final String UPDATE_SERVER_URL = REPO + "/update/update_2.properties"; //$NON-NLS-1$

	// if false, manual and automatic update checks are unconditionally disabled
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
		return IS_UPDATABLE && Platform.isWindows() && getUpdateServerURL() != null;
	}

	public static String getUpdateServerURL() {
		return StringUtils.isNotBlank(UPDATE_SERVER_URL) ? UPDATE_SERVER_URL : null;
	}

	public static String getProfileDirectoryName() {
		return PROFILE_DIRECTORY_NAME; 
	}
}

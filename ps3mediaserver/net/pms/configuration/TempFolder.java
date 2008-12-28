package net.pms.configuration;

import java.io.File;
import java.io.IOException;

import net.pms.PMS;

import org.apache.commons.io.FileUtils;

/**
 * Handles finding a temporary folder.
 * 
 * @author Tim Cox (mail@tcox.org)
 */
public class TempFolder {

	private static final String DEFAULT_TEMP_FOLDER_NAME = "javaps3media";

	private final String userSpecifiedFolder;
	
	private File tempFolder;

	/**
	 * userSpecifiedFolder may be null
	 */
	public TempFolder(String userSpecifiedFolder) {
		this.userSpecifiedFolder = userSpecifiedFolder;
	}

	public synchronized File getTempFolder() throws IOException {
		if (tempFolder == null) {
			tempFolder = getTempFolder(userSpecifiedFolder);
		}
		
		return tempFolder;
	}

	private File getTempFolder(String userSpecifiedFolder) throws IOException {
		if (userSpecifiedFolder == null) {
			return getSystemTempFolder();
		}
		
		try {
			return getUserSpecifiedTempFolder(userSpecifiedFolder);
		} catch (IOException e) {
			PMS.error("Problem with user specified temp directory - using system", e);
			return getSystemTempFolder();
		}
	}

	private File getUserSpecifiedTempFolder(String userSpecifiedFolder) throws IOException {
		if (userSpecifiedFolder.isEmpty()) {
			throw new IOException("temporary folder path must not be empty if specified");
		}
		
		File folderFile = new File(userSpecifiedFolder);
		FileUtils.forceMkdir(folderFile);
		assertFolderIsValid(folderFile);
		return folderFile;
	}

	private static File getSystemTempFolder() throws IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir")); //$NON-NLS-1$
		File myTMP = new File(tmp, DEFAULT_TEMP_FOLDER_NAME); //$NON-NLS-1$
		FileUtils.forceMkdir(myTMP);
		assertFolderIsValid(myTMP);
		return myTMP;
	}

	private static void assertFolderIsValid(File folder) throws IOException {
		if (!folder.isDirectory()) {
			throw new IOException("Temp folder must be a folder: " + folder);
		}

		if (!folder.canWrite()) {
			throw new IOException("Temp folder is not writeable: " + folder);
		}
	}
}

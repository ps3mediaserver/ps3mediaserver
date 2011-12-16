package net.pms.medialibrary.commons.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {
	private static final Logger log = LoggerFactory.getLogger(FileHelper.class);

	public static void copyFile(String srFile, String dtFile) {
		try {
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			// For Overwrite the file.
			OutputStream out = new FileOutputStream(f2);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			if (log.isDebugEnabled()) log.debug(String.format("Copied file %s to %s", srFile, dtFile));
		} catch (FileNotFoundException ex) {
			log.error(String.format("Failed to copy file %s to %s", srFile, dtFile), ex);
		} catch (IOException e) {
			log.error(String.format("Failed to copy file %s to %s", srFile, dtFile), e);
		}
	}

}

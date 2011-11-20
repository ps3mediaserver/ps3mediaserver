/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2010  A.Brochard
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
package net.pms.logging;

import java.io.File;

import ch.qos.logback.core.PropertyDefinerBase;

/**
 * Logback PropertyDefiner to set the path for the <code>debug.log</code> file.
 * <p>
 * If the current working directory is writable it returns an empty string. If
 * not then the path to the system temp directory is returned.
 * </p>
 * <p>
 * This is equivalent to the old behavior of PMS.
 * </p>
 * 
 * @see System#getProperty(String)
 * @author thomas@innot.de
 * 
 */
public class DebugLogPathDefiner extends PropertyDefinerBase {
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.qos.logback.core.spi.PropertyDefiner#getPropertyValue()
	 */
	@Override
	public String getPropertyValue() {

		// Check if current directory is writable
		File file = new File("write_test_file");
		try {
			file.createNewFile();
			if (file.canWrite()) {
				file.delete();
				return new File("").getAbsolutePath();
			}
		} catch (Exception e) {
			// Could not create / write the file
		}

		// Return path to temp folder, which should be writeable
		return System.getProperty("java.io.tmpdir");
	}
}

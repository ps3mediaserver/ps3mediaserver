/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
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
package net.pms.dlna;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.pms.PMS;


import de.innosystec.unrar.Archive;
import de.innosystec.unrar.exception.RarException;
import de.innosystec.unrar.rarfile.FileHeader;

public class RarredFile extends DLNAResource {
	
	private File f;
	private Archive rarFile;
	
	public RarredFile(File f) {
		this.f = f;
		try {
			rarFile = new Archive(f);
			List<FileHeader> headers = rarFile.getFileHeaders();
			for(FileHeader fh:headers) {
				if (fh.getFullUnpackSize() < MAX_ARCHIVE_ENTRY_SIZE)
					addChild(new RarredEntry(fh.getFileNameString(), rarFile, fh));
			}
			//rarFile.close();
		} catch (RarException e) {
			PMS.error(null, e);
		} catch (IOException e) {
			PMS.error(null, e);
		}
	}

	public InputStream getInputStream() throws IOException {
		return new FileInputStream(f);
	}

	public String getName() {
		return f.getName();
	}

	public long length() {
		return f.length();
	}

	public boolean isFolder() {
		return true;
	}
	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return f.getAbsolutePath();
	}

	@Override
	public boolean isValid() {
		return f.exists();
	}

}

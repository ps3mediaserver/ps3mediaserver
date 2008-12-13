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

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.pms.formats.Format;


public class ZippedEntry extends DLNAResource {
	
	@Override
	protected String getThumbnailURL() {
		if (getType() == Format.IMAGE || getType() == Format.AUDIO) // no thumbnail support for now for real based disk images
			return null;
		return super.getThumbnailURL();
	}

	private ZipFile z;
	private ZipEntry ze;
	
	public ZippedEntry(ZipFile z, ZipEntry ze) {
		this.ze = ze;
		this.z = z;
	}

	public InputStream getInputStream() {
		try {
			return z.getInputStream(ze);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getName() {
		return ze.getName();
	}

	public long length() {
		return ze.getSize();
	}

	public DLNAResource[] listFiles() {
		return new DLNAResource [0];
	}

	public boolean isFolder() {
		return false;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return ze.getName();
	}

	@Override
	public boolean isValid() {
		checktype();
		return ext != null;
	}
}

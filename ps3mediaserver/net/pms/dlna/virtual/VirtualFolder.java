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
package net.pms.dlna.virtual;

import java.io.IOException;
import java.io.InputStream;

import net.pms.dlna.DLNAResource;
import net.pms.network.HTTPResource;

public class VirtualFolder extends DLNAResource {
	
	protected String name;
	private String thumbnailIcon;
	private String thumbnailContentType;
	
	public VirtualFolder(String name, String thumbnailIcon) {
		this.name = name;
		this.thumbnailIcon = thumbnailIcon;
		if (thumbnailIcon != null && thumbnailIcon.toLowerCase().endsWith(".png"))
			thumbnailContentType = HTTPResource.PNG_TYPEMIME;
		else
			thumbnailContentType = HTTPResource.JPEG_TYPEMIME;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public long length() {
		return 0;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return getName();
	}
	
	@Override
	public InputStream getThumbnailInputStream() {
		return getResourceInputStream(thumbnailIcon);
	}
	
	@Override
	public String getThumbnailContentType() {
		return thumbnailContentType;
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	/*public void setExpertMode() {
		expert = true;
	}*/
	
}

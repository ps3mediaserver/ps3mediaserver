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

import net.pms.PMS;


public class SkipLoopAction extends VirtualFolder {
	

	@Override
	public void discoverChildren() {
		boolean enable = getName().contains("Enable");
		PMS.get().setSkipLoopFilter(enable);
		super.discoverChildren();
	}

	public SkipLoopAction(String name, String thumbnailIcon) {
		super(name, thumbnailIcon);
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public String getName() {
		return super.getName();
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
	
	public InputStream getThumbnailInputStream() {
		return super.getThumbnailInputStream();
	}
	
	public void action() throws IOException {
		
	}
}

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
import java.io.InputStream;

import net.pms.PMS;
import net.pms.newgui.LooksFrame;



public class RootFolder extends DLNAResource {
	
	private boolean running;
	
	public RootFolder() {
		id = "0";
	}
	
	public void browse(File startFolders []) {
		for(File f:startFolders) {
			addChild(new RealFile(f));
		}
	}

	@Override
	public InputStream getInputStream() {
		return null;
	}

	@Override
	public String getName() {
		return "root";
	}

	@Override
	public boolean isFolder() {
		return true;
	}

	@Override
	public long lastModified() {
		return 0;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return getName();
	}

	@Override
	public boolean isValid() {
		return true;
	}
	
	public void scan() {
		running = true;
		scan(this);
		((LooksFrame) PMS.get().getFrame()).getFt().setScanLibraryEnabled(true);
		PMS.get().getDatabase().cleanup();
		((LooksFrame) PMS.get().getFrame()).setStatusLine(null);
	}
	
	public void stopscan() {
		running = false;
	}
	
	private void scan(DLNAResource resource) {
		if (running) {
			for(DLNAResource child:resource.children) {
				if (running && child instanceof RealFile && child.isFolder()) {
					String trace = "Scanning Folder: " + ((RealFile) child).file.getAbsolutePath();
					PMS.info(trace);
					((LooksFrame) PMS.get().getFrame()).setStatusLine(trace);
					if (child.discovered) {
						child.refreshChildren();
						child.closeChildren(child.childrenNumber(), true);
					} else {
						child.discoverChildren();
						child.closeChildren(0, false);
						child.discovered = true;
					}
					for(DLNAResource ch:child.children) {
						if (running)
							ch.resolve();
					}
					scan(child);
				}
			}
		}
	}

}

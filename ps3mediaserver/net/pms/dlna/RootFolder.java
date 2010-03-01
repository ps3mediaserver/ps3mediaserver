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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;

import net.pms.PMS;
import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.virtual.VirtualFolder;
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
		refreshChildren();
		defaultRenderer = RendererConfiguration.getDefaultConf();
		scan(this);
		((LooksFrame) PMS.get().getFrame()).getFt().setScanLibraryEnabled(true);
		PMS.get().getDatabase().cleanup();
		PMS.get().getFrame().setStatusLine(null);
	}
	
	public void stopscan() {
		running = false;
	}
	
	private synchronized void scan(DLNAResource resource) {
		if (running) {
			/*for(DLNAResource child:resource.children) {
				if (running && child instanceof RealFile && child.isFolder()) {
					child.defaultRenderer = resource.defaultRenderer;
					String trace = "Scanning Folder: " + ((RealFile) child).file.getAbsolutePath();
					PMS.info(trace);
					PMS.get().getFrame().setStatusLine(trace);
					if (child.discovered) {
						child.refreshChildren();
						child.closeChildren(child.childrenNumber(), true);
					} else {
						child.discoverChildren();
						child.analyzeChildren(-1);
						child.closeChildren(0, false);
						child.discovered = true;
					}
					int count = child.children.size();
					if (count == 0)
						continue;
					scan(child);
					child.children.clear();
				}
			}*/
			// No recursive calls anymore, seems it make mediainfo crash on Linux
			ArrayList<DLNAResource> toScan = new ArrayList<DLNAResource>();
			toScan.addAll(resource.children);
			while ( running && toScan.size() > 0) {
				DLNAResource child = toScan.remove(0);
				if (running && child instanceof RealFile && child.isFolder()) {
					child.defaultRenderer = resource.defaultRenderer;
					String trace = "Scanning Folder: " + ((RealFile) child).file.getAbsolutePath();
					PMS.info(trace);
					PMS.get().getFrame().setStatusLine(trace);
					if (child.discovered) {
						child.refreshChildren();
						child.closeChildren(child.childrenNumber(), true);
					} else {
						child.discoverChildren();
						child.analyzeChildren(-1);
						child.closeChildren(0, false);
						child.discovered = true;
					}
					int count = child.children.size();
					if (count == 0)
						continue;
					toScan.addAll(0, child.children);
				}
			}
		}
	}

	@Override
	public boolean refreshChildren() {
		boolean refreshed = false;
		File files[] = null;
		try {
			files = PMS.get().loadFoldersConf(PMS.getConfiguration().getFolders());
			if (files == null || files.length == 0)
				files = File.listRoots();
			int i = 0;
			ArrayList<DLNAResource> removedFiles = new ArrayList<DLNAResource>();
			ArrayList<File> addedFiles = new ArrayList<File>();
			
			for(File f:files) {
				boolean present = false;
				for(DLNAResource d:children) {
					if (i == 0 && !(d instanceof VirtualFolder) ) {
						removedFiles.add(d);
					}
					if (d instanceof RealFile && f.exists() && ((RealFile)d).file.getAbsolutePath().equals(f.getAbsolutePath())) {
						removedFiles.remove(d);
						present = true;
					}
				}
				if (!present)
					addedFiles.add(f);
				i++;
			}
			for(DLNAResource f:removedFiles) {
				children.remove(f);
			}
			for(File f:addedFiles) {
				addChild(new RealFile(f));
			}
			refreshed = removedFiles.size() > 0 || addedFiles.size() > 0;
		} catch (IOException e) {}
		
		
		return refreshed;
	}

}

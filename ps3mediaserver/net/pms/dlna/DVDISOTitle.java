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

import net.pms.PMS;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;

public class DVDISOTitle extends DLNAResource {
	
	@Override
	public void resolve() {
		String cmd [] = new String [] { PMS.get().getMPlayerPath(), "-identify", "-endpos", "0", "-v", "-ao", "null", "-vc", "null", "-vo", "null", "-dvd-device", f.getAbsolutePath(), "dvd://"+title };
		OutputParams params = new OutputParams(PMS.configuration);
		params.maxBufferSize = 1;
		params.log = true;
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmd, params);
		pw.run();
		ArrayList<String> lines = pw.getOtherResults();
		
		String duration = null;
		int nbsectors = 0;
		String fps = null;
		String aspect = null;
		String width = null;
		String height = null;
		ArrayList<DLNAMediaLang> audio = new ArrayList<DLNAMediaLang>();
		ArrayList<DLNAMediaLang> subs = new ArrayList<DLNAMediaLang>();
		int maxsubid = 0;
		for(String line:lines) {
			
			if (line.startsWith("DVD start=")) {
				nbsectors = Integer.parseInt(line.substring(line.lastIndexOf("=")+1).trim());
			}
			if (line.startsWith("audio stream:")) {
				DLNAMediaLang lang = new DLNAMediaLang();
				lang.id = Integer.parseInt(line.substring(line.indexOf("aid: ")+5, line.lastIndexOf(".")).trim());
				lang.lang = line.substring(line.indexOf("language: ")+10, line.lastIndexOf(" aid")).trim();
				lang.format = line.substring(line.indexOf("format: ")+8, line.lastIndexOf(" langu")).trim();
				
				audio.add(lang);
			}
			if (line.startsWith("subtitle")) {
				DLNAMediaLang lang = new DLNAMediaLang();
				lang.id = Integer.parseInt(line.substring(line.indexOf("): ")+3, line.lastIndexOf("language")).trim());
				if (lang.id > maxsubid)
					maxsubid = lang.id;
				lang.lang = line.substring(line.indexOf("language: ")+10).trim();
				subs.add(lang);
			}
			if (line.startsWith("ID_VIDEO_WIDTH=")) {
				width = line.substring(line.indexOf("ID_VIDEO_WIDTH=")+15).trim();
			}
			if (line.startsWith("ID_VIDEO_HEIGHT=")) {
				height = line.substring(line.indexOf("ID_VIDEO_HEIGHT=")+16).trim();
			}
			if (line.startsWith("ID_VIDEO_FPS=")) {
				fps = line.substring(line.indexOf("ID_VIDEO_FPS=")+13).trim();
			}
			if (line.startsWith("ID_LENGTH=")) {
				duration = line.substring(line.indexOf("ID_LENGTH=")+10).trim();
			}
			if (line.startsWith("ID_VIDEO_ASPECT=")) {
				aspect = line.substring(line.indexOf("ID_VIDEO_ASPECT=")+16).trim();
			}
		}
		
		length = nbsectors * 2048;
	
		double d = 0;
		if (duration != null)
			d = Double.parseDouble(duration);
		
		
			media.audioCodes = audio;
			media.subtitlesCodes = subs;
			
			if (duration != null)
				 media.setDurationString(d);
			media.frameRate = fps;
			media.aspect = aspect;
			media.maxsubid = maxsubid;
			media.dvdtrack = title;
			if (width != null && height != null)
				media.resolution = width + "x" + height;
			media.mediaparsed = true;
			
		
		super.resolve();
	}

	private File f;
	private int title;
	private long length;
	
	public long getLength() {
		return length;
	}

	public DVDISOTitle(File f, int title) {
		this.f = f;
		this.title = title;

	}

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return "Title " + title;
	}

	@Override
	public String getSystemName() {
		return f.getAbsolutePath();
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean isValid() {
		if (ext == null)
			ext = PMS.get().getAssociatedExtension("dummy.iso");
		return true;
	}

	@Override
	public long lastModified() {
		return 0;
	}

	@Override
	public long length() {
		return DLNAMediaInfo.TRANS_SIZE;
	}
	
	public InputStream getThumbnailInputStream() throws IOException {
		return getResourceInputStream("images/cdrwblank-256.png");
	}

}

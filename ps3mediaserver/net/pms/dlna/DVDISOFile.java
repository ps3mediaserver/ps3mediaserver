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
import java.util.ArrayList;

import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;

public class DVDISOFile extends VirtualFolder {
	
	
	
	public void init() {
		
	
		double titles [] = new double [99];
		String cmd [] = new String [] { PMS.get().getMPlayerPath(), "-identify", "-endpos", "0", "-v", "-ao", "null", "-vc", "null", "-vo", "null", "-dvd-device", f.getAbsolutePath(), "dvd://1" };
		OutputParams params = new OutputParams();
		params.maxBufferSize = 1;
		params.log = true;
		ProcessWrapperImpl pw = new ProcessWrapperImpl(cmd, params);
		pw.run();
		ArrayList<String> lines = pw.getOtherResults();
		for(String line:lines) {
			if (line.startsWith("ID_DVD_TITLE_") && line.contains("_LENGTH")) {
				int rank = Integer.parseInt(line.substring(13, line.indexOf("_LENGT")));
				double duration = Double.parseDouble(line.substring(line.lastIndexOf("LENGTH=")+7));
				titles[rank] = duration;
			}
		}
		
		double oldduration = -1;
		
		for(int i=1;i<99;i++) {
			if (titles[i] > 5 && titles[i] != oldduration) {
				DVDISOTitle dvd = new DVDISOTitle(f, i);
				addChild(dvd);
				oldduration = titles[i];
			}
		}
		
	}

	private File f;
	
	public DVDISOFile(File f) {
		super("[DVD ISO] " + (f.isFile()?f.getName():"VIDEO_TS"), null);
		this.f = f;
		init();
	}

}

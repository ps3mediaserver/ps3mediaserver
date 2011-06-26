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
package net.pms.network;

import net.pms.PMS;

public class HTMLConsole {
	public static String servePage(String resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html><head><title>PS3 Media Server HTML Console</title></head><body>");

		if (resource.equals("compact") && PMS.getConfiguration().getUseCache()) {
			PMS.get().getDatabase().compact();
			sb.append("<p align=center><b>Database compacted!</b></p><br>");
		}

		if (resource.equals("scan") && PMS.getConfiguration().getUseCache()) {
			if (!PMS.get().getDatabase().isScanLibraryRunning()) {
				PMS.get().getDatabase().scanLibrary();
			}
			if (PMS.get().getDatabase().isScanLibraryRunning()) {
				sb.append("<p align=center><b>Scan in progress! you can also <a href=\"stop\">stop it</a></b></p><br>");
			}
		}

		if (resource.equals("stop") && PMS.getConfiguration().getUseCache() && PMS.get().getDatabase().isScanLibraryRunning()) {
			PMS.get().getDatabase().stopScanLibrary();
			sb.append("<p align=center><b>Scan stopped!</b></p><br>");
		}

		sb.append("<p align=center><img src='/images/thumbnail-256.png'><br>PS3 Media Server HTML console<br><br>Menu:<br>");
		sb.append("<a href=\"home\">Home</a><br>");
		sb.append("<a href=\"scan\">Scan folders</a><br>");
		sb.append("<a href=\"compact\">Shrink media library database (not recommended)</a>");
		sb.append("</p></body></html>");
		return sb.toString();
	}
}

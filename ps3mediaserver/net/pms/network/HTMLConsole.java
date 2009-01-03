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
		StringBuffer sb = new StringBuffer();
		sb.append("<html><title>PS3 Media Server HTML Console</title></html><body>");
		if (resource.equals("compact") && PMS.get().isUsecache()) {
			PMS.get().getDatabase().compact();
			sb.append( "<p align=center><b>Database compacted!</b></p><br>" );
		} 
		
		sb.append("<p align=center><img src='/images/Play1Hot_256.png'><br>PS3 Media Server HTML console<br><br>Menu:<br><a href=\"compact\">Compact media library</a></p>");
		sb.append("</body></html>");
		return sb.toString();
	}

}

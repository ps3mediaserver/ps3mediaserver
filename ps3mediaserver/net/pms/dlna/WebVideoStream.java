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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;

import net.pms.PMS;
import net.pms.formats.Format;

public class WebVideoStream extends WebStream {

	@Override
	public InputStream getInputStream(long low, long high, double timeseek) throws IOException {
		if (URL.toLowerCase().indexOf("youtube") > -1 && URL.toLowerCase().indexOf("?") > -1) {
			try {
				InputStream is = downloadAndSend(URL, false);
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				int n = -1;
				byte buffer [] = new byte [4096];
				while( (n=is.read(buffer))> -1) {
					bout.write(buffer, 0, n);
				}
				is.close();
				String content = new String(bout.toByteArray());
				int fs = content.indexOf("/watch_fullscreen?");
				String newURL = "http://www.youtube.com/get_video%3F";
				if (fs > -1) {
					String seq = content.substring(fs+18, content.indexOf("'", fs));
					seq = seq.trim();
					StringTokenizer st = new StringTokenizer(seq, "&");
					while (st.hasMoreTokens()) {
						String elt = st.nextToken();
						if (elt.startsWith("video_id="))
							newURL += elt.replace("=", "%3D") + "%26";
						else if (elt.startsWith("t="))
							newURL += elt.replace("=", "%3D");
					}
					URL = newURL;
				}
			} catch (IOException e) {
				PMS.error(null, e);
			}
		}
		return super.getInputStream(low, high, timeseek);
	}
	

	public WebVideoStream(String fluxName, String URL, String thumbURL) {
		super(fluxName, URL, thumbURL, Format.VIDEO);
	}


}

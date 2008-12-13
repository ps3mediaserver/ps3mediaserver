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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import net.pms.PMS;
import net.pms.formats.Format;

public class HTTPResource {
	
	public static final String UNKNOWN_VIDEO_TYPEMIME = "video/mpeg";
	public static final String UNKNOWN_IMAGE_TYPEMIME = "image/jpeg";
	public static final String UNKNOWN_AUDIO_TYPEMIME = "audio/mpeg";
	
	public static final String AUDIO_MP3_TYPEMIME = "audio/mpeg";
	public static final String AUDIO_MP4_TYPEMIME = "audio/mp4";
	public static final String AUDIO_WAV_TYPEMIME = "audio/wav";
	
	public static final String MPEG_TYPEMIME = "video/mpeg";
	public static final String MP4_TYPEMIME = "video/mp4";
	public static final String AVI_TYPEMIME = "video/avi";
	
	public static final String PNG_TYPEMIME = "image/png";
	public static final String JPEG_TYPEMIME = "image/jpeg";
	public static final String TIFF_TYPEMIME = "image/tiff";
	public static final String GIF_TYPEMIME = "image/gif";
	public static final String BMP_TYPEMIME = "image/bmp";
	
	public HTTPResource() {
		
	}
	
	public String getDefaultMimeType(int type) {
		String mimeType = HTTPResource.UNKNOWN_VIDEO_TYPEMIME;
		if (type == Format.VIDEO)
			mimeType = HTTPResource.UNKNOWN_VIDEO_TYPEMIME;
		else if (type == Format.IMAGE)
			mimeType = HTTPResource.UNKNOWN_IMAGE_TYPEMIME;
		else if (type == Format.AUDIO)
			mimeType = HTTPResource.UNKNOWN_AUDIO_TYPEMIME;
		return mimeType;
	}
	
	
	protected InputStream getResourceInputStream(String fileName) {
		fileName = "/resources/" + fileName;
		ClassLoader cll = this.getClass().getClassLoader();
		InputStream is = cll.getResourceAsStream(fileName.substring(1));
		while (is == null && cll.getParent() != null) {
			cll = cll.getParent();
			is = cll.getResourceAsStream(fileName.substring(1));
		}
		return is;
	}
	
	protected InputStream downloadAndSend(String u, boolean saveOnDisk) throws IOException {
		URL url = new URL(u);
		File f = null;
		if (saveOnDisk) {
			String host = url.getHost();
			String hostName = convertURLToFileName(host);
			String fileName = url.getFile();
			fileName = convertURLToFileName(fileName);
			File hostDir = new File(PMS.get().getTempFolder(), hostName);
			hostDir.mkdir();
			f = new File(hostDir, fileName);
			if (f.exists())
				return new FileInputStream(f);
		}
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		URLConnection conn = url.openConnection();
		InputStream in = conn.getInputStream();
		FileOutputStream fOUT = null;
		if (saveOnDisk && f != null) {
			//fileName = convertURLToFileName(fileName);
			fOUT = new FileOutputStream(f);
		}
		byte buf [] = new byte [4096];
		int n = -1;
		while ((n=in.read(buf)) > -1) {
			bytes.write(buf, 0, n);
			if (fOUT != null)
				fOUT.write(buf, 0, n);
		}
		in.close();
		if (fOUT != null)
			fOUT.close();
		return new ByteArrayInputStream(bytes.toByteArray());
	}
	
	protected String convertURLToFileName(String url) {
		url = url.replace('/', 'µ');
		url = url.replace('\\', 'µ');
		url = url.replace(':', 'µ');
		url = url.replace('?', 'µ');
		url = url.replace('*', 'µ');
		url = url.replace('|', 'µ');
		url = url.replace('<', 'µ');
		url = url.replace('>', 'µ');
		return url;
	}

}

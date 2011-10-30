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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import net.pms.network.HTTPResourceAuthenticator;

public class WebStream extends DLNAResource {
	@Override
	public boolean isValid() {
		checktype();
		return getExt() != null;
	}

	protected String url;
	protected String fluxName;
	protected String thumbURL;

	public WebStream(String fluxName, String url, String thumbURL, int type) {
		super(type);

		try {
			URL tmpUrl = new URL(url);
			tmpUrl = HTTPResourceAuthenticator.concatenateUserInfo(tmpUrl);
			this.url = tmpUrl.toString();
		} catch (MalformedURLException e) {
			this.url = url;
		}

		try {
			URL tmpUrl = new URL(thumbURL);
			tmpUrl = HTTPResourceAuthenticator.concatenateUserInfo(tmpUrl);
			this.thumbURL = tmpUrl.toString();
		} catch (MalformedURLException e) {
			this.thumbURL = thumbURL;
		}
		
		this.fluxName = fluxName;
	}

	@Override
	public InputStream getThumbnailInputStream() throws IOException {
		if (thumbURL != null) {
			return downloadAndSend(thumbURL, true);
		} else {
			return super.getThumbnailInputStream();
		}
	}

	public InputStream getInputStream() {
		return null;
	}

	public long length() {
		return DLNAMediaInfo.TRANS_SIZE;
	}

	public String getName() {
		return fluxName;
	}

	public boolean isFolder() {
		return false;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return url;
	}
}

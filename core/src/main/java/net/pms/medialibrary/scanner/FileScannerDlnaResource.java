/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2012  Ph.Waeber
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
package net.pms.medialibrary.scanner;

import java.io.IOException;
import java.io.InputStream;

import net.pms.configuration.RendererConfiguration;
import net.pms.dlna.DLNAResource;

/**
 * This class is being used by the FileScanner. It sets the parent of the
 * RealFile being used for parsing to an instance of this class in order to make
 * the usage of mediainfo or ffmpeg configurable through the mediainfo flag
 * 
 * @author pw
 * 
 */
public class FileScannerDlnaResource extends DLNAResource {

	public FileScannerDlnaResource() {
		setDefaultRenderer(RendererConfiguration.getRendererConfigurationByUA("FileParsingRenderer"));
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public String getSystemName() {
		return null;
	}

	@Override
	public long length() {
		return 0;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return null;
	}

	@Override
	public boolean isFolder() {
		return false;
	}

	@Override
	public boolean isValid() {
		return false;
	}

}

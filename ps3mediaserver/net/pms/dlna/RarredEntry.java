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
import java.io.OutputStream;

import net.pms.PMS;
import net.pms.formats.Format;
import net.pms.util.FileUtil;
import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

public class RarredEntry extends DLNAResource implements IPushOutput {
	
	@Override
	protected String getThumbnailURL() {
		if (getType() == Format.IMAGE || getType() == Format.AUDIO) // no thumbnail support for now for real based disk images
			return null;
		return super.getThumbnailURL();
	}

	private String name;
	private File pere;
	private String fileheadername;
	private long length;
	//private boolean nullable;
	//private byte data [];
	
	public RarredEntry(String name, File pere, String fileheadername, long length) {
		this.fileheadername = fileheadername;
		this.name = name;
		this.pere = pere;
		this.length = length;
	}

	public InputStream getInputStream() throws IOException {
		/*if (data == null) {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				Archive rarFile = new Archive(pere);
				FileHeader header = null;
				for(FileHeader fh:rarFile.getFileHeaders()) {
					if (fh.getFileNameString().equals(fileheadername)) {
						header = fh;
						break;
					}
				}
				if (header != null) {
					rarFile.extractFile(header, baos);
					data = baos.toByteArray();
				}
				rarFile.close();
			} catch (RarException e) {
				PMS.error("Error in unpacking of " + name, e);
			} finally {
				try {
					baos.close();
				} catch (IOException e) {}
			}
		}
		if (data == null)
			return null;
		ByteArrayInputStream bytes = new ByteArrayInputStream(data);
		return new UnusedInputStream(bytes, this, 10000) {
			@Override
			public void unusedStreamSignal() {
				PMS.info("RarEntry Data not asked since 10 seconds... Nullify buffer");
				data = null;
			}
		};*/
		return null;
	}

	public String getName() {
		return name;
	}

	public long length() {
		if (player != null && player.type() != Format.IMAGE)
			return DLNAMediaInfo.TRANS_SIZE;
		return length;
	}

	public boolean isFolder() {
		return false;
	}

	public long lastModified() {
		return 0;
	}

	@Override
	public String getSystemName() {
		return FileUtil.getFileNameWithoutExtension(pere.getAbsolutePath()) + "." + FileUtil.getExtension(name);
	}

	@Override
	public boolean isValid() {
		checktype();
		srtFile = FileUtil.doesSubtitlesExists(pere, null);
		return ext != null;
	}
	
	@Override
	public boolean isUnderlyingSeekSupported() {
		return length() < MAX_ARCHIVE_SIZE_SEEK;
	}

	@Override
	public void push(final OutputStream out) throws IOException {
		Runnable r = new Runnable() {
			public void run() {
				Archive rarFile = null;
				try {
					rarFile = new Archive(pere);
					FileHeader header = null;
					for(FileHeader fh:rarFile.getFileHeaders()) {
						if (fh.getFileNameString().equals(fileheadername)) {
							header = fh;
							break;
						}
					}
					if (header != null) {
						PMS.debug("Starting the extraction of " + header.getFileNameString());
						rarFile.extractFile(header, out);
					}
				} catch (Exception e) {
					PMS.info("Unpack error, maybe it's normal, as backend can be terminated: " + e.getMessage());
				} finally {
					try {
						rarFile.close();
						out.close();
					} catch (IOException e) {}
				}
			}
		};
		new Thread(r).start();
	}
	
	@Override
	public void resolve() {
		if (ext == null || !ext.isVideo())
			return;
		boolean found = false;
		if (!found) {
			if (media == null) {
				media = new DLNAMediaInfo();
			}
			found = !media.mediaparsed && !media.parsing;
			if (ext != null) {
				InputFile input = new InputFile();
				input.push = this;
				input.size = length();
				ext.parse(media, input, getType());
			}
		}
		super.resolve();
	}

	@Override
	public InputStream getThumbnailInputStream() throws IOException {
		if (media != null && media.thumb != null)
			return media.getThumbnailInputStream();
		else return super.getThumbnailInputStream();
	}
}

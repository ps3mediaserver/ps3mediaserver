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
package net.pms.medialibrary.dlna;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.dlna.virtual.VirtualFolder;
import net.pms.io.OutputParams;
import net.pms.io.ProcessWrapperImpl;

/**
 * If showing a file as a folder, this class can be added
 * to have an entry with an icon and a name. When played,
 * the thumbnail will be converted to a video to be shown
 * full screen
 */
public class MediaLibraryFileInfo extends VirtualFolder {
	private static final Logger log = LoggerFactory.getLogger(MediaLibraryFileInfo.class);
	private final String GEN_MOVIE_NAME = "tmp_vid.mpg";

	/**
	 * Instantiates a new media library file info.
	 *
	 * @param displayName the display name
	 * @param thumbnailIcon the thumbnail icon
	 */
	public MediaLibraryFileInfo(String displayName, String thumbnailIcon) {
		super(displayName, thumbnailIcon);
	}
	
	/* (non-Javadoc)
	 * @see net.pms.dlna.DLNAResource#isTranscodeFolderAvailable()
	 */
	@Override
	public boolean isTranscodeFolderAvailable() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.virtual.VirtualFolder#isFolder()
	 */
	public boolean isFolder() {
		return false;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.virtual.VirtualFolder#getThumbnailInputStream()
	 */
	@Override
	public InputStream getThumbnailInputStream() {
		InputStream res = null;
		try {
			res = new FileInputStream(thumbnailIcon);
		} catch (FileNotFoundException e) {
			log.error("Failed to getThumbnailInputStream", e);
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.virtual.VirtualFolder#getName()
	 */
	@Override
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.virtual.VirtualFolder#length()
	 */
	@Override
	public long length() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.virtual.VirtualFolder#lastModified()
	 */
	public long lastModified() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.virtual.VirtualFolder#getSystemName()
	 */
	@Override
	public String getSystemName() {
		return getName();
	}
	
	/**
	 * Creates a short video showing the thumbnail.
	 * This allows to view the cover full screen when browsing on the ps3
	 *
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void createVideo() throws IOException {
		String picFolderPath = PMS.getConfiguration().getTempFolder().getAbsolutePath() + File.separator + "thumb_video_pics" + File.separator;
		if(!new File(picFolderPath).isDirectory()){
			new File(picFolderPath).mkdirs();
		}
		//delete all files in pic temp folder
		File picDir = new File(picFolderPath);
		if(picDir.isDirectory() && picDir.listFiles() != null){
			for(File file : picDir.listFiles()){
				file.delete();
			}
		}

		//save images
		File f = new File(picFolderPath + "01.jpg");
		saveThumbnail(f.getAbsolutePath());

		String tmp = "";
		for (int i = 2; i <= 24; i++) {
			if (i < 10)
				tmp = "0" + i;
			else
				tmp = i + "";
			copy(f.getPath(), picFolderPath + tmp + ".jpg");
		}

		//delete previous video if it exists
		f = new File(PMS.getConfiguration().getTempFolder().getAbsolutePath()+ File.separator + GEN_MOVIE_NAME);
		if (f.exists()) f.delete();
		
		//create video
		List<String> args = new ArrayList<String>();
		
		args.add(PMS.getConfiguration().getFfmpegPath());
		args.add("-f");
		args.add("image2");
		args.add("-i");
		args.add(picFolderPath + "%02d.jpg");
		args.add("-vcodec");
		args.add("mpeg2video");
		args.add("-r");
		args.add("24");
		args.add("-s");
		args.add("600x800");
		args.add(PMS.getConfiguration().getTempFolder().getAbsolutePath()+ File.separator + GEN_MOVIE_NAME);
		
		OutputParams params = new OutputParams(PMS.getConfiguration());
		params.workDir = PMS.getConfiguration().getTempFolder();
		params.maxBufferSize = 1;
		params.noexitcheck = true;
		final ProcessWrapperImpl pw = new ProcessWrapperImpl(args.toArray(new String[args.size()]), params);
		// failsafe
		Runnable r = new Runnable() {
			public void run() {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) { }
				pw.stopProcess();
			}
		};
		Thread failsafe = new Thread(r);
		failsafe.start();
		pw.run();
	}
	
	/**
	 * Saves the thumbnail.
	 *
	 * @param saveFilePath the save file path
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	private void saveThumbnail(String saveFilePath) throws IOException {
		File thumbFile = new File(saveFilePath);
		if(thumbFile.exists()){
			thumbFile.delete();
		}
		
		InputStream thumb = getThumbnailInputStream();
		
		// Save InputStream to the file.
		BufferedOutputStream fOut = null;
		try {
			fOut = new BufferedOutputStream(new FileOutputStream(saveFilePath));
			byte[] buffer = new byte[32 * 1024];
			int bytesRead = 0;
			while ((bytesRead = thumb.read(buffer)) != -1) {
				fOut.write(buffer, 0, bytesRead);
			}
		} finally {
			if (fOut != null) {
				fOut.close();
			}
		}
	}

	/* (non-Javadoc)
	 * @see net.pms.dlna.virtual.VirtualFolder#getInputStream()
	 */
	public InputStream getInputStream() throws IOException {
		
		createVideo();
		
		File f = new File(PMS.getConfiguration().getTempFolder().getAbsolutePath()+ File.separator + GEN_MOVIE_NAME);
		InputStream is = null;
		try {
			if (f.exists()){
				is = new FileInputStream(new File(f.getPath()));
			}
		} catch (FileNotFoundException e) {
			log.error("Failed to getInputStream", e);
		}
		
		return is;
	}

	static final int    BUFF_SIZE = 100000;
	static final byte[] buffer    = new byte[BUFF_SIZE];

	/**
	 * Copies a file
	 *
	 * @param from the from
	 * @param to the to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void copy(String from, String to) throws IOException {
		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);
			while (true) {
				synchronized (buffer) {
					int amountRead = in.read(buffer);
					if (amountRead == -1) {
						break;
					}
					out.write(buffer, 0, amountRead);
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.close();
			}
		}
	}
}

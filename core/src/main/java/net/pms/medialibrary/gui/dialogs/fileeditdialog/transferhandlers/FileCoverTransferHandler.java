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
package net.pms.medialibrary.gui.dialogs.fileeditdialog.transferhandlers;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.PMS;
import net.pms.medialibrary.commons.helpers.FileImportHelper;

public class FileCoverTransferHandler  extends TransferHandler {
	private static final long serialVersionUID = 1874409559271363041L;
	private static final Logger log = LoggerFactory.getLogger(FileCoverTransferHandler.class);

	private static final DataFlavor flavors[] = { DataFlavor.javaFileListFlavor /*, DataFlavor.imageFlavor*/ };
	public static final String TMP_FILENAME = "tmp_cover.jpg";
	protected List<String> supportedIconExtensions = Arrays.asList("png", "jpg", "jpeg", "bmp");
	private boolean hasCoverChanged = false;
	private List<ActionListener> coverChangedListeners = new ArrayList<ActionListener>();
	private String coverPath;
	private boolean hasCoverBeenSet = false;

	public void addCoverChangedListeners(ActionListener coverChangedListener) {
		coverChangedListeners.add(coverChangedListener);
	}
	
	protected void fireCoverChanged() {
		for(ActionListener l : coverChangedListeners) {
			l.actionPerformed(new ActionEvent(this, 9374, "CoverChanged"));
		}
	}

	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	@Override
	public boolean canImport(JComponent comp, DataFlavor flavor[]) {
		if (!(comp instanceof JLabel)) {
			return false;
		}
		for (DataFlavor supportedFlavor : flavors) {
			for (DataFlavor f : flavor) {
				if (f.equals(supportedFlavor)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
			if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				try {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() == 1) {
						//get the paths
						String sourceFileName = files.get(0).getAbsolutePath();
						
						//get the file extension
						File f = new File(sourceFileName);
						String name = f.getName();
						int pos = name.lastIndexOf('.');
						String ext = name.substring(pos + 1).toLowerCase();

						//make sure an image has been dropped
						if(!supportedIconExtensions.contains(ext)) {
							return false;
						}
						
						//copy the image
						importCover(sourceFileName);
						
						hasCoverChanged = true;
						fireCoverChanged();
					}
					return true;
				} catch (Throwable th) {
					log.error("Failed to accept dropped image", th);
				}
			}
		return false;
	}
	
	public void setCoverPath(String coverPath) {
		this.coverPath = coverPath;
		hasCoverBeenSet = true;
	}

	public String getCoverPath() {
		if(coverPath == null) coverPath = getDropCoverPath();
		return coverPath;
	}

	public String getDropCoverPath() {
		try {
			return hasCoverBeenSet ? PMS.getConfiguration().getTempFolder() + File.separator + TMP_FILENAME : "";
		} catch (IOException e) {
			return "";
		}
	}
	
	public void importCover(String coverPath) {
		File coverToCopy = new File(coverPath);
		try {
			hasCoverBeenSet = true;
			String filePath = getDropCoverPath();
			FileImportHelper.copyFile(coverToCopy, new File(filePath), true);
			this.coverPath = filePath;
			hasCoverChanged = true;
		} catch (IOException e) {
			log.error(String.format("Failed to copy file '%s' to '%s'", coverPath, getCoverPath()), e);
			hasCoverBeenSet = false;
		}
	}
	
	public boolean hasCoverChanged() {
		return hasCoverChanged;
	}
}

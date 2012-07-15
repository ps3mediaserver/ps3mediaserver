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
package net.pms.medialibrary.gui.tab.libraryview;

import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.enumarations.FileType;

public class LibraryViewPanel extends JPanel {
	private static final Logger log = LoggerFactory.getLogger(LibraryViewPanel.class);
	private static final long serialVersionUID = -3918303753599795894L;
	private FileDisplayer videoFileDisplayer;
	private FileDisplayer audioFileDisplayer;
	private FileDisplayer picturesDisplayer;
	private FileDisplayer fileDisplayer;

	public LibraryViewPanel(){
		super(new GridLayout(1, 1));
		
		JTabbedPane tp = new JTabbedPane();
		tp.addTab(Messages.getString("ML.FileType.VIDEO"), new ImageIcon(getClass().getResource("/resources/images/videofolder-16.png")), videoFileDisplayer = new FileDisplayer(FileType.VIDEO));
		tp.addTab(Messages.getString("ML.FileType.AUDIO"), new ImageIcon(getClass().getResource("/resources/images/audiofolder-16.png")), audioFileDisplayer = new FileDisplayer(FileType.AUDIO));
		tp.addTab(Messages.getString("ML.FileType.PICTURES"), new ImageIcon(getClass().getResource("/resources/images/picturesfolder-16.png")), picturesDisplayer = new FileDisplayer(FileType.PICTURES));
		tp.addTab(Messages.getString("ML.FileType.FILE"), new ImageIcon(getClass().getResource("/resources/images/nofilefilter_folder-16.png")), fileDisplayer = new FileDisplayer(FileType.FILE));
		
		add(tp);
	}

	public void configure(DOFilter filter, FileType fileType) {
		switch (fileType) {
		case AUDIO:
			audioFileDisplayer.setFilter(filter);
			break;
		case VIDEO:
			videoFileDisplayer.setFilter(filter);
			break;
		case PICTURES:
			picturesDisplayer.setFilter(filter);
			break;
		case FILE:
			fileDisplayer.setFilter(filter);
			break;
		default:
			log.warn(String.format("Unhandled file type received (%s). This should never happen!", fileType));
			break;
		}
	}

}

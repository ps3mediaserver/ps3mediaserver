package net.pms.medialibrary.gui.tab.libraryview;

import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.enumarations.FileType;

public class LibraryViewPanel extends JPanel {
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
		}
	}

}

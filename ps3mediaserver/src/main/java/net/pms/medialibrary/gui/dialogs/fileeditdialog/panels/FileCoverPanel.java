package net.pms.medialibrary.gui.dialogs.fileeditdialog.panels;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.commons.interfaces.IFilePropertiesEditor;
import net.pms.medialibrary.gui.dialogs.ImageViewer;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.transferhandlers.FileCoverTransferHandler;

public class FileCoverPanel extends JPanel implements IFilePropertiesEditor {
	private static final long serialVersionUID = 1657923224378578359L;
	private static final Logger log = LoggerFactory.getLogger(FileCoverPanel.class);
	private JLabel lCover;
	private JCheckBox cbCoverEnabled;
	private String coverPath;
	private ImageIcon coverImage;
	private FileCoverTransferHandler transferHandler;
	
	public FileCoverPanel(String coverSavePath) {
		this(coverSavePath, null);
	}
	
	public FileCoverPanel(String coverSavePath, ImageIcon image) {
		setLayout(new BorderLayout());
		setCoverPath(coverSavePath);
		coverImage = image;
		initialize();
		build();
	}

	private void initialize() {
		//check box
		cbCoverEnabled = new JCheckBox(Messages.getString("ML.FileEditTabbedPane.tCover"));
		cbCoverEnabled.setFont(cbCoverEnabled.getFont().deriveFont(Font.BOLD));
		
		//cover panel
		lCover = new JLabel();
		if(coverImage == null) {
			try {
				coverImage = new ImageIcon(ImageIO.read(getClass().getResource("/resources/images/cover_no_image.png")));
			} catch (IOException e) {
				log.error("Failed to load default cover", e);
			}
		}
		lCover.setIcon(coverImage);		
		lCover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		transferHandler = new FileCoverTransferHandler(coverPath);
		transferHandler.addCoverChangedListeners(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					coverImage = new ImageIcon(ImageIO.read(new File(transferHandler.getSaveFilePath())));
				} catch (IOException ex) {
					log.error("Failed to load default cover from " + transferHandler.getSaveFilePath(), ex);
				}
				cbCoverEnabled.setSelected(true);
				resizeCover();
			}
		});
		
		lCover.setTransferHandler(transferHandler);
		lCover.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					//show full size image in modal dialog on left mouse click
					ImageViewer iv = new ImageViewer((ImageIcon) lCover.getIcon(), Messages.getString("ML.FileEditTabbedPane.tCover"));
					iv.setModal(true);
					iv.setLocation(GUIHelper.getCenterDialogOnParentLocation(iv.getSize(), lCover));
					iv.setVisible(true);
				} else if(e.getButton() == MouseEvent.BUTTON3) {
					//TODO: show context menu on right mouse click
				}
			}
		});
		addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {
				resizeCover(lCover.getHeight());
			}
			
			@Override
			public void componentResized(ComponentEvent e) {
				resizeCover(lCover.getHeight());
			}
		});
		
	}

	@Override
	public void build() {
		JPanel pHeader = new JPanel(new GridLayout());
		pHeader.add(cbCoverEnabled);
		add(pHeader, BorderLayout.NORTH);
		add(lCover, BorderLayout.CENTER);
	}
	
	/**
	 * Resizes the image to the current height of the cover label and sets the with to respect the images aspect ratio
	 * @param height the height to set for the image
	 */
	public void resizeCover() {
		resizeCover(lCover.getHeight());
	}
	
	/**
	 * Resizes the image to the given height and sets the with to respect the images aspect ratio
	 * @param height the height to set for the image
	 */
	public void resizeCover(int height) {
		if(height > 0) {
			lCover.setIcon(GUIHelper.getScaledImage(coverImage, height));
		}
	}

	public String getCoverPath() {
		return coverPath;
	}

	public void setCoverPath(String coverPath) {
		this.coverPath = coverPath;
	}

	@Override
	public void updateFileInfo(DOFileInfo fileInfo) {
		fileInfo.setThumbnailPath(coverPath);
	}

	@Override
	public List<ConditionType> getPropertiesToUpdate() {
		ArrayList<ConditionType> res = new ArrayList<ConditionType>();
		if(cbCoverEnabled.isSelected()) {
			res.add(ConditionType.FILE_THUMBNAILPATH);
		}
		return res;
	}

}

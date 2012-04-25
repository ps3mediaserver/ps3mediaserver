package net.pms.medialibrary.gui.tab.dlnaview;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.*;
import net.pms.medialibrary.storage.MediaLibraryStorage;
import net.pms.plugins.FileDetailPlugin;
import net.pms.plugins.TreeEntry;

public class DLNAViewTreeCellRenderer extends DefaultTreeCellRenderer {
	private static final long serialVersionUID = 4620499669832969867L;
	private static final Logger log = LoggerFactory.getLogger(DLNAViewTreeCellRenderer.class);
	private String            iconsFolder      = "/resources/images/";

	private ImageIcon         fileFolderIcon;
	private ImageIcon         fileFolderEntryIcon;
	private ImageIcon         fileFolderFileSingleIcon;
	private ImageIcon         fileFolderFileMultipleIcon;

	public DLNAViewTreeCellRenderer() {
		fileFolderIcon = new ImageIcon(getClass().getResource(iconsFolder + "filefolder-16.png"));
		fileFolderEntryIcon = new ImageIcon(getClass().getResource(iconsFolder + "filefolderentry-16.png"));
		fileFolderFileSingleIcon = new ImageIcon(getClass().getResource(iconsFolder + "filefolderfile_single-16.png"));
		fileFolderFileMultipleIcon = new ImageIcon(getClass().getResource(iconsFolder + "filefolderfile_multiple-16.png"));
		
		setBackground(new Color(237, 243, 254));
	}

	private Icon createIcon(DOMediaLibraryFolder folder) {
		assert(folder != null);
		
		BufferedImage im = null;
		try {
			if(MediaLibraryConfiguration.getInstance().isMediaLibraryEnabled()){
    			switch (folder.getFileType()) {
    				case AUDIO:
    					im = ImageIO.read(getClass().getResource(iconsFolder + "audiofolder-16.png"));
    					break;
    				case VIDEO:
    					im = ImageIO.read(getClass().getResource(iconsFolder + "videofolder-16.png"));
    					break;
    				case PICTURES:
    					im = ImageIO.read(getClass().getResource(iconsFolder + "picturesfolder-16.png"));
    					break;
    				case FILE:
    					im = ImageIO.read(getClass().getResource(iconsFolder + "nofilefilter_folder-16.png"));
    					break;
    			}
			
    			int w = 5;
    			int h = 3;
    			int hFull = 16;
    			if(folder.isInheritsConditions()){
    				Graphics2D g = im.createGraphics();
    				g.setColor(new Color(150, 64, 80));
    				g.fillRect(0, hFull - 3 * h, w, h);
    				g.dispose();
    				
    			}
    			if(folder.getFilter().getConditions().size() > 0){
    				Graphics2D g = im.createGraphics();
    				g.setColor(new Color(255, 0, 0));
    				g.fillRect(0, hFull - 3 * h + h /2, w, 1);
    				g.dispose();				
    			}
    			
    			if(folder.isInheritSort()){
    				Graphics2D g = im.createGraphics();
    				g.setColor(new Color(102, 132, 244));
    				g.fillRect(0, hFull - 2 * h, w, h);
    				g.dispose();				
    			}
    			if(folder.isInheritDisplayFileAs()){
    				Graphics2D g = im.createGraphics();
    				g.setColor(new Color(101, 196, 88));
    				g.fillRect(0, hFull - h, w, h);
    				g.dispose();
    			}
			} else {
				im = ImageIO.read(getClass().getResource(iconsFolder + "nofilefilter_folder-16.png"));
			}
		} catch (Exception e) {
			log.error(String.format("Failed to create icon for folder with id=%s, name=%s", folder.getId(), folder.getDisplayProperties().getDisplayNameMask()));
		}
		
		Icon res;
		if(im == null){
			res = new ImageIcon();
		} else {
			res = new ImageIcon(im);
		}

		return res;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
		setIcon(getIconToDisplay(value));

		// set background color
		if (value instanceof DefaultMutableTreeNode) {
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			if (userObject instanceof DOMediaLibraryFolder) {
				DOMediaLibraryFolder folder = (DOMediaLibraryFolder) userObject;
				long rootFolderId = MediaLibraryStorage.getInstance().getRootFolderId();
				setOpaque(folder.getId() == rootFolderId && !selected);
			}
		}
		return this;
	}

	public Icon getIconToDisplay(Object value) {
		Icon icon = null;
		if (value instanceof DLNAViewFileMutableTreeNode) {
			icon = fileFolderFileSingleIcon;
		} else if (value instanceof DefaultMutableTreeNode) {
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			if (userObject instanceof DOMediaLibraryFolder) {
				icon = createIcon((DOMediaLibraryFolder) userObject);
			} else if (userObject instanceof DOFileEntryFolder) {
				icon = fileFolderIcon;
			} else if (userObject instanceof DOFileEntryInfo) {
				icon = fileFolderEntryIcon;
			} else if (userObject instanceof DOFileEntryFile) {
				DOFileEntryFile fef = (DOFileEntryFile) userObject;
				switch (fef.getFileDisplayMode()) {
					case MULTIPLE:
						icon = fileFolderFileMultipleIcon;
						break;
					default:
						icon = fileFolderFileSingleIcon;
						break;
				}
			} else if (userObject instanceof DOFileEntryPlugin && ((DOFileEntryPlugin) userObject).getPlugin() != null) {
				icon = ((DOFileEntryPlugin) userObject).getPlugin().getTreeIcon();
			} else if (userObject instanceof DOSpecialFolder) {
				DOSpecialFolder sf = (DOSpecialFolder)userObject;
    			if(sf.getSpecialFolderImplementation() != null && sf.getSpecialFolderImplementation().getTreeNode() == null){
    				icon = getNoChildPossibleIcon(sf.getSpecialFolderImplementation().getTreeNodeIcon());
    			}else {
    				icon = sf.getSpecialFolderImplementation().getTreeNodeIcon();    				
    			}
			} else if (userObject instanceof TreeEntry) {
				TreeEntry te = (TreeEntry) userObject;
				if(te.getUserObject() instanceof FileDetailPlugin && ((FileDetailPlugin) te.getUserObject()).getTreeNode() == null){
					icon = getNoChildPossibleIcon(te.getIcon());
				} else {
					icon = te.getIcon();					
				}
			}
		}
		return icon;
	}
	
	private Icon getNoChildPossibleIcon(Icon icon){
		BufferedImage im = new BufferedImage( icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TRANSLUCENT );
		Graphics2D g = im.createGraphics();
		icon.paintIcon( new Canvas(), g, 0, 0 );
		g.setColor(new Color(149, 149, 149));
		g.fillRect(13, 13, 3, 3);
		g.dispose();		
		return new ImageIcon(im);		
	}

}

package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.helpers.FileImportHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class used to drag and drop pictures from the file system or other sources onto the label showing the cover image
 * @author pw
 *
 */
class ImageSelection extends TransferHandler implements Transferable {
	private static final long serialVersionUID = 3468993662766779670L;
	private static final Logger log = LoggerFactory.getLogger(ImageSelection.class);

	private static final DataFlavor flavors[] = { DataFlavor.imageFlavor, DataFlavor.javaFileListFlavor };

	private Image image;
	private DOFileInfo fileInfo;
	private ImageIcon videoCoverImage;

	public ImageSelection(DOFileInfo fileInfo, ImageIcon videoCoverImage) {
		this.fileInfo = fileInfo;
		this.videoCoverImage = videoCoverImage;
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
	public Transferable createTransferable(JComponent comp) {
		// Clear
		image = null;

		if (comp instanceof JLabel) {
			JLabel label = (JLabel) comp;
			Icon icon = label.getIcon();
			if (icon instanceof ImageIcon) {
				image = ((ImageIcon) icon).getImage();
				return this;
			}
		}
		return null;
	}

	@Override
	public boolean importData(JComponent comp, Transferable t) {
		if (comp instanceof JLabel) {
			JLabel label = (JLabel) comp;
			if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				try {
					image = (Image) t.getTransferData(DataFlavor.imageFlavor);
					if(image == null) {
						return false;
					}

					//save the image to file and set cover path in file folder
					String saveFileName = MediaLibraryConfiguration.getInstance().getPictureSaveFolderPath() + fileInfo.getFileName().replaceAll("\\\\|/|:|\\*|\\?|\"|<|>|\\|", "_") + ".cover.jpg";
					BufferedImage bufImg = null;
					if (image instanceof BufferedImage) {
					    bufImg = (BufferedImage) image;
					} else {
					    bufImg = FileImportHelper.getBufferedImage(image);
					}
					File saveFile = new File(saveFileName);
					if(saveFile.exists()) {
						saveFile.delete();
					}
					ImageIO.write(bufImg, "JPEG", new FileImageOutputStream(saveFile));
					
					videoCoverImage = new ImageIcon(image);					
					label.setIcon(videoCoverImage);
					
					fileInfo.setThumbnailPath(saveFileName);
					return true;
				} catch (Throwable th) {
					log.error("Failed to accept dropped image", th);
				}
			} else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				try {
					@SuppressWarnings("unchecked")
					List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
					if (files.size() == 1) {
						ImageIcon icon = new ImageIcon(files.get(0).getAbsolutePath());
						label.setIcon(icon);
					}
					return true;
				} catch (Throwable th) {
					log.error("Failed to accept dropped image", th);
				}
			}
		}
		return false;
	}

	// Transferable
	@Override
	public Object getTransferData(DataFlavor flavor) {
		if (isDataFlavorSupported(flavor)) {
			return image;
		}
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return flavors;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		for (DataFlavor f : flavors) {
			if (f.equals(flavor)) {
				return true;
			}
		}
		return false;
	}
}

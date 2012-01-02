package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.text.SimpleDateFormat;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.helpers.DLNAHelper;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.gui.dialogs.ImageViewer;

public class VideoFileInfoPanel extends JPanel {
	private static final long serialVersionUID = 3818830578372058006L;
	private ImageIcon videoCoverImage;

	public VideoFileInfoPanel(DOVideoFileInfo fileInfo) {
		build(fileInfo);
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension d = super.getPreferredSize();
		d.width += getImageWidth(videoCoverImage, d.height);
		return d;
	}

	private void build(final DOVideoFileInfo fileInfo) {
		setLayout(new GridLayout());
		
		videoCoverImage = new ImageIcon(fileInfo.getThumbnailPath());
		
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 5px, fill:p:grow, 3px", // columns
		        "5px, p, 5px, fill:p:grow, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		
		//Add title
		JLabel lTitle = new JLabel(fileInfo.getName());
		lTitle.setFont(lTitle.getFont().deriveFont(lTitle.getFont().getStyle(), lTitle.getFont().getSize() + 3));
		JLabel lDuration = new JLabel(String.format("(%s)", DLNAHelper.formatSecToHHMMSS((int)fileInfo.getDurationSec())));
		FlowLayout fl = new FlowLayout(FlowLayout.LEFT);
		fl.setAlignOnBaseline(true);
		JPanel pTitle = new JPanel(fl);
		pTitle.add(lTitle);
		pTitle.add(lDuration);
		builder.add(pTitle, cc.xyw(2, 2, 3));

		// Add cover
		final JLabel lCover = new JLabel();
		lCover.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lCover.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ImageViewer iv = new ImageViewer(videoCoverImage, fileInfo.getName());
				iv.setModal(true);
				iv.setLocation(GUIHelper.getCenterDialogOnParentLocation(iv.getSize(), lCover));
				iv.setVisible(true);
			}
		});
		addComponentListener(new ComponentAdapter() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {
				if(lCover.getHeight() > 0) {
					lCover.setIcon(getScaledImage(videoCoverImage, lCover.getHeight()));
				}
			}
			
			@Override
			public void componentResized(ComponentEvent arg0) {
				if(lCover.getHeight() > 0) {
					lCover.setIcon(getScaledImage(videoCoverImage, lCover.getHeight()));
				}
			}
		});
		builder.add(lCover, cc.xy(2, 4));
		
		//Add informations
		builder.add(getInformationsPanel(fileInfo), cc.xy(4, 4));		
		
		add(builder.getPanel());
	}
	
	private Component getInformationsPanel(DOVideoFileInfo fileInfo) {		
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();
		
		FormLayout layout = new FormLayout("3px, r:p, 7px, p, 40px, r:p, 7px, fill:p:grow, 3px", // columns
		        "3px, p, 3px, p, 3px, p, 3px, p, 3px, p, 10px, p, 3px, p, 3px, p, 3px, p, 7px, p, 3px, p, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		
		//add file properties
		JComponent cmp = builder.addSeparator(Messages.getString("ML.VideoFileInfoPanel.lFileProperties"), cc.xyw(2, 2, 7));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
		
		builder.add(new PropertyInfoTitleLabel(Messages.getString("ML.Condition.Type.FILE_SIZEBYTE")), cc.xy(2, 4));
		builder.addLabel((fileInfo.getSize() / (1024 * 1024)) + Messages.getString("ML.Condition.Unit.FILESIZE_MEGABYTE"), cc.xy(4, 4));
		
		builder.add(new PropertyInfoTitleLabel(ConditionType.FILE_ISACTIF), cc.xy(6, 4));
		builder.addLabel(String.valueOf(fileInfo.isActif()), cc.xy(8, 4));

		builder.add(new PropertyInfoTitleLabel(ConditionType.FILE_DATEINSERTEDDB), cc.xy(2, 6));
		builder.addLabel(new SimpleDateFormat().format(fileInfo.getDateInsertedDb()), cc.xy(4, 6));

		builder.add(new PropertyInfoTitleLabel(ConditionType.FILE_DATELASTUPDATEDDB), cc.xy(6, 6));
		builder.addLabel(new SimpleDateFormat().format(fileInfo.getDateLastUpdatedDb()), cc.xy(8, 6));

		builder.add(new PropertyInfoTitleLabel(ConditionType.FILEPLAYS_DATEPLAYEND), cc.xy(2, 8));
		builder.addLabel(fileInfo.getPlayHistory().size() > 0 ? new SimpleDateFormat().format(fileInfo.getPlayHistory().get(0)) : Messages.getString("ML.Condition.NeverPlayed"), cc.xy(4, 8));
		
		builder.add(new PropertyInfoTitleLabel(ConditionType.FILE_PLAYCOUNT), cc.xy(6, 8));
		builder.addLabel(String.valueOf(fileInfo.getPlayCount()), cc.xy(8, 8));

		builder.add(new PropertyInfoTitleLabel(Messages.getString("ML.VideoFileInfoPanel.lFilePath")), cc.xy(2, 10));
		builder.addLabel(fileInfo.getFilePath(), cc.xyw(4, 10, 5));
		
		//add video properties
		cmp = builder.addSeparator(Messages.getString("ML.VideoFileInfoPanel.lVideoProperties"), cc.xyw(2, 12, 7));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
		
		builder.add(new PropertyInfoTitleLabel(Messages.getString("ML.VideoFileInfoPane.lResolution")), cc.xy(2, 14));
		builder.addLabel(String.format("%sx%s", fileInfo.getWidth(), fileInfo.getHeight()),  cc.xy(4, 14));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_FRAMERATE), cc.xy(6, 14));
		builder.addLabel(fileInfo.getFrameRate(), cc.xy(8, 14));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_CODECV), cc.xy(2, 16));
		builder.addLabel(fileInfo.getCodecV(),  cc.xy(4, 16));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_CONTAINER), cc.xy(6, 16));
		builder.addLabel(fileInfo.getContainer(), cc.xy(8, 16));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_BITRATE), cc.xy(2, 18));
		builder.addLabel(String.valueOf(fileInfo.getBitrate() / 1024) + " kbit/s",  cc.xy(4, 18));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_MIMETYPE), cc.xy(6, 18));
		builder.addLabel(fileInfo.getMimeType(), cc.xy(8, 18));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_CONTAINS_VIDEOAUDIO), cc.xy(2, 20));
		builder.addLabel(fileInfo.getDisplayString("%audio_languages"),  cc.xyw(4, 20, 5));

		builder.add(new PropertyInfoTitleLabel(ConditionType.VIDEO_CONTAINS_SUBTITLES), cc.xy(2, 22));
		builder.addLabel(fileInfo.getDisplayString("%subtitle_languages"),  cc.xyw(4, 22, 5));
		
		return builder.getPanel();
	}

	/**
	 * Method used to resize images to fit the cover into the label
	 * @param srcImg source image icon
	 * @param h height of the image
	 * @return resized image icon
	 */
	private static ImageIcon getScaledImage(ImageIcon srcImg, int h) {
		int w = getImageWidth(srcImg, h);
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
		Graphics2D g2 = resizedImg.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg.getImage(), 0, 0, w, h, null);
		g2.dispose();
		return new ImageIcon(resizedImg);
	}
	
	private static int getImageWidth(ImageIcon srcImg, int h) {
		return srcImg.getIconWidth() * h / srcImg.getIconHeight();
	}
}

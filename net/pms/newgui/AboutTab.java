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
package net.pms.newgui;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.util.PMSUtil;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AboutTab {
	private ImagePanel imagePanel;
	private JLabel jl;
	private JProgressBar jpb;

	public JProgressBar getJpb() {
		return jpb;
	}

	public JLabel getJl() {
		return jl;
	}

	public ImagePanel getImagePanel() {
		return imagePanel;
	}

	public JComponent build() {
		FormLayout layout = new FormLayout(
			"0:grow, pref, 0:grow",
			"pref, 3dlu, pref, 12dlu, pref, 3dlu, pref, 3dlu, pref, 3dlu, p, 3dlu, p, 3dlu, p");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		final LinkMouseListener pms3Link = new LinkMouseListener("PS3 Media Server " + PMS.getVersion(),
			"http://www.ps3mediaserver.org/");
		JLabel lPms3Link = builder.addLabel(pms3Link.getLabel(), cc.xy(2, 1, "center, fill"));
		lPms3Link.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lPms3Link.addMouseListener(pms3Link);

		imagePanel = buildImagePanel();
		builder.add(imagePanel, cc.xy(2, 3, "center, fill"));


		builder.addLabel(Messages.getString("LinksTab.5"), cc.xy(2, 5, "center, fill"));

		final LinkMouseListener tsMuxerLink = new LinkMouseListener("tsMuxeR (c) Smartlabs",
			"http://www.smlabs.net/en/products/tsmuxer/");
		JLabel lTsMuxerLink = builder.addLabel(tsMuxerLink.getLabel(), cc.xy(2, 7, "center, fill"));
		lTsMuxerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lTsMuxerLink.addMouseListener(tsMuxerLink);

		final LinkMouseListener ffmpegLink = new LinkMouseListener("FFmpeg",
			"http://ffmpeg.mplayerhq.hu");
		JLabel lFfmpegLink = builder.addLabel(ffmpegLink.getLabel(), cc.xy(2, 9, "center, fill"));
		lFfmpegLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lFfmpegLink.addMouseListener(ffmpegLink);

		final LinkMouseListener mplayerLink = new LinkMouseListener("MPlayer",
			"http://www.mplayerhq.hu");
		JLabel lMplayerLink = builder.addLabel(mplayerLink.getLabel(), cc.xy(2, 11, "center, fill"));
		lMplayerLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lMplayerLink.addMouseListener(mplayerLink);

		final LinkMouseListener mplayerSherpiaBuildsLink = new LinkMouseListener("MPlayer's Sherpya Builds",
			"http://oss.netfarm.it/mplayer-win32.php");
		JLabel lMplayerSherpiaBuildsLink = builder.addLabel(mplayerSherpiaBuildsLink.getLabel(), cc.xy(2, 13, "center, fill"));
		lMplayerSherpiaBuildsLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lMplayerSherpiaBuildsLink.addMouseListener(mplayerSherpiaBuildsLink);

		final LinkMouseListener imageMagickLink = new LinkMouseListener("ImageMagick",
			"http://www.imagemagick.org");
		JLabel lImageMagickLink = builder.addLabel(imageMagickLink.getLabel(), cc.xy(2, 15, "center, fill"));
		lImageMagickLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		lImageMagickLink.addMouseListener(imageMagickLink);

		JScrollPane scrollPane = new JScrollPane(builder.getPanel());
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		return scrollPane;
	}

	private static class LinkMouseListener implements MouseListener {
		private String name;
		private String link;

		public LinkMouseListener(String n, String l) {
			name = n;
			link = l;
		}

		public String getLabel() {
			final StringBuilder sb = new StringBuilder();
			sb.append("<html>");
			sb.append("<a href=\"");
			sb.append(link);
			sb.append("\">");
			sb.append(name);
			sb.append("</a>");
			sb.append("</html>");
			return sb.toString();
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			try {
				PMSUtil.browseURI(link);
			} catch (Exception e1) {
			}
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
		}
	}

	public ImagePanel buildImagePanel() {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(LooksFrame.class.getResourceAsStream("/resources/images/logo.png"));
		} catch (IOException e) {
		}
		return new ImagePanel(bi);
	}
}
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

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
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

		final LinkMouseListener pms3Link = new LinkMouseListener("PS3 Media Server " + PMS.VERSION,
			"http://www.ps3mediaserver.org/");
		builder.addLabel(pms3Link.getLabel(), cc.xy(2, 1, "center, fill")).addMouseListener(pms3Link);

		imagePanel = buildImagePanel();
		builder.add(imagePanel, cc.xy(2, 3, "center, fill"));


		builder.addLabel(Messages.getString("LinksTab.5"), cc.xy(2, 5, "center, fill"));

		final LinkMouseListener tsMuxerLink = new LinkMouseListener("tsMuxeR (c) Smartlabs",
			"http://www.smlabs.net/en/products/tsmuxer/");
		builder.addLabel(tsMuxerLink.getLabel(),
			cc.xy(2, 7, "center, fill")).addMouseListener(tsMuxerLink);

		final LinkMouseListener ffmpegLink = new LinkMouseListener("FFmpeg",
			"http://ffmpeg.mplayerhq.hu");
		builder.addLabel(ffmpegLink.getLabel(),
			cc.xy(2, 9, "center, fill")).addMouseListener(ffmpegLink);

		final LinkMouseListener mplayerLink = new LinkMouseListener("MPlayer",
			"http://www.mplayerhq.hu");
		builder.addLabel(mplayerLink.getLabel(),
			cc.xy(2, 11, "center, fill")).addMouseListener(mplayerLink);

		final LinkMouseListener mplayerSherpiaBuildsLink = new LinkMouseListener("MPlayer's Sherpya Builds",
			"http://oss.netfarm.it/mplayer-win32.php");
		builder.addLabel(mplayerSherpiaBuildsLink.getLabel(),
			cc.xy(2, 13, "center, fill")).addMouseListener(mplayerSherpiaBuildsLink);

		final LinkMouseListener ImageMagickLink = new LinkMouseListener("ImageMagick",
			"http://www.imagemagick.org");
		builder.addLabel(ImageMagickLink.getLabel(),
				cc.xy(2, 15, "center, fill")).addMouseListener(ImageMagickLink);

		JScrollPane scrollPane = new JScrollPane(builder.getPanel());
		scrollPane.setBorder(null);
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
			final StringBuffer sb = new StringBuffer();
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

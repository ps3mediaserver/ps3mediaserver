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

import java.awt.Desktop;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;


import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import net.pms.PMS;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class LinksTab {
	
	private ImagePanel imagePanel;
	private JLabel jl ;
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
	                "pref, 3dlu, pref, 12dlu, pref, 3dlu, pref, 3dlu, p, 3dlu, p, 3dlu, p");

	        PanelBuilder builder = new PanelBuilder(layout);
	        builder.setDefaultDialogBorder();
	        builder.setOpaque(false);
	        CellConstraints cc = new CellConstraints();

	       jl = new JLabel("PS3 Media Server v" + PMS.VERSION);
	        builder.add(jl, cc.xy(2, 1, "center, fill"));
	         imagePanel = buildImagePanel();
	        builder.add(imagePanel, cc.xy(2, 3, "center, fill"));
	        
	       
	        builder.addLabel("Helpful links:",  cc.xy(2,  5, "center, fill"));
	        builder.addLabel("<html>tsMuxer (c) SMartlabs: <a href='http://www.smlabs.net/tsmuxer_en.html'>http://www.smlabs.net/tsmuxer_en.html</a></html>",  cc.xy(2,  7, "center, fill")).addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(new URI("http://www.smlabs.net/tsmuxer_en.html"));
					} catch (Exception e1) {}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseReleased(MouseEvent e) {}
	        });
	       
	        builder.addLabel("<html>FFmpeg: <a href='http://ffmpeg.mplayerhq.hu'>http://ffmpeg.mplayerhq.hu</a></html>",  cc.xy(2,  9, "center, fill")).addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(new URI("http://ffmpeg.mplayerhq.hu"));
					} catch (Exception e1) {}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseReleased(MouseEvent e) {}
	        });
	       
	        builder.addLabel("<html>MPlayer: <a href='http://www.mplayerhq.hu/'>http://www.mplayerhq.hu/</a></html>",  cc.xy(2,  11, "center, fill")).addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(new URI("http://www.mplayerhq.hu/"));
					} catch (Exception e1) {}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseReleased(MouseEvent e) {}
	        });
	       
	        builder.addLabel("<html>MPlayer's Sherpya Builds: <a href='http://oss.netfarm.it/mplayer-win32.php'>http://oss.netfarm.it/mplayer-win32.php</a></html>",  cc.xy(2,  13, "center, fill")).addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						Desktop.getDesktop().browse(new URI("http://oss.netfarm.it/mplayer-win32.php"));
					} catch (Exception e1) {}
				}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseReleased(MouseEvent e) {}
	        });
	        
	        return builder.getPanel();
	}
	
	
	
	public ImagePanel buildImagePanel() {
		BufferedImage bi = null;
		try {
			bi = ImageIO.read(LooksFrame.class.getResourceAsStream("/resources/images/Play1Hot_256.png"));
		} catch (IOException e) {
		}
		return new ImagePanel(bi);
	}
}

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

import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import net.pms.Messages;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class StatusTab {

	private static final int MAX_RENDERERS = 10;
	private ImagePanel imagePanel;
	private ImagePanel renderers[] = new ImagePanel[MAX_RENDERERS];
	private JLabel rendererLabels[] = new JLabel[MAX_RENDERERS];
	private int numRenderers;
	private JLabel jl;
	private JProgressBar jpb;
	private JLabel jio;
	private long rc = 0;
	private long peak;

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
			"0:grow, pref, 0:grow", //$NON-NLS-1$
			"pref, 9dlu, pref, 3dlu, pref, 15dlu, pref, 3dlu, p, 3dlu, p, 3dlu, p, 9dlu, p, 5dlu, p"); //$NON-NLS-1$

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setDefaultDialogBorder();
		builder.setOpaque(true);
		CellConstraints cc = new CellConstraints();

		JComponent cmp = builder.addSeparator(Messages.getString("StatusTab.2"), cc.xy(2, 1)); //$NON-NLS-1$
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		jl = new JLabel(Messages.getString("StatusTab.3")); //$NON-NLS-1$

		builder.add(jl, cc.xy(2, 3));
		imagePanel = buildImagePanel("/resources/images/connect_no-220.png");
		builder.add(imagePanel, cc.xy(2, 5, "center, fill")); //$NON-NLS-1$

		jpb = new JProgressBar(0, 100);
		jpb.setStringPainted(true);
		jpb.setString(Messages.getString("StatusTab.5")); //$NON-NLS-1$

		builder.addLabel(Messages.getString("StatusTab.6"), cc.xy(2, 7)); //$NON-NLS-1$
		builder.add(jpb, cc.xy(2, 9));
		//builder.addLabel(Messages.getString("StatusTab.7"),  cc.xy(2,  11)); //$NON-NLS-1$
		jio = new JLabel(Messages.getString("StatusTab.8")); //$NON-NLS-1$
		builder.add(jio, cc.xy(2, 13));

		cmp = builder.addSeparator(Messages.getString("StatusTab.9"), cc.xy(2, 15));
		cmp = (JComponent) cmp.getComponent(0);
		cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));

		FormLayout layoutRenderer = new FormLayout(
			"0:grow, pref, pref, pref, pref, pref, pref, pref, pref, pref, pref, 0:grow", //$NON-NLS-1$
			"pref, 3dlu, pref");
		PanelBuilder rendererBuilder = new PanelBuilder(layoutRenderer);
		rendererBuilder.setOpaque(true);
		for (int i = 0; i < MAX_RENDERERS; i++) {
			renderers[i] = buildImagePanel(null);
			rendererBuilder.add(renderers[i], cc.xy(2 + i, 1));
			rendererLabels[i] = new JLabel("");
			rendererBuilder.add(rendererLabels[i], cc.xy(2 + i, 3, CellConstraints.CENTER, CellConstraints.DEFAULT));
		}

		builder.add(rendererBuilder.getPanel(), cc.xy(2, 17));

		JPanel panel = builder.getPanel();
		JScrollPane scrollPane = new JScrollPane(
			panel,
			JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		return scrollPane;
	}

	public void setReadValue(long v, String msg) {
		if (v < rc) {
			rc = v;
		} else {
			int sizeinKb = (int) ((v - rc) / 125);
			if (sizeinKb > peak) {
				peak = sizeinKb;
			}
			jio.setText(Messages.getString("StatusTab.8") + sizeinKb + Messages.getString("StatusTab.10") + peak + Messages.getString("StatusTab.11")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			rc = v;
		}
	}

	public ImagePanel buildImagePanel(String url) {
		BufferedImage bi = null;
		if (url != null) {
			try {
				bi = ImageIO.read(LooksFrame.class.getResourceAsStream(url)); //$NON-NLS-1$
			} catch (IOException e) {
			}
		}
		return new ImagePanel(bi);
	}

	public void addRendererIcon(int code, String msg, String icon) {
		BufferedImage bi = null;
		if (icon != null) {
			try {
				InputStream is = LooksFrame.class.getResourceAsStream("/resources/images/clients/" + icon);
				if (is == null) {
					is = LooksFrame.class.getResourceAsStream("/renderers/" + icon);
				}
				if (is != null) {
					bi = ImageIO.read(is); //$NON-NLS-1$
				}
			} catch (IOException e) {
			}
		}
		if (bi != null) {
			renderers[numRenderers].set(bi);
		}
		rendererLabels[numRenderers].setText(msg);
		numRenderers++;
	}
}

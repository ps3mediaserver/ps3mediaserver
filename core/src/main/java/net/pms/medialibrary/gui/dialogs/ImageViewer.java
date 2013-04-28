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
package net.pms.medialibrary.gui.dialogs;

import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

public class ImageViewer extends JDialog {
	private static final long serialVersionUID = -995397066672770511L;

	JScrollPane sp;
	JLabel lImage;

	public ImageViewer(ImageIcon image, String title) {
		((java.awt.Frame) getOwner()).setIconImage(new ImageIcon(getClass().getResource("/resources/images/icon-16.png")).getImage());
		setLayout(new GridLayout());
		setTitle(title);
		
		lImage = new JLabel(image);
		sp = new JScrollPane(lImage);
		sp.setPreferredSize(lImage.getPreferredSize());
		getContentPane().add(sp);
		
		sp.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				checkScrollBars();
			}
		});
		
		pack();
		checkScrollBars();
	}
	
	private void checkScrollBars() {
		if(sp.getWidth() < lImage.getPreferredSize().getWidth()) {
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		} else {
			sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);					
		}
		
		if(sp.getHeight() < lImage.getPreferredSize().getHeight()) {
			sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		} else {
			sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);				
		}
	}
}

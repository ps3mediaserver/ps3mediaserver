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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.pms.Messages;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TextInputDialog extends JDialog {
	private static final long serialVersionUID = 6683403124508519187L;
	private final int MIN_BUTTON_WIDTH = 60;
	
	private List<ActionListener> actionAddValueListeners = new ArrayList<ActionListener>();
	
	private JTextField tfValue;

	public TextInputDialog(String description, String initialValue) {
		((java.awt.Frame) getOwner()).setIconImage(new ImageIcon(getClass().getResource("/resources/images/icon-16.png")).getImage());
		setTitle(Messages.getString("ML.TextInputDialog.Title"));
		
		//initialize components
		tfValue = new JTextField(initialValue);		
		tfValue.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					for(ActionListener l : actionAddValueListeners) {
						l.actionPerformed(new ActionEvent(new Object(), 0, "Add"));
					}
					dispose();
				} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					dispose();
				}
			}
		});

		//build panel
		FormLayout layout = new FormLayout("5px, r:p, 5px, 200, 5px",
		        "3px, p, 1px, p");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.addLabel(description, cc.xy(2, 2));
		builder.add(tfValue, cc.xy(4,  2));
		
		JButton bCancel = new JButton(Messages.getString("ML.TextInputDialog.bCancel"));
		if (bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				dispose();
			}
		});
		JButton bOk = new JButton(Messages.getString("ML.TextInputDialog.bOk"));
		if (bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				for(ActionListener l : actionAddValueListeners) {
					l.actionPerformed(new ActionEvent(new Object(), 0, "Add"));
				}
			}
		});

		JPanel bPanel = new JPanel();
		bPanel.setAlignmentX(CENTER_ALIGNMENT);
		bPanel.add(bOk);
		bPanel.add(bCancel);
		builder.add(bPanel, cc.xyw(2, 4, 3));
		
		add(builder.getPanel());
		pack();
	}
	
	public void addAddValueListener(ActionListener l) {
		actionAddValueListeners.add(l);
	}
	
	public String getValue() {
		return tfValue.getText();
	}
}

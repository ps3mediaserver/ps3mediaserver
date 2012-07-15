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
package net.pms.medialibrary.gui.shared;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOThumbnailPriority;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.TumbnailPrioCBItem;
import net.pms.medialibrary.commons.enumarations.ThumbnailPrioType;

public class ThumbnailPrioChooser {
	private static final Logger log = LoggerFactory.getLogger(ThumbnailPrioChooser.class);
	public JComboBox            cbPrioType;
	public JTextField           tfPicturePath;
	public JTextField           tfSeekPointSec;
	public JButton              bBrowsePicturePath;
	public JLabel               lUnitSec;
	public JLabel               lTitle;
	public JButton              bAdd;
	public JButton              bRemove;
	public JButton              bMoveUp;
	public JButton              bMoveDown;
	
	public enum ActionType {
		Add,
		Remove,
		MoveUp,
		MoveDown
	}

	public List<ActionListener> actionListeners  = new ArrayList<ActionListener>();
	private DOThumbnailPriority currentPrio;

	public ThumbnailPrioChooser(String title, DOThumbnailPriority thumbPrio, List<ThumbnailPrioType> priorityTypes) {
		String iconsFolder = "/resources/images/";
		lTitle = new JLabel(title);

		bAdd = new JButton(new ImageIcon(getClass().getResource(iconsFolder + "tp_add.png")));
		bAdd.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireActionPerformed(ActionType.Add.toString(), currentPrio.getPriorityIndex() + 1);
			}
		});
		bRemove = new JButton(new ImageIcon(getClass().getResource(iconsFolder + "tp_remove.png")));
		bRemove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireActionPerformed(ActionType.Remove.toString(), currentPrio.getPriorityIndex());
			}
		});
		bMoveUp = new JButton(new ImageIcon(getClass().getResource(iconsFolder + "tp_move_up.png")));
		bMoveUp.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireActionPerformed(ActionType.MoveUp.toString(), currentPrio.getPriorityIndex());
			}
		});
		bMoveDown = new JButton(new ImageIcon(getClass().getResource(iconsFolder + "tp_move_down.png")));
		bMoveDown.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				fireActionPerformed(ActionType.MoveDown.toString(), currentPrio.getPriorityIndex());
			}
		});

		cbPrioType = new JComboBox();
		for (ThumbnailPrioType pt : priorityTypes) {
			cbPrioType.addItem(new TumbnailPrioCBItem(pt, Messages.getString("ML.ThumbnailPrioType." + pt)));
		}

		tfPicturePath = new JTextField();
		tfPicturePath.setMinimumSize(new Dimension(200, tfPicturePath.getMinimumSize().width));
		bBrowsePicturePath = new JButton(Messages.getString("ML.ThumbnailPrioChooser.bBrowse"));
		bBrowsePicturePath.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser fc = new JFileChooser(new File(tfPicturePath.getText()));
				if (fc.showDialog(null, "OK") == JFileChooser.APPROVE_OPTION) {
					tfPicturePath.setText(fc.getSelectedFile().getAbsolutePath());
				}
			}
		});

		tfSeekPointSec = new JTextField();
		tfSeekPointSec.setMinimumSize(new Dimension(50, tfPicturePath.getMinimumSize().width));
		lUnitSec = new JLabel(Messages.getString("ML.ThumbnailPrioChooser.lUnitSec"));
		lUnitSec.setVisible(false);

		cbPrioType.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (cbPrioType.getSelectedItem() instanceof TumbnailPrioCBItem) {
					TumbnailPrioCBItem item = (TumbnailPrioCBItem) cbPrioType.getSelectedItem();
					switch (item.getThumbnailPrioType()) {
						case GENERATED:
							tfPicturePath.setVisible(false);
							bBrowsePicturePath.setVisible(false);
							tfSeekPointSec.setVisible(true);
							lUnitSec.setVisible(true);
							break;
						case PICTURE:
							tfPicturePath.setVisible(true);
							bBrowsePicturePath.setVisible(true);
							tfSeekPointSec.setVisible(false);
							lUnitSec.setVisible(false);
							break;
						case THUMBNAIL:
							tfPicturePath.setVisible(false);
							bBrowsePicturePath.setVisible(false);
							tfSeekPointSec.setVisible(false);
							lUnitSec.setVisible(false);
							break;
						default:
							log.warn(String.format("Unhandled thumbnail priority type received (%s). This should never happen!", item.getThumbnailPrioType()));
							break;
					}
				}
			}
		});
		
		bAdd.setPreferredSize(new Dimension(bAdd.getPreferredSize().width, cbPrioType.getPreferredSize().height));
		bRemove.setPreferredSize(new Dimension(bRemove.getPreferredSize().width, cbPrioType.getPreferredSize().height));
		bMoveUp.setPreferredSize(new Dimension(bMoveUp.getPreferredSize().width, cbPrioType.getPreferredSize().height));
		bMoveDown.setPreferredSize(new Dimension(bMoveDown.getPreferredSize().width, cbPrioType.getPreferredSize().height));

		setTumbnailPrio(thumbPrio);
	}

	public ThumbnailPrioType getTumbnailPrioType() {
		ThumbnailPrioType prioType = ThumbnailPrioType.UNKNOWN;
		if (cbPrioType.getSelectedItem() != null && cbPrioType.getSelectedItem() instanceof TumbnailPrioCBItem) {
			prioType = ((TumbnailPrioCBItem) cbPrioType.getSelectedItem()).getThumbnailPrioType();
		}
		return prioType;
	}

	public DOThumbnailPriority getTumbnailPrio() {
		DOThumbnailPriority prio = new DOThumbnailPriority();
		ThumbnailPrioType prioType = getTumbnailPrioType();
		prio.setThumbnailPriorityType(prioType);
		switch (prioType) {
			case THUMBNAIL:
				// do nothing more
				break;
			case GENERATED:
				try {
					prio.setSeekPosition(Integer.valueOf(tfSeekPointSec.getText()));
				} catch (Exception ex) {
					prio.setSeekPosition(30);
					tfSeekPointSec.setText(String.valueOf(prio.getSeekPosition()));
				}
				break;
			case PICTURE:
				prio.setPicturePath(tfPicturePath.getText());
				break;
		default:
			log.warn(String.format("Unhandled thumbnail priority type received (%s). This should never happen!", prioType));
			break;
		}
		return prio;
	}

	public void setTumbnailPrio(DOThumbnailPriority prio) {
		currentPrio = prio;
		cbPrioType.setSelectedItem(null);
		
		if(prio != null){
    		cbPrioType.setSelectedItem(prio);
    		switch (prio.getThumbnailPriorityType()) {
    			case GENERATED:
    				tfSeekPointSec.setText(String.valueOf(prio.getSeekPosition()));
    				break;
    			case PICTURE:
    				tfPicturePath.setText(prio.getPicturePath());
    				break;
			default:
				break;
    		}
    		cbPrioType.setSelectedItem(new TumbnailPrioCBItem(prio.getThumbnailPriorityType(), Messages.getString("ML.ThumbnailPrioType."
    		        + prio.getThumbnailPriorityType().toString())));
		} else {
			cbPrioType.setSelectedIndex(0);
		}
	}

	public void addActionListener(ActionListener e) {
		actionListeners.add(e);
	}

	public void setEnabled(boolean enabled) {
		cbPrioType.setEnabled(enabled);
		tfPicturePath.setEnabled(enabled);
		tfSeekPointSec.setEnabled(enabled);
		lUnitSec.setEnabled(enabled);
		lTitle.setEnabled(enabled);
		
		bAdd.setVisible(enabled);
		bRemove.setVisible(enabled);
		bMoveUp.setVisible(enabled);
		bMoveDown.setVisible(enabled);

		if (enabled) {
			if (cbPrioType.getSelectedItem() instanceof TumbnailPrioCBItem) {
				TumbnailPrioCBItem item = (TumbnailPrioCBItem) cbPrioType.getSelectedItem();
				if (item.getThumbnailPrioType() == ThumbnailPrioType.PICTURE) {
					bBrowsePicturePath.setVisible(enabled);
				}
			}
		} else {
			bBrowsePicturePath.setVisible(enabled);
		}
	}

	public void setVisible(boolean visible) {
		lTitle.setVisible(visible);
		lUnitSec.setVisible(visible);
		tfPicturePath.setVisible(visible);
		tfSeekPointSec.setVisible(visible);
		bBrowsePicturePath.setVisible(visible);
		cbPrioType.setVisible(visible);

		if (visible && currentPrio != null) {
			setTumbnailPrio(currentPrio);
		}
	}
	
	private void fireActionPerformed(String actionCommand, int prioIndex){
		for(ActionListener l : actionListeners){
			l.actionPerformed(new ActionEvent(this, 1, actionCommand, prioIndex));
		}
	}
}

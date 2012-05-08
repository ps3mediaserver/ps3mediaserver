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
package net.pms.medialibrary.gui.dialogs.fileeditdialog.controls;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.pms.Messages;
import net.pms.medialibrary.gui.shared.TagLabel;
import net.pms.medialibrary.gui.shared.WrapLayout;

/**
 * This panel is being used to show a list of TagLabels. Besides the tags, an add button to add more tags is being 
 * @author pw
 *
 */
public class TagLabelPanel extends JPanel {
	private static final long serialVersionUID = 4440237111852696163L;

	public static final String ACTION_COMMAND_DELETE = "DeleteTag";
	
	private static ImageIcon iiAdd;
	private ImageIcon iiDelete;
	
	private String tagName;
	
	//holds the listeners subscribing for delete event notifications
	private List<ActionListener> tagLabelListeners = new ArrayList<ActionListener>();
	private TagLabel tlEditing;

	private JPanel pTagValues;

	
	/**
	 * Constructor
	 * @param tagValues the initial tag values
	 * @param canDelete 
	 */
	public TagLabelPanel(String tagName, List<String> tagValues, boolean canDelete) {
		setLayout(new BorderLayout(5, 3));
		
		this.tagName = tagName;
		
		//add add button
		initImageIcons();
		
		
		JLabel lAdd = new JLabel(iiAdd);
		lAdd.setToolTipText(Messages.getString("ML.TagLabelPanel.toolTipAdd"));
		lAdd.setCursor(new Cursor(Cursor.HAND_CURSOR));
		lAdd.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				createNewLabel();
			}
		});
		
		if(canDelete) {
			JPanel pOptions = new JPanel(new GridLayout(1, 2, 4, 0));
			pOptions.add(lAdd);
			
			JLabel lDelete = new JLabel(iiDelete);
			lDelete.setToolTipText(Messages.getString("ML.TagLabelPanel.toolTipDelete"));
			lDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
			lDelete.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					fireActionEvent(ACTION_COMMAND_DELETE);
				}
			});
			pOptions.add(lDelete);
			
			add(pOptions, BorderLayout.LINE_START);
		} else {
			add(lAdd, BorderLayout.LINE_START);
		}
		
		
		//add all the tag values
		pTagValues = new JPanel(new WrapLayout(FlowLayout.LEFT, 0, 0));
		for(String tagValue : tagValues) {
			final TagLabel tl = createTagLabel(tagValue);
			pTagValues.add(tl);
		}
		add(pTagValues, BorderLayout.CENTER);
		refreshCommas();
	}
	
	public String getTagName() {
		return tagName;
	}
	
	/**
	 * Adds a new label at the end
	 */
	private void createNewLabel() {
		TagLabel tl = createTagLabel("");
		pTagValues.add(tl);
		refreshCommas();
		tlEditing = tl;
		tl.beginEdit();
	}

	/**
	 * A TagLabelListener will be notified of all TagLabel events
	 * These are: Delete, BeginEdit, EndEdit, Layout and NewTagValue
	 * @param l the listener to be notified
	 */
	public void addTagLabelListener(ActionListener l) {
		tagLabelListeners.add(l);
	}

	/**
	 * Gets the label currently being edited or null if none is being edited
	 * @return the label being edited or null
	 */
	public TagLabel getEditingLabel() {
		return tlEditing;
	}
	
	/**
	 * Gets the configured tag values
	 * @return the list of configured tag values
	 */
	public List<String> getTagValues() {
		List<String> res = new ArrayList<String>();
		for(int i = 0; i < pTagValues.getComponentCount(); i++) {
			JComponent c = (JComponent) pTagValues.getComponent(i);
			if(c instanceof TagLabel) {
				String tv = ((TagLabel)c).getTagValue();
				if(!tv.equals("")) {
					//only add not empty values (can happen if getting the values while a new label is being edited)
					res.add(tv);
				}
			}
		}
		return res;
	}

	/**
	 * Lazy-initializes the static image icons
	 */
	private void initImageIcons() {
		//lazy initialize icons only once
		if(iiAdd == null) {
			try {
				iiAdd = new ImageIcon(ImageIO.read(getClass().getResource("/resources/images/add-12.png")));
			} catch (IOException e1) {
				//do nothing
			}
		}
		if(iiDelete == null) {
			try {
				iiDelete = new ImageIcon(ImageIO.read(getClass().getResource("/resources/images/delete-12.png")));
			} catch (IOException e1) {
				//do nothing
			}
		}
	}

	/**
	 * Tells all the labels to draw a comma after the tag value, except for the last one
	 */
	private void refreshCommas() {
		for(int i = 0; i < pTagValues.getComponentCount(); i++) {
			JComponent c = (JComponent) pTagValues.getComponent(i);
			if(c instanceof TagLabel) {
				((TagLabel)c).setAddComma(i < pTagValues.getComponentCount() - 1);
			}
		}
	}

	/**
	 * Notifies all subscribed listeners using actionPerformed
	 * @param command the ActionEvent command
	 */
	private void fireActionEvent(String command) {
		for(ActionListener l : tagLabelListeners) {
			l.actionPerformed(new ActionEvent(this, 0, command));
		}
	}
	
	/**
	 * Creates a new TagLabel with the given tag value and subscribes to its events
	 * @param tagValue the text to show in the TagLabel
	 * @return the created TagLabel
	 */
	private TagLabel createTagLabel(String tagValue) {
		final TagLabel tl = new TagLabel(tagValue);
		tl.addTagLabelListener(new ActionListener() {					
		@Override
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals(TagLabel.ACTION_COMMAND_BEGIN_EDIT)) {
					tlEditing = tl;
				} else if (e.getActionCommand().equals(TagLabel.ACTION_COMMAND_DELETE)) {
					pTagValues.remove(tl);
					refreshCommas();
				} else if (e.getActionCommand().equals(TagLabel.ACTION_COMMAND_END_EDIT)) {
					tlEditing = null;
				} else if (e.getActionCommand().equals(TagLabel.ACTION_COMMAND_NEW_TAGVALUE)) {
					tlEditing.applyEdit();
					createNewLabel();
				}
				
				//propagate every event
				fireActionEvent(e.getActionCommand());
			}
		});
		return tl;
	}
 }

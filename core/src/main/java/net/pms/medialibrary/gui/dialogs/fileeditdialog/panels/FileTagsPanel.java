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
package net.pms.medialibrary.gui.dialogs.fileeditdialog.panels;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.pms.Messages;
import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.gui.dialogs.TextInputDialog;
import net.pms.medialibrary.gui.dialogs.fileeditdialog.controls.TagLabelPanel;
import net.pms.medialibrary.gui.shared.ScrollablePanel;
import net.pms.medialibrary.gui.shared.TagLabel;

/**
 * Used to display a list of tags containing multiple values
 * @author pw
 *
 */
public class FileTagsPanel extends JPanel {
	private static final long serialVersionUID = 1844931635937843708L;
	
	private List<ActionListener> repaintListeners = new ArrayList<ActionListener>();
	
	private Map<String, TagLabelPanel> tagPanels = new LinkedHashMap<String, TagLabelPanel>(); //key=tag name, value=panel containing all tag labels
	private TagLabel tlEditing;
	
	private boolean showTitel;
	private boolean canDelete;
	
	private JLabel lTitle;
	private JButton bAdd;

	/**
	 * Constructor
	 * @param showTitel if true, the title will be shown with a button to create new tags
	 * @param tags map where the key will be set as a tag name and the values as tag values
	 */
	public FileTagsPanel(Map<String, List<String>> tags, boolean showTitel) {
		this(tags, showTitel, true);
	}

	/**
	 * Instantiates a new file tags panel.
	 *
	 * @param tags map where the key will be set as a tag name and the values as tag values
	 * @param showTitel if true, the title will be shown with a button to create new tags
	 * @param canDelete if true, the delete icon which will raise a delete event fill be visible for every tag
	 */
	public FileTagsPanel(Map<String, List<String>> tags, boolean showTitel, boolean canDelete) {
		this.showTitel = showTitel;
		this.canDelete = canDelete;
		init(tags, showTitel);
		build();
	}
	
	public void addRepaintListener(ActionListener repaintListener) {
		if(!repaintListeners.contains(repaintListener)) {
			repaintListeners.add(repaintListener);
		}
	}
	
	public void removeRepaintListener(ActionListener repaintListener) {
		repaintListeners.remove(repaintListener);
	}
	
	private void init(Map<String, List<String>> tags, boolean showTitel) {		
		lTitle = new JLabel(Messages.getString("ML.FileTagsPanel.ConfiguredTags"));
		lTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		lTitle.setFont(lTitle.getFont().deriveFont((float)lTitle.getFont().getSize() + 4));
		lTitle.setFont(lTitle.getFont().deriveFont(Font.BOLD));

		bAdd = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_add.png")));
		bAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				handleAddTag();
			}
		});		

		List<String> tagNames = GUIHelper.asSortedList(tags.keySet());
		for(String tagName : tagNames) {
			List<String> tagValues = tags.get(tagName);
			Collections.sort(tagValues);
			TagLabelPanel pTag = createTagLabelPanel(tagName, tagValues);
			tagPanels.put(tagName, pTag);
		}
	}

	private void handleAddTag() {
		final TextInputDialog tid = new TextInputDialog(Messages.getString("ML.FileTagsPanel.TagName"), "");
		tid.setModal(true);
		tid.setLocation(GUIHelper.getCenterDialogOnParentLocation(tid.getSize(), bAdd));
		tid.addAddValueListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if(tid.getValue() != null) {
					//don't allow tag names containing which are empty or not alpha numeric
					if(tid.getValue().equals("")) {
						JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTopLevelAncestor()), 
								Messages.getString("ML.FileTagsPanel.BlankTagNameError"), 
								Messages.getString("ML.FileTagsPanel.TagCreationErrorHeader"), 
								JOptionPane.WARNING_MESSAGE);
						return;
					} else if (!tid.getValue().matches("[a-zA-Z0-9]*")) {
						JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(getTopLevelAncestor()), 
								Messages.getString("ML.FileTagsPanel.NonAlphaNumericTagNameError"), 
								Messages.getString("ML.FileTagsPanel.TagCreationErrorHeader"), 
								JOptionPane.WARNING_MESSAGE);
						return;
					}
					
					String tagName = tid.getValue();
					TagLabelPanel pTag = createTagLabelPanel(tagName, new ArrayList<String>());
					tagPanels.put(tagName, pTag);
					build();
					tid.dispose();
				}
			}
		});
		tid.setVisible(true);
	}

	/**
	 * Initializes the components and builds the GUI
	 * @param tags map where the key will be set as a tag name and the values as tag values
	 */
	private void build() {
		setLayout(new BorderLayout(0, 5));
		removeAll();
		
		if(showTitel) {
			JPanel pTitle = new JPanel(new BorderLayout());
			pTitle.add(lTitle, BorderLayout.LINE_START);
			pTitle.add(bAdd, BorderLayout.LINE_END);
			
			JPanel pHeader = new JPanel(new BorderLayout(0, 0));
			pHeader.add(pTitle, BorderLayout.CENTER);
			pHeader.add(new JSeparator(), BorderLayout.SOUTH);
			add(pHeader, BorderLayout.NORTH);
		}
		
		//create tags panel
		ScrollablePanel pTags = new ScrollablePanel(new GridBagLayout());
		pTags.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 5;
		c.ipady = 5;
		
		int rowIndex = 0;
		for(String tagName : tagPanels.keySet()) {
			JLabel lTagName = new JLabel(tagName + ":  ", SwingConstants.TRAILING);
			lTagName.setFont(lTagName.getFont().deriveFont(Font.BOLD));
			c.fill = GridBagConstraints.NONE;
			c.gridx = 0;
			c.gridy = rowIndex++;
			c.weightx = 0;
			c.weighty = 0;
			c.anchor = GridBagConstraints.EAST;
			pTags.add(lTagName, c);
			
			TagLabelPanel pTag = tagPanels.get(tagName);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.weightx = 1;
			c.weighty = 1;
			pTags.add(pTag, c);
		}

		JScrollPane spTags = new JScrollPane(pTags);
		spTags.setBorder(BorderFactory.createEmptyBorder());
		
		add(spTags, BorderLayout.CENTER);
	}
	
	public Map<String, List<String>> getTags() {
		Map<String, List<String>> res = new Hashtable<String, List<String>>();
		for(String tagName : tagPanels.keySet()) {
			List<String> tagValues = tagPanels.get(tagName).getTagValues();
			if(tagValues.size() > 0) {
				//add the tag if it contains at least one value
				res.put(tagName, tagValues);
			}
		}
		return res;
	}
	
	/**
	 * Creates a TagLabelPanel
	 * @param tagValues the tag values to add
	 * @return the created TagLabelPanel
	 */
	private TagLabelPanel createTagLabelPanel(String tagName, List<String> tagValues) {
		TagLabelPanel tl = new TagLabelPanel(tagName, tagValues, canDelete);
		
		//only one item can be edited globally
		tl.addTagLabelListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				TagLabelPanel tl = (TagLabelPanel) e.getSource();
				if(e.getActionCommand().equals(TagLabel.ACTION_COMMAND_BEGIN_EDIT)) {
					//store the label starting to be edited, as it might be removed
					//when doing a cancel edit
					TagLabel tmpLabel = tl.getEditingLabel();
					
					if(tlEditing != null) {
						//cancel (undo changes) the editing of the active label to 
						//allow only 1 editing at any time for all tag label panels
						tlEditing.cancelEdit();
					}
					
					//set the new label as the editing one
					tlEditing = tmpLabel;
				} else if(e.getActionCommand().equals(TagLabel.ACTION_COMMAND_END_EDIT)) {
					tlEditing = null;
				} else if(e.getActionCommand().equals(TagLabelPanel.ACTION_COMMAND_DELETE)) {
					tagPanels.remove(((TagLabelPanel) e.getSource()).getTagName());
					build();
				}
				
				validate();
				repaint();
				
				for(ActionListener l : repaintListeners) {
					l.actionPerformed(new ActionEvent(this, 0, e.getActionCommand()));
				}
			}
		});
		return tl;
	}
}

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

import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;

import net.pms.Messages;

/**
 * A TagLabel can be used for editing a string. The set tag value is being shown in a
 * label. When hovering over it, edit and delete options appear. If editing, the label 
 * changes to a text box and can be edited
 * @author pw
 *
 */
public class TagLabel extends JComponent {
	private static final long serialVersionUID = -4117766151825252615L;
	
	//Action event commands
	public static final String ACTION_COMMAND_DELETE = "Delete";
	public static final String ACTION_COMMAND_BEGIN_EDIT = "BeginEdit";
	public static final String ACTION_COMMAND_END_EDIT = "EndEdit";
	public static final String ACTION_COMMAND_LAYOUT = "Layout";
	public static final String ACTION_COMMAND_NEW_TAGVALUE = "NewTagValue";
	
	//the shown images are static to load them only once
	private static ImageIcon iiDelete;
	private static ImageIcon iiEdit;
	private static ImageIcon iiAbortEdit;
	private static ImageIcon iiApplyEdit;
	
	private boolean addComma;
	private String tagValue;
	private boolean isEditing;
	
	private JLabel lText;
	//used to show delete option while not in editing mode and abort while editing
	private JLabel lDeleteAbort;
	//used to show edit option while not in editing mode and apply while editing
	private JLabel lEditApply;
	private JTextField tfText;
	
	//holds the listeners subscribing for delete event notifications
	private List<ActionListener> tagLabelListeners = new ArrayList<ActionListener>();
	
	//the mouse adapter used by the different components to behave the same way on mouse overs
	//the labels have to subscribe to this as well as the component itself
	private MouseAdapter mouseAdapter;
	
	/**
	 * Constructor
	 * @param tagValue the initial tag value
	 */
	public TagLabel(String tagValue) {
		this.tagValue = tagValue;

		init();
		build();
	}
	
	/**
	 * Gets if a comma has to be added at the end of the tag value
	 * @return true if a comma is being added
	 */
	public boolean getAddComma() {
		return addComma;
	}
	
	/**
	 * Gets if a comma has to be added at the end of the tag value
	 * @param addComma true if a comma has to be added
	 */
	public void setAddComma(boolean addComma) {
		this.addComma = addComma;
		lText.setText(addComma ? tagValue + "," : tagValue);
	}
	
	/**
	 * Sets the correct components for the current editing mode
	 */
	private void build() {
		removeAll();

		//add the labels to the component
		if(isEditing) {
			add(tfText);
			
			tfText.requestFocus();

			lDeleteAbort.setVisible(true);
			lDeleteAbort.setIcon(iiAbortEdit);
			lDeleteAbort.setToolTipText(Messages.getString("ML.TagLabel.ToolTip.Cancel"));
			lEditApply.setVisible(true);
			lEditApply.setIcon(iiApplyEdit);
			lEditApply.setToolTipText(Messages.getString("ML.TagLabel.ToolTip.Apply"));
		} else {
			add(lText);

			lDeleteAbort.setVisible(false);
			lDeleteAbort.setIcon(iiDelete);
			lDeleteAbort.setToolTipText(Messages.getString("ML.TagLabel.ToolTip.Delete"));
			lEditApply.setVisible(false);
			lEditApply.setIcon(iiEdit);
			lEditApply.setToolTipText(Messages.getString("ML.TagLabel.ToolTip.Edit"));
		}
		
		add(lEditApply);
		add(lDeleteAbort);
		
		fireActionEvent(ACTION_COMMAND_LAYOUT);
	}
	
	/**
	 * Initializes all GUI components and adds required listeners
	 */
	private void init() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 2, 0));
		
		initImageIcons();

		//initialize the text field
		tfText = new JTextField(tagValue);
		//make the editing field at least 120 wide or a bit wider then the actual text if it's bigger
		int tfWidth = tfText.getPreferredSize().width + 20;
		tfWidth = tfWidth < 120 ? 120 : tfWidth;
		//set the preferred height of the text box to avoid having the components move when the editing mode changes
		tfText.setPreferredSize(new Dimension(tfWidth, tfText.getPreferredSize().height));
		tfText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					applyEdit();
				} else if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					cancelEdit();
				} else if(e.getKeyCode() == KeyEvent.VK_N && e.isControlDown()) {
					fireActionEvent(ACTION_COMMAND_NEW_TAGVALUE);
				}
			}
		});
		
		//initialize the labels
		lText = new JLabel(addComma ? tagValue + "," : tagValue);
		
		lEditApply = new JLabel(iiEdit);
		lEditApply.setSize(new Dimension(14, 12));
		lEditApply.addMouseListener(getMouseAdapter());
		lEditApply.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		lEditApply.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					if(isEditing) {
						applyEdit();
					} else {
						beginEdit();
					}
				}
				super.mouseClicked(e);
			}
		});
		
		lDeleteAbort = new JLabel(iiDelete);
		lDeleteAbort.setSize(new Dimension(14, 12));
		lDeleteAbort.addMouseListener(getMouseAdapter());
		lDeleteAbort.setCursor(new Cursor(Cursor.HAND_CURSOR));
		
		lDeleteAbort.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					if(isEditing) {
						cancelEdit();
					} else {
						fireActionEvent(ACTION_COMMAND_DELETE);
					}
				}
				super.mouseClicked(e);
			}
		});
		
		//add the mouse listener modifying the label
		addMouseListener(getMouseAdapter());
	}

	/**
	 * Changes the editing mode of the label<br>
	 * Fires a begin edit ActionEvent
	 */
	public void beginEdit() {
		isEditing = true;
		build();		
		fireActionEvent(ACTION_COMMAND_BEGIN_EDIT);
	}
	
	/**
	 * Ends editing by setting the value from the text box<br>
	 * Fires a delete ActionEvent if the value is an empty string
	 */
	public void applyEdit() {
		String newTagValue = tfText.getText().trim();
		if(newTagValue.equals("")) {
			fireActionEvent(ACTION_COMMAND_DELETE);
		} else {
			tagValue = newTagValue;
			endEdit();
		}
	}
	
	/**
	 * Sets the initial tag value and ends edit<br>
	 * Fires a delete ActionEvent if the value is an empty string (when a new label is being created)
	 */
	public void cancelEdit() {
		if(tagValue.equals("")) {
			fireActionEvent(ACTION_COMMAND_DELETE);
		} else {
			endEdit();
		}
	}
	
	/**
	 * Sets the initial tag value and ends edit<br>
	 * Fires a end edit ActionEvent 
	 */
	private void endEdit() {
		tfText.setText(tagValue);
		lText.setText(addComma ? tagValue + "," : tagValue);
		isEditing = false;
		build();
		fireActionEvent(ACTION_COMMAND_END_EDIT);
	}

	/**
	 * Lazy-initializes the static image icons
	 */
	private void initImageIcons() {
		//lazy initialize icons only once
		if(iiDelete == null) {
			try {
				iiDelete = new ImageIcon(ImageIO.read(getClass().getResource("/resources/images/delete-12.png")));
				iiEdit = new ImageIcon(ImageIO.read(getClass().getResource("/resources/images/edit-12.png")));
				iiApplyEdit = new ImageIcon(ImageIO.read(getClass().getResource("/resources/images/apply-12.png")));
				iiAbortEdit = new ImageIcon(ImageIO.read(getClass().getResource("/resources/images/abort-12.png")));
			} catch (IOException e1) {
				//do nothing
			}
		}
	}

	/**
	 * A TagLabelListener will be notified of all TagLabel events<br>
	 * These are: Delete, BeginEdit, EndEdit, Layout and NewTagValue
	 * @param l the listener to be notified
	 */
	public void addTagLabelListener(ActionListener l) {
		tagLabelListeners.add(l);
	}
	
	/**
	 * Gets the configured tag value
	 * @return the tag value
	 */
	public String getTagValue() {
		return tagValue.trim();
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
	 * The mouse adapter showing the editing options on mouse over
	 * @return mouse adapter
	 */
	private MouseAdapter getMouseAdapter() {
		if (mouseAdapter == null) {
			mouseAdapter = new MouseAdapter() {
				@Override
				public void mouseEntered(MouseEvent e) {
					if(!isEditing) {
						//only change icon state when not in editing mode
						if (addComma) {
							lText.setText(tagValue);
						}
	
						lDeleteAbort.setVisible(true);
						lEditApply.setVisible(true);
					}
				}

				@Override
				public void mouseExited(MouseEvent e) {
					if(!isEditing) {
						//only change icon state when not in editing mode
						if (addComma) {
							lText.setText(tagValue + ",");
						}
						lDeleteAbort.setVisible(false);
						lEditApply.setVisible(false);
					}
				}
			};
		}
		return mouseAdapter;
	}
}

package net.pms.medialibrary.gui.dialogs.fileeditdialog.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

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
	
	private Map<String, TagLabelPanel> tagPanels = new LinkedHashMap<String, TagLabelPanel>(); //key=tag name, value=panel containing all tag labels
	private TagLabel tlEditing;
	
	private boolean showTitel;
	
	private JLabel lTitle;
	private JButton bAdd;

	/**
	 * Constructor
	 * @param tags map where the key will be set as a tag name and the values as tag values
	 */
	public FileTagsPanel(Map<String, List<String>> tags, boolean showTitel) {
		this.showTitel = showTitel;
		init(tags, showTitel);
		build();		
	}
	
	private void init(Map<String, List<String>> tags, boolean showTitel) {		
		lTitle = new JLabel(Messages.getString("ML.FileTagsPanel.ConfiguredTags"));
		lTitle.setFont(lTitle.getFont().deriveFont((float)lTitle.getFont().getSize() + 4));
		lTitle.setFont(lTitle.getFont().deriveFont(Font.BOLD));

		bAdd = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_add.png")));
		bAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final TextInputDialog tid = new TextInputDialog(Messages.getString("ML.FileTagsPanel.TagName"), "");
				tid.setModal(true);
				tid.setLocation(GUIHelper.getCenterDialogOnParentLocation(tid.getSize(), bAdd));
				tid.addAddValueListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(tid.getValue() != null && !tid.getValue().equals("")) {
							TagLabelPanel pTag = createTagLabelPanel(new ArrayList<String>());
							tagPanels.put(tid.getValue(), pTag);
							build();
						}
					}
				});
				tid.setVisible(true);
			}
		});		

		List<String> tagNames = GUIHelper.asSortedList(tags.keySet());
		for(String tagName : tagNames) {
			List<String> tagValues = tags.get(tagName);
			Collections.sort(tagValues);
			TagLabelPanel pTag = createTagLabelPanel(tagValues);
			tagPanels.put(tagName, pTag);
		}
	}

	/**
	 * Initializes the components and builds the GUI
	 * @param tags map where the key will be set as a tag name and the values as tag values
	 */
	private void build() {
		setLayout(new BorderLayout(3, 5));
		removeAll();
		
		if(showTitel) {
			JPanel pTitle = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 3));
			pTitle.add(lTitle);
			pTitle.add(bAdd);
			
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
			c.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
			pTags.add(lTagName, c);
			
			TagLabelPanel pTag = tagPanels.get(tagName);
			c.fill = GridBagConstraints.HORIZONTAL;
			c.gridx = 1;
			c.weightx = 1;
			c.weighty = 1;
			c.anchor = GridBagConstraints.ABOVE_BASELINE_LEADING;
			pTags.add(pTag, c);
		}

		JScrollPane spTags = new JScrollPane(pTags);
		spTags.setBorder(BorderFactory.createEmptyBorder());
		
		JPanel pMain = new JPanel(new BorderLayout());
		pMain.add(spTags, BorderLayout.NORTH);
		//add a default panel to have the correct backround color for the entire panel
		pMain.add(new JPanel(), BorderLayout.CENTER);
		
		add(pMain, BorderLayout.CENTER);		
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
	private TagLabelPanel createTagLabelPanel(List<String> tagValues) {
		final TagLabelPanel tl = new TagLabelPanel(tagValues);
		
		//only one item can be edited globally
		tl.addTagLabelListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
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
				}

				validate();
				repaint();
			}
		});
		return tl;
	}
	
	public static void main(String[] args) {
		Map<String, List<String>> tags = new Hashtable<String, List<String>>();
		tags.put("Actors", Arrays.asList(new String[]{"Brad Pitt","Angelina Jolie","Hans de Franz"}));
		tags.put("Genres", Arrays.asList(new String[]{"Action","Comedy","Drama", "Romance"}));
		JDialog dialog = new JDialog();
		dialog.setSize(new Dimension(400, 200));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel p = new FileTagsPanel(tags, true);
		JScrollPane sp = new JScrollPane(p);
		sp.setPreferredSize(p.getPreferredSize());
		dialog.getContentPane().add(sp);
		dialog.setVisible(true);
	}
}

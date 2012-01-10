package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.medialibrary.commons.helpers.GUIHelper;
import net.pms.medialibrary.gui.dialogs.TextInputDialog;
import net.pms.medialibrary.gui.shared.TagLabel;

/**
 * Used to display a list of tags containing multiple values
 * @author pw
 *
 */
public class FileTagsPanel extends JPanel {
	private static final long serialVersionUID = 1844931635937843708L;
	
	private Map<String, TagLabelPanel> tagPanels = new HashMap<String, TagLabelPanel>(); //key=tag name, value=panel containing all tag labels
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
		setLayout(new GridLayout());
		
		lTitle = new JLabel("Configured tags");
		lTitle.setFont(lTitle.getFont().deriveFont((float)lTitle.getFont().getSize() + 4));
		lTitle.setFont(lTitle.getFont().deriveFont(Font.BOLD));

		bAdd = new JButton(new ImageIcon(getClass().getResource("/resources/images/tp_add.png")));
		bAdd.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				final TextInputDialog tid = new TextInputDialog("Tag Name", "");
				tid.setModal(true);
				tid.setLocation(GUIHelper.getCenterDialogOnParentLocation(tid.getSize(), bAdd));
				tid.addAddValueListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent e) {
						if(tid.getValue() != null && !tid.getValue().equals("")) {
							TagLabelPanel pTag = createTagLabelPanel(new ArrayList<String>());
							tagPanels.put(tid.getValue(), pTag);
							build();
							validate();
							repaint();
						}
					}
				});
				tid.setVisible(true);
			}
		});		

		for(String tagName : tags.keySet()) {
			TagLabelPanel pTag = createTagLabelPanel(tags.get(tagName));
			tagPanels.put(tagName, pTag);
		}
	}

	/**
	 * Initializes the components and builds the GUI
	 * @param tags map where the key will be set as a tag name and the values as tag values
	 */
	private void build() {
		int nbRows = tagPanels.size();
		
		String colsStr = "";
		if(showTitel) {
			colsStr += "d, d, 5px, ";	
		}
		
		for(int i = 0; i < nbRows; i++) {
			colsStr += "d, 5px, ";
		}
		colsStr = colsStr.substring(0, colsStr.length() - 7);
		
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();
		
		FormLayout layout = new FormLayout("r:d, 5px, f:d:g", // columns
		        colsStr); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		int tagIndex = 1;
		if(showTitel) {
			FlowLayout fl = new FlowLayout(FlowLayout.LEFT, 10, 0);
			JPanel p = new JPanel(fl);
			p.add(lTitle);
			p.add(bAdd);
			builder.add(p, cc.xyw(1, tagIndex++, 3));			
			builder.addSeparator("", cc.xyw(1, tagIndex, 3));
			tagIndex += 2;
		}
		
		for(String tagName : tagPanels.keySet()) {
			JLabel lTagName = builder.addLabel(tagName + ":", cc.xy(1, tagIndex));
			lTagName.setFont(lTagName.getFont().deriveFont(Font.BOLD));
			lTagName.setPreferredSize(new Dimension(lTagName.getPreferredSize().width, new JTextField().getPreferredSize().height));
			
			TagLabelPanel pTag = tagPanels.get(tagName);
			builder.add(pTag, cc.xy(3, tagIndex, CellConstraints.LEFT, CellConstraints.CENTER));
			
			tagIndex += 2;
		}
		
		removeAll();
		add(builder.getPanel());
		
		validate();
		repaint();
	}
	
	public Map<String, List<String>> getTags() {
		Map<String, List<String>> res = new HashMap<String, List<String>>();
		for(String tagName : tagPanels.keySet()) {
			res.put(tagName, tagPanels.get(tagName).getTagValues());
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
		Map<String, List<String>> tags = new HashMap<String, List<String>>();
		tags.put("Actors", Arrays.asList(new String[]{"Brad Pitt","Angelina Jolie","Hans de Franz"}));
		tags.put("Genres", Arrays.asList(new String[]{"Action","Comedy","Drama", "Romance"}));
		JDialog dialog = new JDialog();
		dialog.setSize(new Dimension(400, 200));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		JPanel p = new FileTagsPanel(tags, true);
//		p.setBorder(new TitledBorder("Tags"));
		JScrollPane sp = new JScrollPane(p);
		sp.setPreferredSize(p.getPreferredSize());
		dialog.getContentPane().add(sp);
		dialog.setVisible(true);
	}
}

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

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.MediaLibraryConfiguration;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryBase;
import net.pms.medialibrary.commons.dataobjects.DOFileEntryFile;
import net.pms.medialibrary.commons.dataobjects.DOThumbnailPriority;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ConditionTypeCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.ScreeResolutionCBItem;
import net.pms.medialibrary.commons.enumarations.FileDisplayMode;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.enumarations.ScreenResolution;
import net.pms.medialibrary.commons.enumarations.ThumbnailPrioType;
import net.pms.medialibrary.commons.helpers.FolderHelper;
import net.pms.medialibrary.gui.shared.ThumbnailPrioChooser.ActionType;

public class FileDisplayPanel extends JPanel {
	private static final long         serialVersionUID = 6342698042364671785L;
	private MediaLibraryConfiguration libConfig;
	private JLabel                    lMask;
	private JTextField                tfDisplaynameMask;
	private JComboBox                 cbMaskHelp;
	private JRadioButton              rbSingleFile;
	private JRadioButton              rbMultipleFiles;

	private JPanel                    pThumbnailPrio;
	private JPanel                    pDisplayNameMask;
	private JPanel                    pFileDisplayMode;
	private JComboBox                 cbScreeResolution;
	private JTextField                tfMaxLineLength;
	private JPanel                    pMaxLineLength;
	private FileType                  fileType;
	private boolean                   isUpdating;
	private List<ThumbnailPrioChooser> prioChoosers = new ArrayList<ThumbnailPrioChooser>();

	public FileDisplayPanel(DOFileEntryBase fileEntry, FileType fileType) {
		libConfig = MediaLibraryConfiguration.getInstance();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.fileType = fileType;
		pDisplayNameMask = getDisplayNameMaskPanel(fileEntry.getDisplayNameMask());
		pThumbnailPrio = getThumbnailPrioPanel(fileEntry.getThumbnailPriorities());
		pFileDisplayMode = getFileDisplayModePanel();
		pMaxLineLength = buildMaxLineLength();

		setMaxLineLength(fileEntry.getMaxLineLength());
		setThumbnailPriorities(fileEntry.getThumbnailPriorities());
		if (fileEntry instanceof DOFileEntryFile) {
			switch (((DOFileEntryFile) fileEntry).getFileDisplayMode()) {
				case SINGLE:
					rbSingleFile.setSelected(true);
					break;
				case MULTIPLE:
					rbMultipleFiles.setSelected(true);
					tfDisplaynameMask.setEnabled(false);
					cbMaskHelp.setEnabled(false);
					break;
			}
		}
		refreshPanel();
	}

	public void setFileType(FileType fileType) {
		this.fileType = fileType;

		isUpdating = true;
		cbMaskHelp.removeAllItems();
		for (ConditionTypeCBItem item : FolderHelper.getHelper().getMaskConditionTypes(Arrays.asList(fileType))) {
			cbMaskHelp.addItem(item);
		}
		isUpdating = false;
	}

	private JPanel getFileDisplayModePanel() {
		JLabel lTitle = new JLabel(Messages.getString("ML.FileDisplayPanel.lTitle"));
		rbSingleFile = new JRadioButton(Messages.getString("ML.FileDisplayPanel.rbSingleFile"));
		rbSingleFile.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tfDisplaynameMask.setEnabled(true);
				cbMaskHelp.setEnabled(true);
			}
		});
		rbMultipleFiles = new JRadioButton(Messages.getString("ML.FileDisplayPanel.rbMultipleFiles"));
		rbMultipleFiles.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				tfDisplaynameMask.setText("");
				tfDisplaynameMask.setEnabled(false);
				cbMaskHelp.setEnabled(false);
			}
		});

		ButtonGroup rbGroup = new ButtonGroup();
		rbGroup.add(rbSingleFile);
		rbGroup.add(rbMultipleFiles);

		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.add(lTitle);
		p.add(rbSingleFile);
		p.add(rbMultipleFiles);

		rbSingleFile.setSelected(true);

		return p;
	}

	private JPanel getThumbnailPrioPanel(List<DOThumbnailPriority> prios) {
		if(prios == null || prios.size() == 0){
			prios = Arrays.asList(new DOThumbnailPriority(-1, ThumbnailPrioType.THUMBNAIL, 30, 0));
		}
		
		String rowSpecs = "";
		for(int i = 0; i < prios.size(); i++){
			rowSpecs += "5px, p, ";
		}
		if(rowSpecs.endsWith(", ")) {
			rowSpecs = rowSpecs.substring(0, rowSpecs.length() -2);
		}
		
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();
		FormLayout layout = new FormLayout("5px, p, 5px, p, 5px, fill:10:grow, 5px, p, 15px, p, 3px, p, 3px, p, 3px, p, 5px", // columns
		        rowSpecs); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
		
		List<ThumbnailPrioType> pts = new ArrayList<ThumbnailPrioType>();
		pts.add(ThumbnailPrioType.THUMBNAIL);
		pts.add(ThumbnailPrioType.PICTURE);
		pts.add(ThumbnailPrioType.GENERATED);
		
		int row = 2;
		int prioIndex = 1;
		prioChoosers.clear();
		for(DOThumbnailPriority prio : prios){
			ThumbnailPrioChooser pc = new ThumbnailPrioChooser("#" + prioIndex++, prio, pts);
			pc.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					//Add
					if(e.getActionCommand().equals(ActionType.Add.toString())){
						boolean newPrioAdded = false;
						DOThumbnailPriority newPrio = new DOThumbnailPriority(-1, ThumbnailPrioType.PICTURE, 30, e.getModifiers());
						List<DOThumbnailPriority> prios = new ArrayList<DOThumbnailPriority>();
						for(DOThumbnailPriority prio : getThumbnailPriorities()){
							if(prio.getPriorityIndex() == e.getModifiers()){
								prios.add(newPrio);
								newPrioAdded = true;
							}
							if(prio.getPriorityIndex() >= e.getModifiers()){
								prio.setPriorityIndex(prio.getPriorityIndex() + 1);
							}
							prios.add(prio);
						}
						
						if(!newPrioAdded){
							prios.add(newPrio);							
						}
						
						setThumbnailPriorities(prios);
					}
					
					//Remove
					else if(e.getActionCommand().equals(ActionType.Remove.toString())){
						List<DOThumbnailPriority> prios = new ArrayList<DOThumbnailPriority>();
						for(DOThumbnailPriority prio : getThumbnailPriorities()){
							if(prio.getPriorityIndex() != e.getModifiers()){
								if(prio.getPriorityIndex() >= e.getModifiers()){
									prio.setPriorityIndex(prio.getPriorityIndex() - 1);
								}
								prios.add(prio);
							}
						}
						
						setThumbnailPriorities(prios);						
					}
					
					//Move up
					else if(e.getActionCommand().equals(ActionType.MoveUp.toString())){
						if(e.getModifiers() > 0) {
    						List<DOThumbnailPriority> prios = new ArrayList<DOThumbnailPriority>();
    						for(DOThumbnailPriority prio : getThumbnailPriorities()){								
    							if(prio.getPriorityIndex() == e.getModifiers()){
    								prio.setPriorityIndex(prio.getPriorityIndex() - 1);
    							} else if(prio.getPriorityIndex() == e.getModifiers() - 1){
    								prio.setPriorityIndex(prio.getPriorityIndex() + 1);
    							}
    							prios.add(prio);
    						}
    						
    						setThumbnailPriorities(prios);
						}
					}
					
					//Move down
					else if(e.getActionCommand().equals(ActionType.MoveDown.toString())){
						List<DOThumbnailPriority> currentPrios = getThumbnailPriorities();
						if(e.getModifiers() < currentPrios.size() - 1) {
    						List<DOThumbnailPriority> prios = new ArrayList<DOThumbnailPriority>();
    						for(DOThumbnailPriority prio : getThumbnailPriorities()){								
    							if(prio.getPriorityIndex() == e.getModifiers()){
    								prio.setPriorityIndex(prio.getPriorityIndex() + 1);
    							} else if(prio.getPriorityIndex() == e.getModifiers() + 1){
    								prio.setPriorityIndex(prio.getPriorityIndex() - 1);
    							}
    							prios.add(prio);
    						}
    						
    						setThumbnailPriorities(prios);
						}
					}
				}
			});			
			prioChoosers.add(pc);
			
			builder.add(pc.lTitle, cc.xy(2, row));
			builder.add(pc.cbPrioType, cc.xy(4, row));
			builder.add(pc.tfPicturePath, cc.xy(6, row));
			builder.add(pc.tfSeekPointSec, cc.xy(6, row));
			builder.add(pc.lUnitSec, cc.xy(8, row));
			builder.add(pc.bBrowsePicturePath, cc.xy(8, row));
			builder.add(pc.bRemove, cc.xy(10, row));
			builder.add(pc.bAdd, cc.xy(12, row));
			builder.add(pc.bMoveDown, cc.xy(14, row));
			builder.add(pc.bMoveUp, cc.xy(16, row));
			
			row += 2;
		}

		JPanel p = new JPanel();
		p.setLayout(new GridLayout());
		p.setBorder(new TitledBorder(Messages.getString("ML.FileDisplayPanel.ImageBorder")));
		JScrollPane sp = new JScrollPane(builder.getPanel());
		sp.setBorder(null);
		p.add(sp);

		return p;
	}

	private JPanel getDisplayNameMaskPanel(String displaynameMask) {
		lMask = new JLabel(Messages.getString("ML.FileDisplayPanel.lMask"));
		tfDisplaynameMask = new JTextField(displaynameMask);
		cbMaskHelp = new JComboBox(FolderHelper.getHelper().getMaskConditionTypes(Arrays.asList(fileType)));
		cbMaskHelp.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!isUpdating && cbMaskHelp.getSelectedItem() != null && cbMaskHelp.getSelectedItem() instanceof ConditionTypeCBItem) {
					StringBuffer text = new StringBuffer(tfDisplaynameMask.getText());
					int finalCaretPosition = 0;
					String insertItem = FolderHelper.getHelper().getDisplayNameMaskSubstitute(((ConditionTypeCBItem) cbMaskHelp.getSelectedItem()).getConditionType());

					if (tfDisplaynameMask.getSelectedText() != null) {
						// replace text
						int start = tfDisplaynameMask.getSelectionStart();
						int end = tfDisplaynameMask.getSelectionEnd();
						text = text.replace(start, end, insertItem);
						finalCaretPosition = start + insertItem.length();
					} else {
						// insert text
						int caretPos = tfDisplaynameMask.getCaretPosition();
						text = text.insert(caretPos, insertItem);
						finalCaretPosition = caretPos + insertItem.length();
					}

					// workaround for the text not to be selected automatically
					// if the append text is at the end of the string
					if (text.length() == finalCaretPosition) {
						text.append(" ");
					}

					// set the focus and cureser poition in the text field
					tfDisplaynameMask.requestFocus();
					tfDisplaynameMask.setText(text.toString());
					tfDisplaynameMask.setCaretPosition(finalCaretPosition);
				}
			}
		});

		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("p, 3px, fill:100:grow, 3px, p", // columns
		        "p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(lMask, cc.xy(1, 1));
		builder.add(tfDisplaynameMask, cc.xy(3, 1));
		builder.add(cbMaskHelp, cc.xy(5, 1));

		return builder.getPanel();

	}

	public List<DOThumbnailPriority> getThumbnailPriorities() {		
		int index = 0;
		List<DOThumbnailPriority> res = new ArrayList<DOThumbnailPriority>();
		for(ThumbnailPrioChooser pc : prioChoosers){
			DOThumbnailPriority prio = pc.getTumbnailPrio();
			prio.setPriorityIndex(index++);
			res.add(prio);
		}
		return res;
	}

	public void setThumbnailPriorities(List<DOThumbnailPriority> thumbnailPriorities) {
		Collections.sort(thumbnailPriorities, new Comparator<DOThumbnailPriority>() {

			@Override
            public int compare(DOThumbnailPriority o1, DOThumbnailPriority o2) {
	            return ((Integer)o1.getPriorityIndex()).compareTo(o2.getPriorityIndex());
            }
		});
		JPanel newPanel = getThumbnailPrioPanel(thumbnailPriorities);
		pThumbnailPrio = newPanel;
		refreshPanel();
	}

	public String getDisplaynameMask() {
		return tfDisplaynameMask.getText().trim();
	}

	public void setDisplayNameMask(String mask) {
		tfDisplaynameMask.setText(mask);
	}

	public int getMaxLineLength() {
		return Integer.parseInt(tfMaxLineLength.getText());
	}

	public void setMaxLineLength(int length) {
		tfMaxLineLength.setText(String.valueOf(length));
	}

	public void setDisplayNameMaskVisible(boolean visible) {
		pDisplayNameMask.setVisible(visible);
	}

	public void setThumbnailPrioVisible(boolean visible) {
		pThumbnailPrio.setVisible(visible);
	}

	public void setFileDisplayModeVisible(boolean visible) {
		pFileDisplayMode.setVisible(visible);
	}

	public void setMaxLineLengthVisible(boolean visible) {
		pMaxLineLength.setVisible(visible);
	}

	public FileDisplayMode getFileDisplayMode() {
		if (rbSingleFile.isSelected()) {
			return FileDisplayMode.SINGLE;
		} else {
			return FileDisplayMode.MULTIPLE;
		}
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		lMask.setEnabled(enabled);
		for(ThumbnailPrioChooser pc : prioChoosers){
			pc.setEnabled(enabled);
		}
		tfDisplaynameMask.setEnabled(enabled);
		pThumbnailPrio.setEnabled(enabled);

		cbMaskHelp.setVisible(enabled);
	}

	private void refreshPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, fill:300:grow, 3px", // columns
		        "3px, p, 3px, p, 3px, p, 3px, fill:100:grow, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(pFileDisplayMode, cc.xy(2, 2));
		builder.add(pDisplayNameMask, cc.xy(2, 4));
		builder.add(pMaxLineLength, cc.xy(2, 6));
		builder.add(pThumbnailPrio, cc.xy(2, 8));

		removeAll();
		add(builder.getPanel());
		validate();
	}

	private JPanel buildMaxLineLength() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("p, 3px, p, 3px, fill:10:grow, 3px, p", // columns
		        "p, p,  fill:p:grow, p, p"); // rows
		builder = new PanelBuilder(layout);

		// Max line length
		JLabel lMaxLineLength = new JLabel(Messages.getString("ML.FileDisplayPanel.lMaxLineLength"));
		builder.add(lMaxLineLength, cc.xy(1, 1));

		List<ScreeResolutionCBItem> resItems = new ArrayList<ScreeResolutionCBItem>();
		resItems
		        .add(new ScreeResolutionCBItem(ScreenResolution.NO_LIMIT, Messages.getString("ML.GeneralOptions.ScreenResolutions." + ScreenResolution.NO_LIMIT.toString())));
		resItems.add(new ScreeResolutionCBItem(ScreenResolution.HD, Messages.getString("ML.GeneralOptions.ScreenResolutions." + ScreenResolution.HD.toString())));
		resItems.add(new ScreeResolutionCBItem(ScreenResolution.SD_16_9, Messages.getString("ML.GeneralOptions.ScreenResolutions." + ScreenResolution.SD_16_9.toString())));
		resItems.add(new ScreeResolutionCBItem(ScreenResolution.SD_4_3, Messages.getString("ML.GeneralOptions.ScreenResolutions." + ScreenResolution.SD_4_3.toString())));
		resItems.add(new ScreeResolutionCBItem(ScreenResolution.CUSTOM, Messages.getString("ML.GeneralOptions.ScreenResolutions." + ScreenResolution.CUSTOM.toString())));
		cbScreeResolution = new JComboBox(resItems.toArray());
		builder.add(cbScreeResolution, cc.xy(3, 1));

		tfMaxLineLength = new JTextField();
		tfMaxLineLength.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				validate();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				validate();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				validate();
			}

			private void validate() {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String newText = tfMaxLineLength.getText();
						int caretPos = 0;

						// check if value only contains 0-9 numbers and remove
						// others otherwise
						if (!newText.matches("^[0-9]*$")) {
							newText = tfMaxLineLength.getText().replaceAll("[^0-9]", "");
							caretPos = tfMaxLineLength.getCaret().getMark();
							caretPos--;
						}

						// check that line length is not bigger then 8
						// characters
						if (newText.length() > 8) {
							newText = newText.substring(0, 8);
							caretPos = tfMaxLineLength.getCaret().getMark();
							if (caretPos > newText.length()) {
								caretPos = newText.length();
							}
						}

						if (newText.equals("")) {
							newText = "0";
						}

						// if text had to be adapted to be valid, set it in the
						// text field
						if (!tfMaxLineLength.getText().equals(newText)) {
							tfMaxLineLength.setText(newText);
							tfMaxLineLength.setCaretPosition(caretPos);
						}

						// save the value if it has changed
						int lineLength = Integer.parseInt(newText);
						if (libConfig.getMaxLineLength() != lineLength) {
							libConfig.setMaxLineLength(Integer.parseInt(newText));
							updateMaxLineLength(lineLength);
						}
					}
				});
			}
		});

		cbScreeResolution.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cb = (JComboBox) e.getSource();
				ScreenResolution res = ((ScreeResolutionCBItem) cb.getSelectedItem()).getScreenResolution();
				switch (res) {
					case HD:
						tfMaxLineLength.setText("55");
						break;
					case NO_LIMIT:
						tfMaxLineLength.setText("0");
						break;
					case SD_16_9:
						tfMaxLineLength.setText("40");
						break;
					case SD_4_3:
						tfMaxLineLength.setText("20");
						break;
				}
			}
		});

		builder.add(tfMaxLineLength, cc.xy(5, 1));
		builder.addLabel(Messages.getString("ML.FileDisplayPnael.lCharacters"), cc.xy(7, 1));
		updateMaxLineLength(libConfig.getMaxLineLength());
		return builder.getPanel();
	}

	private void updateMaxLineLength(int maxLineLength) {
		tfMaxLineLength.setText(String.valueOf(maxLineLength));
		switch (maxLineLength) {
			case 55:
				cbScreeResolution.setSelectedItem(new ScreeResolutionCBItem(ScreenResolution.HD, Messages.getString("ML.GeneralOptions.ScreenResolutions."
				        + ScreenResolution.HD.toString())));
				break;
			case 40:
				cbScreeResolution.setSelectedItem(new ScreeResolutionCBItem(ScreenResolution.SD_16_9, Messages.getString("ML.GeneralOptions.ScreenResolutions."
				        + ScreenResolution.SD_16_9.toString())));
				break;
			case 20:
				cbScreeResolution.setSelectedItem(new ScreeResolutionCBItem(ScreenResolution.SD_4_3, Messages.getString("ML.GeneralOptions.ScreenResolutions."
				        + ScreenResolution.SD_4_3.toString())));
				break;
			case 0:
				cbScreeResolution.setSelectedItem(new ScreeResolutionCBItem(ScreenResolution.NO_LIMIT, Messages.getString("ML.GeneralOptions.ScreenResolutions."
				        + ScreenResolution.NO_LIMIT.toString())));
				break;
			default:
				cbScreeResolution.setSelectedItem(new ScreeResolutionCBItem(ScreenResolution.CUSTOM, Messages.getString("ML.GeneralOptions.ScreenResolutions."
				        + ScreenResolution.CUSTOM.toString())));
				break;
		}
	}
}

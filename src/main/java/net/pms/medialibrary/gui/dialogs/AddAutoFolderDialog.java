package net.pms.medialibrary.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.AutoFolderProperyCBItem;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.AutoFolderTypeCBItem;
import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.commons.enumarations.AutoFolderProperty;
import net.pms.medialibrary.commons.enumarations.AutoFolderType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.events.AutoFolderDialogActionEvent;
import net.pms.medialibrary.commons.events.AutoFolderDialogActionListener;
import net.pms.medialibrary.gui.dialogs.folderdialog.FolderDialog;
import net.pms.medialibrary.storage.MediaLibraryStorage;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AddAutoFolderDialog extends JDialog {
	private static final long                    serialVersionUID = 1L;
	private final int MIN_BUTTON_WIDTH = 60;
	private static final Logger logger = LoggerFactory.getLogger(AddAutoFolderDialog.class);

	private List<AutoFolderDialogActionListener> autoFolderDialogActionListeners;

	private JComboBox                            cbAutoFolder;
	private JComboBox                            cbProperty;
	private JRadioButton                         rbAscending;
	private JRadioButton                         rbDescending;
	private JButton                              bCancel;
	private JButton                              bOk;
	private JPanel                               jPanelButtons;
	private JLabel                               lOptionName;
	private JTextField                           tfOption;
	private JButton                              bBrowse;
	private JTextField tfMinOccurences;
	private JPanel pMinOccurences;

	private List<AutoFolderType>                 autoFolderTypes;


	public AddAutoFolderDialog(List<AutoFolderType> autoFolderTypes) {
		((java.awt.Frame) getOwner()).setIconImage(new ImageIcon(FolderDialog.class.getResource("/resources/images/icon-32.png")).getImage());
		this.autoFolderTypes = autoFolderTypes;
		initDialog();
	}

	private void initDialog() {
		autoFolderDialogActionListeners = new ArrayList<AutoFolderDialogActionListener>();

		setTitle(Messages.getString("ML.AddAutoFolderDialog.Title"));

		jPanelButtons = new JPanel();
		cbAutoFolder = new JComboBox();
		cbProperty = new JComboBox();
		
		tfMinOccurences = new JTextField("3");
		pMinOccurences = new JPanel(new BorderLayout());
		pMinOccurences.add(tfMinOccurences, BorderLayout.CENTER);
		pMinOccurences.add(new JLabel(Messages.getString("ML.AddAutoFolderDialog.lMinOccurences")), BorderLayout.WEST);
		
		cbAutoFolder.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				AutoFolderType folderType = ((AutoFolderTypeCBItem) cbAutoFolder.getSelectedItem()).getAutoFolderType();
				if (folderType == AutoFolderType.FILE_SYSTEM) {
					rbAscending.setEnabled(false);
					rbDescending.setEnabled(false);

					lOptionName.setText(Messages.getString("ML.AddAutoFolderDialog.lBaseFolder"));
					lOptionName.setVisible(true);
					tfOption.setVisible(true);
					bBrowse.setVisible(true);
					cbProperty.setVisible(false);
					pMinOccurences.setVisible(false);
				} else if (folderType == AutoFolderType.A_TO_Z) {
					rbAscending.setEnabled(true);
					rbDescending.setEnabled(true);

					cbProperty.removeAllItems();
					cbProperty.addItem(Messages.getString("ML.AddAutoFolderDialog.cbMovieName"));
					cbProperty.setVisible(true);

					lOptionName.setVisible(false);
					tfOption.setVisible(false);
					bBrowse.setVisible(false);
					pMinOccurences.setVisible(false);
				} else if (folderType == AutoFolderType.TYPE_NAME) {
					rbAscending.setEnabled(true);
					rbDescending.setEnabled(true);

					cbProperty.removeAllItems();
					for (AutoFolderProperty prop : AutoFolderProperty.values()) {
						cbProperty.addItem(new AutoFolderProperyCBItem(prop, Messages.getString("ML.AutoFolderProperty." + prop)));
					}
					cbProperty.setVisible(true);

					lOptionName.setVisible(false);
					tfOption.setVisible(false);
					bBrowse.setVisible(false);
					pMinOccurences.setVisible(true);
				} else if (folderType == AutoFolderType.TAG) {
					rbAscending.setEnabled(true);
					rbDescending.setEnabled(true);

					cbProperty.removeAllItems();
					for (String prop : MediaLibraryStorage.getInstance().getExistingTags(FileType.VIDEO)) {
						cbProperty.addItem(prop);
					}
					cbProperty.setVisible(true);

					lOptionName.setVisible(false);
					tfOption.setVisible(false);
					bBrowse.setVisible(false);
					pMinOccurences.setVisible(true);
				}
				rebuildPanel();
			}
		});

		ButtonGroup group = new ButtonGroup();
		rbAscending = new JRadioButton(Messages.getString("ML.AddAutoFolderDialog.rbAscending"));
		rbAscending.setSelected(true);
		rbDescending = new JRadioButton(Messages.getString("ML.AddAutoFolderDialog.rbDescending"));
		group.add(rbAscending);
		group.add(rbDescending);

		bOk = new JButton(Messages.getString("ML.AddAutoFolderDialog.bOk"));
		bOk.setName("buttonOk");
		if (bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleButtonAction(DialogActionType.OK);
			}
		});
		jPanelButtons.add(bOk);

		bCancel = new JButton(Messages.getString("ML.AddAutoFolderDialog.bCancel"));
		bCancel.setName("buttonCancel");
		if (bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.setMinimumSize(new Dimension(60, 20));
		bCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleButtonAction(DialogActionType.CANCEL);
			}
		});
		jPanelButtons.add(bCancel);

		lOptionName = new JLabel();
		tfOption = new JTextField();
		bBrowse = new JButton(Messages.getString("ML.AddAutoFolderDialog.bBrowse"));
		bBrowse.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				showSelectDirChooser();
			}
		});

		lOptionName.setVisible(false);
		tfOption.setVisible(false);
		bBrowse.setVisible(false);

		for (AutoFolderType ft : autoFolderTypes) {
			cbAutoFolder.addItem(new AutoFolderTypeCBItem(ft, Messages.getString("ML.AutoFolderType." + ft)));
		}		

		rebuildPanel();
	}

	private void showSelectDirChooser() {
		JFileChooser fc = new JFileChooser();
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		switch (fc.showOpenDialog(this)) {
			case JFileChooser.APPROVE_OPTION:
				tfOption.setText(fc.getSelectedFile().getAbsolutePath());
				break;
		}
	}

	private void rebuildPanel() {
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("2dlu, fill:p:grow, 2dlu, p, 2dlu, p, 2dlu, fill:p:grow, 2dlu", // columns
		        "2dlu, p, 2dlu, p, 2dlu, p"); // raws
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		// Options
		builder.add(cbAutoFolder, cc.xy(2, 2));
		builder.add(rbAscending, cc.xy(4, 2));
		builder.add(rbDescending, cc.xy(6, 2));

		builder.add(pMinOccurences, cc.xyw(4, 4, 5));
		
		// Add A-Z options
		builder.add(cbProperty, cc.xy(2, 4));

		// Add file system options
		builder.add(lOptionName, cc.xy(2, 4, CellConstraints.RIGHT, CellConstraints.DEFAULT));
		builder.add(tfOption, cc.xyw(4, 4, 3));
		builder.add(bBrowse, cc.xy(8, 4));

		// Buttons
		builder.add(jPanelButtons, cc.xyw(2, 6, 7));

		getContentPane().removeAll();
		getContentPane().add(builder.getPanel());
		
		pack();
	}

	public void addAutoFolderDialogActionListener(AutoFolderDialogActionListener l) {
		if (!autoFolderDialogActionListeners.contains(l)) {
			autoFolderDialogActionListeners.add(l);
		}
	}

	private void handleButtonAction(DialogActionType actionType) {
		// notify listeners if ok, apply or cancel has been clicked
		if (actionType != DialogActionType.UNKNOWN) {
			AutoFolderType folderType = ((AutoFolderTypeCBItem) cbAutoFolder.getSelectedItem()).getAutoFolderType();
			Object userObject = null;
			if (folderType == AutoFolderType.FILE_SYSTEM) {
				userObject = tfOption.getText();
			} else if (folderType == AutoFolderType.TYPE_NAME) {
				userObject = ((AutoFolderProperyCBItem) cbProperty.getSelectedItem()).getAutoFolderProperty();
			} else if (folderType == AutoFolderType.TAG) {
				userObject = cbProperty.getSelectedItem();
			}
			int minOccurences = 0;
			try {
				minOccurences = Integer.parseInt(tfMinOccurences.getText());
			} catch(NumberFormatException ex) {
				logger.warn("Failed to read value for min occurences. Using 0 by default");
			}
			
			for (AutoFolderDialogActionListener l : autoFolderDialogActionListeners) {
				l.autoFolderDialogActionReceived(new AutoFolderDialogActionEvent(this, folderType, rbAscending.isSelected(), minOccurences, actionType, userObject));
			}
		}
	}
}

package net.pms.medialibrary.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.dataobjects.DOVideoFileInfo;
import net.pms.medialibrary.commons.enumarations.FileProperty;
import net.pms.plugins.FileImportPlugin;
import net.pms.plugins.PluginsFactory;

public class FileUpdateWithPluginDialog extends JDialog {
	private static final long serialVersionUID = 3602614964875257644L;

	private static final Logger log = LoggerFactory.getLogger(FileUpdateWithPluginDialog.class);
	private final int MIN_BUTTON_WIDTH = 60;

	private JRadioButton rbId;
	private JRadioButton rbName;
	private JComboBox cbPlugins;
	private JTextField tfValue;
	private JPanel pButtons;
	private JLabel lValueHeader;
	private JList lResults;
	private JButton bImport;
	
	private String nameValue = "";
	private String idValue = "";

	private boolean isUpdate = false;
	private FileImportPlugin importPlugin;
	
	private ActionListener radioButtonActionaListener = new ActionListener() {		
		@Override
		public void actionPerformed(ActionEvent e) {
			handleRadioButtonSelectionChange();
		}
	};

	
	public FileUpdateWithPluginDialog(DOFileInfo fileInfo) {
		((java.awt.Frame)getOwner()).setIconImage(new ImageIcon(getClass().getResource("/resources/images/icon-32.png")).getImage());
		setTitle(String.format(Messages.getString("ML.FileUpdateWithPluginDialog.Title"), fileInfo.getFileName(true)));
		setLayout(new GridLayout());
		
		if(fileInfo instanceof DOVideoFileInfo) {
			DOVideoFileInfo videoFileInfo = (DOVideoFileInfo)fileInfo;
			idValue = videoFileInfo.getImdbId();
			nameValue = videoFileInfo.getName();
		}
		
		init();
		build();
		
		rbName.setSelected(true);
		tfValue.setText(idValue);
		handleRadioButtonSelectionChange();
	}

	public boolean isUpdate() {
		return isUpdate;
	}

	public FileImportPlugin getPlugin() {
		return importPlugin;
	}

	private void setUpdate(boolean isUpdate) {
		this.isUpdate = isUpdate;
	}

	private void init() {
		rbId = new JRadioButton(Messages.getString("ML.FileUpdateWithPluginDialog.rbId"));
		rbId.addActionListener(radioButtonActionaListener);
		rbName = new JRadioButton(Messages.getString("ML.FileUpdateWithPluginDialog.rbName"));
		rbName.addActionListener(radioButtonActionaListener);
		ButtonGroup bgImportType = new ButtonGroup();
		bgImportType.add(rbId);
		bgImportType.add(rbName);		
		
		cbPlugins = new JComboBox();
		tfValue = new JTextField();
		lValueHeader = new JLabel();
		lResults = new JList(new DefaultListModel());
		lResults.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lResults.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if(lResults.getSelectedValue() != null) {
					bImport.setEnabled(true);
				}
			}
		});
		
		//buttons
		JButton bSearch = new JButton(Messages.getString("ML.FileUpdateWithPluginDialog.bSearch"));
		if(bSearch.getPreferredSize().width < MIN_BUTTON_WIDTH) bSearch.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bSearch.getPreferredSize().height));
		bSearch.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(tfValue.getText().equals("") || cbPlugins.getSelectedItem() == null || !(cbPlugins.getSelectedItem() instanceof FileImportPluginWrapper)) {
					return;
				}
				
				String value = tfValue.getText();
				FileImportPlugin plugin = ((FileImportPluginWrapper)cbPlugins.getSelectedItem()).getFileImportPlugin();
				
				bImport.setEnabled(false);
				DefaultListModel listModel = (DefaultListModel)lResults.getModel();
				listModel.removeAllElements();
				
				if(rbId.isSelected()) {
					try {
						setCursor(new Cursor(Cursor.WAIT_CURSOR));
						plugin.importFileById(value);
						listModel.add(0, plugin.getFileProperty(FileProperty.VIDEO_NAME));
					} catch (Throwable t) {
						log.error(String.format("Failed to query %s plugin", plugin.getName()), t);
					} finally {
						setCursor(Cursor.getDefaultCursor());
					}
				} else if(rbName.isSelected()) {
					setCursor(new Cursor(Cursor.WAIT_CURSOR));
					List<Object> results = plugin.searchForFile(value);
					for(int i = 0; i< results.size(); i++) {
						listModel.add(0, results.get(i));
					}
					setCursor(Cursor.getDefaultCursor());
				}
			}
		});
		
		bImport = new JButton(Messages.getString("ML.FileUpdateWithPluginDialog.bImport"));
		bImport.setEnabled(false);
		if(bImport.getPreferredSize().width < MIN_BUTTON_WIDTH) bImport.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bImport.getPreferredSize().height));
		bImport.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				FileImportPlugin plugin = ((FileImportPluginWrapper)cbPlugins.getSelectedItem()).getFileImportPlugin();
				if(rbName.isSelected()) {
					try {
						plugin.importFileBySearchObject(lResults.getSelectedValue());
					} catch(Throwable t) {
						//catch all calls to plugin with throwable to avoid plugins crashing pms
						log.error(String.format("Failed to query importFileBySearchObject for plugin %s", plugin.getName()), t);
					}
				}
				setUpdate(true);
				importPlugin = plugin;
				
				dispose();
			}
		});

		JButton bCancel = new JButton(Messages.getString("ML.FileUpdateWithPluginDialog.bCancel"));
		if(bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});

		pButtons = new JPanel();
		pButtons.add(bSearch);
		pButtons.add(bImport);
		pButtons.add(bCancel);
	}

	private void build() {
		FormLayout layout = new FormLayout("5px, r:p, 5px, p, 5px, f:p:g, 5px",
										   "5px, p, 5px, p, 5px, p, 5px, p, 5px, f:80:g, 5px, p, 5px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();
		
		builder.addLabel(Messages.getString("ML.FileUpdateWithPluginDialog.lImportBy"), cc.xy(2, 2));
		builder.add(rbName, cc.xy(4, 2));
		builder.add(rbId, cc.xy(6, 2));

		builder.addLabel(Messages.getString("ML.FileUpdateWithPluginDialog.lPlugin"), cc.xy(2, 4));
		builder.add(cbPlugins, cc.xyw(4, 4, 3));

		builder.add(lValueHeader, cc.xy(2, 6));
		builder.add(tfValue, cc.xyw(4, 6, 3));
		
		builder.addLabel(Messages.getString("ML.FileUpdateWithPluginDialog.lResults"), cc.xyw(2, 8, 5, CellConstraints.LEFT, CellConstraints.DEFAULT));

		JPanel pResults = new JPanel(new BorderLayout());
		pResults.setBorder(BorderFactory.createLineBorder(Color.lightGray));
		JPanel pResultsFiller = new JPanel();
		pResultsFiller.setBackground(lResults.getBackground());
		pResults.add(lResults, BorderLayout.NORTH);
		pResults.add(pResultsFiller, BorderLayout.CENTER);
		JScrollPane spResults = new JScrollPane(pResults);
		spResults.setBorder(BorderFactory.createEmptyBorder());
		builder.add(spResults, cc.xyw(2, 10, 5));

		builder.add(pButtons, cc.xyw(2, 12, 5));
		
		add(builder.getPanel());
	}
	
	private void handleRadioButtonSelectionChange() {
		if(rbId.isSelected()) {
			//update plugins combo box
			cbPlugins.removeAllItems();
			for(FileImportPlugin fileImportPlugin : PluginsFactory.getFileImportPlugins()) {
				if(fileImportPlugin.isImportByIdPossible()) {
					cbPlugins.addItem(new FileImportPluginWrapper(fileImportPlugin));
				}
			}
			
			//store current value
			nameValue = tfValue.getText();
			
			//update value
			lValueHeader.setText(Messages.getString("ML.FileUpdateWithPluginDialog.rbId"));
			tfValue.setText(idValue);
		} else if(rbName.isSelected()) {
			//update plugins combo box
			cbPlugins.removeAllItems();
			for(FileImportPlugin fileImportPlugin : PluginsFactory.getFileImportPlugins()) {
				if(fileImportPlugin.isSearchForFilePossible()) {
					cbPlugins.addItem(new FileImportPluginWrapper(fileImportPlugin));
				}
			}
			
			//store current value
			idValue = tfValue.getText();

			//update value
			lValueHeader.setText(Messages.getString("ML.FileUpdateWithPluginDialog.rbName"));
			tfValue.setText(nameValue);
		}
	}
	
	private class FileImportPluginWrapper {
		private FileImportPlugin fileImportPlugin;

		public FileImportPluginWrapper(FileImportPlugin fileImportPlugin) {
			setFileImportPlugin(fileImportPlugin);
		}

		public FileImportPlugin getFileImportPlugin() {
			return fileImportPlugin;
		}

		public void setFileImportPlugin(FileImportPlugin fileImportPlugin) {
			this.fileImportPlugin = fileImportPlugin;
		}
		
		@Override
		public String toString() {
			return fileImportPlugin == null ? "" : fileImportPlugin.getName();
		}
	}
}

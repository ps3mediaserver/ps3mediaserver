package net.pms.medialibrary.gui.dialogs.folderdialog;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOMediaLibraryFolder;
import net.pms.medialibrary.commons.dataobjects.comboboxitems.FileTypeCBItem;
import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.commons.enumarations.FileType;
import net.pms.medialibrary.commons.events.FolderDialogFolderUpdateEvent;
import net.pms.medialibrary.commons.events.FolderDialogActionListener;
import net.pms.medialibrary.commons.exceptions.ConditionException;
import net.pms.medialibrary.commons.exceptions.TemplateException;
import net.pms.medialibrary.commons.helpers.FolderHelper;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FolderDialog extends JDialog {
	private static final Logger log = LoggerFactory.getLogger(FolderDialog.class);
	private static final long serialVersionUID = 1L;
	
	private final int MIN_BUTTON_WIDTH = 60;
	
	private DOMediaLibraryFolder folder;
	private List<FolderDialogActionListener> folderDialogActionListeners = new ArrayList<FolderDialogActionListener>();
	private boolean isNewFolder;
	private FileTypeCBItem selectedFileType = new FileTypeCBItem();
	private boolean isInitializing = false;
	
	private JTextField tfName;
	private JLabel lName;
	private JPanel pButtons;
	private JLabel lFolderType;
	private JComboBox cbFileType;
	private FolderPropsTabbedPane tabs;

	private boolean isMediaLibraryFolderEnabled;
	
	public FolderDialog(DOMediaLibraryFolder folder, boolean isNewFilter, boolean isMediaLibraryFolderEnabled){
		this.folder = folder.clone();
		setTitle(String.format(Messages.getString("ML.FolderDialog.Title"), getMediaLibraryFolder().getName()));
		this.isNewFolder = isNewFilter;
		
		setMinimumSize(new Dimension(810, 100));
		
		((java.awt.Frame)getOwner()).setIconImage(new ImageIcon(FolderDialog.class.getResource("/resources/images/icon-32.png")).getImage());
		
		isInitializing = true;
		init();
		setMediaLibraryFolderEnabled(isMediaLibraryFolderEnabled);
		isInitializing = false;
	}
	
	public void addFolderDialogActionListener(FolderDialogActionListener l){
		if(!folderDialogActionListeners.contains(l)){
			folderDialogActionListeners.add(l);
		}
	}
	
	public DOMediaLibraryFolder getMediaLibraryFolder(){
		return folder;
	}
	
	public void setMediaLibraryFolderEnabled(boolean enabled){
		isMediaLibraryFolderEnabled = enabled;
		cbFileType.setEnabled(enabled);
	}
	
	public boolean getMediaLibraryFolderEnabled(){
		return isMediaLibraryFolderEnabled;
	}
	
	/*****************
	 * 
	 * Private Methods
	 * 
	 *****************/
	
	private void notifyFolderDialogAction(DOMediaLibraryFolder folder, DialogActionType actionType, boolean isNewFolder){
		for(FolderDialogActionListener l:folderDialogActionListeners){
			l.folderDialogActionReceived(new FolderDialogFolderUpdateEvent(this, getMediaLibraryFolder(), actionType, isNewFolder));
		}
	}

	private void init() {
		
		//Name
		lName = new JLabel(Messages.getString("ML.FolderDialog.Name"));
		tfName = new JTextField();
		tfName.setPreferredSize(new Dimension(200, tfName.getPreferredSize().height));
		tfName.setText(folder.getName());

		//Folder Type		
		cbFileType = new JComboBox(FolderHelper.getHelper().getAllFileTypeCBItems());

		cbFileType.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (isInitializing) { return; }

				FileType ft = ((FileTypeCBItem) cbFileType.getSelectedItem()).getFileType();
				if (tabs.hasConditions()) {
					int resp = JOptionPane.showConfirmDialog(SwingUtilities.getRoot(cbFileType), String.format(Messages.getString("ML.FolderDialog.ChangeFoldertypeMsg"), System.getProperty("line.separator")), Messages.getString("ML.General.MessageType.WARNING"), JOptionPane.YES_NO_OPTION);
					if (resp == JOptionPane.YES_OPTION) {
						tabs.resetConditions();
					} else {
						cbFileType.setSelectedItem(selectedFileType);
						return;
					}
				}

				tabs.setFileType(ft);
				folder.setFileType(ft);

				selectedFileType = FolderHelper.getHelper().getFileTypeCBItem(ft);
			}
		});
		lFolderType = new JLabel(Messages.getString("ML.FolderDialog.FolderType"));
		
		tabs = new FolderPropsTabbedPane(folder, folderDialogActionListeners);

		//Buttons		
		JButton buttonOk = new JButton(Messages.getString("ML.FolderDialog.bOk"));
		buttonOk.setName("buttonOk");
		if(buttonOk.getPreferredSize().width < MIN_BUTTON_WIDTH) buttonOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, buttonOk.getPreferredSize().height));
		buttonOk.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					DOMediaLibraryFolder tmpFolder = getDisplayedMediaLibraryFolder();
					tmpFolder.getInheritedFilter().validate();
					folder = tmpFolder;
					notifyFolderDialogAction(getMediaLibraryFolder(), DialogActionType.OK, isNewFolder);
					isNewFolder = false;
				} catch (Exception e2) {
					log.error("Save error: " + e2.getMessage(), e2);
					JOptionPane.showMessageDialog(tabs.getTopLevelAncestor(), e2.getMessage(), Messages.getString("ML.FolderDialog.SaveErrorHeader"), JOptionPane.WARNING_MESSAGE);
				}	
			}
		});		
		
		JButton buttonApply = new JButton(Messages.getString("ML.FolderDialog.bApply"));
		buttonApply.setName("buttonApply");
		if(buttonApply.getPreferredSize().width < MIN_BUTTON_WIDTH) buttonApply.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, buttonApply.getPreferredSize().height));
		buttonApply.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					DOMediaLibraryFolder tmpFolder = getDisplayedMediaLibraryFolder();
					tmpFolder.getInheritedFilter().validate();
					folder = tmpFolder;
					setTitle(String.format(Messages.getString("ML.FolderDialog.Title"), getMediaLibraryFolder().getName()));
					notifyFolderDialogAction(getMediaLibraryFolder(), DialogActionType.APPLY, isNewFolder);
					isNewFolder = false;
				} catch (Exception e2) {
					log.error("Save error: " + e2.getMessage(), e2);
					JOptionPane.showMessageDialog(tabs.getTopLevelAncestor(), e2.getMessage(), Messages.getString("ML.FolderDialog.SaveErrorHeader"), JOptionPane.WARNING_MESSAGE);
				}	
			}
		});
		
		JButton buttonCancel = new JButton(Messages.getString("ML.FolderDialog.bCancel"));
		buttonCancel.setName("buttonCancel");
		if(buttonCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) buttonCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, buttonCancel.getPreferredSize().height));
		buttonCancel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				notifyFolderDialogAction(getMediaLibraryFolder(), DialogActionType.CANCEL, isNewFolder);
			}
		});
		
		pButtons = new JPanel();
		pButtons.setLayout(new FlowLayout(FlowLayout.CENTER));
		pButtons.add(buttonOk);
		pButtons.add(buttonApply);
		pButtons.add(buttonCancel);
		
		selectedFileType = FolderHelper.getHelper().getFileTypeCBItem(folder.getFileType());
		cbFileType.setSelectedItem(selectedFileType);
		
		rebuildPanel();
		pack();
	}
	
	private void rebuildPanel(){
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 3px, p, p:grow, p, 3px, p, 3px, p, 3px", // columns
			"10px, p, 10px, fill:p:grow, p, 3px"); // raws
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		//Name & Filter Type
		builder.add(lName, cc.xy(2, 2));
		builder.add(tfName, cc.xy(4, 2));
		builder.add(lFolderType, cc.xy(6, 2));
		builder.add(cbFileType, cc.xy(8, 2));
		builder.addSeparator("", cc.xyw(2, 3, 9));
		builder.add(tabs, cc.xyw(2, 4, 9));

		builder.add(pButtons, cc.xyw(2, 5, 9));

		getContentPane().removeAll();
		getContentPane().add(builder.getPanel());
	}

	private DOMediaLibraryFolder getDisplayedMediaLibraryFolder() throws ConditionException, TemplateException {
		DOMediaLibraryFolder tmpFolder = folder;
		tmpFolder.setName(tfName.getText().trim());
		tmpFolder.setFileType(((FileTypeCBItem) cbFileType.getSelectedItem()).getFileType());
		tmpFolder.setInheritsConditions(tabs.getInheritsConditions());
	    tmpFolder.setFilter(tabs.getFilter());
		tmpFolder.setDisplayProperties(tabs.getDisplayProperties());
		tmpFolder.setInheritSort(tabs.isInheritSort());
		tmpFolder.setInheritDisplayFileAs(tabs.isInheritDisplayFileAs());
		tmpFolder.setDisplayItems(tabs.isDisplayItems());
		tmpFolder.setMaxFiles(tabs.getMaxFiles());
		
		return tmpFolder;
	}
	
	@Override
	public void pack(){
		super.pack();
		setMinimumSize(getSize());
	}
}

package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.exceptions.ConditionTypeException;
import net.pms.medialibrary.commons.interfaces.FileEditLinkedList;
import net.pms.medialibrary.storage.MediaLibraryStorage;

/**
 * Dialog used to visualize and edit file properties
 * @author pw
 *
 */
public class FileEditDialog extends JDialog {
	private static final long serialVersionUID = 2921067184273978956L;
	private final int MIN_BUTTON_WIDTH = 60;
	
	private FileEditLinkedList fileEditList;
	
	private JButton bPrevious;
	private JButton bNext;
	private JButton bOk;
	private JButton bCancel;
	private FileEditTabbedPane tpFileEdit;	

	public FileEditDialog(FileEditLinkedList fel) {
		((java.awt.Frame) getOwner()).setIconImage(new ImageIcon(getClass().getResource("/resources/images/icon-16.png")).getImage());
		setFileEditList(fel);
		setTitle(fileEditList.getSelected().getFilePath());
		setMinimumSize(new Dimension(400, 300));
		
		build();
	}

	public FileEditLinkedList getFileEditList() {
		return fileEditList;
	}

	public void setFileEditList(FileEditLinkedList fileEditList) {
		this.fileEditList = fileEditList;
	}

	private void build() {
		//initialize tabbed pane
		tpFileEdit = new FileEditTabbedPane(getFileEditList().getSelected());
		
		//initialize previous and next buttons
		bPrevious = new JButton(new ImageIcon(getClass().getResource("/resources/images/previous-16.png")));
		bPrevious.setToolTipText(Messages.getString("ML.FileEditDialog.bPrevious"));
		bPrevious.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean success = saveUpdatedFileInfo();
				if(success) {
					tpFileEdit.setContent(fileEditList.selectPreviousFile());
					setTitle(fileEditList.getSelected().getFilePath());
					refreshButtonStates();
				}
			}
		});
		
		bNext = new JButton(new ImageIcon(getClass().getResource("/resources/images/next-16.png")));
		bNext.setToolTipText(Messages.getString("ML.FileEditDialog.bNext"));
		bNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean success = saveUpdatedFileInfo();
				if(success) {
					tpFileEdit.setContent(fileEditList.selectNextFile());
					setTitle(fileEditList.getSelected().getFilePath());
					refreshButtonStates();
				}
			}
		});
		
		//initialize ok and cancel buttons
		bOk = new JButton(Messages.getString("ML.FileEditDialog.bOk"));
		if (bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				boolean success = saveUpdatedFileInfo();
				if(success) {
					dispose();
				}
			}
		});
		
		bCancel = new JButton(Messages.getString("ML.FileEditDialog.bCancel"));
		if (bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		
		refreshButtonStates();
		
		//build the panel
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, d, 3px, d, fill:10:grow, d, 3px, d, 3px", // columns
		        "3px, fill:10:grow, 3px, p, 3px"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(tpFileEdit, cc.xyw(2, 2, 7));
		builder.add(bPrevious, cc.xy(2, 4));
		builder.add(bNext, cc.xy(4, 4));
		builder.add(bOk, cc.xy(6, 4));
		builder.add(bCancel, cc.xy(8, 4));

		getContentPane().add(builder.getPanel());
	}
	
	private void refreshButtonStates() {
		bPrevious.setEnabled(fileEditList.hasPreviousFile());
		bNext.setEnabled(fileEditList.hasNextFile());
	}

	private boolean saveUpdatedFileInfo() {
		DOFileInfo fileInfo;
		try {
			fileInfo = tpFileEdit.getUpdatedFileInfo();
		} catch(ConditionTypeException ex) {
			String msg = String.format(Messages.getString("ML.FileEditDialog.InvalidInt"),Messages.getString("ML.Condition.Header.Type." + ex.getConditionType()), System.getProperty("line.separator"));
			JOptionPane.showMessageDialog(this, msg, "Save error", JOptionPane.ERROR_MESSAGE);
			return false;
		}
		
		MediaLibraryStorage.getInstance().updateFileInfo(fileInfo);
		return true;
	}
}

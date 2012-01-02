package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.interfaces.FileEditLinkedList;

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
		setTitle(fel.getSelected().getFilePath());
		setFileEditList(fel);
		
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
				//TODO: save changes
				tpFileEdit.setContent(fileEditList.selectPreviousFile());
				refreshButtonStates();
				pack();
			}
		});
		
		bNext = new JButton(new ImageIcon(getClass().getResource("/resources/images/next-16.png")));
		bNext.setToolTipText(Messages.getString("ML.FileEditDialog.bNext"));
		bNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//TODO: save changes
				tpFileEdit.setContent(fileEditList.selectNextFile());
				refreshButtonStates();
				pack();
			}
		});
		
		//initialize ok and cancel buttons
		bOk = new JButton(Messages.getString("ML.FileEditDialog.bOk"));
		if (bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bOk.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//TODO: save changes
				dispose();
			}
		});
		
		bCancel = new JButton(Messages.getString("ML.FileEditDialog.bCancel"));
		if (bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		bCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//TODO: save changes
				dispose();
			}
		});
		
		refreshButtonStates();
		
		//build the panel
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 3px, p, fill:10:grow, p, 3px, p, 3px", // columns
		        "3px, fill:p:grow, 3px, p, 3px"); // raws
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
}

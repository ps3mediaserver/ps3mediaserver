package net.pms.medialibrary.gui.dialogs.fileeditdialog;

import java.awt.Dimension;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOFileInfo;

public class FileEditDialog extends JDialog {
	private static final long serialVersionUID = 2921067184273978956L;
	private final int MIN_BUTTON_WIDTH = 60;
	
	private DOFileInfo editFileInfo;
	private List<DOFileInfo> filesToEdit;
	
	private JButton bPrevious;
	private JButton bNext;
	private JButton bOk;
	private JButton bCancel;
	private FileEditTabbedPane tpFileEdit;	

	public FileEditDialog(DOFileInfo editFileInfo, List<DOFileInfo> filesToEdit) {
		((java.awt.Frame) getOwner()).setIconImage(new ImageIcon(getClass().getResource("/resources/images/icon-16.png")).getImage());
		setTitle(editFileInfo.getFilePath());
		setEditFileInfo(editFileInfo);
		setFilesToEdit(filesToEdit);
		
		build();
	}


	public DOFileInfo getEditFileInfo() {
		return editFileInfo;
	}


	public void setEditFileInfo(DOFileInfo editFileInfo) {
		this.editFileInfo = editFileInfo;
	}


	public List<DOFileInfo> getFilesToEdit() {
		return filesToEdit;
	}


	public void setFilesToEdit(List<DOFileInfo> filesToEdit) {
		this.filesToEdit = filesToEdit;
	}


	private void build() {
		//initialize tabbed pane
		tpFileEdit = new FileEditTabbedPane(editFileInfo);
		
		//initialize previous and next buttons
		bPrevious = new JButton(new ImageIcon(getClass().getResource("/resources/images/previous-16.png")));
		bPrevious.setToolTipText(Messages.getString("ML.FileEditDialog.bPrevious"));
		bNext = new JButton(new ImageIcon(getClass().getResource("/resources/images/next-16.png")));
		bNext.setToolTipText(Messages.getString("ML.FileEditDialog.bNext"));
		
		//initialize ok and cancel buttons
		bOk = new JButton(Messages.getString("ML.FileEditDialog.bOk"));
		if (bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bCancel = new JButton(Messages.getString("ML.FileEditDialog.bCancel"));
		if (bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
		
		//build the panel
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("3px, p, 3px, p, fill:10:grow, p, 3px, p, 3px", // columns
		        "3px, fill:p:grow, 3px, p"); // raws
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		builder.add(tpFileEdit, cc.xyw(2, 2, 7));
		builder.add(bPrevious, cc.xy(2, 4));
		builder.add(bNext, cc.xy(4, 4));
		builder.add(bOk, cc.xy(6, 4));
		builder.add(bCancel, cc.xy(8, 4));

		getContentPane().add(builder.getPanel());
	}
}

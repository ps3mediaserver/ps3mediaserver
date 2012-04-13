package net.pms.medialibrary.gui.dialogs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOSpecialFolder;
import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.commons.events.SpecialFolderDialogActionEvent;
import net.pms.medialibrary.commons.events.SpecialFolderDialogActionListener;
import net.pms.medialibrary.gui.dialogs.folderdialog.FolderDialog;

public class SpecialFolderDialog extends JDialog {
	private static final long                       serialVersionUID = -3958360301187444404L;
	private DOSpecialFolder                         specialFolder;
	private JPanel                                  pButtons;
	private JTextField                              tfName;

	private List<SpecialFolderDialogActionListener> dialogListeners  = new ArrayList<SpecialFolderDialogActionListener>();

	public SpecialFolderDialog(DOSpecialFolder f) {
		((java.awt.Frame)this.getOwner()).setIconImage(new ImageIcon(FolderDialog.class.getResource("/resources/images/icon-32.png")).getImage());
		init();

		setTitle(f.getSpecialFolderImplementation().getName());
		setSpecialFolder(f);
	}

	public void addSpecialFolderDialogActionListener(SpecialFolderDialogActionListener l) {
		if (!dialogListeners.contains(l)) {
			dialogListeners.add(l);
		}
	}

	public void removeSpecialFolderDialogActionListener(SpecialFolderDialogActionListener l) {
		if (dialogListeners.contains(l)) {
			dialogListeners.remove(l);
		}
	}

	public void setSpecialFolder(DOSpecialFolder specialFolder) {
		this.specialFolder = specialFolder;
		tfName.setText(specialFolder.getName());
		refreshDialog();
	}

	public DOSpecialFolder getSpecialFolder() {
		return specialFolder;
	}

	private void init() {
		tfName = new JTextField();

		JButton bSave = new JButton(Messages.getString("ML.SpecialFolderDialog.bSave"));
		bSave.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireSpecialFolderDialogAction(DialogActionType.OK);
			}
		});

		JButton bApply = new JButton(Messages.getString("ML.SpecialFolderDialog.bApply"));
		bApply.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireSpecialFolderDialogAction(DialogActionType.APPLY);
			}
		});

		JButton bCancel = new JButton(Messages.getString("ML.SpecialFolderDialog.bCancel"));
		bCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				fireSpecialFolderDialogAction(DialogActionType.CANCEL);
			}
		});

		pButtons = new JPanel();
		pButtons.add(bSave);
		pButtons.add(bApply);
		pButtons.add(bCancel);
	}

	private void fireSpecialFolderDialogAction(DialogActionType actionType) {
		specialFolder.setName(tfName.getText());
		SpecialFolderDialogActionEvent e = new SpecialFolderDialogActionEvent(this, getSpecialFolder(), actionType);
		for (SpecialFolderDialogActionListener l : dialogListeners) {
			l.specialFolderDialogActionReceived(e);
		}
	}

	private void refreshDialog() {
		FormLayout layout = new FormLayout("3px, p, 3px, p:grow , 3px", 
				"3px, p, 3px, p, 3px, fill:p:grow, 3px, p, 3px, p, 3px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();

		builder.addLabel(Messages.getString("ML.SpecialFolderDialog.lName"), cc.xy(2, 2));
		builder.add(tfName, cc.xy(4, 2));
		builder.addSeparator("", cc.xyw(2, 4, 3));

		builder.add(getSpecialFolder().getSpecialFolderImplementation().getConfigurationPanel(), cc.xyw(2, 6, 3));
		builder.addSeparator("", cc.xyw(2, 8, 3));
		builder.add(pButtons, cc.xyw(2, 10, 3));

		getContentPane().removeAll();
		getContentPane().add(builder.getPanel());
		validate();
	}
}

package net.pms.newgui.plugins;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.plugins.PluginBase;

public class PluginDetailDialog extends JDialog {
	public static final Logger log = LoggerFactory.getLogger(PluginDetailDialog.class);
	private static final long serialVersionUID = 8030759789435633435L;
	private final int MIN_BUTTON_WIDTH = 60;
	
	private PluginBase plugin;
	private boolean hasConfigurationPanel = false;

	public PluginDetailDialog(PluginBase plugin) {
		((java.awt.Frame) getOwner()).setIconImage(new ImageIcon(getClass().getResource("/resources/images/icon-16.png")).getImage());
		setLayout(new BorderLayout());
		setTitle(plugin.getName());
		
		this.plugin = plugin;
		

		try {
			hasConfigurationPanel = plugin.getGlobalConfigurationPanel() != null;
		} catch(Throwable t) {
			//catch throwable for every external call to avoid having a plugin crash pms
			log.error(String.format("Failed to get configuration panel for plugin '%s'", plugin == null ? "null" : plugin.getName()), t);
		}
		
		build();
	}

	private void build() {
		Component comp = null;
		JPanel pAbout = new PluginAboutPanel(plugin);
		pAbout.setPreferredSize(new Dimension(0, 0));
		if(plugin.getGlobalConfigurationPanel() == null) {
			comp = pAbout;
			setTitle(Messages.getString("PluginGroupPanel.1"));
		} else {
			JTabbedPane tpPlugin = new JTabbedPane();
			tpPlugin.addTab(Messages.getString("PluginGroupPanel.2"), plugin.getGlobalConfigurationPanel());
			tpPlugin.addTab(Messages.getString("PluginGroupPanel.1"), pAbout);
			comp = tpPlugin;
			setTitle(Messages.getString("PluginGroupPanel.2"));
		}

		getContentPane().add(comp, BorderLayout.CENTER);
		getContentPane().add(getButtonsPanel(), BorderLayout.SOUTH);
	}
	
	private JPanel getButtonsPanel() {
		JPanel jPanelButtons = new JPanel();
		
		JButton bOk = new JButton(Messages.getString("ML.AddAutoFolderDialog.bOk"));
		if (bOk.getPreferredSize().width < MIN_BUTTON_WIDTH) bOk.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bOk.getPreferredSize().height));
		bOk.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleButtonAction(DialogActionType.OK);
			}
		});
		jPanelButtons.add(bOk);

		if(hasConfigurationPanel) {
			JButton bCancel = new JButton(Messages.getString("ML.AddAutoFolderDialog.bCancel"));
			if (bCancel.getPreferredSize().width < MIN_BUTTON_WIDTH) bCancel.setPreferredSize(new Dimension(MIN_BUTTON_WIDTH, bCancel.getPreferredSize().height));
			bCancel.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					handleButtonAction(DialogActionType.CANCEL);
				}
			});
			jPanelButtons.add(bCancel);			
		}
		
		return jPanelButtons;
	}

	private void handleButtonAction(DialogActionType actionType) {
		switch(actionType) {
		case OK:
			try {
				plugin.saveConfiguration();
			} catch(Throwable t) {
				//catch throwable for every external call to avoid having a plugin crash pms
				log.error(String.format("Failed to get save configuration for plugin '%s'", plugin == null ? "null" : plugin.getName()), t);
			}
			break;
		}
		dispose();
	}
}

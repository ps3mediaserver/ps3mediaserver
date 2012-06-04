package net.pms.newgui.plugins;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.DialogActionType;
import net.pms.medialibrary.gui.shared.ScrollablePanel;
import net.pms.plugins.PluginBase;

public class PluginDetailDialog extends JDialog {
	private static final long serialVersionUID = 8030759789435633435L;
	private static final Logger log = LoggerFactory.getLogger(PluginDetailDialog.class);
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
		//wrap the about panel in a scrollable panel to have the long description
		//in the about panel use the available width while stretching horizontally
		//as required
		ScrollablePanel sp = new ScrollablePanel(new GridLayout());
		sp.setScrollableWidth(ScrollablePanel.ScrollableSizeHint.FIT);
		sp.setScrollableHeight(ScrollablePanel.ScrollableSizeHint.STRETCH);
		sp.setScrollableBlockIncrement(ScrollablePanel.VERTICAL, ScrollablePanel.IncrementType.PERCENT, 200);
		sp.add(new PluginAboutPanel(plugin));
		
		JScrollPane spAbout = new JScrollPane(sp);
		spAbout.setBorder(BorderFactory.createEmptyBorder());
		//set the preferred size to 0 to only use the preferred size of the
		//configuration panel (if it exists) to resize the dialog
		spAbout.setPreferredSize(new Dimension(0, 0));
		
		JTabbedPane tpPlugin = new JTabbedPane();
		if(plugin.getGlobalConfigurationPanel() != null) {
			tpPlugin.addTab(Messages.getString("PluginGroupPanel.2"), plugin.getGlobalConfigurationPanel());
		}
		tpPlugin.addTab(Messages.getString("PluginGroupPanel.1"), spAbout);

		getContentPane().add(tpPlugin, BorderLayout.CENTER);
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

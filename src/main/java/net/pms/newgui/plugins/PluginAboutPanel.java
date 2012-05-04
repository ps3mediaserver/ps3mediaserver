package net.pms.newgui.plugins;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.newgui.components.LinkLabel;
import net.pms.plugins.PluginBase;

public class PluginAboutPanel extends JPanel {
	public static final Logger log = LoggerFactory.getLogger(PluginAboutPanel.class);
	private static final long serialVersionUID = 3008160735135291826L;
	
	private PluginBase plugin;

	public PluginAboutPanel(PluginBase plugin) {
		setLayout(new GridLayout());
		this.plugin = plugin;
		build();
	}

	private void build() {
		FormLayout layout = new FormLayout(
				"5px, p, 5px, fill:p:grow, 5px",
				"5px, p, 15px,  p, 5px,  f:p:g, 5px");
		PanelBuilder builder = new PanelBuilder(layout);
		builder.setOpaque(true);

		CellConstraints cc = new CellConstraints();

		Icon icon = null;
		try {
			icon = plugin.getPluginIcon();
		} catch(Throwable t) {
			//catch throwable for every external call to avoid having a plugin crash pms
			log.error(String.format("Failed to load icon for plugin '%s'", plugin == null ? "null" : plugin.getName()), t);
		}
		if(icon == null) {
			icon = new ImageIcon(getClass().getResource("/resources/images/icon-32.png"));
		}
		builder.add(new JLabel(icon), cc.xy(2, 2));

		String pluginName = "";
		try {
			pluginName = plugin.getName();
		} catch(Throwable t) {
			//catch throwable for every external call to avoid having a plugin crash pms
			log.error("Failed to load name for plugin", t);
		}
		JLabel lName = builder.addLabel(pluginName, cc.xy(4, 2));
		lName.setFont(lName.getFont().deriveFont(Font.BOLD));

		String longDescription = "";
		try {
			longDescription = "<html>" + plugin.getLongDescription() + "</html>";
		} catch(Throwable t) {
			//catch throwable for every external call to avoid having a plugin crash pms
			log.error(String.format("Failed to load long description for plugin '%s'", plugin == null ? "null" : plugin.getName()), t);
		}
		builder.addLabel(longDescription, cc.xyw(2, 4, 3));

		//web site and update labels
		String websiteUrl = null;
		try {
			websiteUrl = plugin.getWebSiteUrl();
		} catch(Throwable t) {
			//catch throwable for every external call to avoid having a plugin crash pms
			log.error("Failed to get web site url for plugin", t);
		}
		LinkLabel llWebSite = new LinkLabel(Messages.getString("PluginAboutPanel.1"), websiteUrl);
		if(websiteUrl == null || websiteUrl.equals("")) {
			llWebSite.setEnabled(false);
		}
		String updateUrl = null;
		try {
			updateUrl = plugin.getUpdateUrl();
		} catch(Throwable t) {
			//catch throwable for every external call to avoid having a plugin crash pms
			log.error("Failed to get update url for plugin", t);
		}
		LinkLabel llUpdate = new LinkLabel(Messages.getString("PluginAboutPanel.2"), updateUrl);
		if(updateUrl == null || updateUrl.equals("")) {
			llUpdate.setEnabled(false);
		}
		
		JPanel pLinks = new JPanel(new FlowLayout(FlowLayout.LEADING, 12, 0));
		pLinks.add(llWebSite);
		pLinks.add(llUpdate);
		
		builder.add(pLinks, cc.xyw(2, 6, 3, CellConstraints.LEFT, CellConstraints.BOTTOM));

		
		add(builder.getPanel());
	}
}

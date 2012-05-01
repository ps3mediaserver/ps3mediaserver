package net.pms.newgui.plugins;

import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.plugins.PluginBase;

/**
 * Shows a list of plugins under a separator showing the name
 * 
 * @author pw
 *
 */
public class PluginGroupPanel extends JPanel {
	private static final long serialVersionUID = 3632030292301129244L;
	public static final Logger log = LoggerFactory.getLogger(PluginGroupPanel.class);
	
	private String header;
	private List<PluginBase> plugins;
	
	/**
	 * Builds a new panel with the list of plugins under a separator showing the name
	 * @param name the header text
	 * @param plugins list of plugins
	 */
	public PluginGroupPanel(String name, List<PluginBase> plugins) {
		setLayout(new GridLayout());
		header = name;
		this.plugins = plugins;
		
		build();
	}

	/**
	 * Builds the panel
	 */
	private void build() {
		String rowStr = "5px, p, ";
		for(int i = 0;  i < plugins.size(); i++) {
			rowStr += "5px, p, ";
		}
		if(rowStr.endsWith(", ")) {
			rowStr += "5px";
		}
		
		FormLayout layout = new FormLayout(
				"10px, p, 15px, p, 15px, p, 15px, f:p:g, 15px, p, 10px",
				rowStr);
			PanelBuilder builder = new PanelBuilder(layout);
			builder.setOpaque(true);

			CellConstraints cc = new CellConstraints();
			
			//add header
			builder.addSeparator(header, cc.xyw(2, 2, 9));
			
			int row = 4;
			for(PluginBase plugin : plugins) {
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
				builder.add(new JLabel(icon), cc.xy(2, row));

				String pluginName = "";
				try {
					pluginName = plugin.getName();
				} catch(Throwable t) {
					//catch throwable for every external call to avoid having a plugin crash pms
					log.error(String.format("Failed to load name for plugin '%s'", plugin == null ? "null" : plugin.getName()), t);
				}
				JLabel lName = builder.addLabel(pluginName, cc.xy(4, row));
				lName.setFont(lName.getFont().deriveFont(Font.BOLD));
					
				String pluginVersion = "";
				try {
					pluginVersion = plugin.getVersion();
				} catch(Throwable t) {
					//catch throwable for every external call to avoid having a plugin crash pms
					log.error(String.format("Failed to load version for plugin '%s'", plugin == null ? "null" : plugin.getName()), t);
				}
				builder.addLabel(pluginVersion, cc.xy(6, row));

				String shortDescription = "";
				try {
					shortDescription = plugin.getShortDescription();
				} catch(Throwable t) {
					//catch throwable for every external call to avoid having a plugin crash pms
					log.error(String.format("Failed to load short description for plugin '%s'", plugin == null ? "null" : plugin.getName()), t);
				}
				builder.addLabel(shortDescription, cc.xy(8, row));
					
				JButton bConfig = new JButton();
				try {
					if(plugin.getGlobalConfigurationPanel() == null) {
						bConfig.setText(Messages.getString("PluginGroupPanel.1"));
					} else {
						bConfig.setText(Messages.getString("PluginGroupPanel.2"));
					}
				} catch(Throwable t) {
					//catch throwable for every external call to avoid having a plugin crash pms
					log.error(String.format("Failed to load configuration panel for plugin '%s'", plugin == null ? "null" : plugin.getName()), t);
				}
				builder.add(bConfig, cc.xy(10, row));
				
				row += 2;
			}
			
			removeAll();
			add(builder.getPanel());
	}
}

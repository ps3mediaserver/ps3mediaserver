package net.pms.newgui.plugins;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.pms.Messages;
import net.pms.newgui.components.ComboBoxItem;
import net.pms.notifications.NotificationCenter;
import net.pms.notifications.NotificationSubscriber;
import net.pms.notifications.types.PluginEvent;
import net.pms.plugins.DlnaTreeFolderPlugin;
import net.pms.plugins.FileDetailPlugin;
import net.pms.plugins.FileImportPlugin;
import net.pms.plugins.FinalizeTranscoderArgsListener;
import net.pms.plugins.Plugin;
import net.pms.plugins.PluginBase;
import net.pms.plugins.PluginsFactory;
import net.pms.plugins.StartStopListener;

/**
 * Panel for global plugin configuration<br>
 * It shows a combo box containing an entry for every registered plugin interface.
 * When selecting one, all registered plugins of this type are beign shown
 *  
 * @author pw
 *
 */
public class PluginsTab extends JPanel {
	private static final long serialVersionUID = -194502162257865623L;
	
	private JComboBox cbPluginType;
	private Map<Class<?>, PluginGroupPanel> pluginGroups;

	/**
	 * Creates a new instance of the panel, which is ready to use
	 */
	public PluginsTab() {
		setLayout(new BorderLayout(0, 5));		
		initPluginChangeListener();
	}

	private void initPluginChangeListener() {
		NotificationCenter.getInstance(PluginEvent.class).subscribe(new NotificationSubscriber<PluginEvent>() {			
			@Override
			public void onMessage(PluginEvent obj) {
				refreshPlugins();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void refreshPlugins() {
		ComboBoxItem<Class<?>> selected = null;
		Object selectedItem;
		if(cbPluginType != null && (selectedItem = cbPluginType.getSelectedItem()) != null && selectedItem instanceof ComboBoxItem<?>) {
			selected = (ComboBoxItem<Class<?>>) selectedItem;
		}
		init();
		build(selected == null ? Object.class : selected.getValue());
	}

	/**
	 * Initializes the combo box and plugin groups
	 */
	private void init() {		
		//initialize plugin groups
		Map<Class<?>, List<PluginBase>> pluginsByClass = new Hashtable<Class<?>, List<PluginBase>>();
		for(PluginBase plugin : PluginsFactory.getPlugins()) {
			List<Class<?>> usedInterfaces = new ArrayList<Class<?>>();
			if(plugin instanceof DlnaTreeFolderPlugin) {
				usedInterfaces.add(DlnaTreeFolderPlugin.class);
			}
			if(plugin instanceof FileDetailPlugin) {
				usedInterfaces.add(FileDetailPlugin.class);
			}
			if(plugin instanceof FileImportPlugin) {
				usedInterfaces.add(FileImportPlugin.class);
			}
			if(plugin instanceof FinalizeTranscoderArgsListener) {
				usedInterfaces.add(FinalizeTranscoderArgsListener.class);
			}
			if(plugin instanceof StartStopListener) {
				usedInterfaces.add(StartStopListener.class);
			}
			if(plugin instanceof Plugin) {
				usedInterfaces.add(Plugin.class);
			}
			
			for(Class<?> usedInterface : usedInterfaces) {
				List<PluginBase> plugins = pluginsByClass.get(usedInterface);
				if(plugins == null) {
					plugins = new ArrayList<PluginBase>();
					pluginsByClass.put(usedInterface, plugins);
				}
				
				if(!plugins.contains(plugin)) {
					plugins.add(plugin);
				}
			}
		}
		
		List<ComboBoxItem<Class<?>>> groupNames = new ArrayList<ComboBoxItem<Class<?>>>();
		groupNames.add(new ComboBoxItem<Class<?>>(Messages.getString("PluginsTab.2"), Object.class));
		
		pluginGroups = new Hashtable<Class<?>, PluginGroupPanel>();
		for(Class<?> c : pluginsByClass.keySet()) {
			String localizedName = Messages.getString("Plugin." + c.getSimpleName());
			groupNames.add(new ComboBoxItem<Class<?>>(localizedName, c));
			pluginGroups.put(c, new PluginGroupPanel(localizedName, pluginsByClass.get(c)));
		}
		Collections.sort(groupNames, new Comparator<ComboBoxItem<Class<?>>>() {
			@Override
			public int compare(ComboBoxItem<Class<?>> arg0, ComboBoxItem<Class<?>> arg1) {
				return arg0.getDisplayName().compareTo(arg1.getDisplayName());
			}			
		});

		//initialize combo box
		cbPluginType = new JComboBox(groupNames.toArray());
		cbPluginType.addActionListener(new ActionListener() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent arg0) {
				build(((ComboBoxItem<Class<?>>)cbPluginType.getSelectedItem()).getValue());
			}
		});
	}

	/**
	 * Build the panel by setting the combo box and center component in a {@link BorderLayout}
	 * @param centerComponent the {@link JComponent} to place in the center
	 */
	private void build(JComponent centerComponent) {
		JPanel pHeader = new JPanel(new FlowLayout(FlowLayout.LEFT));
		pHeader.add(new JLabel(Messages.getString("PluginsTab.1")));
		pHeader.add(cbPluginType);
		
		JScrollPane spCenter = new JScrollPane(centerComponent);
		spCenter.setBorder(BorderFactory.createEmptyBorder());
		
		removeAll();
		add(pHeader, BorderLayout.NORTH);
		add(spCenter, BorderLayout.CENTER);
		
		validate();
		repaint();
	}

	/**
	 * Select the correct entry in the combo box
	 * and show the according plugins
	 * @param classToShow one of the child interfaces of {@link PluginBase}
	 */
	@SuppressWarnings("unchecked")
	private void build(Class<?> classToShow) {
		//select combo box item
		for(int i = 0; i < cbPluginType.getItemCount(); i++) {
			if(((ComboBoxItem<Class<?>>)cbPluginType.getItemAt(i)).getValue().equals(classToShow)) {
				cbPluginType.setSelectedIndex(i);
				break;
			}
		}
		if(classToShow.equals(Object.class)) {
			//special case where the object class is being used to show all plugin types
			List<PluginGroupPanel> groupNames = Arrays.asList(pluginGroups.values().toArray(new PluginGroupPanel[pluginGroups.size()]));
			Collections.sort(groupNames, new Comparator<PluginGroupPanel>() {

				@Override
				public int compare(PluginGroupPanel arg0, PluginGroupPanel arg1) {
					return arg0.getHeader().compareTo(arg1.getHeader());
				}
				
			});
			
			JPanel pAllPlugins = new JPanel(new GridBagLayout());
		    GridBagConstraints gbc = new GridBagConstraints();
		    gbc.weightx = 1;
			int y = 0;
		    for(PluginGroupPanel pgp : groupNames) {
			    gbc.fill = GridBagConstraints.HORIZONTAL;
			    gbc.gridx = 0;
			    gbc.gridy = y++;
				pAllPlugins.add(pgp, gbc);
				//don't add the top padding to the top element
			    gbc.insets = new Insets(10, 0, 0, 0);
			}
		    
		    //avoid vertically center the panel
		    JPanel p = new JPanel(new BorderLayout());
		    p.add(pAllPlugins, BorderLayout.NORTH);
		    
			build(p);
		} else {
			PluginGroupPanel pluginGroup = pluginGroups.get(classToShow);
			if(pluginGroup != null) {
				build(pluginGroup);
			}
		}
	}
}
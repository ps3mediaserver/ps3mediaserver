/*
 * PS3 Media Server, for streaming any medias to your PS3.
 * Copyright (C) 2008  A.Brochard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; version 2
 * of the License only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package net.pms.newgui;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.apache.commons.lang.StringUtils;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.util.KeyedComboBoxModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.sun.jna.Platform;

public class NetworkTab {
	
	private JCheckBox  smcheckBox;
	private JCheckBox  tmcheckBox;
	private JCheckBox  blockBox;
	private JTextField host;
	private JTextField port; 
	private JComboBox langs ;
	private JComboBox networkinterfacesCBX;
	
	private final PmsConfiguration configuration;
	
	NetworkTab(PmsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public JComponent build() {
		FormLayout layout = new FormLayout(
                "left:pref, 2dlu, p, 2dlu , p, 2dlu, p, 2dlu, pref:grow", //$NON-NLS-1$
                "p, 0dlu, p, 0dlu, p, 3dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 15dlu, p, 3dlu,p, 3dlu, p,  3dlu, p, 3dlu, p, 3dlu, p,3dlu, p, 3dlu, p, 15dlu, p,3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 3dlu "); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(true);

        CellConstraints cc = new CellConstraints();
       
         smcheckBox = new JCheckBox(Messages.getString("NetworkTab.3")); //$NON-NLS-1$
        smcheckBox.setContentAreaFilled(false);
        smcheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setMinimized((e.getStateChange() == ItemEvent.SELECTED));
			}
        	
        });
        if (PMS.getConfiguration().isMinimized())
        	smcheckBox.setSelected(true);
        
        JComponent cmp = builder.addSeparator("General",  cc.xyw(1, 1, 9)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
       
     
       
       builder.addLabel(Messages.getString("NetworkTab.0"),  cc.xy(1,  7)); //$NON-NLS-1$
       final KeyedComboBoxModel kcbm = new KeyedComboBoxModel(new Object[] { "nl", "en", "fr", "de", "gr, "is", "it", "ja", "pl", "pt", "es", "se", "ru" }, new Object[] { "Dutch", Messages.getString("NetworkTab.9"), Messages.getString("NetworkTab.10"), "German", "Greek", "Icelandic", "Italian", "Japanese", "Polish", "Portuguese", "Spanish", "Swedish", "Russian" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$
       langs = new JComboBox(kcbm);
      langs.setEditable(false);
      //langs.setSelectedIndex(0);
      String defaultLang = null;
      if (configuration.getLanguage() != null && configuration.getLanguage().length() > 0) {
    	  defaultLang = configuration.getLanguage();
      } else {
    	  defaultLang = Locale.getDefault().getLanguage();
      }
      if (defaultLang == null)
    	  defaultLang = "en"; //$NON-NLS-1$
     kcbm.setSelectedKey(defaultLang);
     if (langs.getSelectedIndex() == -1)
    	 langs.setSelectedIndex(0);
      
      langs.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setLanguage((String) kcbm.getSelectedKey());
					
				}
			}
     	
     });
       builder.add(langs, cc.xyw(3, 7,7));
       
       builder.add(smcheckBox,          cc.xyw(1,  9, 9));
       
       JButton service = new JButton(Messages.getString("NetworkTab.4")); //$NON-NLS-1$
       service.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (PMS.get().installWin32Service()) {
					JOptionPane.showMessageDialog(
							(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
		                    Messages.getString("NetworkTab.11") + //$NON-NLS-1$
		                    Messages.getString("NetworkTab.12"), //$NON-NLS-1$
		                    "Information", //$NON-NLS-1$
		                    JOptionPane.INFORMATION_MESSAGE);
					
				} else {
					JOptionPane.showMessageDialog(
							(JFrame) (SwingUtilities.getWindowAncestor((Component) PMS.get().getFrame())),
		                    Messages.getString("NetworkTab.14"), //$NON-NLS-1$
		                    "Error", //$NON-NLS-1$
		                    JOptionPane.ERROR_MESSAGE);
				}
			}
 		  
 	  });
 	  builder.add(service,          cc.xy(1,  11));
      if (System.getProperty(LooksFrame.START_SERVICE) != null || !Platform.isWindows())
    	  service.setEnabled(false);
 	
       
     
    	  
        host = new JTextField(PMS.getConfiguration().getServerHostname());
        host.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
       			configuration.setHostname(host.getText());
       			PMS.get().getFrame().setReloadable(true);
       		}
        });
       // host.setEnabled( StringUtils.isBlank(configuration.getNetworkInterface())) ;
        port = new JTextField(configuration.getServerPort()!=5001?"" + configuration.getServerPort():""); //$NON-NLS-1$ //$NON-NLS-2$
        port.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
       			try {
       				String p = port.getText();
       				if (StringUtils.isEmpty(p))
       					p = "5001"; //$NON-NLS-1$
    				int ab = Integer.parseInt(p);
    				configuration.setServerPort(ab);
    				PMS.get().getFrame().setReloadable(true);
    			} catch (NumberFormatException nfe) {}
       			
       		}
        });
        
        cmp = builder.addSeparator(Messages.getString("NetworkTab.22"),  cc.xyw(1, 21,9)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
        ArrayList<String> names = new ArrayList<String>();
        names.add(""); //$NON-NLS-1$
        ArrayList<String> interfaces = new ArrayList<String>();
        interfaces.add(""); //$NON-NLS-1$
        Enumeration<NetworkInterface> enm;
		try {
			enm = NetworkInterface.getNetworkInterfaces();
			while (enm.hasMoreElements()) 
			{
			  NetworkInterface ni = enm.nextElement();
			  // check for interface has at least one ip address.
			  if ( ni.getInetAddresses().hasMoreElements() )
			  {
			    names.add(ni.getName());
			    String displayName = ni.getDisplayName();
			    if ( displayName == null )
			      displayName = ni.getName();
			    interfaces.add(displayName.trim());
			  }
			}
		} catch (SocketException e1) {
			PMS.error(null, e1);
		}
		
			
        final KeyedComboBoxModel networkInterfaces = new KeyedComboBoxModel(names.toArray(), interfaces.toArray() );
        networkinterfacesCBX = new JComboBox(networkInterfaces);
        networkInterfaces.setSelectedKey(configuration.getNetworkInterface());
        networkinterfacesCBX.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					configuration.setNetworkInterface((String) networkInterfaces.getSelectedKey());
					//host.setEnabled( StringUtils.isBlank(configuration.getNetworkInterface())) ;
					PMS.get().getFrame().setReloadable(true);
				}
			}
     	
     });
        
        builder.addLabel(Messages.getString("NetworkTab.20"),  cc.xy(1,  23)); //$NON-NLS-1$
        builder.add(networkinterfacesCBX,          cc.xyw(3,  23, 7)); 
        builder.addLabel(Messages.getString("NetworkTab.23"),  cc.xy(1,  25)); //$NON-NLS-1$
        builder.add(host,          cc.xyw(3,  25, 7)); 
        builder.addLabel(Messages.getString("NetworkTab.24"),  cc.xy(1, 27)); //$NON-NLS-1$
        builder.add(port,          cc.xyw(3,  27, 7)); 
       
        /*
       cmp = builder.addSeparator(Messages.getString("NetworkTab.25"),  cc.xyw(1, 31, 9)); //$NON-NLS-1$
       cmp = (JComponent) cmp.getComponent(0);
       cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
     
       encoding = new JTextField(PMS.getConfiguration().getCharsetEncoding());
       
       encoding.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			try {
   				PMS.getConfiguration().setCharsetEncoding(encoding.getText());
   			} catch (NumberFormatException nfe) {
   			}
   			
   		}
       	   
          });
       builder.addLabel(Messages.getString("NetworkTab.26"),  cc.xy(1,  33)); //$NON-NLS-1$
       builder.add(encoding,          cc.xyw(3,  33, 7)); 
       */
       cmp = builder.addSeparator(Messages.getString("NetworkTab.27"),  cc.xyw(1, 37, 9)); //$NON-NLS-1$
       cmp = (JComponent) cmp.getComponent(0);
       cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
       
       tmcheckBox = new JCheckBox(Messages.getString("NetworkTab.28")); //$NON-NLS-1$
       tmcheckBox.setContentAreaFilled(false);
       tmcheckBox.setSelected(PMS.getConfiguration().isTurboModeEnabled());
       tmcheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setTurboModeEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
       	
       }); builder.add(tmcheckBox,          cc.xyw(1,  39, 9));
       tmcheckBox.setEnabled(false);
       
       blockBox = new JCheckBox(Messages.getString("NetworkTab.29")); //$NON-NLS-1$
       blockBox.setContentAreaFilled(false);
       blockBox.setSelected(PMS.getConfiguration().getTrancodeBlocksMultipleConnections());
       blockBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setTranscodeBlocksMultipleConnections((e.getStateChange() == ItemEvent.SELECTED));
			}
       	
       }); builder.add(blockBox,          cc.xyw(1,  41, 9));
       blockBox.setEnabled(false);
        return builder.getPanel();
	}
	
	
}

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

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
	
	private JTextField maxbuffer;
	private JTextField seekpos;
	private JCheckBox  tncheckBox;
	private JCheckBox  cacheenable;
	private JCheckBox  smcheckBox;
	private JCheckBox  tmcheckBox;
	private JCheckBox  blockBox;
	private JCheckBox  archive;
	private JTextField host;
	private JTextField port; 
	private JTextField encoding ;
	private JComboBox nbcores ;
	private JComboBox langs ;
	
	private final PmsConfiguration configuration;
	
	NetworkTab(PmsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	public JComponent build() {
		FormLayout layout = new FormLayout(
                "left:pref, 2dlu, p, 2dlu , p, 2dlu, p, 2dlu, pref:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu,p, 15dlu, p, 3dlu, p, 3dlu,p, 3dlu, p,  15dlu, p, 3dlu, p, 3dlu, p,3dlu, p, 15dlu, p, 3dlu, p,3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 3dlu "); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(true);

        CellConstraints cc = new CellConstraints();
        
         tncheckBox = new JCheckBox(Messages.getString("NetworkTab.2")); //$NON-NLS-1$
        tncheckBox.setContentAreaFilled(false);
        tncheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setThumbnailsEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
        	
        });
        if (PMS.getConfiguration().getThumbnailsEnabled())
        	tncheckBox.setSelected(true);
        
         smcheckBox = new JCheckBox(Messages.getString("NetworkTab.3")); //$NON-NLS-1$
        smcheckBox.setContentAreaFilled(false);
        smcheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setMinimized((e.getStateChange() == ItemEvent.SELECTED));
			}
        	
        });
        if (PMS.getConfiguration().isMinimized())
        	smcheckBox.setSelected(true);
        
        maxbuffer = new JTextField("" + configuration.getMaxMemoryBufferSize()); //$NON-NLS-1$
        maxbuffer.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
    			try {
    				int ab = Integer.parseInt(maxbuffer.getText());
    				configuration.setMaxMemoryBufferSize(ab);
    			} catch (NumberFormatException nfe) {
    			}
    			
    		}
        	   
           });
        
        builder.addSeparator(Messages.getString("NetworkTab.5"),  cc.xyw(1, 1, 9)); //$NON-NLS-1$
        
        builder.addLabel(Messages.getString("NetworkTab.6"),  cc.xy(1,  3)); //$NON-NLS-1$
        builder.add(maxbuffer,          cc.xyw(3,  3, 7)); 
        
        builder.addLabel(Messages.getString("NetworkTab.7") + Runtime.getRuntime().availableProcessors() + Messages.getString("NetworkTab.8"),  cc.xy(1,  5)); //$NON-NLS-1$ //$NON-NLS-2$
        
        nbcores = new JComboBox(new Object [] {"1", "2", "4", "8"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        nbcores.setEditable(false);
        if (PMS.getConfiguration().getNumberOfCpuCores() >0 && PMS.getConfiguration().getNumberOfCpuCores() < 32) {
        	nbcores.setSelectedItem("" + PMS.getConfiguration().getNumberOfCpuCores()); //$NON-NLS-1$
        } else
        	nbcores.setSelectedIndex(0);
  
        nbcores.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setNumberOfCpuCores(Integer.parseInt(e.getItem().toString().substring(0, 1)));
 			}
        	
        });
        builder.add(nbcores,          cc.xyw(3,  5, 7)); 
        
       
       seekpos = new JTextField("" + configuration.getThumbnailSeekPos()); //$NON-NLS-1$
       seekpos.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			try {
   				int ab = Integer.parseInt(seekpos.getText());
   				configuration.setThumbnailSeekPos(ab);
   			} catch (NumberFormatException nfe) {
   			}
   			
   		}
       	   
          });
       
       builder.addLabel(Messages.getString("NetworkTab.0"),  cc.xy(1,  7)); //$NON-NLS-1$
       final KeyedComboBoxModel kcbm = new KeyedComboBoxModel(new Object[] { "en", "fr" }, new Object[] { Messages.getString("NetworkTab.9"), Messages.getString("NetworkTab.10") }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
       langs = new JComboBox(kcbm);
      langs.setEditable(false);
      //langs.setSelectedIndex(0);
      String defaultLang = null;
      if (configuration.getLanguage() != null && configuration.getLanguage().length() > 0) {
    	  defaultLang = configuration.getLanguage();
      } else {
    	  defaultLang = Locale.getDefault().getLanguage();
      }
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
       
       builder.add(smcheckBox,          cc.xy(3,  9));
       
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
 	  builder.add(service,          cc.xy(3,  11));
      if (System.getProperty(LooksFrame.START_SERVICE) != null || !Platform.isWindows())
    	  service.setEnabled(false);
 	
       
       builder.addSeparator(Messages.getString("NetworkTab.15"),  cc.xyw(1, 13, 9)); //$NON-NLS-1$
       
       archive = new JCheckBox(Messages.getString("NetworkTab.1")); //$NON-NLS-1$
       archive.setContentAreaFilled(false);
       archive.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setArchiveBrowsing(e.getStateChange() == ItemEvent.SELECTED);
				PMS.get().getFrame().setReloadable(true);
			}
       	
       });
       if (PMS.getConfiguration().isArchiveBrowsing())
    	   archive.setSelected(true);
       
       
       
       builder.add(tncheckBox,          cc.xyw(1,  15, 2));
       
       builder.add(archive,          cc.xy(3,  15));
       
       builder.addLabel(Messages.getString("NetworkTab.16"),  cc.xy(1,  17)); //$NON-NLS-1$
       builder.add(seekpos,          cc.xyw(3,  17, 5));
      
       final JButton cachereset = new JButton(Messages.getString("NetworkTab.18")); //$NON-NLS-1$
 	  
       cacheenable = new JCheckBox(Messages.getString("NetworkTab.17")); //$NON-NLS-1$
       cacheenable.setContentAreaFilled(false);
       cacheenable.setSelected(PMS.getConfiguration().getUseCache());
       cacheenable.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setUseCache((e.getStateChange() == ItemEvent.SELECTED));
				cachereset.setEnabled(PMS.getConfiguration().getUseCache());
				((LooksFrame) PMS.get().getFrame()).setReloadable(true);
				if ((LooksFrame) PMS.get().getFrame() != null)
					((LooksFrame) PMS.get().getFrame()).getFt().setScanLibraryEnabled(PMS.getConfiguration().getUseCache());
			}
      	
      });
     
      
       //cacheenable.setEnabled(false);
       
    	  builder.add(cacheenable,          cc.xyw(1,  19, 2));
    	  
    	  
    	  cachereset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				PMS.get().getDatabase().init(true);
			}
    		  
    	  });
    	  builder.add(cachereset,          cc.xyw(3,  19, 5));
    	  
    	  
    	  cachereset.setEnabled(PMS.getConfiguration().getUseCache());
    	  
        host = new JTextField(PMS.getConfiguration().getServerHostname());
        host.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
       			configuration.setHostname(host.getText());
       			
       		}
        });
        port = new JTextField(configuration.getServerPort()!=5001?"" + configuration.getServerPort():""); //$NON-NLS-1$ //$NON-NLS-2$
        port.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
       			try {
    				int ab = Integer.parseInt(port.getText());
    				configuration.setServerPort(ab);
    			} catch (NumberFormatException nfe) {}
       			
       		}
        });
        
        builder.addSeparator(Messages.getString("NetworkTab.22"),  cc.xyw(1, 21,9)); //$NON-NLS-1$
        builder.addLabel(Messages.getString("NetworkTab.23"),  cc.xy(1,  23)); //$NON-NLS-1$
        builder.add(host,          cc.xyw(3,  23, 7)); 
        builder.addLabel(Messages.getString("NetworkTab.24"),  cc.xy(1, 27)); //$NON-NLS-1$
        builder.add(port,          cc.xyw(3,  27, 7)); 
       
       
       builder.addSeparator(Messages.getString("NetworkTab.25"),  cc.xyw(1, 31, 9)); //$NON-NLS-1$
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
       builder.add(encoding,          cc.xyw(3,  33, 2)); 
       
       builder.addSeparator(Messages.getString("NetworkTab.27"),  cc.xyw(1, 37, 9)); //$NON-NLS-1$
       
       tmcheckBox = new JCheckBox(Messages.getString("NetworkTab.28")); //$NON-NLS-1$
       tmcheckBox.setContentAreaFilled(false);
       tmcheckBox.setSelected(PMS.getConfiguration().isTurboModeEnabled());
       tmcheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setTurboModeEnabled((e.getStateChange() == ItemEvent.SELECTED));
			}
       	
       }); builder.add(tmcheckBox,          cc.xyw(1,  39, 5));
       
       blockBox = new JCheckBox(Messages.getString("NetworkTab.29")); //$NON-NLS-1$
       blockBox.setContentAreaFilled(false);
       blockBox.setSelected(PMS.getConfiguration().getTrancodeBlocksMultipleConnections());
       blockBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.getConfiguration().setTranscodeBlocksMultipleConnections((e.getStateChange() == ItemEvent.SELECTED));
			}
       	
       }); builder.add(blockBox,          cc.xyw(1,  41, 5));
       blockBox.setEnabled(false);
        return builder.getPanel();
	}
	
	
}

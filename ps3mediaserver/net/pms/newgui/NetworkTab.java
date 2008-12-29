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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.io.CacheManager;
import net.pms.util.KeyedComboBoxModel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

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
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu,p, 3dlu, p,  15dlu, p, 3dlu, p, 3dlu, p,3dlu, p, 15dlu, p, 3dlu, p,3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 3dlu "); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(true);

        CellConstraints cc = new CellConstraints();
        
         tncheckBox = new JCheckBox(Messages.getString("NetworkTab.2")); //$NON-NLS-1$
        tncheckBox.setContentAreaFilled(false);
        tncheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setThumbnails(e.getStateChange() == ItemEvent.SELECTED);
			}
        	
        });
        if (PMS.get().isThumbnails())
        	tncheckBox.setSelected(true);
        
         smcheckBox = new JCheckBox(Messages.getString("NetworkTab.3")); //$NON-NLS-1$
        smcheckBox.setContentAreaFilled(false);
        smcheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMinimized(e.getStateChange() == ItemEvent.SELECTED);
			}
        	
        });
        if (PMS.get().isMinimized())
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
        if (PMS.get().getNbcores() >0 && PMS.get().getNbcores() < 32) {
        	nbcores.setSelectedItem("" + PMS.get().getNbcores()); //$NON-NLS-1$
        } else
        	nbcores.setSelectedIndex(0);
  
        nbcores.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setNbcores(Integer.parseInt(e.getItem().toString().substring(0, 1)));
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
       /*
       JButton service = new JButton("Install PMS Service");
       service.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new PMSService().install();
			}
 		  
 	  });
 	  builder.add(service,          cc.xyw(3,  7, 1));
      
 	 JButton serviceun = new JButton("Uninstall PMS Service");
 	serviceun.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new PMSService().uninstall();
			}
		  
	  });
	  builder.add(serviceun,          cc.xyw(4,  7, 1));
    */
       
       builder.addSeparator(Messages.getString("NetworkTab.15"),  cc.xyw(1, 11, 9)); //$NON-NLS-1$
       
       archive = new JCheckBox(Messages.getString("NetworkTab.1")); //$NON-NLS-1$
       archive.setContentAreaFilled(false);
       archive.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.configuration.setArchiveBrowsing(e.getStateChange() == ItemEvent.SELECTED);
				PMS.get().getFrame().setReloadable(true);
			}
       	
       });
       if (PMS.configuration.isArchiveBrowsing())
    	   archive.setSelected(true);
       
       
       
       builder.add(tncheckBox,          cc.xyw(1,  13, 2));
       
       builder.add(archive,          cc.xy(3,  13));
       
       builder.addLabel(Messages.getString("NetworkTab.16"),  cc.xy(1,  15)); //$NON-NLS-1$
       builder.add(seekpos,          cc.xyw(3,  15, 5));
      

       cacheenable = new JCheckBox(Messages.getString("NetworkTab.17")); //$NON-NLS-1$
       cacheenable.setContentAreaFilled(false);
       cacheenable.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setUsecache(e.getStateChange() == ItemEvent.SELECTED);
			}
      	
      });
      if (PMS.get().isUsecache())
    	  cacheenable.setSelected(true);
       cacheenable.setEnabled(false);
       
    	  builder.add(cacheenable,          cc.xyw(1,  17, 5));
    	  
    	  
    	  JButton cachereset = new JButton(Messages.getString("NetworkTab.18")); //$NON-NLS-1$
    	  cachereset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					CacheManager.resetCache();
				} catch (IOException e1) {
					PMS.debug("Cache unexpected error: " + e1.getMessage()); //$NON-NLS-1$
				}
			}
    		  
    	  });
    	  builder.add(cachereset,          cc.xyw(3,  17, 5));
    	  cachereset.setEnabled(false);
       
        host = new JTextField(PMS.get().getHostname());
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
        
        builder.addSeparator(Messages.getString("NetworkTab.22"),  cc.xyw(1, 19,9)); //$NON-NLS-1$
        builder.addLabel(Messages.getString("NetworkTab.23"),  cc.xy(1,  21)); //$NON-NLS-1$
        builder.add(host,          cc.xyw(3,  21, 7)); 
        builder.addLabel(Messages.getString("NetworkTab.24"),  cc.xy(1, 25)); //$NON-NLS-1$
        builder.add(port,          cc.xyw(3,  25, 7)); 
       
       
       builder.addSeparator(Messages.getString("NetworkTab.25"),  cc.xyw(1, 29, 9)); //$NON-NLS-1$
       encoding = new JTextField(PMS.get().getEncoding());
       
       encoding.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			try {
   				PMS.get().setCharsetencoding(encoding.getText());
   			} catch (NumberFormatException nfe) {
   			}
   			
   		}
       	   
          });
       builder.addLabel(Messages.getString("NetworkTab.26"),  cc.xy(1,  31)); //$NON-NLS-1$
       builder.add(encoding,          cc.xyw(3,  31, 2)); 
       
       builder.addSeparator(Messages.getString("NetworkTab.27"),  cc.xyw(1, 35, 9)); //$NON-NLS-1$
       
       tmcheckBox = new JCheckBox(Messages.getString("NetworkTab.28")); //$NON-NLS-1$
       tmcheckBox.setContentAreaFilled(false);
       tmcheckBox.setSelected(PMS.get().isTurbomode());
       tmcheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setTurbomode(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       }); builder.add(tmcheckBox,          cc.xyw(1,  37, 5));
       
       blockBox = new JCheckBox(Messages.getString("NetworkTab.29")); //$NON-NLS-1$
       blockBox.setContentAreaFilled(false);
       blockBox.setSelected(PMS.get().isTranscode_block_multiple_connections());
       blockBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setTranscode_block_multiple_connections(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       }); builder.add(blockBox,          cc.xyw(1,  39, 5));
       blockBox.setEnabled(false);
        return builder.getPanel();
	}
	
	
}

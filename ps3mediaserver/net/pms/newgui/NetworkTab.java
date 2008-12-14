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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import net.pms.PMS;
import net.pms.io.CacheManager;

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
	private JTextField host;
	private JTextField port; 
	private JTextField encoding ;
	private JComboBox nbcores ;
	
	public JComponent build() {
		FormLayout layout = new FormLayout(
                "left:pref, 2dlu, p, 100dlu , pref:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p,3dlu, p, 15dlu, p, 3dlu, p,3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu,p, 3dlu, p, 3dlu ");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
         tncheckBox = new JCheckBox("Thumbnails generation");
        tncheckBox.setContentAreaFilled(false);
        tncheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setThumbnails(e.getStateChange() == ItemEvent.SELECTED);
			}
        	
        });
        if (PMS.get().isThumbnails())
        	tncheckBox.setSelected(true);
        
         smcheckBox = new JCheckBox("Start minimized");
        smcheckBox.setContentAreaFilled(false);
        smcheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setMinimized(e.getStateChange() == ItemEvent.SELECTED);
			}
        	
        });
        if (PMS.get().isMinimized())
        	smcheckBox.setSelected(true);
        
        maxbuffer = new JTextField("" + PMS.get().getMaxMemoryBufferSize());
        maxbuffer.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
    			try {
    				int ab = Integer.parseInt(maxbuffer.getText());
    				if (ab > 630)
    					ab = 630;
    				PMS.get().setMaxMemoryBufferSize(ab);
    			} catch (NumberFormatException nfe) {
    			}
    			
    		}
        	   
           });
        
        builder.addSeparator("General settings",  cc.xyw(1, 1, 5));
        
        builder.addLabel("Transcode buffer maximum size, in megabytes (maximum: 650):",  cc.xy(1,  3));
        builder.add(maxbuffer,          cc.xyw(3,  3, 2)); 
        
        builder.addLabel("Number of cores used for transcoding: (it seems you have " + Runtime.getRuntime().availableProcessors() + " core(s) available)",  cc.xy(1,  5));
        
        nbcores = new JComboBox(new Object [] {"1", "2", "4", "8"});
        nbcores.setEditable(false);
        if (PMS.get().getNbcores() >0 && PMS.get().getNbcores() < 32) {
        	nbcores.setSelectedItem("" + PMS.get().getNbcores());
        } else
        	nbcores.setSelectedIndex(0);
  
        nbcores.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setNbcores(Integer.parseInt(e.getItem().toString().substring(0, 1)));
 			}
        	
        });
        builder.add(nbcores,          cc.xyw(3,  5, 2)); 
        
       
       seekpos = new JTextField("" + PMS.get().getThumbnail_seek_pos());
       seekpos.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			try {
   				int ab = Integer.parseInt(seekpos.getText());
   				PMS.get().setThumbnail_seek_pos(ab);
   			} catch (NumberFormatException nfe) {
   			}
   			
   		}
       	   
          });
       
       
       builder.add(smcheckBox,          cc.xy(1,  7));
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
       
       builder.addSeparator("Navigation/Parsing settings",  cc.xyw(1, 9, 5));
       
       builder.add(tncheckBox,          cc.xyw(1,  11, 5));
       
       builder.addLabel("Thumbnail seeking position (in seconds):",  cc.xy(1,  13));
       builder.add(seekpos,          cc.xyw(3,  13, 2));
      

       cacheenable = new JCheckBox("Enable the parsing/metadata files cache (Parsing is only done at first access of a folder)");
       cacheenable.setContentAreaFilled(false);
       cacheenable.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setUsecache(e.getStateChange() == ItemEvent.SELECTED);
			}
      	
      });
      if (PMS.get().isUsecache())
    	  cacheenable.setSelected(true);
       cacheenable.setEnabled(false);
       
    	  builder.add(cacheenable,          cc.xyw(1,  15, 5));
    	  
    	  
    	  JButton cachereset = new JButton("Reset Cache");
    	  cachereset.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					CacheManager.resetCache();
				} catch (IOException e1) {
					PMS.debug("Cache unexpected error: " + e1.getMessage());
				}
			}
    		  
    	  });
    	  builder.add(cachereset,          cc.xyw(1,  17, 1));
    	  cachereset.setEnabled(false);
       
        host = new JTextField(PMS.get().getHostname());
        host.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
       			PMS.get().setHostname(host.getText());
       			
       		}
        });
        port = new JTextField(PMS.get().getPort()!=5001?"" + PMS.get().getPort():"");
        port.addKeyListener(new KeyListener() {

    		@Override
    		public void keyPressed(KeyEvent e) {}
    		@Override
    		public void keyTyped(KeyEvent e) {}
    		@Override
    		public void keyReleased(KeyEvent e) {
       			try {
    				int ab = Integer.parseInt(port.getText());
    				PMS.get().setPort(ab);
    			} catch (NumberFormatException nfe) {}
       			
       		}
        });
        
        builder.addSeparator("Network Settings, change them only if troubles",  cc.xyw(1, 19, 5));
        builder.addLabel("Force IP of the server: [Need Application Restart]",  cc.xy(1,  21));
        builder.add(host,          cc.xyw(3,  21, 2)); 
        builder.addLabel("Force port of the server (5001 by default) [Need Application Restart]:",  cc.xy(1, 25));
        builder.add(port,          cc.xyw(3,  25, 2)); 
       
       
       builder.addSeparator("PS3 Settings",  cc.xyw(1, 29, 5));
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
       builder.addLabel("Character encoding of your PS3 file names (see XMB->System settings->Charset):",  cc.xy(1,  31));
       builder.add(encoding,          cc.xyw(3,  31, 2)); 
       
       builder.addSeparator("Unused settings you shouldn't use :p",  cc.xyw(1, 35, 5));
       
       tmcheckBox = new JCheckBox("Turbo mode (enable tcp_nodelay) / be careful, not sure if that's ok to do this");
       tmcheckBox.setContentAreaFilled(false);
       tmcheckBox.setSelected(PMS.get().isTurbomode());
       tmcheckBox.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				PMS.get().setTurbomode(e.getStateChange() == ItemEvent.SELECTED);
			}
       	
       }); builder.add(tmcheckBox,          cc.xyw(1,  37, 5));
       
       blockBox = new JCheckBox("Block incoming request for the same file from PS3 when transcode has started");
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

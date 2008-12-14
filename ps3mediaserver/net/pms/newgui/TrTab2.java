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

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.pms.PMS;
import net.pms.encoders.Player;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TrTab2 {
	
	private JCheckBox  disableSubs ;
	
	public JCheckBox getDisableSubs() {
		return disableSubs;
	}




	private DefaultMutableTreeNode parent [];
	private JPanel tabbedPane;
	private CardLayout  cl;
	private JTextField abitrate;
	private JTextField maxbitrate;
	private JTree tree;
	private JCheckBox  forcePCM ;
	private JComboBox channels;
	private JComboBox vq ;
	
	private void updateEngineModel() {
		StringBuffer sb = new StringBuffer();
		ArrayList<String> engines = new ArrayList<String>();
		Object root = tree.getModel().getRoot();
		for(int i=0;i<tree.getModel().getChildCount(root);i++) {
			Object firstChild = tree.getModel().getChild(root, i);
			if (!tree.getModel().isLeaf(firstChild)) {
				for(int j=0;j<tree.getModel().getChildCount(firstChild);j++) {
					Object secondChild = tree.getModel().getChild(firstChild, j);
					if (secondChild instanceof TreeNodeSettings) {
						TreeNodeSettings tns = (TreeNodeSettings) secondChild;
						if (tns.isEnable() && tns.getPlayer() != null) {
							if (sb.length() > 0)
								sb.append(",");
							sb.append(tns.getPlayer().id());
							engines.add(tns.getPlayer().id());
						}
					}
				}
			}
		}
		PMS.get().getFrame().setReloadable(true);
		PMS.get().setEngines(sb.toString());
		PMS.get().setEnginesAsList(engines);
	}
	
	public JComponent build() {
		FormLayout mainlayout = new FormLayout(
				"left:pref, pref, 7dlu, pref, pref, 0:grow",
				"p, 3dlu" );
		PanelBuilder builder = new PanelBuilder(mainlayout);
        builder.setBorder(Borders.DLU4_BORDER);
		
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        builder.add(buildRightTabbedPane(), cc.xyw(4, 1, 3));
        builder.add(buildLeft(), cc.xy(2, 1));
        
        return builder.getPanel();
	}
	
	
	
	public JComponent buildRightTabbedPane() {
		cl = new CardLayout();
		  tabbedPane = new JPanel (cl);
		  tabbedPane.setBorder(BorderFactory.createEmptyBorder());
		return tabbedPane;
	}
	
	public JComponent buildLeft() {
		FormLayout layout = new FormLayout(
                "left:pref, pref, pref, pref, 0:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 30dlu, 0:grow");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);
        
        
        
        CellConstraints cc = new CellConstraints();
      
       
       JButton but = new JButton(LooksFrame.readImageIcon("kdevelop_down-32.png"));
       but.setToolTipText("Sort the transcoding engines list. First one will appears in the original video folder");
       but.setBorder(BorderFactory.createEmptyBorder());
       but.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			TreePath path = tree.getSelectionModel().getSelectionPath();
			if (path != null && path.getLastPathComponent() instanceof TreeNodeSettings) {
				TreeNodeSettings node = ((TreeNodeSettings) path.getLastPathComponent());
				if (node.getPlayer() != null) {
					DefaultTreeModel dtm = (DefaultTreeModel)tree.getModel();   // get the tree model
			         //now get the index of the selected node in the DefaultTreeModel
			         int index = dtm.getIndexOfChild(node.getParent(), node);
			         // if selected node is first, return (can't move it up)
			         if(index < node.getParent().getChildCount()-1) {
			            dtm.insertNodeInto(node, (DefaultMutableTreeNode)node.getParent(), index+1);   // move the node
			            dtm.reload();
			            for(int i=0;i<tree.getRowCount();i++)
			    			tree.expandRow(i);
			            tree.getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
			            updateEngineModel();
			         }
				}
			}
		}   	   
       });
       
       
       
       builder.add(but,          cc.xy(2,  3));
       JButton but2 = new JButton(LooksFrame.readImageIcon("up-32.png"));
       but2.setToolTipText("Sort the transcoding engines list. First one will appears in the original video folder");
       but2.setBorder(BorderFactory.createEmptyBorder());
       but2.addActionListener(new ActionListener() {
   		public void actionPerformed(ActionEvent e) {
   			TreePath path = tree.getSelectionModel().getSelectionPath();
			if (path != null && path.getLastPathComponent() instanceof TreeNodeSettings) {
				TreeNodeSettings node = ((TreeNodeSettings) path.getLastPathComponent());
				if (node.getPlayer() != null) {
					DefaultTreeModel dtm = (DefaultTreeModel)tree.getModel();   // get the tree model
			         //now get the index of the selected node in the DefaultTreeModel
			         int index = dtm.getIndexOfChild(node.getParent(), node);
			         // if selected node is first, return (can't move it up)
			         if(index != 0) {
			            dtm.insertNodeInto(node, (DefaultMutableTreeNode)node.getParent(), index-1);   // move the node
			            dtm.reload();
			            for(int i=0;i<tree.getRowCount();i++)
			    			tree.expandRow(i);
			            tree.getSelectionModel().setSelectionPath(new TreePath(node.getPath()));
			            updateEngineModel();
			         }
				}
			}
   		}   	   
          });
       builder.add(but2,          cc.xy(3,  3));
       JButton but3 = new JButton(LooksFrame.readImageIcon("connect_no-32.png"));
       but3.setToolTipText("Enable/disable a transcoding engine");
       but3.setBorder(BorderFactory.createEmptyBorder());
       but3.addActionListener(new ActionListener() {

		public void actionPerformed(ActionEvent e) {
			
			TreePath path = tree.getSelectionModel().getSelectionPath();
			if (path != null && path.getLastPathComponent() instanceof TreeNodeSettings && ((TreeNodeSettings) path.getLastPathComponent()).getPlayer() != null) {
				((TreeNodeSettings) path.getLastPathComponent()).setEnable(!((TreeNodeSettings) path.getLastPathComponent()).isEnable());
				updateEngineModel();
				tree.updateUI();
			}
			
		}
    	   
       });
       builder.add(but3,          cc.xy(4,  3));
       
       DefaultMutableTreeNode root = new DefaultMutableTreeNode("Engines");
       TreeNodeSettings commonDec = new TreeNodeSettings("Common decoder settings", null, buildCommon());
       tabbedPane.add(commonDec.id(), commonDec.getConfigPanel());
       TreeNodeSettings commonEnc = new TreeNodeSettings("Common encoder settings", null, buildMEncoder());
       tabbedPane.add(commonEnc.id(), commonEnc.getConfigPanel());
       root.add(commonDec);
       root.add(commonEnc);
       
       parent = new DefaultMutableTreeNode[5];
       parent[0] = new DefaultMutableTreeNode("Video Files Engines");
       parent[1] = new DefaultMutableTreeNode("Audio Files Engines");
       parent[2] = new DefaultMutableTreeNode("Video Web Streaming Engines");
       parent[3] = new DefaultMutableTreeNode("Audio Web Streaming Engines");
       parent[4] = new DefaultMutableTreeNode("Misc Engines");
       root.add(parent[0]);
       root.add(parent[1]);
       root.add(parent[2]);
       root.add(parent[3]);
       root.add(parent[4]);
       

       tree = new JTree(new DefaultTreeModel(root)) {
    	   
		private static final long serialVersionUID = -6703434752606636290L;

		/*protected void setExpandedState(TreePath path, boolean state) {
	           // Ignore all collapse requests; collapse events will not be fired
	           if (state) {
	               super.setExpandedState(path, state);
	           }
	       }*/
       };
      tree.setRootVisible(false);
      tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      tree.addTreeSelectionListener(new TreeSelectionListener() {

		@Override
		public void valueChanged(TreeSelectionEvent e) {
			if (e.getNewLeadSelectionPath() != null && e.getNewLeadSelectionPath().getLastPathComponent() instanceof TreeNodeSettings) {
				TreeNodeSettings tns = (TreeNodeSettings)e.getNewLeadSelectionPath().getLastPathComponent();
				cl.show(tabbedPane, tns.id());
			}
		}
    	  
      });
      
      tree.setCellRenderer(new TreeRenderer());
       JScrollPane pane = new JScrollPane(tree);
       
       builder.add(pane,          cc.xyw(2,  1, 4));
       
       builder.addLabel("Engine in bold will be the priority one", cc.xyw(2, 5, 4));
       builder.addLabel("and will replace the original video", cc.xyw(2, 7, 4));
    //   
       
        return builder.getPanel();
	}
	
	public void addEngines() {
		ArrayList<Player> disPlayers = new ArrayList<Player>();
		ArrayList<Player> ordPlayers = new ArrayList<Player>();
		for(String id:PMS.get().getEnginesAsList()) {
			//boolean matched = false;
			for(Player p:PMS.get().getAllPlayers()) {
				if (p.id().equals(id)) {
					ordPlayers.add(p);
					//matched = true;
				}
			}
		}
		for(Player p:PMS.get().getAllPlayers()) {
			if (!ordPlayers.contains(p)) {
				ordPlayers.add(p);
				disPlayers.add(p);
			}
		}
			
		
		for(Player p:ordPlayers) {
			TreeNodeSettings en = new TreeNodeSettings(p.name(), p, null);
			if (disPlayers.contains(p))
				en.setEnable(false);
			JComponent jc = en.getConfigPanel();
			if (jc == null)
				jc = buildEmpty();
			tabbedPane.add(en.id(), jc);
			parent[p.purpose()].add(en);
			
		}
		for(int i=0;i<tree.getRowCount();i++)
			tree.expandRow(i);
	}
	
	public JComponent buildEmpty() {
		FormLayout layout = new FormLayout(
				"left:pref, 2dlu, pref:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 20dlu, p, 3dlu, p, 3dlu, p");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator("No settings for now",  cc.xyw(1, 1, 3));
        
        return builder.getPanel();
        
	}
	
	public JComponent buildMEncoder() {
		FormLayout layout = new FormLayout(
				"left:pref, 2dlu, pref:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 20dlu, p, 3dlu, p, 3dlu, p");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator("Video encoder settings with following engines: Mencoder/AviSynth/FFmpeg",  cc.xyw(1, 1, 3));
        
        forcePCM = new JCheckBox("DTS/FLAC -> LPCM remux (You need an HDMI receiver for streaming PCM 5.1 ! Average Bitrate = 4.6Mbps)");
        forcePCM.setContentAreaFilled(false);
       /* if (!PMS.get().isWindows())
        	forcePCM.setEnabled(false);
        else {*/
	        if (PMS.get().isMencoder_usepcm())
	        	forcePCM.setSelected(true);
	        forcePCM.addItemListener(new ItemListener() {
	
				public void itemStateChanged(ItemEvent e) {
					PMS.get().setMencoder_usepcm(e.getStateChange() == ItemEvent.SELECTED);
				}
	        	
	        });
       // }
       
        builder.add(forcePCM, cc.xyw(1, 3, 3));
        
        abitrate = new JTextField("" + PMS.get().getAudiobitrate());
        abitrate.addKeyListener(new KeyListener() {

 		@Override
 		public void keyPressed(KeyEvent e) {}
 		@Override
 		public void keyTyped(KeyEvent e) {}
 		@Override
 		public void keyReleased(KeyEvent e) {
 			try {
 				int ab = Integer.parseInt(abitrate.getText());
 				PMS.get().setAudiobitrate(ab);
 			} catch (NumberFormatException nfe) {
 			}
 		}

        });
        
       builder.addLabel("AC3 Audio bitrate (in Kbits/s) (ex: 384, 576, 640):", cc.xy(1, 5));
       builder.add(abitrate, cc.xy(3, 5));
       
       builder.addLabel("Maximum bandwidth in Mbits/s (0 means no limit):", cc.xy(1, 9));
       
       maxbitrate = new JTextField("" + PMS.get().getMaximumbitrate());
       maxbitrate.addKeyListener(new KeyListener() {

		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {
			PMS.get().setMaximumbitrate(maxbitrate.getText());
		}

       });
       builder.add(maxbitrate, cc.xy(3, 9));
      
       builder.addLabel("Mpeg2 Video quality settings (Some presets are available in the drop-down, you can also edit values, but be careful):", cc.xyw(1, 13, 3));
       
        Object data [] = new Object [] { PMS.get().getMencoderMainSettings(),
    		   "keyint=1:vqscale=1:vqmin=2  /* Best Quality */",
    		   "keyint=1:vqscale=1:vqmin=1  /* Lossless Quality, Crazy Bitrate */",
    		   "keyint=3:vqscale=2:vqmin=3  /* Good quality */",
    		   "keyint=5:vqscale=3:vqmin=5  /* Medium quality, Low-end CPU or HD Wifi Transcoding */"};
       MyComboBoxModel cbm = new MyComboBoxModel(data);
       
       vq = new JComboBox(cbm);
       vq.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					if (s.indexOf("/*") > -1) {
						s = s.substring(0, s.indexOf("/*")).trim();
					}
					PMS.get().setMencoder_main(s);
				}
			}
       	
       });
       vq.setEditable(true);
       builder.add(vq,          cc.xyw(1,  15, 3));
       
      String help1 = "Encoder Tips:\n\nThe video is automatically transcoded and muxed to a MPEG-PS / AC3 audio (highly compatible on PS3)";
      help1 += "\nYou can play with the vqscale, vqmin and keyint parameters to achieve good, even almost lossless transcoding quality.";
      help1 += "\nDrawback to this is the VBR bitrate who can peaks sometimes above your max network capacity.";
      help1 += "\nThat's why you can also set the bandwith if you're on WiFi, CPL, etc. However, the transcoding quality";
     help1 += "\nis a balance between network speed and cpu power: the more quality you will put in a constrained bitrate,";
   help1 += 	"\nthe more your cpu will suffer! Also, don't expect to fit a 1080p action movie in the purest quality in 15Mbps :p";
   
      
       JTextArea decodeTips = new JTextArea(help1);
       decodeTips.setEditable(false);
       decodeTips.setBorder(BorderFactory.createEtchedBorder());
       decodeTips.setBackground(new Color(255, 255, 192));
       builder.add(decodeTips, cc.xyw(1, 17, 3));
       
       
        return builder.getPanel();
	}
	



	public JComponent buildCommon() {
		FormLayout layout = new FormLayout(
				"left:pref, 2dlu, pref:grow",
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, 0:grow");
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
        builder.addSeparator("Video decoder settings, apply to following decoders: Mencoder/AviSynth/FFmpeg",  cc.xyw(1, 1, 3));
        
        channels = new JComboBox(new Object [] {"2 channels Stereo", "6 channels 5.1" /*, "8 channels 7.1" */ }); // 7.1 not supported by Mplayer :\
        channels.setEditable(false);
        if (PMS.get().getAudiochannels() == 2)
     	   channels.setSelectedIndex(0);
        else
     	   channels.setSelectedIndex(1);
        channels.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setAudiochannels(Integer.parseInt(e.getItem().toString().substring(0, 1)));
 			}
        	
        });
        
        builder.addLabel("Number of audio channels:", cc.xy(1, 3));
        builder.add(channels, cc.xy(3, 3));
       
        disableSubs = new JCheckBox("Definitely disable subtitles");
        disableSubs.setContentAreaFilled(false);
      
        builder.add(disableSubs,          cc.xy(1,  5));
       
       
        return builder.getPanel();
	}
}

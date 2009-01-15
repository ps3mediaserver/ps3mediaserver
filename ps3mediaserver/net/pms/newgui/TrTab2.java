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
import java.awt.Font;
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

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.encoders.Player;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class TrTab2 {
	
	private final PmsConfiguration configuration;
	
	TrTab2(PmsConfiguration configuration) {
		this.configuration = configuration;
	}
	
	private JCheckBox  disableSubs ;
	
	public JCheckBox getDisableSubs() {
		return disableSubs;
	}




	private JTextField notranscode;
	private JTextField maxbuffer;
	private JComboBox nbcores ;
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
							engines.add(tns.getPlayer().id());
						}
					}
				}
			}
		}
		PMS.get().getFrame().setReloadable(true);
		PMS.getConfiguration().setEnginesAsList(engines);
	}
	
	public JComponent build() {
		FormLayout mainlayout = new FormLayout(
				"left:pref, pref, 7dlu, pref, pref, 0:grow", //$NON-NLS-1$
				"top:p, 3dlu" ); //$NON-NLS-1$
		PanelBuilder builder = new PanelBuilder(mainlayout);
        builder.setBorder(Borders.DLU4_BORDER);
		
        builder.setOpaque(true);

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
                "left:pref, pref, pref, pref, 0:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 30dlu, 0:grow"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);
        
        
        
        CellConstraints cc = new CellConstraints();
      
       
       JButton but = new JButton(LooksFrame.readImageIcon("kdevelop_down-32.png")); //$NON-NLS-1$
       but.setToolTipText(Messages.getString("TrTab2.6")); //$NON-NLS-1$
      // but.setBorder(BorderFactory.createEmptyBorder());
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
       JButton but2 = new JButton(LooksFrame.readImageIcon("up-32.png")); //$NON-NLS-1$
       but2.setToolTipText(Messages.getString("TrTab2.6")); //$NON-NLS-1$
      // but2.setBorder(BorderFactory.createEmptyBorder());
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
       JButton but3 = new JButton(LooksFrame.readImageIcon("connect_no-32.png")); //$NON-NLS-1$
       but3.setToolTipText(Messages.getString("TrTab2.0")); //$NON-NLS-1$
       //but3.setBorder(BorderFactory.createEmptyBorder());
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
       
       DefaultMutableTreeNode root = new DefaultMutableTreeNode(Messages.getString("TrTab2.11")); //$NON-NLS-1$
      /* TreeNodeSettings commonDec = new TreeNodeSettings(Messages.getString("TrTab2.12"), null, buildCommon()); //$NON-NLS-1$
       tabbedPane.add(commonDec.id(), commonDec.getConfigPanel());
       TreeNodeSettings commonEnc = new TreeNodeSettings(Messages.getString("TrTab2.13"), null, buildMEncoder()); //$NON-NLS-1$
       tabbedPane.add(commonEnc.id(), commonEnc.getConfigPanel());*/
       TreeNodeSettings commonEnc = new TreeNodeSettings("Common transcode settings", null, buildCommon()); //$NON-NLS-1$
       tabbedPane.add(commonEnc.id(), commonEnc.getConfigPanel());
     //  root.add(commonDec);
       root.add(commonEnc);
       
       parent = new DefaultMutableTreeNode[5];
       parent[0] = new DefaultMutableTreeNode(Messages.getString("TrTab2.14")); //$NON-NLS-1$
       parent[1] = new DefaultMutableTreeNode(Messages.getString("TrTab2.15")); //$NON-NLS-1$
       parent[2] = new DefaultMutableTreeNode(Messages.getString("TrTab2.16")); //$NON-NLS-1$
       parent[3] = new DefaultMutableTreeNode(Messages.getString("TrTab2.17")); //$NON-NLS-1$
       parent[4] = new DefaultMutableTreeNode(Messages.getString("TrTab2.18")); //$NON-NLS-1$
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
       
       builder.addLabel(Messages.getString("TrTab2.19"), cc.xyw(2, 5, 4)); //$NON-NLS-1$
       builder.addLabel(Messages.getString("TrTab2.20"), cc.xyw(2, 7, 4)); //$NON-NLS-1$
    //   
       
        return builder.getPanel();
	}
	
	public void addEngines() {
		ArrayList<Player> disPlayers = new ArrayList<Player>();
		ArrayList<Player> ordPlayers = new ArrayList<Player>();
		PMS r = PMS.get();
		for(String id:PMS.getConfiguration().getEnginesAsList(r.getRegistry())) {
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
		
		
		tree.setSelectionRow(0);
	}
	
	public JComponent buildEmpty() {
		FormLayout layout = new FormLayout(
				"left:pref, 2dlu, pref:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 20dlu, p, 3dlu, p, 3dlu, p"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator(Messages.getString("TrTab2.1"),  cc.xyw(1, 1, 3)); //$NON-NLS-1$
        
        return builder.getPanel();
        
	}
	
	public JComponent buildMEncoder() {
		FormLayout layout = new FormLayout(
				"left:pref, 2dlu, pref:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p , 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        builder.addSeparator(Messages.getString("TrTab2.2"),  cc.xyw(1, 1, 3)); //$NON-NLS-1$
        
        forcePCM = new JCheckBox(Messages.getString("TrTab2.27")); //$NON-NLS-1$
        forcePCM.setContentAreaFilled(false);
       /* if (!PMS.get().isWindows())
        	forcePCM.setEnabled(false);
        else {*/
	        if (configuration.isMencoderUsePcm())
	        	forcePCM.setSelected(true);
	        forcePCM.addItemListener(new ItemListener() {
	
				public void itemStateChanged(ItemEvent e) {
					configuration.setMencoderUsePcm(e.getStateChange() == ItemEvent.SELECTED);
				}
	        	
	        });
       // }
       
        builder.add(forcePCM, cc.xyw(1, 3, 3));
        
        abitrate = new JTextField("" + PMS.getConfiguration().getAudioBitrate()); //$NON-NLS-1$
        abitrate.addKeyListener(new KeyListener() {

 		@Override
 		public void keyPressed(KeyEvent e) {}
 		@Override
 		public void keyTyped(KeyEvent e) {}
 		@Override
 		public void keyReleased(KeyEvent e) {
 			try {
 				int ab = Integer.parseInt(abitrate.getText());
 				PMS.getConfiguration().setAudioBitrate(ab);
 			} catch (NumberFormatException nfe) {
 			}
 		}

        });
        
       builder.addLabel(Messages.getString("TrTab2.29"), cc.xy(1, 5)); //$NON-NLS-1$
       builder.add(abitrate, cc.xy(3, 5));
       
       builder.addLabel(Messages.getString("TrTab2.30"), cc.xy(1, 9)); //$NON-NLS-1$
       
       maxbitrate = new JTextField("" + PMS.getConfiguration().getMaximumBitrate()); //$NON-NLS-1$
       maxbitrate.addKeyListener(new KeyListener() {

		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {
			PMS.getConfiguration().setMaximumBitrate(maxbitrate.getText());
		}

       });
       builder.add(maxbitrate, cc.xy(3, 9));
      
       builder.addLabel(Messages.getString("TrTab2.32"), cc.xyw(1, 13, 3)); //$NON-NLS-1$
       
        Object data [] = new Object [] { PMS.getConfiguration().getMencoderMainSettings(),
    		   "keyint=1:vqscale=1:vqmin=2  /* Best Quality */", //$NON-NLS-1$
    		   "keyint=1:vqscale=1:vqmin=1  /* Lossless Quality, Crazy Bitrate */", //$NON-NLS-1$
    		   "keyint=3:vqscale=2:vqmin=3  /* Good quality */", //$NON-NLS-1$
        	   "keyint=25:vqmax=5:vqmin=2  /* Good quality for HD Wifi Transcoding */", //$NON-NLS-1$
    		   "keyint=25:vqmax=7:vqmin=2  /* Medium quality for HD Wifi Transcoding */", //$NON-NLS-1$
        	   "keyint=25:vqmax=8:vqmin=3  /* Low quality, Low-end CPU or HD Wifi Transcoding */", //$NON-NLS-1$
        	   "keyint=50:vrc_maxrate=40000:vbitrate=24000000:vrc_buf_size=1835  /* TEST */"}; //$NON-NLS-1$
       MyComboBoxModel cbm = new MyComboBoxModel(data);
       
       vq = new JComboBox(cbm);
       vq.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					if (s.indexOf("/*") > -1) { //$NON-NLS-1$
						s = s.substring(0, s.indexOf("/*")).trim(); //$NON-NLS-1$
					}
					PMS.getConfiguration().setMencoderMainSettings(s);
				}
			}
       	
       });
       vq.setEditable(true);
       builder.add(vq,          cc.xyw(1,  15, 3));
       
      String help1 = Messages.getString("TrTab2.39"); //$NON-NLS-1$
      help1 += Messages.getString("TrTab2.40"); //$NON-NLS-1$
      help1 += Messages.getString("TrTab2.41"); //$NON-NLS-1$
      help1 += Messages.getString("TrTab2.42"); //$NON-NLS-1$
     help1 += Messages.getString("TrTab2.43"); //$NON-NLS-1$
   help1 += 	Messages.getString("TrTab2.44"); //$NON-NLS-1$
   
      
       JTextArea decodeTips = new JTextArea(help1);
       decodeTips.setEditable(false);
       decodeTips.setBorder(BorderFactory.createEtchedBorder());
       decodeTips.setBackground(new Color(255, 255, 192));
       builder.add(decodeTips, cc.xyw(1, 17, 3));
       
       
        return builder.getPanel();
	}
	



	public JComponent buildCommon() {
		FormLayout layout = new FormLayout(
				"left:pref, 2dlu, pref:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, p"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
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
        
        JComponent cmp = builder.addSeparator(Messages.getString("NetworkTab.5"),  cc.xyw(1, 1, 3)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
        builder.addLabel(Messages.getString("NetworkTab.6"),  cc.xy(1,  3)); //$NON-NLS-1$
        builder.add(maxbuffer,          cc.xy(3,  3)); 
        
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
        builder.add(nbcores,          cc.xy(3,  5)); 
        
        
        
       cmp = builder.addSeparator(Messages.getString("TrTab2.3"),  cc.xyw(1, 11, 3)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
        channels = new JComboBox(new Object [] {"2 channels Stereo", "6 channels 5.1" /*, "8 channels 7.1" */ }); // 7.1 not supported by Mplayer :\ //$NON-NLS-1$ //$NON-NLS-2$
        channels.setEditable(false);
        if (PMS.getConfiguration().getAudioChannelCount() == 2)
     	   channels.setSelectedIndex(0);
        else
     	   channels.setSelectedIndex(1);
        channels.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setAudioChannelCount(Integer.parseInt(e.getItem().toString().substring(0, 1)));
 			}
        	
        });
        
        builder.addLabel(Messages.getString("TrTab2.50"), cc.xy(1, 13)); //$NON-NLS-1$
        builder.add(channels, cc.xy(3, 13));
       
       
        
       
        
        abitrate = new JTextField("" + PMS.getConfiguration().getAudioBitrate()); //$NON-NLS-1$
        abitrate.addKeyListener(new KeyListener() {

 		@Override
 		public void keyPressed(KeyEvent e) {}
 		@Override
 		public void keyTyped(KeyEvent e) {}
 		@Override
 		public void keyReleased(KeyEvent e) {
 			try {
 				int ab = Integer.parseInt(abitrate.getText());
 				PMS.getConfiguration().setAudioBitrate(ab);
 			} catch (NumberFormatException nfe) {
 			}
 		}

        });
        
       builder.addLabel(Messages.getString("TrTab2.29"), cc.xy(1, 15)); //$NON-NLS-1$
       builder.add(abitrate, cc.xy(3, 15));
       
       forcePCM = new JCheckBox(Messages.getString("TrTab2.27")); //$NON-NLS-1$
       forcePCM.setContentAreaFilled(false);
      /* if (!PMS.get().isWindows())
       	forcePCM.setEnabled(false);
       else {*/
	        if (configuration.isMencoderUsePcm())
	        	forcePCM.setSelected(true);
	        forcePCM.addItemListener(new ItemListener() {
	
				public void itemStateChanged(ItemEvent e) {
					configuration.setMencoderUsePcm(e.getStateChange() == ItemEvent.SELECTED);
				}
	        	
	        });
      // }
      
       builder.add(forcePCM, cc.xyw(1, 17, 3));
       
      
       
      cmp = builder.addSeparator(Messages.getString("TrTab2.4"),  cc.xyw(1, 19, 3)); //$NON-NLS-1$
       cmp = (JComponent) cmp.getComponent(0);
       cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
       
       
       builder.addLabel(Messages.getString("TrTab2.30"), cc.xy(1, 21)); //$NON-NLS-1$
       
       
       maxbitrate = new JTextField("" + PMS.getConfiguration().getMaximumBitrate()); //$NON-NLS-1$
       maxbitrate.addKeyListener(new KeyListener() {

		@Override
		public void keyPressed(KeyEvent e) {}
		@Override
		public void keyTyped(KeyEvent e) {}
		@Override
		public void keyReleased(KeyEvent e) {
			PMS.getConfiguration().setMaximumBitrate(maxbitrate.getText());
		}

       });
       builder.add(maxbitrate, cc.xy(3, 21));
      
       builder.addLabel(Messages.getString("TrTab2.32"), cc.xyw(1, 23, 3)); //$NON-NLS-1$
       
        Object data [] = new Object [] { PMS.getConfiguration().getMencoderMainSettings(),
    		   "keyint=1:vqscale=1:vqmin=2  /* Best Quality */", //$NON-NLS-1$
    		   "keyint=1:vqscale=1:vqmin=1  /* Lossless Quality, Crazy Bitrate */", //$NON-NLS-1$
    		   "keyint=3:vqscale=2:vqmin=3  /* Good quality */", //$NON-NLS-1$
        	   "keyint=25:vqmax=5:vqmin=2  /* Good quality for HD Wifi Transcoding */", //$NON-NLS-1$
    		   "keyint=25:vqmax=7:vqmin=2  /* Medium quality for HD Wifi Transcoding */", //$NON-NLS-1$
        	   "keyint=25:vqmax=8:vqmin=3  /* Low quality, Low-end CPU or HD Wifi Transcoding */", //$NON-NLS-1$
        	   "keyint=50:vrc_maxrate=40000:vbitrate=24000000:vrc_buf_size=1835  /* TEST */"}; //$NON-NLS-1$
       MyComboBoxModel cbm = new MyComboBoxModel(data);
       
       vq = new JComboBox(cbm);
       vq.addItemListener(new ItemListener() {

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String s = (String) e.getItem();
					if (s.indexOf("/*") > -1) { //$NON-NLS-1$
						s = s.substring(0, s.indexOf("/*")).trim(); //$NON-NLS-1$
					}
					PMS.getConfiguration().setMencoderMainSettings(s);
				}
			}
       	
       });
       vq.setEditable(true);
       builder.add(vq,          cc.xyw(1,  25, 3));
       
      String help1 = Messages.getString("TrTab2.39"); //$NON-NLS-1$
      help1 += Messages.getString("TrTab2.40"); //$NON-NLS-1$
      help1 += Messages.getString("TrTab2.41"); //$NON-NLS-1$
      help1 += Messages.getString("TrTab2.42"); //$NON-NLS-1$
     help1 += Messages.getString("TrTab2.43"); //$NON-NLS-1$
   help1 += 	Messages.getString("TrTab2.44"); //$NON-NLS-1$
   
      
       JTextArea decodeTips = new JTextArea(help1);
       decodeTips.setEditable(false);
       decodeTips.setBorder(BorderFactory.createEtchedBorder());
       decodeTips.setBackground(new Color(255, 255, 192));
       builder.add(decodeTips, cc.xyw(1, 27, 3));
       
       disableSubs = new JCheckBox(Messages.getString("TrTab2.51")); //$NON-NLS-1$
       disableSubs.setContentAreaFilled(false);
     
       cmp = builder.addSeparator("Misc",  cc.xyw(1, 31, 3)); //$NON-NLS-1$
       cmp = (JComponent) cmp.getComponent(0);
       cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
       
       builder.add(disableSubs,          cc.xy(1,  33));
       
       builder.addLabel("Skip automatic transcode for following extensions (coma separated): ", cc.xy(1,35)); //$NON-NLS-1$
       
       notranscode = new JTextField(configuration.getNoTranscode());
       notranscode.addKeyListener(new KeyListener() {

   		@Override
   		public void keyPressed(KeyEvent e) {}
   		@Override
   		public void keyTyped(KeyEvent e) {}
   		@Override
   		public void keyReleased(KeyEvent e) {
   			configuration.setNoTranscode(notranscode.getText());
   			PMS.get().getFrame().setReloadable(true);
   		}
       	   
          });
       builder.add(notranscode, cc.xy(3, 35));
       
       
        return builder.getPanel();
	}
}

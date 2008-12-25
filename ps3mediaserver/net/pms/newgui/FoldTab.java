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
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JScrollPane;

import net.pms.Messages;
import net.pms.PMS;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class FoldTab {
	
	public static final String ALL_DRIVES = Messages.getString("FoldTab.0"); //$NON-NLS-1$
	private JList FList;
	private  DefaultListModel df;
	private JCheckBox  hidevideosettings ;
	
	public DefaultListModel getDf() {
		return df;
	}
	
	private void updateModel() {
		if (df.size() == 1 && df.getElementAt(0).equals(ALL_DRIVES)) {
			PMS.get().setFolders(""); //$NON-NLS-1$
		} else {
			StringBuffer sb = new StringBuffer();
			for(int i=0;i<df.size();i++) {
				if (i> 0)
					sb.append(","); //$NON-NLS-1$
				sb.append(df.getElementAt(i));
			}
			PMS.get().setFolders(sb.toString());
		}
		PMS.get().getFrame().setReloadable(true);
	}

	public JComponent build() {
		FormLayout layout = new FormLayout(
                "left:pref, pref, pref, pref, pref, 0:grow", //$NON-NLS-1$
                "p, 3dlu, p, 15dlu, p, 3dlu, p, 3dlu, fill:default:grow"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.DLU4_BORDER);
        builder.setOpaque(true);

        CellConstraints cc = new CellConstraints();
        
       df = new DefaultListModel();
       if (PMS.get().getFolders() != null && PMS.get().getFolders().length() > 0) {
    	 try {
			File f [] =  PMS.get().loadFoldersConf(PMS.get().getFolders());
			for(File file:f) {
				df.addElement(file.getAbsolutePath());
			}
		} catch (IOException e1) {
			PMS.error(null, e1);
		}
       } else
		df.addElement(ALL_DRIVES);
		FList = new JList();
		FList.setModel(df);
        JScrollPane pane = new JScrollPane(FList);
        
        builder.addSeparator(Messages.getString("FoldTab.1"),  cc.xyw(2, 1, 5)); //$NON-NLS-1$
        hidevideosettings = new JCheckBox(Messages.getString("FoldTab.6")); //$NON-NLS-1$
        hidevideosettings.setContentAreaFilled(false);
        if (PMS.get().isHidevideosettings())
        	hidevideosettings.setSelected(true);
        hidevideosettings.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.get().setHidevideosettings(e.getStateChange() == ItemEvent.SELECTED);
 				PMS.get().getFrame().setReloadable(true);
 			}
        	
        });
        
        builder.add(hidevideosettings,          cc.xyw(2,  3, 5));
       
        
        builder.addSeparator(Messages.getString("FoldTab.7"),  cc.xyw(2, 5, 5)); //$NON-NLS-1$
        
       JButton but = new JButton(LooksFrame.readImageIcon("folder_new-32.png")); //$NON-NLS-1$
       but.setBorder(BorderFactory.createEmptyBorder());
       but.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showDialog((Component) e.getSource(), Messages.getString("FoldTab.9")); //$NON-NLS-1$
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			    	((DefaultListModel) FList.getModel()).add(FList.getModel().getSize(),chooser.getSelectedFile().getAbsolutePath());
			    	if (FList.getModel().getElementAt(0).equals(ALL_DRIVES))
			    		((DefaultListModel) FList.getModel()).remove(0);
			    	updateModel();
			    }
			}
		});
       builder.add(but,          cc.xy(2,  7));
       JButton but2 = new JButton(LooksFrame.readImageIcon("button_cancel-32.png")); //$NON-NLS-1$
       but2.setBorder(BorderFactory.createEmptyBorder());
       but2.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent e) {
				if (FList.getSelectedIndex() > -1) {
					((DefaultListModel) FList.getModel()).remove(FList.getSelectedIndex());
					if (FList.getModel().getSize() == 0)
			    		((DefaultListModel) FList.getModel()).add(0, ALL_DRIVES);
					updateModel();
				}
			}
		});
       builder.add(but2,          cc.xy(3,  7));
       
       JButton but3 = new JButton(LooksFrame.readImageIcon("kdevelop_down-32.png")); //$NON-NLS-1$
       but3.setToolTipText(Messages.getString("FoldTab.12")); //$NON-NLS-1$
       but3.setBorder(BorderFactory.createEmptyBorder());
       but3.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			DefaultListModel model = ((DefaultListModel) FList.getModel());
			for(int i=0;i<model.size()-1;i++) {
				if (FList.isSelectedIndex(i)) {
					String value = model.get(i).toString();
					model.set(i, model.get(i+1));
					model.set(i+1, value);
					FList.setSelectedIndex(i+1);
					updateModel();
					break;
				}
			}
		}   	   
       });
       
       
       
       builder.add(but3,          cc.xy(4,  7));
       JButton but4 = new JButton(LooksFrame.readImageIcon("up-32.png")); //$NON-NLS-1$
       but4.setToolTipText(Messages.getString("FoldTab.12")); //$NON-NLS-1$
       but4.setBorder(BorderFactory.createEmptyBorder());
       but4.addActionListener(new ActionListener() {
   		public void actionPerformed(ActionEvent e) {
   			DefaultListModel model = ((DefaultListModel) FList.getModel());
   			for(int i=1;i<model.size();i++) {
   				if (FList.isSelectedIndex(i)) {
   					String value = model.get(i).toString();
   					
   					model.set(i, model.get(i-1));
   					model.set(i-1, value);
   					FList.setSelectedIndex(i-1);
   					updateModel();
   					break;

   				}
   			}
   		}   	   
          });
       builder.add(but4,          cc.xy(5,  7));
       
       builder.add(pane,          cc.xyw(2,  9, 5));
       
       
        return builder.getPanel();
	}
	
	
}

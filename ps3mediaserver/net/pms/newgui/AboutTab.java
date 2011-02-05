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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class AboutTab {

	
	class PopupTriggerMouseListener extends MouseAdapter
    {
        private JPopupMenu popup;
        private JComponent component;

        public PopupTriggerMouseListener(JPopupMenu popup, JComponent component)
        {
            this.popup = popup;
            this.component = component;
        }

        //some systems trigger popup on mouse press, others on mouse release, we want to cater for both
        private void showMenuIfPopupTrigger(MouseEvent e)
        {
            if (e.isPopupTrigger())
            {
               popup.show(component, e.getX() + 3, e.getY() + 3);
            }
        }

        //according to the javadocs on isPopupTrigger, checking for popup trigger on mousePressed and mouseReleased 
        //should be all  that is required
        //public void mouseClicked(MouseEvent e)  
        //{
        //    showMenuIfPopupTrigger(e);
        //}

        public void mousePressed(MouseEvent e)
        {
            showMenuIfPopupTrigger(e);
        }

        public void mouseReleased(MouseEvent e)
        {
            showMenuIfPopupTrigger(e);
        }

    }
	private JTextArea jList;
	
	public JTextArea getList() {
		return jList;
	}

	public JComponent build() {
		FormLayout layout = new FormLayout(
                "left:pref, 0:grow", //$NON-NLS-1$
                "pref, fill:default:grow"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
      //  builder.setBorder(Borders.DLU14_BORDER);
        builder.setOpaque(true);

        CellConstraints cc = new CellConstraints();
        
        jList = new JTextArea();
		jList.setEditable(false);
		jList.setWrapStyleWord(false);
		jList.setBackground(Color.WHITE);
		//jList.setFont(new Font("Courier New", Font.PLAIN, 12)); //$NON-NLS-1$
		FileInputStream fIN;
		try {
			fIN = new FileInputStream("README"); //$NON-NLS-1$
			byte buf [] = new byte [fIN.available()];
			fIN.read(buf);
			fIN.close();
			jList.setText(new String(buf));
		} catch (IOException e) {
			
		}
		
       
        JScrollPane pane = new JScrollPane(jList, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setPreferredSize(new Dimension(500,400));
      //  pane.setBorder(BorderFactory.createEtchedBorder());
        
       builder.add(pane,          cc.xy(2,  2));
       
       
        return builder.getPanel();
	}
	
	
}

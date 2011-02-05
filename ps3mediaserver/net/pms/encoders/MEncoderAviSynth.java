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
package net.pms.encoders;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.StringTokenizer;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import net.pms.Messages;
import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class MEncoderAviSynth extends MEncoderVideo {

	public MEncoderAviSynth(PmsConfiguration configuration) {
		super(configuration);
	}
	
	private JTextArea textArea;
	private JCheckBox convertfps;
	
	@Override
	public JComponent config() {
		FormLayout layout = new FormLayout(
                "left:pref, 0:grow", //$NON-NLS-1$
                "p, 3dlu, p, 3dlu, p, 3dlu,  0:grow"); //$NON-NLS-1$
         PanelBuilder builder = new PanelBuilder(layout);
        builder.setBorder(Borders.EMPTY_BORDER);
        builder.setOpaque(false);

        CellConstraints cc = new CellConstraints();
        
        
        JComponent cmp = builder.addSeparator(Messages.getString("MEncoderAviSynth.2"),  cc.xyw(2, 1, 1)); //$NON-NLS-1$
        cmp = (JComponent) cmp.getComponent(0);
        cmp.setFont(cmp.getFont().deriveFont(Font.BOLD));
        
        convertfps = new JCheckBox(Messages.getString("MEncoderAviSynth.3")); //$NON-NLS-1$
        convertfps.setContentAreaFilled(false);
        if (PMS.getConfiguration().getAvisynthConvertFps())
        	convertfps.setSelected(true);
        convertfps.addItemListener(new ItemListener() {

 			public void itemStateChanged(ItemEvent e) {
 				PMS.getConfiguration().setAvisynthConvertFps((e.getStateChange() == ItemEvent.SELECTED));
 			}
        	
        });
        builder.add(convertfps, cc.xy(2, 3));
        
        String clip = PMS.getConfiguration().getAvisynthScript();
        if (clip == null)
        	clip = ""; //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        StringTokenizer st = new StringTokenizer(clip, PMS.AVS_SEPARATOR);
        int i=0;
        while (st.hasMoreTokens()) {
        	if (i> 0)
        		sb.append("\n"); //$NON-NLS-1$
        	sb.append(st.nextToken());
        	i++;
        }
        textArea = new JTextArea(sb.toString());
        textArea.addKeyListener(new KeyListener() {

       		@Override
       		public void keyPressed(KeyEvent e) {}
       		@Override
       		public void keyTyped(KeyEvent e) {}
       		@Override
       		public void keyReleased(KeyEvent e) {
       			StringBuffer sb = new StringBuffer();
       			StringTokenizer st = new StringTokenizer(textArea.getText(), "\n"); //$NON-NLS-1$
       	        int i=0;
       	        while (st.hasMoreTokens()) {
       	        	if (i> 0)
       	        		sb.append(PMS.AVS_SEPARATOR);
       	        	sb.append(st.nextToken());
       	        	i++;
       	        }
       	        PMS.getConfiguration().setAvisynthScript(sb.toString());
       		}
           	   
              });
        
   
        /*
        JTextArea firstLine = new JTextArea("clip=DirectShowSource(<mymovie>, <convertfps>)\nclip=TextSub(clip, <mysubs>) <- depends on subtitles existence");
        firstLine.setEditable(false);
        builder.add(firstLine, cc.xy(2, 5));
        
        builder.addLabel("These 2 first lines are generated. You can now change the AviSynth script using the 'clip' variable.", cc.xy(2, 7));
        builder.addLabel("AviSynth script is fully customisable:", cc.xy(2, 5));
        builder.addLabel("<movie>: insert the complete DirectShowSource instruction [ DirectShowSource(movie, convertfps) ]", cc.xy(2, 11));
        builder.addLabel("<sub>: insert the complete TextSub/VobSub instruction if there's any detected srt/sub/ass subtitle file", cc.xy(2, 13));
        builder.addLabel("<moviefilename>: variable of the movie filename, if you want to do all this by yourself", cc.xy(2, 15));*/
        JScrollPane pane = new JScrollPane(textArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setPreferredSize(new Dimension(500,350));
        builder.add(pane, cc.xy(2, 5));
        
        
        return builder.getPanel();
	}

	@Override
	public int purpose() {
		return VIDEO_SIMPLEFILE_PLAYER;
	}
	
	public static final String ID = "avsmencoder"; //$NON-NLS-1$
	
	@Override
	public String id() {
		return ID;
	}
	
	@Override
	public boolean avisynth() {
		return true;
	}
	
	@Override
	public String name() {
		return "Avisynth/MEncoder"; //$NON-NLS-1$
	}

}

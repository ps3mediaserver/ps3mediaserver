package net.pms.medialibrary.gui.tab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

public class OptionEntry extends JPanel implements MouseListener {
    private static final long serialVersionUID = 1556143280377575566L;
	private ImageIcon icon;
	private JLabel lTitle;
	private JLabel lIcon;
	
	private boolean isSelected = false;

    public OptionEntry(String title, String iconPath){
    	setBackground(Color.white);
    	
	    lTitle = new JLabel();
	    lIcon = new JLabel();
	    
	    setTitle(title);
	    setIcon(iconPath);
	    
    	initEntries();
    	
    	this.addMouseListener(this);
    }

    public OptionEntry(String title, ImageIcon icon){
		this.setIcon(icon);
    	initEntries();
    }

	private void initEntries() {		
		FormLayout layout = new FormLayout("p, 7px, fill:10:grow",
		"p");
        PanelBuilder builder = new PanelBuilder(layout);
        builder.setOpaque(false);
        
        CellConstraints cc = new CellConstraints();

	    builder.add(lIcon, cc.xy(1, 1));
	    builder.add(lTitle, cc.xy(3, 1));

	    this.setLayout(new GridLayout());
	    this.add(builder.getPanel());
    }

	public void setIcon(String iconPath) {
		setIcon(new ImageIcon(getClass().getResource(iconPath)));
    }

	public void setIcon(ImageIcon icon) {
		this.lIcon.setPreferredSize(new Dimension(icon.getIconWidth() + 4, icon.getIconHeight() + 4));
		this.lIcon.setIcon(icon);
	    this.icon = icon;
    }

	public ImageIcon getIcon() {
	    return icon;
    }

	public void setTitle(String title) {
	    this.lTitle.setText(title);
    }

	public String getTitle() {
	    return lTitle.getText();
    }
	
	public void setSelected(boolean selected){
		this.isSelected = selected;
		if(selected){
			setBackground(new Color(120, 185, 212));
		} else {
			setBackground(Color.white);	 	
		}
	}
	
	public boolean isSelected(){
		return this.isSelected;
	}
	

	@Override
    public void mouseClicked(MouseEvent e) {
		if(!isSelected){
			//setSelected(true);
		}
    }

	@Override
    public void mouseEntered(MouseEvent e) {
		if(!isSelected){
			setBackground(new Color(241, 251, 255));
		}
    }

	@Override
    public void mouseExited(MouseEvent e) {
		if(!isSelected){
			setBackground(Color.white);	    
		}
    }

	@Override
    public void mousePressed(MouseEvent e) { }
	@Override
    public void mouseReleased(MouseEvent e) { }
}

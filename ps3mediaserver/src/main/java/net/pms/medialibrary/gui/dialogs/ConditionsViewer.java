package net.pms.medialibrary.gui.dialogs;

import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOCondition;
import net.pms.medialibrary.commons.dataobjects.DOFilter;
import net.pms.medialibrary.commons.enumarations.ConditionType;
import net.pms.medialibrary.commons.enumarations.ConditionUnit;
import net.pms.medialibrary.commons.helpers.FolderHelper;

public class ConditionsViewer extends JDialog{
    private static final long serialVersionUID = 4045025554595034476L;

    public ConditionsViewer(DOFilter filter, String folderName, Dialog owner){
    	super(owner, true);
    	setTitle(String.format(Messages.getString("ML.ConditionsViewer.Title"), folderName));
    	setResizable(false);
    	
		PanelBuilder builder;
		CellConstraints cc = new CellConstraints();

		FormLayout layout = new FormLayout("5px, r:p, 20px, p, 20px, p, 20px, p, 5px", // columns
		        "10px, p, p, p, p, p, p, p, p, p, p, " +
		        "p, p, p, p, p, p, p, p, p, p, " +
		        "p, p, p, p, p, p, p, p, p, p, " +
		        "p, p, p, p, p, p, p, p, p, p, 5px, p, 2px, p, p"); // rows
		builder = new PanelBuilder(layout);
		builder.setOpaque(true);
    	
		//add conditions
		int i = 2;
		for(DOCondition c : filter.getConditions()){
			String coString;
			if(c.getType() == ConditionType.FILE_CONTAINS_TAG){
				//add the tag name if required
				coString = String.format("'%s' %s",  c.getTagName(), Messages.getString("ML.Condition.Operator.Contains." + c.getOperator().toString()));
			} else if(c.getType().toString().contains("_CONTAINS_")) {
				//do a certain mapping for some condition types
				coString = Messages.getString("ML.Condition.Operator.Contains." + c.getOperator().toString());
			} else {
				//normal behavior
				coString = Messages.getString("ML.Condition.Operator." + c.getOperator().toString());
			}
			
			builder.addLabel(c.getName() , cc.xy(2, i));
			builder.addLabel(FolderHelper.getHelper().getConditionTypeCBItem(c.getType()).getDisplayName(), cc.xy(4, i));
			builder.addLabel(coString, cc.xy(6, i));
			String cStr = c.getCondition();
			if(c.getUnit() != ConditionUnit.UNKNOWN){
				cStr += " " + FolderHelper.getHelper().getConditionUnitCBItem(c.getUnit()).getDisplayName();
			}
			builder.addLabel(cStr, cc.xy(8, i));
			i++;
		}
		
		//add equation
		JTextField tfEq = new JTextField(filter.getEquation());
		tfEq.setEditable(false);
		builder.add(tfEq, cc.xyw(2, 43, 7));
		
		//add button
		JButton bOk = new JButton(Messages.getString("ML.ConditionsViewer.bOk"));
		if(bOk.getPreferredSize().width < 60) bOk.setPreferredSize(new Dimension(60, bOk.getPreferredSize().height));
		bOk.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		builder.addSeparator("", cc.xyw(1, 45, 9));
		builder.add(bOk , cc.xyw(1, 46, 9, CellConstraints.CENTER, CellConstraints.FILL));
		
		getContentPane().add(builder.getPanel());
		pack();
    }
}

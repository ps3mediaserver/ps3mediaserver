package net.pms.medialibrary.gui.tab.libraryview;

import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import net.pms.Messages;
import net.pms.medialibrary.commons.dataobjects.DOTableColumnConfiguration;
import net.pms.medialibrary.commons.enumarations.ConditionType;

public class FileDisplayTableColumnModel extends DefaultTableColumnModel {
	private static final long serialVersionUID = 1892314277797605186L;

	@Override
	public TableColumn getColumn(int columnIndex){
		TableColumn res = super.getColumn(columnIndex);
		if(!(res.getIdentifier() instanceof DOTableColumnConfiguration)){
			DOTableColumnConfiguration cConf = new DOTableColumnConfiguration(getConditionTypeByName(res.getHeaderValue().toString()), columnIndex, res.getWidth());
			res.setIdentifier(cConf);
			res.setHeaderValue(cConf);
		}
		return res;		
	}
	
	private ConditionType getConditionTypeByName(String columnHeader){
		ConditionType res = ConditionType.UNKNOWN;
		String baseName = "ML.Condition.Header.Type.";
		for(ConditionType c : ConditionType.values()){
			if(Messages.getString(baseName + c.toString()).equals(columnHeader)){
				res = c;
				break;
			}
		}
		return res;
	}	
}

package net.pms.medialibrary.commons.interfaces;

import java.util.List;

import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.enumarations.ConditionType;

public interface IFilePropertiesEditor {
	void build();
	void updateFileInfo(DOFileInfo fileInfo);
	List<ConditionType> getPropertiesToUpdate();
}

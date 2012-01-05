package net.pms.medialibrary.commons.interfaces;

import net.pms.medialibrary.commons.dataobjects.DOFileInfo;
import net.pms.medialibrary.commons.exceptions.ConditionTypeException;

public interface IFilePropertiesEditor {
	void build();
	void updateFileInfo(DOFileInfo fileInfo) throws ConditionTypeException;
}

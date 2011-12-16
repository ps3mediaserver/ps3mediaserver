package net.pms.medialibrary.commons.helpers;

import java.util.Comparator;

import net.pms.medialibrary.commons.dataobjects.DOFolder;

public class FolderComparator implements Comparator<DOFolder>{
	public enum CompareType{
		POSITION_IN_PARENT;
	}
	
	private CompareType compareType;
	
	public FolderComparator(){
		this(CompareType.POSITION_IN_PARENT);
	}
	
	public FolderComparator(CompareType compareType){
		this.compareType = compareType;
	}

	@Override
	public int compare(DOFolder o1, DOFolder o2) {
		int retVal = 0;
		
		if(this.compareType == CompareType.POSITION_IN_PARENT){
			if(o1.getPositionInParent() == o2.getPositionInParent()){
				retVal = 0;
			}else if(o1.getPositionInParent() < o2.getPositionInParent()){
				retVal = -1;
			}else{
				retVal = 1;
			}
		}
		return retVal;
	}

}

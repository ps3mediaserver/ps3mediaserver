package net.pms.medialibrary.commons.dataobjects.comboboxitems;

import net.pms.Messages;
import net.pms.medialibrary.commons.enumarations.KeyCombination;

public class KeyCombinationCBItem implements Comparable<KeyCombinationCBItem> {
	private KeyCombination keyCombination;
	private String displayName;
	
	public KeyCombinationCBItem(){
		
	}
	
	public KeyCombinationCBItem(KeyCombination keyCombination){
		this(keyCombination, Messages.getString("ML.KeyCombination." + keyCombination));
	}
	
	public KeyCombinationCBItem(KeyCombination keyCombination, String displayName){
		this.setKeyCombination(keyCombination);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setKeyCombination(KeyCombination keyCombination) {
		this.keyCombination = keyCombination;
	}

	public KeyCombination getKeyCombination() {
		return keyCombination;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof KeyCombinationCBItem)){
			return false;
		}

		KeyCombinationCBItem compObj = (KeyCombinationCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getKeyCombination() == compObj.getKeyCombination()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getKeyCombination().hashCode();
		return hashCode;
	}

	@Override
    public int compareTo(KeyCombinationCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}

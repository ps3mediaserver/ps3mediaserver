package net.pms.medialibrary.commons.dataobjects.comboboxitems;

public class TemplateCBItem implements Comparable<TemplateCBItem> {
	private long id;
	private String displayName;
	
	public TemplateCBItem(){
		this(-1, "");
	}
	
	public TemplateCBItem(long id, String displayName){
		this.setId(id);
		this.setDisplayName(displayName);
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}
	
	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}
	
	@Override
	public String toString(){
		return getDisplayName();
	}
	
	@Override
	public boolean equals(Object o){
		if(!(o instanceof TemplateCBItem)){
			return false;
		}

		TemplateCBItem compObj = (TemplateCBItem)o;
		if(getDisplayName() == compObj.getDisplayName()
			&& getId() == compObj.getId()){
			return true;
		}
		return false;
	}

	@Override
	public int hashCode(){
		int hashCode = 24 + getDisplayName().hashCode();
		hashCode *= 24 + getId();
		return hashCode;
	}

	@Override
    public int compareTo(TemplateCBItem o) {
	    return getDisplayName().compareTo(o.getDisplayName());
    }
}

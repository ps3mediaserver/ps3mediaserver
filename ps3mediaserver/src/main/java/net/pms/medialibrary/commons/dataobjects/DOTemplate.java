package net.pms.medialibrary.commons.dataobjects;

public class DOTemplate {
	private String name;
	private long id;
	
	public DOTemplate(String name, long id){
		this.setName(name);
		this.setId(id);
	}

	public void setName(String name) {
	    this.name = name;
    }

	public String getName() {
		if(name == null) name = "";
	    return name;
    }

	public void setId(long id) {
	    this.id = id;
    }

	public long getId() {
	    return id;
    }
	
	@Override 
	public String toString(){
		return getName();
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof DOTemplate)){
			return false;
		}
		
		DOTemplate compObj = (DOTemplate)obj;
		if(getId() == compObj.getId() 
				&& getName().equals(compObj.getName())){
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hashCode = 24 + String.valueOf(getId()).hashCode();
		hashCode *= 24 + getName().hashCode();
		return hashCode;
	}

	@Override
	public DOTemplate clone(){
		return new DOTemplate(getName(), getId());
	}
}

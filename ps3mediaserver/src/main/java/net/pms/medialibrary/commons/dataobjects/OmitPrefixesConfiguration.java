package net.pms.medialibrary.commons.dataobjects;

import java.util.ArrayList;
import java.util.List;

public class OmitPrefixesConfiguration {
	private boolean sorting;
	private boolean filtering;
	private List<String> prefixes;
	
	public OmitPrefixesConfiguration(){
		this(false, false, new ArrayList<String>());
	}
	
	public OmitPrefixesConfiguration(boolean sorting, boolean filtering, List<String> prefixes){
		setSorting(sorting);
		setFiltering(filtering);
		setPrefixes(prefixes);
	}
	
	public void setSorting(boolean sorting) {
	    this.sorting = sorting;
    }
	
	public boolean isSorting() {
	    return sorting;
    }
	
	public void setFiltering(boolean filtering) {
	    this.filtering = filtering;
    }
	
	public boolean isFiltering() {
	    return filtering;
    }
	
	public void setPrefixes(List<String> prefixes) {
	    this.prefixes = prefixes;
    }
	
	public List<String> getPrefixes() {
	    return prefixes;
    }
}

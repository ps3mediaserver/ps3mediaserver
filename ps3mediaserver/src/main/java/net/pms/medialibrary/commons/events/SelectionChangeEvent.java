package net.pms.medialibrary.commons.events;

import java.util.EventObject;

import net.pms.medialibrary.commons.enumarations.OptionType;

public class SelectionChangeEvent extends EventObject {
    private static final long serialVersionUID = 357585839937709109L;
	private OptionType optionType;
	
	public SelectionChangeEvent(Object source, OptionType optionType) {
	    super(source);
	    this.optionType = optionType;
    }
	
	public OptionType getOptionType(){
		return optionType;
	}
}

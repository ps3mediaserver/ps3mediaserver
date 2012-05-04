package net.pms.newgui.components;

public class ComboBoxItem<T> {
	private String displayName;
	private T value;
	
	public ComboBoxItem(String displayName, T value) {
		this.displayName = displayName;
		this.value = value;
	}

	public String getDisplayName() {
		return displayName;
	}

	public T getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return getDisplayName();
	}
}

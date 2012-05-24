package net.pms.newgui.components;

/**
 * This calss can be used to add a combo box item with a name to display and a
 * generic value
 * 
 * @param <T> the generic type
 */
public class ComboBoxItem<T> {
	private String displayName;
	private T value;

	/**
	 * Instantiates a new combo box item.
	 * 
	 * @param displayName the display name
	 * @param value the value
	 */
	public ComboBoxItem(String displayName, T value) {
		this.displayName = displayName;
		this.value = value;
	}

	/**
	 * Gets the display name.
	 * 
	 * @return the display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Gets the generic value.
	 * 
	 * @return the value
	 */
	public T getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ComboBoxItem)) {
			return false;
		}
		return hashCode() == obj.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		int hash = displayName == null ? 1 : displayName.hashCode();
		hash *= value == null ? 2 : value.hashCode();
		return hash;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return getDisplayName();
	}
}

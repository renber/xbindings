package org.jdesktop.xbindings.properties;

public interface XPropertyBase<T> {

	/**
	 * Return the name of this property
	 */
	public String getName();
	
	/**
	 * Return the type of this property's value
	 */
	public Class<T> getValueType();
	
	/**
	 * Returns whether this property is writable
	 */
	public boolean isWritable();
}

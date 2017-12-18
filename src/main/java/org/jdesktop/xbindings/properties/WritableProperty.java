package org.jdesktop.xbindings.properties;

/**
 * Represents a property of type T which is writable through a setter
 * @author berre
 */
public interface WritableProperty<T> extends XPropertyBase<T> {

	public void set(T newValue);
	
}

package org.jdesktop.xbindings.properties;

/**
 * Represents a property of type T which is readable through a getter
 * @author berre
 */
public interface ReadableProperty<T> extends XPropertyBase<T> {

	public T get();
	
}

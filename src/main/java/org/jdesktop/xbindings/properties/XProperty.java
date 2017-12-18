package org.jdesktop.xbindings.properties;

/**
 * A property which is both readable and writable
 * @author berre
 */
public interface XProperty<T> extends ReadableProperty<T>, WritableProperty<T> {

	@Override
	default public boolean isWritable() {
		return true;
	}
	
}

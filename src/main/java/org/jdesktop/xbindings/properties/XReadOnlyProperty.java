package org.jdesktop.xbindings.properties;

public interface XReadOnlyProperty<T> extends ReadableProperty<T> {

	default public boolean isWritable() {
		return false;
	}	
}

package org.jdesktop.xbindings.properties.creators;

import java.util.function.Consumer;

import org.jdesktop.xbindings.properties.XProperty;

/**
 * A chainable interface for creating XProperties
 */
public interface PropertyCreator<T> {		
	
	/**
	 * Declares the name of the property to create
	 * @param propertyName
	 */
	public PropertyCreator<T> name(String propertyName);
	
	/**
	 * Declares that this property should override the properties
	 * of all super classes of instance instead of just hiding them	 
	 */
	public PropertyCreator<T> overrideOn(Object instance);
	
	/**
	 * Sets the action which is called when the value of the Property has been changed
	 */
	public PropertyCreator<T> notify(PropertyChangeNotifier notifyFunc);
	
	/**
	 * Creates an XProperty which is both readable and writeable with the given default value
	 * @param defaultValue
	 * @return
	 */
	public XProperty<T> createWithDefault(T defaultValue);	
}

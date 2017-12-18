package org.jdesktop.xbindings.properties;

import java.lang.reflect.Field;
import java.util.function.Supplier;

import org.jdesktop.xbindings.XNotifyPropertyChanged;
import org.jdesktop.xbindings.properties.creators.PropertyChangeNotifier;
import org.jdesktop.xbindings.properties.creators.PropertyCreator;

/**
 * Class which allows to create XProperties
 */
public class AutoProperty {	
	
	private AutoProperty() {
		// static
	}

	public static <T> PropertyCreator<T> ofType(Class<T> propertyType) {
		return new XPropertyCreator<T>(propertyType);
	}	
	
	public static <T> XReadOnlyProperty<T> readOnly(XProperty<T> property) {
		return new XReadOnlyWrapper<>(property);
	}

	private static class XPropertyCreator<T> implements PropertyCreator<T> {

		String propertyName;
		PropertyChangeNotifier notifyFunc;
		Class<T> valueType;
		Object overrideOnInstance;
		
		public XPropertyCreator(Class<T> valueType) {
			this.valueType = valueType;
		}
		
		@Override
		public PropertyCreator<T> name(String propertyName) {
			if (propertyName == null)
				throw new IllegalArgumentException("propertyName must not be null");
			
			this.propertyName = propertyName;
			return this;
		}

		@Override
		public PropertyCreator<T> notify(PropertyChangeNotifier notifyFunc) {
			this.notifyFunc = notifyFunc;
			return this;
		}

		@Override
		public XProperty<T> createWithDefault(T defaultValue) {
						
			XProperty<T> prop = new XPropertyImpl<T>(propertyName, notifyFunc, valueType, defaultValue);
			
			if (overrideOnInstance != null) {
				setFieldOnSuperInstances(overrideOnInstance, overrideOnInstance.getClass().getSuperclass(), prop.getName(), prop);
			}
			
			return prop;
		}

		/**
		 * Indicate that this property should override all properties of the same name
		 * in the property owner's super classes instead of hiding them
		 */
		@Override
		public PropertyCreator<T> overrideOn(Object instance) {
			overrideOnInstance = instance;
			return this;
		}
		
		private void setFieldOnSuperInstances(Object instance, Class<?> superClass, String fieldName, Object value) {									
			if (superClass != null) {
				try {					
					Field f = getFieldOfClass(superClass, fieldName);
					if (f != null) {
						if (!f.isAccessible()) {
							f.setAccessible(true);
						}
						
						f.set(instance, value);													
					}
					
					setFieldOnSuperInstances(instance, superClass.getSuperclass(), fieldName, value);
				} catch (IllegalArgumentException | IllegalAccessException | SecurityException e) {
					// 
				}
			}
		}
		
		/**
		 * Return the field declaration for the given class or null
		 * if the class has no field of teh given name
		 */
		Field getFieldOfClass(Class<?> clazz, String fieldName) {
		    for (Field field : clazz.getFields()) {
		        if (field.getName().equals(fieldName)) {
		            return field;
		        }
		    }
		    return null;
		}
	
	}		
	
	/**
	 * Implementation of a readable and writable property
	 * @author berre	 
	 */
	private static class XPropertyImpl<T> implements XProperty<T> {

		String propertyName;
		PropertyChangeNotifier notifyFunc;
		T value;
		Class<T> valueType;
		
		@Override
		public String getName() {
			return propertyName;
		}
		
		public Class<T> getValueType() {
			return valueType;	
		}
		
		public XPropertyImpl(String propertyName, PropertyChangeNotifier notifyFunc, Class<T> valueType, T defaultValue) {
			
			if (propertyName == null)
				throw new IllegalArgumentException("propertyName must not be null");
					
			this.propertyName = propertyName;
			this.notifyFunc = notifyFunc;
			this.valueType = valueType;
			value = defaultValue;			
		}
		
		@Override
		public T get() {
			return value;
		}

		@Override
		public void set(T newValue) {	
			if (notifyFunc == null)
				value = newValue;
			else {			
				if (value != newValue || !value.equals(newValue)) {				
					T oldValue = value;
					value = newValue;
					notifyFunc.notifyPropertyChanged(propertyName, oldValue, newValue);
				}			
			}
		}	
	}
	
	/**
	 * Implementation of a read-only property which wraps an XProperty making it read-only to the outside
	 * @author berre	 
	 */
	private static class XReadOnlyWrapper<T> implements XReadOnlyProperty<T> {
		
		XProperty<T> property;

		public XReadOnlyWrapper(XProperty<T> wrappedProperty) {
			property = wrappedProperty;
		}
		
		@Override
		public T get() {
			return property.get();
		}

		@Override
		public String getName() {
			return property.getName();
		}

		@Override
		public Class<T> getValueType() {
			return property.getValueType();
		}
					
	}	
}



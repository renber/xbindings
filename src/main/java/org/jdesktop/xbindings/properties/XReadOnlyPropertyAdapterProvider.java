package org.jdesktop.xbindings.properties;

import java.beans.PropertyChangeListener;
import java.lang.reflect.Field;

import org.jdesktop.beansbinding.ext.BeanAdapterProvider;
import org.jdesktop.swingbinding.adapters.BeanAdapterBase;
import org.jdesktop.xbindings.XBindingOptions;
import org.jdesktop.xbindings.XNotifyPropertyChanged;

public class XReadOnlyPropertyAdapterProvider implements BeanAdapterProvider {

	@Override
	public boolean providesAdapter(Class<?> type, String property) {

		if (!XBindingOptions.getActive().areXPropertiesEnabled()) {
			return false;
		}

		Field f = tryGetField(type, property);
		if (f != null) {
			return XReadOnlyProperty.class.isAssignableFrom(f.getType());
		} else {
			return false;
		}
	}

	private static Field tryGetField(Class<?> type, String fieldName) {
		Field[] fields = type.getFields();
		for(int i = 0; i < fields.length; i++)
		{
			if (fields[i].getName().equals(fieldName)) {
				return fields[i];
			}
		}
		return null;
	}

	@Override
	public Object createAdapter(Object source, String property) {				
		try {
			return new Adapter(source, (XReadOnlyProperty)source.getClass().getField(property).get(source));
		} catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public Class<?> getAdapterClass(Class<?> type) {
		return XReadOnlyProperty.class.isAssignableFrom(type) ? Adapter.class : null;
	}

	public static final class Adapter extends BeanAdapterBase {
		
		// The object which contains the XProperty
		Object owner;
		// The XProperty
		XReadOnlyProperty prop;
		
		PropertyChangeListener ownerListener;
		
		public Adapter(Object owner, XReadOnlyProperty prop) {
			super(prop.getName());
			
			this.owner = owner;
			this.prop = prop;
		}		
				
		public Object getValue() {
			return prop.get();
		}
		
		@Override
		protected void listeningStarted() {
			if (owner instanceof XNotifyPropertyChanged) {
				if (ownerListener == null) {
					ownerListener = (e) -> {
						if (e.getPropertyName().equals(prop.getName())) {
							firePropertyChange(e.getOldValue(), e.getNewValue());
						}};												
				}					
							
				((XNotifyPropertyChanged)owner).addPropertyChangeListener(ownerListener);
			}
		}
		
		@Override
		protected void listeningStopped() {
			if (ownerListener != null && owner instanceof XNotifyPropertyChanged)
				((XNotifyPropertyChanged)owner).removePropertyChangeListener(ownerListener);
		}
	}
	
}

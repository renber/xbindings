package org.jdesktop.xbindings.properties;

import java.beans.PropertyChangeListener;

import org.jdesktop.beansbinding.ext.BeanAdapterProvider;
import org.jdesktop.swingbinding.adapters.BeanAdapterBase;
import org.jdesktop.xbindings.XNotifyPropertyChanged;
import org.jdesktop.xbindings.properties.XPropertyAdapterProvider.Adapter;

public class XReadOnlyPropertyAdapterProvider implements BeanAdapterProvider {

	@Override
	public boolean providesAdapter(Class<?> type, String property) {				
		try {
			return XReadOnlyProperty.class.isAssignableFrom(type.getField(property).getType());
		} catch (NoSuchFieldException e) {
			// there is no XReadOnlyProperty with this name
			return false;
		}
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

package org.jdesktop.xbindings.context;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.PropertyHelper;
import org.jdesktop.beansbinding.PropertyStateListener;

/**
 * DataContext implementation for java beans
 * @author renber
 *
 */
public class BeansDataContext implements DataContext {

	Object source;
	String path = "";
	PropertyHelper propertyHelper;
	
	public BeansDataContext(Object source) {
		this.source = source;			
	}
	
	private BeansDataContext(Object source, String path) {
		if (path == null)
			throw new IllegalArgumentException("path must not be null");
		
		this.source = source;
		this.path = path;
	}

	@Override
	public DataContext path(String path) {
		if (this.path.isEmpty())
			return new BeansDataContext(source, path);
		else
			return new BeansDataContext(source, String.join(".", this.path, path));
	}
	
	/**
	 * Return the source data object of this DataContext
	 */
	public Object getSource() {
		return source;
	}

	@Override
	public Object getValue() {
		if (path.isEmpty())
			return source;
		else
			return getPropertyHelper().getValue(source);
	}
		
	public PropertyHelper getPropertyHelper() {
		if (propertyHelper == null)
			propertyHelper = BeanProperty.create(path);
		
		return propertyHelper;
	}

	@Override
	public void addPropertyStateListener(PropertyStateListener listener) {		
		// root objects never fire property change events
		if (!path.isEmpty()) {
			getPropertyHelper().addPropertyStateListener(source, listener);
		}
	}
	
	@Override
	public void removePropertyStateListener(PropertyStateListener listener) {		
		// root objects never fire property change events
		if (!path.isEmpty()) {
			getPropertyHelper().removePropertyStateListener(source, listener);
		}
	}
	
}

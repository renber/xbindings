package org.jdesktop.xbindings.context;

import org.jdesktop.beansbinding.PropertyHelper;
import org.jdesktop.beansbinding.PropertyStateListener;

/**
 * Wraps a ViewModel and supplies methods to access property paths without knowing the ViewModel's class
 * @author renber
 */
public interface DataContext {	
	
	/**
	 * Returns a new DataContext instance which points to the given subPath of this DataContext
	 * @param path The sub property path
	 */
	public DataContext path(String path);
	
	/**
	 * Evaluate this datacontext and return its current value, if any
	 * @return
	 */
	public Object getValue();	
		
	public void addPropertyStateListener(PropertyStateListener listener);
	public void removePropertyStateListener(PropertyStateListener listener);
}
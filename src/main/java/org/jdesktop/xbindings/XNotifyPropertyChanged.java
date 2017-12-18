package org.jdesktop.xbindings;

import java.beans.PropertyChangeListener;

/**
 * Interface for property change support
 * Interface which wraps access to a beans PropertyChangeSupport field
 * @author berre
 */
public interface XNotifyPropertyChanged {

	/**
	 * Register the given property change listener.
	 * The listener will be informed when a property of this object changes its value
	 * @param listener
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * Remove the given property change listener.
	 * The listener will no longer be informed of property changes for this object
	 * @param listener
	 */
    public void removePropertyChangeListener(PropertyChangeListener listener);

}

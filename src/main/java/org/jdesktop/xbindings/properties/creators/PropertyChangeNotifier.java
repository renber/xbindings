package org.jdesktop.xbindings.properties.creators;

@FunctionalInterface
public interface PropertyChangeNotifier {

	public void notifyPropertyChanged(String propertyName, Object oldValue, Object newValue);
	
}

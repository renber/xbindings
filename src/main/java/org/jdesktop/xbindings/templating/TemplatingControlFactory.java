package org.jdesktop.xbindings.templating;

import java.awt.Component;
import java.awt.Container;
import java.awt.LayoutManager;

import org.jdesktop.xbindings.context.DataContext;

/**
 * Factory to create controls with a DataContext
 * @author renber
 */
public interface TemplatingControlFactory extends TemplatingFactory<Container, Component> {
		
	/**
	 * Return the layout data to use for the given control which has been assigned the given dataContext
	 * @param itemComposite
	 * @param dataContext
	 * @return
	 */
	public Object getLayoutData(LayoutManager parentLayout, Component itemControl, DataContext itemDataContext);	
}

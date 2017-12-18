package org.jdesktop.xbindings.templating;

import org.jdesktop.xbindings.context.DataContext;

/**
 * A generic factory to create objects with a dataContext
 * @author renber
 */
public interface TemplatingFactory<ParentType, ObjectType> {

	/**
	 * Create a control for the given dataContext 
	 * @param parent
	 * @param dataContext
	 * @return
	 */
	public ObjectType create(ParentType parent, DataContext itemDataContext);
}
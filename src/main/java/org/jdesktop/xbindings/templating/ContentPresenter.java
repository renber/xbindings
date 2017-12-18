package org.jdesktop.xbindings.templating;

import java.awt.BorderLayout;
import java.awt.Component;

import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;

/**
 * A swing container which dynamically creates its content based on a (bound) object
 * 
 * @author renber
 */
public class ContentPresenter extends TemplatingParent<Object> {

	public ContentPresenter() {
		// the default layout
		this.setLayout(new BorderLayout());
	}
	
	@Override
	protected TemplatingControlFactory getDefaultItemFactory() {
		return new DefaultControlFactory();
	}

	@Override
	protected void itemSourceChanged() {
		updateContent();
	}
	
	protected void updateContent() {

		try {
			// suspend redrawing of this container until we updated all contains
			// (suppresses flickering)
			this.setIgnoreRepaint(true);

			// remove all children of this composite
			while (this.getComponentCount() > 0) {
				this.remove(0);
			}			

			// recreate them for the current item source value
			if (itemSource == null || itemControlFactory == null)
				return;

			if (itemSource == null)
				return;

			// instantiate a new child control based on the template class
			// and pass in the list item as DataContext
			DataContext itemDataContext = new BeansDataContext(itemSource);
			Component itemControl = itemControlFactory.create(this, itemDataContext);
			Object ld_item = itemControlFactory.getLayoutData(getLayout(), itemControl, itemDataContext);
			if (ld_item == null && this.getLayout() instanceof BorderLayout)
				ld_item = BorderLayout.CENTER;
				
			this.add(itemControl, ld_item);							

			// relayout the children			
			this.doLayout();			
		} finally {
			this.setIgnoreRepaint(false);
		}
	}

}

package org.jdesktop.xbindings.templating;

import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.beans.Beans;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.RepaintManager;

import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;
import org.jdesktop.xbindings.XBinding;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;

import net.miginfocom.swing.MigLayout;

/**
 * A swing container which dynamically creates its children based on a (bound) ObservableList
 * and a TemplateComponent which is bound to the corresponding list item
 * 
 * @author renber
 */
public class ItemsControl extends TemplatingParent<ObservableList> {
	
	ObservableList currentSourceList;
	ObservableListListener listListener;

	/**
	 * Create an ItemsControl whose default Layout is a one-column GridLayout
	 */
	public ItemsControl() {				
		listListener = new SourceListChangeListener();

		BoxLayout boxLayout = new BoxLayout(this, BoxLayout.Y_AXIS);
		setLayout(boxLayout);				
		if (Beans.isDesignTime()) {			
			JLabel designTimeCaptionLbl = new JLabel("<ItemsControl>");						
			this.add(designTimeCaptionLbl);
		}
	}
	
	@Override
	protected TemplatingControlFactory getDefaultItemFactory() {
		return new DefaultControlFactory();
	}

	@Override
	protected void itemSourceChanged() {
			
		if (currentSourceList != itemSource) {			
			// remove listener of the old list
			if (currentSourceList != null)
				currentSourceList.removeObservableListListener(listListener);
	
			// track content changes of the new list
			currentSourceList = itemSource;
			if (currentSourceList != null)
				currentSourceList.addObservableListListener(listListener);		
	
			updateChildren();
		}
	}

	protected void updateChildren() {

		try {
			// suspend redrawing of this container until we updated all contents
			// (suppresses flickering)
			this.setIgnoreRepaint(true);			

			// remove all children of this composite
			while (this.getComponentCount() > 0) {
				Component c = this.getComponent(0);
				if (c instanceof XBinding) {
					((XBinding)c).unbind();
				}
				
				this.remove(0);
			}				

			// recreate them for the current list value
			if (itemSource == null || itemControlFactory == null)
				return;

			if (currentSourceList == null)
				return;					

			for (Object item : currentSourceList) {
				// instantiate a new child control based on the template class
				// and pass in the list item as DataContext
				DataContext itemDataContext = new BeansDataContext(item);
				Component itemControl = itemControlFactory.create(this, itemDataContext);
				Object ld_item = itemControlFactory.getLayoutData(getLayout(), itemControl, itemDataContext);								
				
				this.add(itemControl, ld_item);				
			}				
		} finally {
			// relayout the children
            this.setIgnoreRepaint(false);
            this.revalidate();
		}				
	}

	class SourceListChangeListener implements ObservableListListener {

		@Override
		public void listElementsAdded(ObservableList list, int index, int length) {
			updateChildren();
		}

		@Override
		public void listElementsRemoved(ObservableList list, int index, List oldElements) {
			updateChildren();
		}

		@Override
		public void listElementReplaced(ObservableList list, int index, Object oldElement) {
			updateChildren();
		}

		@Override
		public void listElementPropertyChanged(ObservableList list, int index) {
			updateChildren();
		}
		
	}
}

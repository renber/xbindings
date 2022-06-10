package org.jdesktop.xbindings.templating;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Scrollable;

import org.jdesktop.beansbinding.PropertyHelper;
import org.jdesktop.beansbinding.PropertyStateEvent;
import org.jdesktop.beansbinding.PropertyStateListener;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.xbindings.context.DataContext;

/**
 * Base class for controls which lay out their children according to a bindable
 * source and a TemplateFactory
 * 
 * @author renber
 *
 */
public abstract class TemplatingParent<TItemSource> extends JPanel {

	protected TItemSource itemSource;

	protected TemplatingControlFactory itemControlFactory;	

	/**
	 * Create a TemplatingParent
	 */
	public TemplatingParent() {		
		itemControlFactory = getDefaultItemFactory();
	}

	/**
	 * Set the source list
	 * 
	 * @param itemSourceValue
	 */
	public void setItemSource(TItemSource itemSource) {
		if (this.itemSource != itemSource) {
			this.itemSource = itemSource;		
			itemSourceChanged();
		}
	}

	public TItemSource getItemSource() {
		return itemSource;
	}

	/**
	 * The factory to use to create the child composites
	 * 
	 * @param itemCompositeFactory The factory to use or null to use the default factory of this control
	 */
	public void setItemFactory(TemplatingControlFactory itemControlFactory) {
		if (itemControlFactory == null)
			this.itemControlFactory = getDefaultItemFactory();
		else
			this.itemControlFactory = itemControlFactory;
	}

	public TemplatingControlFactory getItemFactory() {
		return this.itemControlFactory;
	}
	
	/**
	 * Return an instance of the default item factory for this control
	 * (Must not return null!)
	 */
	protected abstract TemplatingControlFactory getDefaultItemFactory();

	/**
	 * Called when the item source changes its value and the content of this
	 * control has to be recreated or relayouted	 
	 */
	protected abstract void itemSourceChanged();

}

package org.jdesktop.xbindings.templating;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.Beans;
import java.beans.PropertyChangeListener;

import javax.swing.*;

import org.jdesktop.beansbinding.Converter;
import org.jdesktop.xbindings.BindingContext;
import org.jdesktop.xbindings.XBinding;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;
import org.jdesktop.xbindings.templating.ItemsControl.SourceListChangeListener;

import net.miginfocom.swing.MigLayout;

/**
 * A swing container which dynamically creates its children based on a (bound) ObservableList
 * and a TemplateComponent which is bound to the corresponding list item and supports selection of items
 * 
 * To allow scrolling in the listbox it has to be put inside a JScrollPane
 * 
 * @author renber
 */
public class ControlListBox extends ItemsControl implements Scrollable {

	// TODO: multiselection
	
	BindingContext internalBindingContext = new BindingContext();
	MouseListener itemMouseListener = new ItemMouseListener();
	
	Color selectionBackgroundColor;
	Color selectionForegroundColor;
	
	Object selectedItem;	
	
	public ControlListBox() {		
		listListener = new SourceListChangeListener();		
		
		MigLayout migLayout = new MigLayout("gap 0, ins 0", "[fill]", "[fill]");
		setLayout(migLayout);			
		
		if (Beans.isDesignTime()) {			
			JLabel designTimeCaptionLbl = new JLabel("<ItemsListBox>");						
			this.add(designTimeCaptionLbl);
		}		
				
		selectionBackgroundColor = SystemColor.textHighlight;
		selectionForegroundColor = SystemColor.textHighlightText;
		
		this.setBackground(Color.white);
		this.setFocusable(true);
		
		this.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {						
				// focus the listbox
				ControlListBox.this.requestFocusInWindow();
			}
		});
		
		setupKeyboardInput();
	}
	
	private void setupKeyboardInput() {
		ActionMap actionMap = this.getActionMap();
	    int condition = JComponent.WHEN_FOCUSED;
	    InputMap inputMap = this.getInputMap(condition);

	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "ARROW_UP");
	    actionMap.put("ARROW_UP", new AbstractAction () {
	    	@Override
	    	   public void actionPerformed(ActionEvent arg0) {
	    	      setSelectedIndex(getSelectedIndex() - 1);
	    	   // scroll to make the new selection visible, if required
	    	      ensureItemIsVisible(selectedItem);
	    	   }
	    });
	    
	    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "ARROW_DOWN");
	    actionMap.put("ARROW_DOWN", new AbstractAction () {
	    	@Override
	    	   public void actionPerformed(ActionEvent arg0) {
	    	      setSelectedIndex(getSelectedIndex() + 1);
	    	      // scroll to make the new selection visible, if required
	    	      ensureItemIsVisible(selectedItem);
	    	   }
	    });
	}
	
	public void setSelectedItem(Object modelItem) {
		Object oldValue = selectedItem;
		selectedItem = modelItem;
		firePropertyChange("selectedItem", oldValue, selectedItem);

		// scroll the item into view, if necessary
		if (modelItem != null)
			ensureItemIsVisible(modelItem);
	}
	
	public Object getSelectedItem() {
		return selectedItem;
	}
	
	private int getSelectedIndex() {
		if (currentSourceList == null || getSelectedItem() == null)
			return -1;
		
		return currentSourceList.indexOf(getSelectedItem());
	}
	
	private void setSelectedIndex(int newIndex) {
		if (currentSourceList == null)
			return;
		
		if (newIndex >= 0 && newIndex < currentSourceList.size())
			setSelectedItem(currentSourceList.get(newIndex));
	}

	private void ensureItemIsVisible(Object item) {
		ensureItemIsVisible(item, true);
	}

	private void ensureItemIsVisible(Object item, boolean allowInvoke) {
				
		if (item == null)
			return;
		
		Component target = getContainerForModel(item);
		if (target != null) {
			Rectangle rect = target.getBounds();

			if (rect.x == 0 && rect.y == 0 && rect.width == 0 && rect.height == 0 && allowInvoke) {
				// item has not been layouted yet, wait for layouting then try again once
				SwingUtilities.invokeLater(() -> ensureItemIsVisible(item, false));
			} else {
				this.scrollRectToVisible(rect);
			}
		}
	}
	
	@Override
	protected void updateChildren() {

		try {
			// suspend redrawing of this container until we updated all contains
			// (suppresses flickering)
			this.setIgnoreRepaint(true);

			internalBindingContext.clear();
			
			// remove all children of this composite
			for (int i = 0; i < this.getComponentCount(); i++) {
				Component c = this.getComponent(0);				
				if (c instanceof XBinding) {
					((XBinding)c).unbind();
				}						
			}						
			this.removeAll();

			// recreate them for the current list value
			if (itemSource == null || itemControlFactory == null)
				return;

			if (currentSourceList == null)
				return;

			for (Object item : currentSourceList) {
				// instantiate a new child control based on the template class
				// and pass in the list item as DataContext
				DataContext itemDataContext = new BeansDataContext(item);
				
				// the generated components will be wrapped in a Panel to allow
				// change of selection background etc.
				Container itemContainer = createItemContainer(item);
				
				Component itemControl = itemControlFactory.create(itemContainer, itemDataContext);				
				
				if (itemControl != null) {
					prepareItemControl(itemControl);
					itemContainer.add(itemControl, BorderLayout.CENTER);
				}				
				
				// allow the items to shrink
				itemContainer.setMinimumSize(new Dimension(1, 1));
				
				Object ld_item = itemControlFactory.getLayoutData(getLayout(), itemContainer, itemDataContext);
				if (ld_item == null)
					ld_item = "growx, pushx, wrap";								
				
				this.add(itemContainer, ld_item);			
				itemContainer.invalidate();
			}						
		} finally {
			// relayout the children											
			this.revalidate();
			this.setIgnoreRepaint(false);
		}		
	}
	
	/**
	 * Creates a new container which houses the contents of a item
	 */
	private Container createItemContainer(Object modelItem) {
		ItemContainerPanel c = new ItemContainerPanel(modelItem);
		c.setLayout(new BorderLayout());
		c.addMouseListener(itemMouseListener);		
		
		internalBindingContext.bind(this, "selectedItem", c, "background", new Converter<Object, Color>() {
			@Override
			public Color convertForward(Object value) {
				if (value == c.getModelObject())
					return selectionBackgroundColor;
				
				return getBackground();
			}

			@Override
			public Object convertReverse(Color value) {
				// not used
				return null;
			}});
		internalBindingContext.bind(this, "selectedItem", c, "foreground", new Converter<Object, Color>() {
			@Override
			public Color convertForward(Object value) {
				if (value == c.getModelObject())
					return selectionForegroundColor;
				
				return getForeground();
			}

			@Override
			public Object convertReverse(Color value) {
				// not used
				return null;
			}});		
		
		return c;
	}
	
	private void prepareItemControl(Component itemControl) {
		// set background and foreground of JLabels and JPanels to null
		// so that they inherit the parent's values
		if ((itemControl instanceof JLabel) || (itemControl instanceof JPanel)) {
			itemControl.setBackground(null);
			itemControl.setForeground(null);
		}
		
		if (itemControl instanceof Container) {
			for(int i = 0; i < ((Container)itemControl).getComponentCount(); i++) {
				prepareItemControl(((Container) itemControl).getComponent(i));
			}
		} 	
	}
	
	/**
	 * Returns component which has been generated by the ItemFactory for the given model 
	 * @return the container or null if the given model is not part of the list items
	 */
	public Component getContainerForModel(Object item) {
		for(int i = 0; i < this.getComponentCount(); i++) {
			Component c = this.getComponent(i);
			if (c instanceof ItemContainerPanel && ((ItemContainerPanel)c).modelObject == item)
				return c;
		}
		
		return null;
	}
	
	/**
	 * MouseListener for the listbox items
	 *
	 */
	private class ItemMouseListener extends MouseAdapter {
		
		public void mousePressed(MouseEvent e) {
			setSelectedItem(((ItemContainerPanel)e.getSource()).getModelObject());
			
			// focus the listbox
			ControlListBox.this.requestFocusInWindow();
		}
	}
	
	private static class ItemContainerPanel extends JPanel {
		
		private Object modelObject;
		
		public ItemContainerPanel(Object modelObject) {
			super();
			
			this.modelObject = modelObject;
		}
		
		public Object getModelObject() {
			return modelObject;
		}
		
	}

	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (getComponentCount() > 0) {
			// use the height of the first component
			return (int)getComponent(0).getPreferredSize().getHeight();
		}
		
		return 0;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
		if (getComponentCount() > 0) {
			// use the height of the first component
			return (int)getComponent(0).getPreferredSize().getHeight();
		}
		
		return 0;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		// no horizontal scrollbar, but shrink items when the vertical scrollbar is shown
		return true;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
	
}

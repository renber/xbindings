package org.jdesktop.xbindings;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.PropertyHelper;
import org.jdesktop.beansbinding.PropertyStateEvent;
import org.jdesktop.beansbinding.PropertyStateListener;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;

/**
 * Class which helps in creating two-way bindings of the selection of a list to
 * a ViewModel-Property/-List (the default binding of beans binding only works
 * from target -> source)
 * 
 * @author berre
 */
public abstract class XSelectionBinding implements XBinding {

	/**
	 * The view model source list (which is bound to the List)
	 */
	protected DataContext sourceListContext;	

	/**
	 * The target selection model to bind to
	 */
	protected ListSelectionModel targetSelectionModel;

	final List emptyList = new ArrayList(0);
	
	/**
	 * The selection listener of the target
	 */
	protected ListSelectionListener targetListSelectionListener;

	protected XSelectionBinding(DataContext sourceListContext, ListSelectionModel targetSelectionModel) {
		if (sourceListContext == null)
			throw new IllegalArgumentException("Parameter sourceListContext must not be null");
		if (targetSelectionModel == null)
			throw new IllegalArgumentException("Parameter targetSelectionModel must not be null");

		this.sourceListContext = sourceListContext;
		this.targetSelectionModel = targetSelectionModel;
	}	
	
	protected List getSourceList() {
		Object o = sourceListContext.getValue();
		if (o == null || !(o instanceof List))
			return emptyList;
		else
			return (List)o;
	}
	
	/**
	 * Two-way binds the selected element (single selection) of the given JList
	 * to the property of source
	 */
	public static XSelectionBinding bindSingleSelection(Object source, String sourceProperty, List sourceList, JList list) {
		XSingleSelectionBinding bnd = new XSingleSelectionBinding(source, BeanProperty.create(sourceProperty), new BeansDataContext(sourceList), list.getSelectionModel());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bnd.bind();

		return bnd;
	}
	
	/**
	 * Two-way binds the selected element (single selection) of the given JList
	 * to the given dataContext
	 */
	public static XSelectionBinding bindSingleSelection(DataContext selectedItemContext, DataContext sourceListContext, JList list) {
		XSingleSelectionBinding bnd = new XSingleSelectionBinding(selectedItemContext.getSource(), selectedItemContext.getPropertyHelper(), sourceListContext, list.getSelectionModel());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bnd.bind();

		return bnd;
	}	

	/**
	 * Two-way binds the selected (row) element (single selection) of the given
	 * JTable to the property of source
	 */
	public static XSelectionBinding bindSingleSelection(Object source, String sourceProperty, List sourceList, JTable table) {
		XSingleSelectionBinding bnd = new XTableSingleSelectionBinding(source, BeanProperty.create(sourceProperty), new BeansDataContext(sourceList), table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bnd.bind();

		return bnd;
	}
	
	/**
	 * Two-way binds the selected row element (single selection) of the given JTable
	 * to the given dataContext
	 */
	public static XSelectionBinding bindSingleSelection(DataContext selectedItemContext, DataContext sourceListContext, JTable table) {		
		XSingleSelectionBinding bnd = new XTableSingleSelectionBinding(selectedItemContext.getSource(), selectedItemContext.getPropertyHelper(), sourceListContext, table);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bnd.bind();

		return bnd;
	}	
	
	/**
	 * Two-way binds the selected elements (multiple selection) of the given JList
	 * to selectedItemsList
	 * @param sourceList The source list (containing all elements)
	 * @param selectedItemsList The list which should be synchronized with the selected items
	 * @param list The target JList whose selection shall be bound
	 */
	public static XSelectionBinding bindMultiSelection(ObservableList sourceList, ObservableList selectedItemsList, JList list) {
		XMultiSelectionBinding bnd = new XMultiSelectionBinding(new BeansDataContext(sourceList), selectedItemsList, list.getSelectionModel());
		list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		bnd.bind();
		
		return bnd;
	}
	
	/**
	 * Two-way binds the selected (row) elements (multiple selection) of the given JTable
	 * to selectedItemsList
	 * @param sourceList The source list (containing all elements)
	 * @param selectedItemsList The list which should be synchronized with the selected items
	 * @param table The target JTable whose selection shall be bound
	 */
	public static XSelectionBinding bindMultiSelection(ObservableList sourceList, ObservableList selectedItemsList, JTable table) {
		XTableMultiSelectionBinding bnd = new XTableMultiSelectionBinding(new BeansDataContext(sourceList), selectedItemsList, table);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		bnd.bind();
		
		return bnd;
	}

	/**
	 * Two-way binds the selected (row) elements (multiple selection) of the given JTable
	 * to selectedItemsList
	 * @param sourceListContext The source list data context (containing all elements)
	 * @param selectedItemsList The list which should be synchronized with the selected items
	 * @param table The target JTable whose selection shall be bound
	 */
	public static XSelectionBinding bindMultiSelection(DataContext sourceListContext, ObservableList selectedItemsList, JTable table) {
		XTableMultiSelectionBinding bnd = new XTableMultiSelectionBinding(sourceListContext, selectedItemsList, table);
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		bnd.bind();

		return bnd;
	}

	@Override
	public boolean isBound() {
		return targetListSelectionListener != null;
	}

	// we cannot be sure that the displaying component (e.g. JList, JTable)
	// contains the objects from the source list (because of Converters etc.)
	// so unfortunately, we have to sync using indices
	
	/**
	 * Return the element of the source list for the given (view) index
	 */
	protected Object getSourceElementForIndex(int index) {
		if (index < 0 || index >= getSourceList().size())
			return null;

		return getSourceList().get(index);
	}

	/**
	 * Return the (view) index for the given element of the source list
	 */
	protected int getIndexForElement(Object element) {
		if (element == null)
			return -1;

		return getSourceList().indexOf(element);
	}
}

/**
 * Extension of XMultiSelectionBinding which supports sorted and filtered
 * JTables
 * 
 * @author berre
 *
 */
class XTableMultiSelectionBinding extends XMultiSelectionBinding {
	/**
	 * The JTable instance which shows the data
	 */
	JTable targetTable;
	
	protected XTableMultiSelectionBinding(DataContext sourceListContext, ObservableList selectedItemsList, JTable targetTable) {
		super(sourceListContext, selectedItemsList, targetTable.getSelectionModel());
		
		this.targetTable = targetTable;
	}
	
	@Override
	protected Object getSourceElementForIndex(int index) {
		if (index == -1)
			return null;

		index = targetTable.convertRowIndexToModel(index);

		if (index < 0 || index >= getSourceList().size())
			return null;

		return getSourceList().get(index);
	}

	@Override
	protected int getIndexForElement(Object element) {
		if (element == null)
			return -1;

		int idx = getSourceList().indexOf(element);
		if (idx == -1)
			return -1;

		return targetTable.convertRowIndexToModel(idx);
	}
}

/**
 * Base class for selection bindings which bind multiple selections (multiple
 * selected items of a selection model to an observable source list)
 * 
 * @author berre
 */
class XMultiSelectionBinding extends XSelectionBinding {

	ObservableList selectedItemsList;
	ObservableListListener selectedItemsListListener;

	// true, if currently an update from target -> source is carried out
	protected boolean isAdjustingSource = false;

	// true, if currently an update from source -> target is carried out
	protected boolean isAdjustingTarget = false;	

	protected XMultiSelectionBinding(DataContext sourceListContext, ObservableList selectedItemsList, ListSelectionModel targetSelectionModel) {
		super(sourceListContext, targetSelectionModel);
		
		if (selectedItemsList == null)
			throw new IllegalArgumentException("Parameter selectedItemsList must not be null");
		
		this.selectedItemsList = selectedItemsList;
	}

	@Override
	public void bind() {
		targetListSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!isAdjustingTarget && !e.getValueIsAdjusting()) {
					try {
						isAdjustingSource = true;
						// update source list
						
						selectedItemsList.clear();
						
						List newSelection = new ArrayList();
						for(int idx = targetSelectionModel.getMinSelectionIndex(); idx <= targetSelectionModel.getMaxSelectionIndex(); idx++)
							if (targetSelectionModel.isSelectedIndex(idx))
								newSelection.add(getSourceElementForIndex(idx));
							
						selectedItemsList.addAll(newSelection);

					} finally {
						isAdjustingSource = false;
					}
				}
			}
		};

		selectedItemsListListener = new ObservableListListener() {
			@Override
			public void listElementsAdded(ObservableList list, int index, int length) {				
				if (!isAdjustingSource) {
					try {
						isAdjustingTarget = true;
						
						for(int i = index; i < index+length; i++) {
							int selIndex = getIndexForElement(list.get(i));
							targetSelectionModel.addSelectionInterval(selIndex, selIndex);
						}							
					} finally {
						isAdjustingTarget = false;
					}
				}
				
			}

			@Override
			public void listElementsRemoved(ObservableList list, int index, List oldElements) {
				if (!isAdjustingSource) {
					try {
						isAdjustingTarget = true;
						
						for(Object element: oldElements) {
							int selIndex = getIndexForElement(element);
							targetSelectionModel.removeSelectionInterval(selIndex, selIndex);
						}							
					} finally {
						isAdjustingTarget = false;
					}
				}
			}

			@Override
			public void listElementReplaced(ObservableList list, int index, Object oldElement) {
				if (!isAdjustingSource) {
					try {
						isAdjustingTarget = true;
						
						// remove selection of the old element
						int selIndex = getIndexForElement(oldElement);
						targetSelectionModel.removeSelectionInterval(selIndex, selIndex);
						
						// add selection for the new element
						selIndex = getIndexForElement(list.get(index));
						targetSelectionModel.addSelectionInterval(selIndex, selIndex);
					} finally {
						isAdjustingTarget = false;
					}
				}
			}

			@Override
			public void listElementPropertyChanged(ObservableList list, int index) {
				// does not influence selection
			}
		};

		targetSelectionModel.addListSelectionListener(targetListSelectionListener);
		selectedItemsList.addObservableListListener(selectedItemsListListener);

		// set initial selection
		SwingUtilities.invokeLater( () -> selectedItemsListListener.listElementsAdded(selectedItemsList, 0, selectedItemsList.size()) );
	}

	@Override
	public void unbind() {
		if (isBound()) {
			selectedItemsList.removeObservableListListener(selectedItemsListListener);
			targetSelectionModel.removeListSelectionListener(targetListSelectionListener);

			selectedItemsListListener = null;
			targetListSelectionListener = null;
		}
	}

}

/**
 * Extension of XSingleSelectionBinding which supports sorted and filtered
 * JTables
 * 
 * @author berre
 *
 */
class XTableSingleSelectionBinding extends XSingleSelectionBinding {

	/**
	 * The JTable instance which shows the data
	 */
	JTable targetTable;

	public XTableSingleSelectionBinding(Object source, PropertyHelper sourceProperty, DataContext sourceListContext, JTable targetTable) {
		super(source, sourceProperty, sourceListContext, targetTable.getSelectionModel());

		this.targetTable = targetTable;
	}

	@Override
	protected Object getSourceElementForIndex(int index) {
		if (index == -1)
			return null;

		index = targetTable.convertRowIndexToModel(index);

		if (index < 0 || index >= getSourceList().size())
			return null;

		return getSourceList().get(index);
	}

	@Override
	protected int getIndexForElement(Object element) {
		if (element == null)
			return -1;

		int idx = getSourceList().indexOf(element);
		if (idx == -1)
			return -1;

		return targetTable.convertRowIndexToModel(idx);
	}
}

/**
 * Base class for selection bindings which bind single selections (one selected
 * item of a selection model to a source property)
 * 
 * @author berre
 */
class XSingleSelectionBinding extends XSelectionBinding {

	/**
	 * The object which contains the source property
	 */
	protected Object source;

	/**
	 * The (source) property to which to bind the selected item to
	 */
	protected PropertyHelper sourceProperty;

	/**
	 * The listener to listen for changes of the source property
	 */
	protected PropertyStateListener sourceListener;

	public XSingleSelectionBinding(Object source, PropertyHelper sourceProperty, DataContext sourceListContext, ListSelectionModel targetSelectionModel) {
		super(sourceListContext, targetSelectionModel);

		if (source == null)
			throw new IllegalArgumentException("Parameter source must not be null");
		if (sourceProperty == null)
			throw new IllegalArgumentException("Parameter sourceProperty must not be null");

		this.source = source;
		this.sourceProperty = sourceProperty;
	}

	@Override
	public void bind() {
		targetListSelectionListener = new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					// update source
					Object targetSelected = getSourceElementForIndex(getTargetSelectedIndex());

					if (sourceProperty.getValue(source) != targetSelected) {
						sourceProperty.setValue(source, targetSelected);
					}
				}
			}
		};

		sourceListener = new PropertyStateListener() {
			@Override
			public void propertyStateChanged(PropertyStateEvent pse) {
				// update target
				if (pse.getValueChanged()) {
					int sourceIndex = getIndexForElement(pse.getNewValue());
					if (getTargetSelectedIndex() != sourceIndex) {
						setTargetSelectedIndex(sourceIndex);
						// scroll to new selection
						// targetList.ensureIndexIsVisible(sourceIndex);
					}
				}
			}
		};

		targetSelectionModel.addListSelectionListener(targetListSelectionListener);
		sourceProperty.addPropertyStateListener(source, sourceListener);

		// get the current value from the source
		setTargetSelectedIndex(getIndexForElement(sourceProperty.getValue(source)));
	}

	private int getTargetSelectedIndex() {
		return targetSelectionModel.getMinSelectionIndex();
	}

	private void setTargetSelectedIndex(int selectedIndex) {
		if (selectedIndex == -1)
			targetSelectionModel.clearSelection();
		else
			targetSelectionModel.setSelectionInterval(selectedIndex, selectedIndex);
	}

	@Override
	public void unbind() {
		if (isBound()) {
			sourceProperty.removePropertyStateListener(source, sourceListener);
			targetSelectionModel.removeListSelectionListener(targetListSelectionListener);

			sourceListener = null;
			targetListSelectionListener = null;
		}
	}
}

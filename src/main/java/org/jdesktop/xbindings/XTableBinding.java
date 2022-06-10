package org.jdesktop.xbindings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.jdesktop.beansbinding.*;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;

import static org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ;
import static org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE;

import org.jdesktop.beansbinding.Binding.SyncFailure;
import org.jdesktop.beansbinding.util.Parameters;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;
import org.jdesktop.swingbinding.SwingBindings;
import org.jdesktop.swingbinding.impl.AbstractColumnBinding;
import org.jdesktop.swingbinding.impl.ListBindingManager;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;

/**
 * Supports binding of table rows and columns to ObservableLists
 * and performs better than the default SwingBindings table binding
 * (inserting many rows at once is much faster)
 * Basic support for value setting (in cells) has been implemented
 * @author berre
 */

public final class XTableBinding implements XBinding, ObservableListListener {

	ObservableList sourceList;
	PerfBoundTableModel tableModel;
	JTable targetTable;

	ObservableList columnSource;

	protected boolean isBound = false;

	/**
	 * Create a new table binding
	 * @param _sourceList The source list
	 * @param _targetTable The target JTable whose item source shall be bound
	 */
	private XTableBinding(ObservableList _sourceList, JTable _targetTable) {
		sourceList = _sourceList;
		targetTable = _targetTable;

		bind();
	}

	public void setSourceList(ObservableList newValue) {
		if (sourceList != null) {
			sourceList.removeObservableListListener(this);
		}
		sourceList = newValue;

		if (isBound()) {
			sourceList.addObservableListListener(this);
			tableModel.clear();
			tableModel.addAll(sourceList);
		}
	}

	public ObservableList getSourceList() {
		return sourceList;
	}

	public void bind()
	{
		if (!isBound()) {
			tableModel = new PerfBoundTableModel();
			if (sourceList != null) {
				sourceList.addObservableListListener(this);
				tableModel.addAll(sourceList);
			}
			targetTable.setModel(tableModel);

			isBound = true;
		}
	}

	public void unbind()
	{
		targetTable.getSelectionModel().clearSelection();

		if (sourceList != null) {
			sourceList.removeObservableListListener(this);
		}

		if (tableModel != null)
			tableModel.clear();

		isBound = false;
	}

	public boolean isBound() {
		return isBound;
	}
	
	/**
	 * Two-way binds the selected (row) element (single selection) of this TableBinding
	 * to the property of source
	 */
	public XSelectionBinding bindSingleSelection(Object source, String sourceProperty) {
		return XSelectionBinding.bindSingleSelection(source, sourceProperty, sourceList, targetTable);
	}

	/**
	 * Two-way binds the selected (row) elements (multiple selection) of this TableBinding
	 * to selectedItemsList
	 * @param selectedItemsList The list which should be synchronized with the selected items
	 */
	public XSelectionBinding bindMultiSelection(ObservableList selectedItemsList) {
		XTableMultiSelectionBinding bnd = new XTableMultiSelectionBinding(new BeansDataContext(sourceList), selectedItemsList, targetTable);
		targetTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		bnd.bind();
		
		return bnd;
	}	
	
	/**
	 * Create and establish a new table binding
	 * @param sourceList The source list
	 * @param targetTable The target JTable whose item source shall be bound
	 */
	public static <E> XTableBinding createJTableBinding(ObservableList<E> sourceList, JTable targetTable) {
		return new XTableBinding(sourceList, targetTable);
	}

	public static XTableBinding createJTableBinding(DataContext sourceContext, JTable targetTable) {
		XTableBinding xtb = new XTableBinding((ObservableList)sourceContext.getValue(), targetTable);

		AutoBinding listBinding = Bindings.createAutoBinding(READ, sourceContext.getSource(), sourceContext.getPropertyHelper(), xtb, BeanProperty.create("sourceList"));
		listBinding.bind();

		return xtb;
	}
	
	/**
	 * Create and establish a new table binding for the given view
	 * and enable column sorting on the table (if allowSort is true)
	 * @param sourceView The source view (which can be sorted by the column headers)
	 * @param targetTable The target JTable whose item source shall be bound
	 * @param enableSort If true a rowSorter is installed on the table which sorts the sourceView
	 */
	public static <E> XTableBinding createJTableBinding(XViewList<E> sourceView, JTable targetTable, boolean enableSort) {
		XTableBinding xbnd = new XTableBinding(sourceView, targetTable);
		
		if (enableSort) {
			targetTable.setRowSorter(new XViewListRowSorter<E>((PerfBoundTableModel<E>)targetTable.getModel(), targetTable.getSelectionModel(), sourceView));			
		}
		
		return xbnd;
	}	

	/**
	 * Bind the columns to the given list
	 * @param source The source list which contains objects to be converted to columns
	 * @param converter Converter to transform a source list object into an XColumnBinding
	 */
	public <T> void setColumnSource(final ObservableList<T> source, final Converter<T, XColumnBinding> converter) {
		columnSource = source;

		/**
		 * Add a listener to the column source to be able to update the JTable's columns
		 * when the source list changes
		 */
		columnSource.addObservableListListener(new ObservableListListener() {

			@Override
			public void listElementPropertyChanged(ObservableList arg0, int arg1) {
				updateColumns(source, converter);
			}

			@Override
			public void listElementReplaced(ObservableList arg0, int arg1,
					Object arg2) {
				updateColumns(source, converter);
			}

			@Override
			public void listElementsAdded(ObservableList arg0, int arg1,
					int arg2) {
				updateColumns(source, converter);
			}

			@Override
			public void listElementsRemoved(ObservableList arg0, int arg1,
					List arg2) {
				updateColumns(source, converter);
			}
		});

		// get the existing columns right-away (if any)
		updateColumns(source, converter);
	}
	
	private <T> void updateColumns(ObservableList<T> source, final Converter<T, XColumnBinding> converter) {
		List<XColumnBinding> newCols = new ArrayList<XColumnBinding>();

		// convert the object to an XColumnBinding using the user provided converter
		for(T cItem: source) {
			newCols.add(converter.convertForward(cItem));
		}

		tableModel.clearColumnBindings();
		tableModel.addColumnBindings(newCols);
	}

	/**
	 * Sets the columns
	 * (Fails when the columns are bound to a list (using setColumnSource))
	 */
	public void setColumns(Collection<XColumnBinding> columns) {
		if (columnSource != null)
			throw new IllegalStateException("Cannot set the columns of an XTableBinding if the columns are already bound to an ObservableList.");
			
		tableModel.clearColumnBindings();
		tableModel.addColumnBindings(columns);			
	}
	
	/**
	 * Adds the given column binding which displays the row elements as string
	 * (Does not work when the column source is set)
	 */
	public void addColumnBinding(String headerText) {
		if (columnSource != null)
			throw new IllegalStateException("Cannot set the columns of an XTableBinding if the columns are already bound to an ObservableList.");
		
		tableModel.addColumnBinding(new XColumnBinding(headerText));
	}	
	
	/**
	 * Adds the given column binding which returns the row elements of the given class
	 * (Does not work when the column source is set)
	 */
	public void addColumnBinding(String headerText, Class<?> cellClass) {
		if (columnSource != null)
			throw new IllegalStateException("Cannot set the columns of an XTableBinding if the columns are already bound to an ObservableList.");
		
		tableModel.addColumnBinding(new XColumnBinding(headerText, cellClass));
	}	

	/**
	 * Adds the given column binding with the given cell class
	 * (Does not work when the column source is set)
	 */
	public void addColumnBinding(String headerText, String propertyName, Class<?> cellClass) {
		if (columnSource != null)
			throw new IllegalStateException("Cannot add a column binding to an XTableBinding if the columns are already bound to an ObservableList.");
		
		tableModel.addColumnBinding(headerText, propertyName, cellClass);
	}
	
	/**
	 * Adds the given column binding with the given cell class
	 * (Does not work when the column source is set)
	 */
	public void addColumnBinding(XColumnBinding columnBinding) {
		if (columnSource != null)
			throw new IllegalStateException("Cannot set the columns of an XTableBinding if the columns are already bound to an ObservableList.");
		
		tableModel.addColumnBinding(columnBinding);
	}
	
	/**
	 * Adds a column binding with the cell class as String
	 * (Does not work when the column source is set)
	 */
	public void addColumnBinding(String headerText, String propertyName) {
		if (columnSource != null)
			throw new IllegalStateException("Cannot add a column binding to an XTableBinding if the columns are already bound to an ObservableList.");
		
		tableModel.addColumnBinding(headerText, propertyName, String.class);
	}

	// ************************************
	// List changed listener implementation
	// ************************************

	@Override
	public void listElementPropertyChanged(ObservableList lst, int idx) {
		// ObservableList has no element property changed support            
	}

	@Override
	public void listElementReplaced(ObservableList lst, int idx, Object element) {
		tableModel.set(idx, element);
	}

	@Override
	public void listElementsAdded(ObservableList lst, int startIdx, int count) {
		if (startIdx >= tableModel.size()) {
			tableModel.addAll(sourceList.subList(startIdx, startIdx + count));
		} else {
			tableModel.addAll(startIdx,
					sourceList.subList(startIdx, startIdx + count));
		}
	}

	@Override
	public void listElementsRemoved(ObservableList lst, int idx, List elements) {
		tableModel.removeRange(idx, idx + elements.size() - 1);
	}               
}

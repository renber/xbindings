package org.jdesktop.xbindings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.impl.AbstractColumnBinding;
import org.jdesktop.swingbinding.impl.ListBindingManager;

/**
 * TableModel used by XTableBinding, which has
 * a better performance than the default BetterBeansBinding JTableBinding
 * @author renber
 *
 */
class PerfBoundTableModel<E> implements TableModel, List<E>, PropertyChangeListener {

	private List<TableModelListener> listeners = new ArrayList<TableModelListener>();

	protected ArrayList<E> elements = new ArrayList<E>();

	List<XColumnBinding> columnBindings = new ArrayList<XColumnBinding>();

	public PerfBoundTableModel() {

	}
	
	// *************************
	// TableModel implementation
	// *************************

	@Override
	public Class<?> getColumnClass(int cellIdx) {
		return columnBindings.get(cellIdx).getCellClass();
	}

	@Override
	public int getColumnCount() {
		return columnBindings.size();
	}

	@Override
	public String getColumnName(int colIdx) {
		return columnBindings.get(colIdx).getHeaderText();
	}

	@Override
	public int getRowCount() {
		return elements.size();
	}

	@Override
	public Object getValueAt(int rowIdx, int colIdx) {
		XColumnBinding cb = columnBindings.get(colIdx);
		return cb.getValue(elements.get(rowIdx));
	}

	@Override
	public boolean isCellEditable(int rowIdx, int colIdx) {
		XColumnBinding cb = columnBindings.get(colIdx);
		return cb.isWritable(elements.get(rowIdx));
	}

	@Override
	public void setValueAt(Object newValue, int rowIdx, int colIdx) {
		XColumnBinding cb = columnBindings.get(colIdx);
		cb.setValue(elements.get(rowIdx), newValue);
	}

	// ***************
	// Column Bindings
	// ***************
	
	/**
	 * Add a column binding
	 * @param headerText Text to display in the column header
	 * @param propertyName Property whose content shall be displayed for cells in this column
	 */
	public void addColumnBinding(String headerText, String propertyName, Class<?> cellClass) {
		columnBindings.add(new XColumnBinding(headerText, BeanProperty
				.create(propertyName), cellClass));

		fireColumnsChanged();
	}

	/**
	 * Add column bindings from an existing collection
	 * @param columns The column bindings to add
	 */
	public void addColumnBinding(XColumnBinding columnBinding) {
		columnBindings.add(columnBinding);
		fireColumnsChanged();
	}
	
	/**
	 * Add column bindings from an existing collection
	 * @param columns The column bindings to add
	 */
	public void addColumnBindings(Collection<XColumnBinding> columns) {
		columnBindings.addAll(columns);
		fireColumnsChanged();
	}

	public List<XColumnBinding> getColumnBindings() {
		return columnBindings;
	}
	
	/**
	 * Remove the column bindings
	 */
	public void clearColumnBindings() {
		columnBindings.clear();
		fireColumnsChanged();
	}

	// *********
	// Listeners
	// *********

	@Override
	public void addTableModelListener(TableModelListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
	}

	@Override
	public void removeTableModelListener(TableModelListener listener) {
		listeners.remove(listener);
	}

	protected void fireColumnsChanged() {
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
	}

	protected void fireRowItemsAdded(int startIdx, int count) {
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this, startIdx,
					startIdx + count - 1, TableModelEvent.ALL_COLUMNS,
					TableModelEvent.INSERT));
	}

	protected void fireRowItemsRemoved(int startIdx, int count) {
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this, startIdx,
					startIdx + count - 1, TableModelEvent.ALL_COLUMNS,
					TableModelEvent.DELETE));
	}

	protected void fireRowItemChanged(int index) {
		for (TableModelListener listener : listeners)
			listener.tableChanged(new TableModelEvent(this, index,
					index, TableModelEvent.ALL_COLUMNS,
					TableModelEvent.UPDATE));
	}

	// ***************
	// Item management
	// ***************

	// *****************************
	// java.util.List implementation
	// *****************************

	@Override
	public int size() {
		return elements.size();
	}

	@Override
	public boolean add(E e) {
		int startIdx = elements.size();
		elements.add(e);
		addItemChangeListener(e);
		fireRowItemsAdded(startIdx, 1);
		return true;
	}

	@Override
	public void add(int index, E element) {
		elements.add(index, element);
		addItemChangeListener(element);
		fireRowItemsAdded(index, 1);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		int startIdx = elements.size();		
		elements.addAll(c);
		for(Object o: c) {
			addItemChangeListener(o);
		}
		fireRowItemsAdded(startIdx, c.size());
		return true;
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		elements.addAll(index, c);
		fireRowItemsAdded(index, c.size());
		for(Object o: c) {
			addItemChangeListener(o);
		}
		return true;
	}

	@Override
	public void clear() {
		int cnt = elements.size();
		for(Object o: elements) {
			removeItemChangeListener(o);
		}
		elements.clear();				
		
		// fireRowItemsRemoved(0, cnt); // throws an exception in DefaultListSelectionModel
	}

	@Override
	public boolean contains(Object o) {
		return elements.contains(o);
	}

	@Override
	public boolean containsAll(Collection c) {
		return elements.containsAll(c);
	}

	@Override
	public E get(int index) {
		return elements.get(index);
	}

	@Override
	public int indexOf(Object o) {
		return elements.indexOf(o);
	}

	@Override
	public boolean isEmpty() {
		return elements.isEmpty();
	}

	@Override
	public Iterator iterator() {
		return elements.iterator();
	}

	@Override
	public int lastIndexOf(Object o) {
		return elements.lastIndexOf(o);
	}

	@Override
	public ListIterator listIterator() {
		return elements.listIterator();
	}

	@Override
	public ListIterator listIterator(int index) {
		return elements.listIterator(index);
	}

	@Override
	public boolean remove(Object o) {
		int idx = elements.indexOf(o);
		if (idx > -1) {
			elements.remove(idx);
			return true;
		}
		return false;
	}

	@Override
	public E remove(int index) {
		E o = elements.get(index);
		removeItemChangeListener(o);
		elements.remove(index);
		fireRowItemsRemoved(index, 1);
		return o;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		// TODO: maybe solution with better performance
		for (Object o : c)
			remove(o);
		return true;
	}

	public void removeRange(int fromIdx, int toIdx) {
		List<E> lst = elements.subList(fromIdx, toIdx + 1);

		for(Object o: lst)
			removeItemChangeListener(o);

		lst.clear();

		fireRowItemsRemoved(fromIdx, toIdx - fromIdx + 1);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException(
				"retainAll(...) is not supported yet");
	}

	@Override
	public E set(int index, E element) {
		E o = elements.get(index);
		elements.set(index, element);
		fireRowItemChanged(index);
		return o;
	}

	@Override
	public List subList(int fromIndex, int toIndex) {
		return elements.subList(fromIndex, toIndex);
	}

	@Override
	public Object[] toArray() {
		return elements.toArray();
	}

	@Override
	public Object[] toArray(Object[] a) {
		return elements.toArray(a);
	}

	// ************************
	// Property change of items
	// ************************
	
	// register PropertyChangeHandlers for all table items and
	// invoke a row update when a value of an item is changed

	protected void addItemChangeListener(Object item) {
		if (item instanceof XNotifyPropertyChanged) {
			((XNotifyPropertyChanged) item).addPropertyChangeListener(this);
		}
	}

	protected void removeItemChangeListener(Object item) {
		if (item instanceof XNotifyPropertyChanged) {
			((XNotifyPropertyChanged) item).removePropertyChangeListener(this);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// refresh the item's row
		int elementIdx = elements.indexOf(evt.getSource());
		if (elementIdx > -1) {
			fireRowItemChanged(elementIdx);
		}
	}
}

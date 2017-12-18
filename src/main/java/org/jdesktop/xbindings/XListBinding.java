package org.jdesktop.xbindings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;

/**
 * Replacement for the JListBinding with increased performance for bulk inserts
 * @author berre
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class XListBinding implements XBinding, ObservableListListener {

	ObservableList sourceList;

	PerfBoundListModel listModel;
	JList targetList;
	
	private XListBinding(ObservableList _sourceList, JList _targetList) {
		sourceList = _sourceList;
		targetList = _targetList;

		bind();		
	}
	
	/**
	 * Two-way binds the selected element (single selection) of this ListBinding
	 * to the property of source
	 */
	public XSelectionBinding bindSingleSelection(Object source, String sourceProperty) {
		return XSelectionBinding.bindSingleSelection(source, sourceProperty, sourceList, targetList);
	}	
	
	/**
	 * Two-way binds the selected elements (multiple selection) of this list binding
	 * to selectedItemsList
	 * @param sourceList The source list (containing all elements)
	 * @param selectedItemsList The list which should be synchronized with the selected items
	 * @param list The target JList whose selection shall be bound
	 */
	public XSelectionBinding bindMultiSelection(ObservableList selectedItemsList) {
		return XSelectionBinding.bindMultiSelection(sourceList, selectedItemsList, targetList);
	}	

	/**
	 * Create a new list binding for the given JList
	 * @param _sourceList The list which holds the items
	 * @param _targetList The JList whose item source shall be bound
	 * @return
	 */
	public static XListBinding createJListBinding(ObservableList _sourceList,
			JList _targetList) {

		XListBinding xbnd = new XListBinding(_sourceList, _targetList);

		return xbnd;
	}
	
	// ************************************
	//XBinding implementation
	// ************************************	

	@Override
	public void bind() {
		sourceList.addObservableListListener(this);

		listModel = new PerfBoundListModel();
		listModel.addAll(sourceList);
		targetList.setModel(listModel);
	}

	@Override
	public void unbind() {
		sourceList.removeObservableListListener(this);		
		listModel = null;
	}

	@Override
	public boolean isBound() {
		return listModel != null && targetList.getModel() == listModel;
	}
	
	// ************************************
	// List changed listener implementation
	// ************************************

	@Override
	public void listElementPropertyChanged(ObservableList lst, int idx) {
		// currently not supported
	}

	@Override
	public void listElementReplaced(ObservableList lst, int idx, Object element) {
		// currently not supported

	}

	@Override
	public void listElementsAdded(ObservableList lst, int startIdx, int count) {
		if (startIdx >= listModel.size())
		{
			listModel.addAll(sourceList.subList(startIdx, startIdx + count));
		} else
		{
			listModel.addAll(startIdx, sourceList.subList(startIdx, startIdx + count - 1));
		}
	}

	@Override
	public void listElementsRemoved(ObservableList lst, int idx, List elements) {
		listModel.removeRange(idx, idx + elements.size() - 1);
	}

	/**
	 * The ListModel which is used by the bound JList
	 * @author berre
	 *
	 */
	private class PerfBoundListModel implements ListModel, java.util.List {

		protected List<Object> elements = new ArrayList<Object>();

		protected List<ListDataListener> listeners = new ArrayList<ListDataListener>();

		// ************************
		// ListModel implementation
		// ************************
		
		@Override
		public Object getElementAt(int index) {
			return elements.get(index);
		}

		@Override
		public int getSize() {
			return elements.size();
		}

		@Override
		public void addListDataListener(ListDataListener listener) {
			if (!listeners.contains(listener))
				listeners.add(listener);
		}

		@Override
		public void removeListDataListener(ListDataListener listener) {
			listeners.remove(listener);
		}

		protected void fireItemsAdded(int startIdx, int count) {
			for(ListDataListener listener: listeners)
				listener.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, startIdx, startIdx + count - 1));
		}

		protected void fireItemsRemoved(int startIdx, int count) {
			for(ListDataListener listener: listeners)
				listener.intervalAdded(new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, startIdx, startIdx + count - 1));
		}

		// *****************************
		// java.util.List implementation
		// *****************************

		@Override
		public boolean add(Object e) {
			int startIdx = elements.size();
			elements.add(e);
			fireItemsAdded(startIdx, 1);
			return true;
		}

		@Override
		public void add(int index, Object element) {
			elements.add(index, element);
			fireItemsAdded(index, 1);
		}

		@Override
		public boolean addAll(Collection c) {
			int startIdx = elements.size();
			elements.addAll(c);
			fireItemsAdded(startIdx, c.size());
			return true;
		}

		@Override
		public boolean addAll(int index, Collection c) {
			elements.addAll(index, c);
			fireItemsAdded(index, c.size());
			return true;
		}

		@Override
		public void clear() {
			int cnt = elements.size();
			elements.clear();
			fireItemsRemoved(0, cnt);
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
		public Object get(int index) {
			return getElementAt(index);
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
		public Object remove(int index) {
			Object o = elements.get(index);
			elements.remove(index);
			fireItemsRemoved(index, 1);
			return o;
		}

		@Override
		public boolean removeAll(Collection c) {
			// TODO: solution with better performance?
			for(Object o: c)
				remove(o);
			return true;
		}

		public void removeRange(int fromIdx, int toIdx) {
			elements.subList(fromIdx, toIdx + 1).clear();
			fireItemsRemoved(fromIdx, toIdx - fromIdx + 1);
		}

		@Override
		public boolean retainAll(Collection c) {
			// TODO: solution with better performance?
			
			// find the elements to remove (elements not in c)
			int diff = Math.max(0, this.size() - c.size());			
			List<Object> rem =  new ArrayList<Object>(diff);
			for(Object o: elements) {
				if (!c.contains(o))
					rem.add(o);
			}
			
			if (rem.size() > 0) {
				this.removeAll(rem);
				return true;	
			} else
				return false;
		}

		@Override
		public Object set(int index, Object element) {
			Object o = elements.get(index);
			elements.set(index, element);
			return o;
		}

		@Override
		public int size() {
			return getSize();
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

	}

}

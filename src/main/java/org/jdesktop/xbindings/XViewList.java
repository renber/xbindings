package org.jdesktop.xbindings;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.jdesktop.observablecollections.ObservableListListener;

/**
 * An ObservableList which is a read-only View onto another ObservableList and
 * supports filtering and sorting
 * 
 * @author berre
 */
public class XViewList<E> implements ObservableList<E> {

	// The source list which contains all elements
	private ObservableList<E> sourceList;

	// the current elements of this ViewList
	// and actual source of the list changed notifications of the View
	private ObservableList<E> viewList;

	// Listen to listen for changes in the sourceList
	private SourceListListener sourceListListener;

	// The currently active filters
	private List<Predicate<E>> filters = new ArrayList<>();
	
	// The comparator to use for sorting the list (if any)
	private Comparator<? super E> sortComparator;

	// indicates whether the view should refresh automatically when the source
	// list,
	// attached filters or sorting changes
	private boolean autoRefresh = true;

	/**
	 * Creates a new ViewList which is a view onto the given source list
	 * with autoRefresh enabled
	 * @param sourceList The source list for this view
	 */
	public XViewList(ObservableList<E> sourceList) {
		this(sourceList, true);
	}
	
	/**
	 * Creates a new ViewList which is a view onto the given source list
	 * @param sourceList The source list for this view
	 * @param autoRefresh When enabled, changes to source list will immediately be reflected by this view. If false, call refreshView() to udpate the view
	 */
	public XViewList(ObservableList<E> sourceList, boolean autoRefresh) {
		if (sourceList == null)
			throw new IllegalArgumentException("Parameter sourceList must not be null");
		
		viewList = new InnerObservableList<E>(new ArrayList<E>(), false, this);

		this.sourceList = sourceList;
		sourceListListener = new SourceListListener();

		this.autoRefresh = autoRefresh;
		if (autoRefresh) {
			sourceList.addObservableListListener(sourceListListener);
		}

		refreshView();
	}	
	
	/**
	 * Sets the auto refresh property of this View.
	 * When enabled, changes to source list will immediately be reflected by this view. If false, call refreshView() to udpate the view
	 * @param autoRefresh The new value for auto refresh
	 */
	public void setAutoRefresh(boolean autoRefresh) {
		this.autoRefresh = autoRefresh;
		
		// remove the listener always, to make sure that we do not register twice
		sourceList.removeObservableListListener(sourceListListener);
		
		if (autoRefresh)
			sourceList.addObservableListListener(sourceListListener);
	}
	
	/**
	 * Return the current value for auto refresh of this view
	 */
	public boolean getAutoRefresh() {
		return autoRefresh;
	}

	/**
	 * Return whether there are active filters
	 */
	public boolean isFiltered() {
		return filters.size() > 0;
	}

	/**
	 * Return a list with the currently active filters This list can be modified
	 * but changes will only take effect after calling refreshView()
	 */
	public List<Predicate<E>> getFilters() {
		return filters;
	}

	/**
	 * Adds the given filter (if it is not part of the filter list yet)
	 * and refreshes this list when autoRefresh is enabled
	 * 
	 * @param filter
	 *            The filter to add
	 */
	public void addFilter(Predicate<E> filter) {
		if (!filters.contains(filter))
			filters.add(filter);

		if (autoRefresh)
			refreshView();
	}
	
	/**
	 * Removes the given filter and refreshes this list when autoRefresh is enabled
	 * 
	 * @param filter
	 *            The filter to remove
	 */
	public void removeFilter(Predicate<E> filter) {
		filters.remove(filter);

		if (autoRefresh)
			refreshView();
	}

	/**
	 * Removes all filters and refreshes this list when autoRefresh is enabled
	 */
	public void clearFilters() {
		filters.clear();

		if (autoRefresh)
			refreshView();
	}

	/**
	 * Returns whether the given element matches the current filters
	 */
	private boolean matchesFilters(E element) {
		for (Predicate<E> filter : filters)
			if (!filter.test(element))
				return false;

		return true;
	}
	
	/**
	 * Return whether the view is currently sorted
	 */
	public boolean isSorted() {
		return sortComparator != null;
	}

	/**
	 * Sets the given comparator as sort criterion for this view (or disables sorting when null is passed)
	 * and refreshes this list when autoRefresh is enabled
	 * @param sortComparator The new comparator to use for sorting or null to revert the sort order
	 */
	@Override
	public void sort(Comparator<? super E> sortComparator) {
		this.sortComparator = sortComparator;
		
		if (autoRefresh)
			refreshView();
	}
	
	/**
	 * Refreshes the view
	 */
	public void refreshView() {
		// find the items of source list which match the current filters
		List<E> newItems = new ArrayList<E>(sourceList.size());

		if (isFiltered()) {
			for (E item : sourceList) {
				if (matchesFilters(item))
					newItems.add(item);
			}
		} else {
			newItems.addAll(sourceList);			
		}

		if (isSorted())
			Collections.sort(newItems, sortComparator);
		
		viewList.clear();
		viewList.addAll(newItems);
	}

	/**
	 * Return the index where the element of the sourceList at the given
	 * sourceIndex should be inserted in the view
	 */
	private int getViewInsertionIndex(int sourceIndex) {
		// Todo: consider sorted view
		
		if (isSorted()) {
			// since the view is sorted, we can use binary search to
			// find the insertion point
			E element = sourceList.get(sourceIndex);
			int targetIndex = Collections.binarySearch(viewList, element, sortComparator);
			if (targetIndex < 0) {
				// Collections.binarySearch returns the index as negative value, when the element does not exist in the list
				// subtract one, since the the element should be inserted before the given index
				return Math.abs(targetIndex) - 1;
			} else			
				return targetIndex;
		} else {		
			// if the view is not sorted
			// find the first item which is above the given sourceIndex
			// in the source list and also visible in the view
			if (isFiltered()) {
				sourceIndex--;
	
				while (sourceIndex >= 0) {
					E element = sourceList.get(sourceIndex);
					if (matchesFilters(element)) {
						return indexOf(element) + 1;
					}
	
					sourceIndex--;
				}
	
				// insert at top
				return 0;
			} else {
				return sourceIndex;
			}
		}
	}

	/**
	 * Add the element which is at position sourceIndex of the sourceList to the view
	 * @param sourceIndex The element's position in the source list
	 */
	private void addElementToView(int sourceIndex) {
		int index = getViewInsertionIndex(sourceIndex);
		viewList.add(index, sourceList.get(sourceIndex));
	}

	/**
	 * Listeners which listens for changes of the source list and udpates the view
	 * accordingly (if autoRefresh is enabled)
	 * @author renber
	 *
	 */
	private class SourceListListener implements ObservableListListener {

		@Override
		public void listElementsAdded(ObservableList list, int index, int length) {
			for (int sourceIdx = index; sourceIdx < index + length; sourceIdx++) {
				E newElement = (E) list.get(sourceIdx);
				if (matchesFilters(newElement))
					addElementToView(sourceIdx);
			}
		}

		@Override
		public void listElementsRemoved(ObservableList list, int index, List oldElements) {
			// remove the elements if they are currently in the view
			viewList.removeAll(oldElements);
		}

		@Override
		public void listElementReplaced(ObservableList list, int index, Object oldElement) {
			viewList.remove(oldElement);

			E newElement = (E) list.get(index);
			if (matchesFilters(newElement)) {
				addElementToView(index);
			}
		}

		@Override
		public void listElementPropertyChanged(ObservableList list, int index) {			
			E element = (E) list.get(index);
			
			if (viewList.contains(element)) {
				// check if the item still matches the filters (if it was in the view)
				// if not, remove it
				// Todo: if the list is sorted the item might need to change its position				
				if (!matchesFilters(element)) {
					viewList.remove(element);
				}	
			} else {
				// does the changed item need to be added to the view?
				if (matchesFilters(element))
					addElementToView(index);
			}			
		}
	}

	// -----------------------------
	// (read-only) List implementation
	// -----------------------------

	@Override
	public int size() {
		return viewList.size();
	}

	@Override
	public boolean isEmpty() {
		return viewList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return viewList.contains(o);
	}

	/**
	 * Returns a read-only iterator for this ViewList
	 */
	@Override
	public Iterator<E> iterator() {
		Iterator<E> delegate = viewList.iterator();
		return new Iterator<E>() {
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
			}

			@Override
			public boolean hasNext() {
				return delegate.hasNext();
			}

			@Override
			public E next() {
				return delegate.next();
			}
		};
	}

	@Override
	public Object[] toArray() {
		return viewList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return viewList.toArray(a);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return viewList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");		
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public E get(int index) {
		return viewList.get(index);
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
	}

	@Override
	public int indexOf(Object o) {
		return viewList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return viewList.lastIndexOf(o);
	}

	/**
	 * Returns a read-only iterator for this ViewList
	 */
	@Override
	public ListIterator<E> listIterator() {
		return listIterator(0);
	}

	/**
	 * Returns a read-only iterator for this ViewList beginning at the given
	 * index
	 */
	@Override
	public ListIterator<E> listIterator(int index) {
		ListIterator<E> delegate = viewList.listIterator(index);
		return new ListIterator<E>() {
			@Override
			public void remove() {
				throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
			}

			@Override
			public boolean hasNext() {
				return delegate.hasNext();
			}

			@Override
			public E next() {
				return delegate.next();
			}

			@Override
			public boolean hasPrevious() {
				return delegate.hasPrevious();
			}

			@Override
			public E previous() {
				return delegate.previous();
			}

			@Override
			public int nextIndex() {
				return delegate.nextIndex();
			}

			@Override
			public int previousIndex() {
				return delegate.previousIndex();
			}

			@Override
			public void set(E e) {
				throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
			}

			@Override
			public void add(E e) {
				throw new UnsupportedOperationException("Cannot modify a read-only XViewList");
			}
		};
	}

	/**
	 * Returns a read-only sub list of this view between the two given indices
	 * (inclusive)
	 */
	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return Collections.unmodifiableList(viewList.subList(fromIndex, toIndex));
	}

	// -----------------------------
	// ObservableList implementation
	// -----------------------------

	@Override
	public void addObservableListListener(ObservableListListener listener) {
		viewList.addObservableListListener(listener);
	}

	@Override
	public void removeObservableListListener(ObservableListListener listener) {
		viewList.removeObservableListListener(listener);
	}

	@Override
	public boolean supportsElementPropertyChanged() {
		return viewList.supportsElementPropertyChanged();
	}
	
	/**
	 * ObservableList implementation which raises the elements with a different
	 * source (to be used by ViewSource)
	 * 
	 * @author renber
	 */
	private static final class InnerObservableList<E> extends AbstractList<E>
			implements ObservableList<E> {
		private final boolean supportsElementPropertyChanged;
		private List<E> list;
		private List<ObservableListListener> listeners;

		private ObservableList<E> eventSource;

		/**
		 * 
		 * @param list
		 * @param supportsElementPropertyChanged
		 * @param eventSource
		 *            The source list to pass to listeners
		 */
		InnerObservableList(List<E> list, boolean supportsElementPropertyChanged, ObservableList<E> eventSource) {
			this.list = list;
			listeners = new CopyOnWriteArrayList<ObservableListListener>();
			this.supportsElementPropertyChanged = supportsElementPropertyChanged;
			this.eventSource = eventSource;
		}

		public E get(int index) {
			return list.get(index);
		}

		public int size() {
			return list.size();
		}

		public E set(int index, E element) {
			E oldValue = list.set(index, element);

			for (ObservableListListener listener : listeners) {
				listener.listElementReplaced(this, index, oldValue);
			}

			return oldValue;
		}

		public void add(int index, E element) {
			list.add(index, element);
			modCount++;

			for (ObservableListListener listener : listeners) {
				listener.listElementsAdded(this, index, 1);
			}
		}

		public E remove(int index) {
			E oldValue = list.remove(index);
			modCount++;

			for (ObservableListListener listener : listeners) {
				listener.listElementsRemoved(this, index,
						java.util.Collections.singletonList(oldValue));
			}

			return oldValue;
		}

		public boolean addAll(Collection<? extends E> c) {
			return addAll(size(), c);
		}

		public boolean addAll(int index, Collection<? extends E> c) {
			if (list.addAll(index, c)) {
				modCount++;

				for (ObservableListListener listener : listeners) {
					listener.listElementsAdded(eventSource, index, c.size());
				}
			}

			return false;
		}

		public void clear() {
			List<E> dup = new ArrayList<E>(list);
			list.clear();
			modCount++;

			if (dup.size() != 0) {
				for (ObservableListListener listener : listeners) {
					listener.listElementsRemoved(eventSource, 0, dup);
				}
			}
		}

		public boolean containsAll(Collection<?> c) {
			return list.containsAll(c);
		}

		public <T> T[] toArray(T[] a) {
			return list.toArray(a);
		}

		public Object[] toArray() {
			return list.toArray();
		}

		private void fireElementChanged(int index) {
			for (ObservableListListener listener : listeners) {
				listener.listElementPropertyChanged(eventSource, index);
			}
		}

		public void addObservableListListener(ObservableListListener listener) {
			listeners.add(listener);
		}

		public void removeObservableListListener(
				ObservableListListener listener) {
			listeners.remove(listener);
		}

		public boolean supportsElementPropertyChanged() {
			return supportsElementPropertyChanged;
		}
	}	
}
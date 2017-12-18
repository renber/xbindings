package org.jdesktop.xbindings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;

/**
 * An auto-generated row sorter which sorts the underlying XViewList of a table
 * thus making the sort available in the ViewModel
 * @author renber
 */
public class XViewListRowSorter<E> extends RowSorter<PerfBoundTableModel<E>> {

	// the table model of the Table (to retrieve the column bindings)
	private PerfBoundTableModel<E> tableModel;
	
	// the selection model (to restore the selection after sorting)
	private ListSelectionModel selectionModel;
	
	// The view list which is bound to the table this rowSorter sorts
	private XViewList<E> viewList;	
	
	// the current sort column (or -1 if none)
	private int sortColumn = -1;
	// the current sort order
	private SortOrder sortOrder = SortOrder.UNSORTED;	
	
	/**
	 * Create a new row sorter which sorts the given viewList
	 */
	public XViewListRowSorter(PerfBoundTableModel<E> tableModel, ListSelectionModel selectionModel, XViewList<E> viewList) {
		if (tableModel == null)
			throw new IllegalArgumentException("Parameter tableModel must not be null");
		if (selectionModel == null)
			throw new IllegalArgumentException("Parameter selectionModel must not be null");
		if (viewList == null)
			throw new IllegalArgumentException("Parameter viewList must not be null");
		
		this.tableModel = tableModel;
		this.selectionModel = selectionModel;
		this.viewList = viewList;		
	}
	
	@Override
	public PerfBoundTableModel<E> getModel() {
		return tableModel;
	}

	@Override
	public void toggleSortOrder(int column) {		
		XColumnBinding columnBinding = getModel().getColumnBindings().get(column);
		
		// cache selection		
		ArrayList<E> selectedItems = new ArrayList<E>();
		for(int i = selectionModel.getMinSelectionIndex(); i <= selectionModel.getMaxSelectionIndex(); i++) {
			if (selectionModel.isSelectedIndex(i))
				selectedItems.add(viewList.get(i));
		}
		
		// reverse sort order when user re-clicked on the same column
		if (column == sortColumn)
			sortOrder = sortOrder == SortOrder.ASCENDING ? SortOrder.DESCENDING : SortOrder.ASCENDING;
		else
			// default sort order is ascending
			sortOrder = SortOrder.ASCENDING;
		
		viewList.sort(new DefaultBoundColumnComparator<E>(columnBinding, sortOrder == SortOrder.DESCENDING));		
		
		sortColumn = column;
		
		// restore selection
		selectionModel.clearSelection();
		for (E element: selectedItems) {
			int index = viewList.indexOf(element);
			selectionModel.addSelectionInterval(index, index);
		}		
	}

	@Override
	public int convertRowIndexToModel(int index) {
		// there is no difference between model and view
		return index;
	}

	@Override
	public int convertRowIndexToView(int index) {
		// there is no difference between model and view		
		return index;
	}

	@Override
	public void setSortKeys(List<? extends javax.swing.RowSorter.SortKey> keys) {
		if (keys == null) {
			viewList.sort(null);
			sortColumn = -1;
			sortOrder = SortOrder.UNSORTED; 
		}
	}

	@Override
	public List<? extends RowSorter.SortKey> getSortKeys() {
		if (sortColumn == -1) {
			return new ArrayList<SortKey>(1);
		} else
		{
			return Arrays.asList(new SortKey[] {new SortKey(sortColumn, sortOrder)});
		}
	}

	@Override
	public int getViewRowCount() {
		return viewList.size();
	}

	@Override
	public int getModelRowCount() {
		return viewList.size();
	}

	@Override
	public void modelStructureChanged() {
		// --
	}

	@Override
	public void allRowsChanged() {
		// --
	}

	@Override
	public void rowsInserted(int firstRow, int endRow) {
		// --
	}

	@Override
	public void rowsDeleted(int firstRow, int endRow) {
		// --		
	}

	@Override
	public void rowsUpdated(int firstRow, int endRow) {
		// --
	}

	@Override
	public void rowsUpdated(int firstRow, int endRow, int column) {
		// --
	}

	static class DefaultBoundColumnComparator<T> implements Comparator<T> {
		
		// the column binding to sort by
		XColumnBinding columnBinding;
		
		boolean sortDescending;
		
		public DefaultBoundColumnComparator(XColumnBinding columnBinding, boolean sortDescending) {
			this.columnBinding = columnBinding;
			this.sortDescending = sortDescending;
		}
		
		@Override
		public int compare(T o1, T o2) {	
			if (columnBinding == null)
				return 0;
			
			if (o1 == o2)
				return 0;
			
			if (o1 == null || o2 == null)
				return o1 == null ? -1 : 1;
			
			Object v1 = columnBinding.getValue(o1);
			Object v2 = columnBinding.getValue(o2);
			
			if (v1 == null || v2 == null)
				return v1 == null ? -1 : 1;
			
			int result;
			
			if (v1 instanceof Comparable) {
				result = ((Comparable)v1).compareTo(v2);
			} else {
				result = v1.toString().compareTo(v2.toString());
			}		
			
			return sortDescending ? -result : result;
		}
	}
}

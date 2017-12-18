package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import java.awt.List;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JList;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.table.TableRowSorter;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableCollections.ObservableListHelper;
import org.jdesktop.observablecollections.ObservableList;
import org.junit.Test;

public class XSelectionBindingTest {

	@Test
	public void testJListSingleSelectionBinding() {							
		TestViewModel vm = new TestViewModel();		
		JList<String> targetList = new JList<String>();		
		XListBinding.createJListBinding(vm.getItems(), targetList);
						
		XSelectionBinding.bindSingleSelection(vm, "selectedItem", vm.getItems(), targetList);		
		
		assertEquals(-1, targetList.getSelectedIndex());
		
		// test source -> target
		vm.setSelectedItem("Item2");
		assertEquals(1, targetList.getSelectedIndex());
		
		// remove selection at source
		vm.setSelectedItem(null);
		assertEquals(-1, targetList.getSelectedIndex());
		
		// test target -> source
		targetList.setSelectedIndex(2);
		assertEquals("Item3", vm.getSelectedItem());
		
		// remove selection at target
		targetList.clearSelection();
		assertNull(vm.getSelectedItem());
	}
	
	@Test
	public void testJListSingleIntervalSelectionBinding() {
		TestViewModel vm = new TestViewModel();		
		JList<String> targetList = new JList<String>();		
		XListBinding.createJListBinding(vm.getItems(), targetList);
						
		XSelectionBinding.bindMultiSelection(vm.getItems(), vm.getSelectedItems(), targetList);				
		assertEquals(0, targetList.getSelectedIndices().length);
		targetList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		
		// test source -> target (select two items)		
		vm.getSelectedItems().add("Item2");
		vm.getSelectedItems().add("Item3");		
		assertEquals(2, targetList.getSelectedIndices().length);
		
		// remove selection at source
		vm.getSelectedItems().clear();
		assertEquals(0, targetList.getSelectedIndices().length);
		
		// test target -> source
		targetList.setSelectedIndices(new int[] {0, 1});
		assertEquals(2, vm.getSelectedItems().size());
		assertTrue(vm.getSelectedItems().contains("Item1"));
		assertTrue(vm.getSelectedItems().contains("Item2"));
		
		// remove selection at target
		targetList.clearSelection();
		assertEquals(0, vm.getSelectedItems().size());
	}
	
	@Test
	public void testJListMultipleIntervalSelectionBinding() {
		TestViewModel vm = new TestViewModel();
		vm.getItems().add("Item4");
		vm.getItems().add("Item5");
		vm.getItems().add("Item6");
		vm.getItems().add("Item7");
		
		JList<String> targetList = new JList<String>();		
		XListBinding.createJListBinding(vm.getItems(), targetList);
										
		XSelectionBinding.bindMultiSelection(vm.getItems(), vm.getSelectedItems(), targetList);				
		assertEquals(0, targetList.getSelectedIndices().length);
		
		// test source -> target (select three items with gaps)		
		vm.getSelectedItems().add("Item2");
		vm.getSelectedItems().add("Item4");
		vm.getSelectedItems().add("Item7");
		assertEquals(3, targetList.getSelectedIndices().length);
		
		// remove selection at source
		vm.getSelectedItems().clear();
		assertEquals(0, targetList.getSelectedIndices().length);
		
		// test target -> source
		targetList.setSelectedIndices(new int[] {1, 4, 6});
		assertEquals(3, vm.getSelectedItems().size());
		assertTrue(vm.getSelectedItems().contains("Item2"));
		assertTrue(vm.getSelectedItems().contains("Item5"));
		assertTrue(vm.getSelectedItems().contains("Item7"));
		
		// remove selection at target
		targetList.clearSelection();
		assertEquals(0, vm.getSelectedItems().size());
	}	
	
	@Test
	public void testJListMultipleSelectionReplaceItemBinding() {
		TestViewModel vm = new TestViewModel();
		vm.getItems().add("Item4");
		vm.getItems().add("Item5");
		vm.getItems().add("Item6");
		vm.getItems().add("Item7");
		
		JList<String> targetList = new JList<String>();		
		XListBinding.createJListBinding(vm.getItems(), targetList);
										
		XSelectionBinding.bindMultiSelection(vm.getItems(), vm.getSelectedItems(), targetList);				
		assertEquals(0, targetList.getSelectedIndices().length);
		
		// test if using set (replacing an element) on the source list correctly adjusts the selection		
		vm.getSelectedItems().add("Item2");
		vm.getSelectedItems().add("Item3");
		assertEquals(2, targetList.getSelectedIndices().length);
		assertTrue(targetList.getSelectedValuesList().contains("Item2"));
		assertTrue(targetList.getSelectedValuesList().contains("Item3"));
		
		// replace the selection of Item3 with Item5
		vm.getSelectedItems().set(1, "Item5");
		assertEquals(2, targetList.getSelectedIndices().length);		
		assertTrue(targetList.getSelectedValuesList().contains("Item2"));
		assertTrue(targetList.getSelectedValuesList().contains("Item5"));
		
	}		
	
	@Test
	public void testJTableSingleSelectionBinding() {
		TestViewModel vm = new TestViewModel();		
		JTable targetTable = new JTable();		
		XTableBinding tableBinding = XTableBinding.createJTableBinding(vm.getItems(), targetTable);
		tableBinding.addColumnBinding("Column1");
		
		XSelectionBinding.bindSingleSelection(vm, "selectedItem", vm.getItems(), targetTable);
		
		assertEquals(-1, targetTable.getSelectedRow());
		
		// test source -> target
		vm.setSelectedItem("Item2");
		assertEquals(1, targetTable.getSelectedRow());
		
		// remove selection at source
		vm.setSelectedItem(null);
		assertEquals(-1, targetTable.getSelectedRow());
				
		// test target -> source
		targetTable.getSelectionModel().setSelectionInterval(2, 2);
		assertEquals("Item3", vm.getSelectedItem());
				
		// remove selection at target
		targetTable.clearSelection();
		assertNull(vm.getSelectedItem());
	}
	
	@Test
	public void testSortedJTableSingleSelectionBinding() {
		TestViewModel vm = new TestViewModel();		
		JTable targetTable = new JTable();		
		XTableBinding tableBinding = XTableBinding.createJTableBinding(vm.getItems(), targetTable);
		tableBinding.addColumnBinding("Column1");
		
		XSelectionBinding.bindSingleSelection(vm, "selectedItem", vm.getItems(), targetTable);
		
		TableRowSorter<PerfBoundTableModel> sorter = new TableRowSorter<PerfBoundTableModel>();
		targetTable.setRowSorter(sorter);
		sorter.setModel((PerfBoundTableModel)targetTable.getModel());
		// sort in reverse (so the displayed order is Item3,Item2,Item1)
		sorter.toggleSortOrder(0);
		sorter.toggleSortOrder(0);
		
		// test source -> target
		vm.setSelectedItem("Item3");
		assertEquals("Item3", targetTable.getModel().getValueAt(targetTable.convertRowIndexToModel(0), 0));
		assertEquals(0, targetTable.getSelectedRow());
		
		// remove selection at source
		vm.setSelectedItem(null);
		assertEquals(-1, targetTable.getSelectedRow());
				
		// test target -> source
		targetTable.getSelectionModel().setSelectionInterval(2, 2);
		assertEquals("Item1", vm.getSelectedItem());
				
		// remove selection at target
		targetTable.clearSelection();
		assertNull(vm.getSelectedItem());
	}	
	
	@Test
	public void testJTableMultipleIntervalSelectionBinding() {
		TestViewModel vm = new TestViewModel();
		vm.getItems().add("Item4");
		vm.getItems().add("Item5");
		vm.getItems().add("Item6");
		vm.getItems().add("Item7");
		
		JTable targetTable = new JTable();		
		XTableBinding tableBinding = XTableBinding.createJTableBinding(vm.getItems(), targetTable);
		tableBinding.addColumnBinding("Column1");
										
		XSelectionBinding.bindMultiSelection(vm.getItems(), vm.getSelectedItems(), targetTable);				
		assertEquals(0, targetTable.getSelectedRowCount());
		
		// test source -> target (select three items with gaps)		
		vm.getSelectedItems().add("Item2");
		vm.getSelectedItems().add("Item4");
		vm.getSelectedItems().add("Item7");
		assertEquals(3, targetTable.getSelectedRowCount());
		
		// remove selection at source
		vm.getSelectedItems().clear();
		assertEquals(0, targetTable.getSelectedRowCount());
		
		// test target -> source
		targetTable.getSelectionModel().addSelectionInterval(1,1);
		targetTable.getSelectionModel().addSelectionInterval(4,4);
		targetTable.getSelectionModel().addSelectionInterval(6,6);
		assertEquals(3, vm.getSelectedItems().size());
		assertTrue(vm.getSelectedItems().contains("Item2"));
		assertTrue(vm.getSelectedItems().contains("Item5"));
		assertTrue(vm.getSelectedItems().contains("Item7"));
		
		// remove selection at target
		targetTable.clearSelection();
		assertEquals(0, vm.getSelectedItems().size());
	}	
	
	@Test
	public void testSortedJTableMultipleIntervalSelectionBinding() {
		TestViewModel vm = new TestViewModel();		
		vm.getItems().add("Item4");
		vm.getItems().add("Item5");
		vm.getItems().add("Item6");
		vm.getItems().add("Item7");
		
		JTable targetTable = new JTable();		
		XTableBinding tableBinding = XTableBinding.createJTableBinding(vm.getItems(), targetTable);
		tableBinding.addColumnBinding("Column1");
		
		XSelectionBinding.bindMultiSelection(vm.getItems(), vm.getSelectedItems(), targetTable);
		
		TableRowSorter<PerfBoundTableModel> sorter = new TableRowSorter<PerfBoundTableModel>();
		targetTable.setRowSorter(sorter);
		sorter.setModel((PerfBoundTableModel)targetTable.getModel());
		// sort in reverse (so the displayed order is Item7,Item6,Item5,Item4,Item3,Item2,Item1)
		sorter.toggleSortOrder(0);
		sorter.toggleSortOrder(0);
		
		// test source -> target
		vm.getSelectedItems().add("Item3");
		vm.getSelectedItems().add("Item5");
		vm.getSelectedItems().add("Item7");
		assertEquals("Item7", targetTable.getModel().getValueAt(targetTable.convertRowIndexToModel(0), 0));
		assertEquals(3, targetTable.getSelectedRowCount());
		
		// remove selection at source
		vm.getSelectedItems().clear();
		assertEquals(0, targetTable.getSelectedRowCount());
				
		// test target -> source
		targetTable.getSelectionModel().addSelectionInterval(2, 3);
		targetTable.getSelectionModel().addSelectionInterval(5, 5);
		assertTrue(vm.getSelectedItems().contains("Item4"));
		assertTrue(vm.getSelectedItems().contains("Item5"));
		assertTrue(vm.getSelectedItems().contains("Item2"));
				
		// remove selection at target
		targetTable.clearSelection();
		assertEquals(0, vm.getSelectedItems().size());
	}	
	
	/**
	 * ViewModel used for the tests in this test class
	 * @author berre
	 *
	 */
	public static class TestViewModel implements XNotifyPropertyChanged {

		PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
		
		ObservableList<String> items = ObservableCollections.observableList(new ArrayList<String>());
		
		ObservableList<String> selectedItems = ObservableCollections.observableList(new ArrayList<String>());
		
		public ObservableList<String> getItems() {
			return items;
		}
		
		/**
		 * Returns the first selected item
		 * @return
		 */
		public String getSelectedItem() {
			if (selectedItems.size() == 0)
				return null;
			
			return selectedItems.get(0);
		}
		
		/**
		 * Returns a mutable list containing all selected items
		 */
		public ObservableList<String> getSelectedItems() {
			return selectedItems;
		}
		
		/**
		 * Sets the given item as teh only selected item
		 */
		public void setSelectedItem(String selection) {
			String oldValue = getSelectedItem();
			
			if (selection != null)
				selectedItems.add(selection);
			else
				selectedItems.clear();
			
			changeSupport.firePropertyChange("selectedItem", oldValue, getSelectedItem());
		}
		
		public TestViewModel() {
			items.add("Item1");
			items.add("Item2");
			items.add("Item3");
		}
		
		@Override
		public void addPropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.addPropertyChangeListener(listener);
		}

		@Override
		public void removePropertyChangeListener(PropertyChangeListener listener) {
			changeSupport.removePropertyChangeListener(listener);			
		}
		
	}
}

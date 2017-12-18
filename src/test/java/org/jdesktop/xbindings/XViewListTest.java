package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class XViewListTest {

	@Test
	public void test_syncAutoRefresh() {
		ObservableList<String> sourceList = ObservableCollections.observableList(new ArrayList<String>());
		sourceList.add("Item1");
		
		XViewList<String> view = new XViewList<>(sourceList);
		
		assertEquals(1, view.size());
		assertEquals("Item1", view.get(0));
		
		// test add
		sourceList.add("Item2");
		sourceList.add("Item3");
		sourceList.add("Item4");
		
		assertEquals(4, view.size());
		assertEquals("Item3", view.get(2));

		// test remove
		sourceList.remove("Item2");
		assertEquals(3, view.size());
		assertFalse(view.contains("Item2"));
		
		// test replace
		sourceList.set(2, "Item5");
		assertEquals(3, view.size());
		assertEquals("Item5", view.get(2));
	}
	
	@Test
	public void test_filterAutoRefresh() {
		ObservableList<String> sourceList = ObservableCollections.observableList(new ArrayList<String>());
		sourceList.add("Item1");
		sourceList.add("LongItem2");
		sourceList.add("Item3");
		
		XViewList<String> view = new XViewList<>(sourceList);
		
		assertEquals(3, view.size());
		
		// test filter
		view.addFilter((s) -> s.length() <= 5);
		assertSequenceEquals(view, "Item1", "Item3");		
		
		// test add with non-matching item
		sourceList.add("AnotherLongItem");
		assertSequenceEquals(view, "Item1", "Item3");
		
		// test add with matching item
		sourceList.add("Item4");
		assertSequenceEquals(view, "Item1", "Item3", "Item4");
		
		// test insert below non-matching item
		sourceList.add(2, "Item5");
		assertSequenceEquals(view, "Item1", "Item5", "Item3", "Item4");		
		
		// remove filters
		view.clearFilters();
		assertSequenceEquals(view, "Item1", "LongItem2", "Item5", "Item3", "AnotherLongItem", "Item4");
	}
	
	@Test
	public void test_sortAutoRefresh() {
		ObservableList<Integer> sourceList = ObservableCollections.observableList(new ArrayList<Integer>());
		sourceList.add(2);
		sourceList.add(4);
		sourceList.add(1);
		sourceList.add(5);
		
		XViewList<Integer> view = new XViewList<>(sourceList);
		assertSequenceEquals(view, 2, 4, 1, 5);
		
		// test sort on existing elements
		view.sort(new Comparator<Integer>() {
			@Override
			public int compare(Integer o1, Integer o2) {
				// sort by length, ascending
				return o1.compareTo(o2);
			}});
		assertSequenceEquals(view, 1, 2, 4, 5);
		
		// test add element
		sourceList.add(3);
		assertSequenceEquals(view, 1, 2, 3, 4, 5);
		
		// test add duplicate elements
		sourceList.add(2);
		sourceList.add(1);
		assertSequenceEquals(view, 1, 1, 2, 2, 3, 4, 5);
		
		// test add at
		sourceList.add(0, 8);
		assertSequenceEquals(view, 1, 1, 2, 2, 3, 4, 5, 8);
		
		// disable sort (order of the source list should be restored)
		view.sort(null);
		assertSequenceEquals(view, 8, 2, 4, 1, 5, 3, 2, 1);
	}
	
	// used to test for exceptions
	@Rule public ExpectedException thrown= ExpectedException.none();
	
	@Test
	public void test_readOnly_add() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>()));		
		thrown.expect( UnsupportedOperationException.class );
		view.add("Test");		
	}
	
	@Test
	public void test_readOnly_addAt() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2"}))));		
		thrown.expect( UnsupportedOperationException.class );
		view.add(1, "Item3");
	}
	
	@Test
	public void test_readOnly_addAll() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>()));		
		thrown.expect( UnsupportedOperationException.class );
		view.addAll(Arrays.asList(new String[] {"Item1", "Item2"}));		
	}
	
	@Test
	public void test_readOnly_addAllAt() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3"}))));		
		thrown.expect( UnsupportedOperationException.class );
		view.addAll(1, Arrays.asList(new String[] {"Item4", "Item5"}));		
	}	
	
	@Test
	public void test_readOnly_set() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2"}))));		
		thrown.expect( UnsupportedOperationException.class );
		view.set(1, "Item3");
	}	
	
	@Test
	public void test_readOnly_remove() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2"}))));		
		thrown.expect( UnsupportedOperationException.class );
		view.remove("Item2");
	}	
	
	@Test
	public void test_readOnly_removeAt() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2"}))));		
		thrown.expect( UnsupportedOperationException.class );
		view.remove(1);
	}		
	
	@Test
	public void test_readOnly_removeAll() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2"}))));		
		thrown.expect( UnsupportedOperationException.class );
		view.removeAll(Arrays.asList(new String[] {"Item1", "Item2"}));
	}	
	
	@Test
	public void test_readOnly_clearAll() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2"}))));		
		thrown.expect( UnsupportedOperationException.class );
		view.clear();
	}		
	
	@Test
	public void test_readOnly_retainAll() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3", "Item4"}))));		
		thrown.expect( UnsupportedOperationException.class );
		view.retainAll(Arrays.asList(new String[] {"Item2", "Item4"}));
	}
	
	@Test
	public void test_readOnly_iterator() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3", "Item4"}))));		
		
		Iterator<String> it = view.iterator();
		it.next();
		it.next();		
		thrown.expect( UnsupportedOperationException.class );
		it.remove();
	}	
	
	@Test
	public void test_readOnly_listIterator_remove() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3", "Item4"}))));		
		
		ListIterator<String> it = view.listIterator();
		it.next();
		it.next();				
		thrown.expect( UnsupportedOperationException.class );
		it.remove();
	}		
	
	@Test
	public void test_readOnly_listIteratorAt_remove() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3", "Item4"}))));		
		
		ListIterator<String> it = view.listIterator(2);
		it.next();			
		thrown.expect( UnsupportedOperationException.class );
		it.remove();
	}		
	
	@Test
	public void test_readOnly_listIterator_set() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3", "Item4"}))));		
		
		ListIterator<String> it = view.listIterator();
		it.next();
		it.next();				
		thrown.expect( UnsupportedOperationException.class );
		it.set("NewItem");
	}		
	
	@Test
	public void test_readOnly_listIteratorAt_set() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3", "Item4"}))));		
		
		ListIterator<String> it = view.listIterator(2);
		it.next();		
		thrown.expect( UnsupportedOperationException.class );
		it.set("NewItem");
	}		
	
	@Test
	public void test_readOnly_listIterator_add() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3", "Item4"}))));		
		
		ListIterator<String> it = view.listIterator();
		it.next();
		it.next();				
		thrown.expect( UnsupportedOperationException.class );
		it.add("NewItem");
	}		
	
	@Test
	public void test_readOnly_listIteratorAt_add() {		
		XViewList<String> view = new XViewList<>(ObservableCollections.observableList(new ArrayList<String>(Arrays.asList(new String[] {"Item1", "Item2", "Item3", "Item4"}))));		
		
		ListIterator<String> it = view.listIterator(2);
		it.next();		
		thrown.expect( UnsupportedOperationException.class );
		it.add("NewItem");
	}	
	
	/**
	 * Helper method which allows to assert that a list contains a given number of elements in the correct order
	 * @param list the list to test
	 * @param expectedItems The expected list items
	 */
	private void assertSequenceEquals(List list, Object...expectedItems) {		
		assertArrayEquals(expectedItems, list.toArray());
	}
}

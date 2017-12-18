package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;

import javax.swing.JList;
import javax.swing.JTable;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.junit.Test;

public class XTableBindingTest {

	@Test
	public void testJTableBinding() {
		ObservableList<TestViewModel> items = ObservableCollections.observableList(new ArrayList<TestViewModel>());
		items.add(new TestViewModel("Item1", 1));
		items.add(new TestViewModel("Item2", 2));
		items.add(new TestViewModel("Item3", 3));
		items.add(new TestViewModel("Item4", 4));
		
		JTable table = new JTable();
		XTableBinding bnd = XTableBinding.createJTableBinding(items, table);
		bnd.addColumnBinding("Column1", "strValue");
		bnd.addColumnBinding("Column2", "intValue");		
		
		assertEquals(2, table.getColumnCount());
		assertEquals(4, table.getRowCount());
		
		assertEquals(new Integer(2), table.getValueAt(1, 1));		
		assertEquals("Item3", table.getValueAt(2, 0));		
	}
	
	@Test
	public void testJTableBindingWithConverter() {
		ObservableList<TestViewModel> items = ObservableCollections.observableList(new ArrayList<TestViewModel>());
		items.add(new TestViewModel("Item1", 1));
		items.add(new TestViewModel("Item2", 2));
		items.add(new TestViewModel("Item3", 3));
		items.add(new TestViewModel("Item4", 4));
		
		JTable table = new JTable();
		XTableBinding bnd = XTableBinding.createJTableBinding(items, table);
		bnd.addColumnBinding("Column1", "strValue");
		bnd.addColumnBinding(new XColumnBinding("Column2", BeanProperty.create("intValue"), String.class, new Converter<Integer, String>() {
			@Override
			public String convertForward(Integer value) {
				return "The number is " + value.toString();
			}

			@Override
			public Integer convertReverse(String value) {
				return null;
			}}));		
		
		assertEquals(2, table.getColumnCount());
		assertEquals(4, table.getRowCount());
		
		assertEquals("The number is 2", table.getValueAt(1, 1));				
	}

	/**
	 * ViewModel used for the tests in this test class
	 * @author berre
	 *
	 */
	public static class TestViewModel implements XNotifyPropertyChanged {

		PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
		
		String strValue;
		int intValue;
		
		public int getIntValue() {
			return intValue;
		}
		
		public void setIntValue(int newValue) {
			int oldValue = intValue;
			intValue = newValue;
			changeSupport.firePropertyChange("intValue", oldValue, newValue);
		}
		
		public String getStrValue() {
			return strValue;
		}
		
		public void setStrValue(String newValue) {
			String oldValue = strValue;
			strValue = newValue;
			changeSupport.firePropertyChange("strValue", oldValue, newValue);
		}		
		
		public TestViewModel(String strValue, int intValue) {
			this.strValue = strValue;
			this.intValue = intValue;
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

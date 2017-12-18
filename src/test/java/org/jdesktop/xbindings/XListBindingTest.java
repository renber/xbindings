package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import java.util.ArrayList;

import javax.swing.JList;

import org.jdesktop.observablecollections.ObservableCollections;
import org.jdesktop.observablecollections.ObservableList;
import org.junit.Test;

public class XListBindingTest {

	@Test
	public void testJListBinding() {
		ObservableList<String> items = ObservableCollections.observableList(new ArrayList<String>());
		items.add("Item1");
		items.add("Item2");
		items.add("Item3");			
		
		JList<String> list = new JList<String>();		
		XListBinding bnd = XListBinding.createJListBinding(items, list);

		assertTrue(bnd.isBound());
		assertEquals(3, list.getModel().getSize());
		
		assertEquals("Item2", list.getModel().getElementAt(1));
		
		items.add("Item4");
		items.add("Item5");
		assertEquals(5, list.getModel().getSize());
		
		items.remove("Item3");
		assertEquals(4, list.getModel().getSize());
		assertEquals("Item4", list.getModel().getElementAt(2));
				
		bnd.unbind();
		assertFalse(bnd.isBound());
		
		assertEquals(4, list.getModel().getSize());
		items.add("Item6");
		assertEquals(4, list.getModel().getSize());			
	}

}

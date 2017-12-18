package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import javax.swing.JButton;
import javax.swing.text.ChangedCharSetException;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.xbindings.XPropertyTest.TestViewModel;
import org.jdesktop.xbindings.properties.AutoProperty;
import org.jdesktop.xbindings.properties.XProperty;
import org.jdesktop.xbindings.properties.XReadOnlyProperty;
import org.junit.Test;

public class XReadOnlyPropertyTest {

	@Test
	public void test_get() {
		
		TestViewModel vm = new TestViewModel("Test");
		assertEquals("Test", vm.name.get());		
	}

	@Test
	public void test_BeanProperty_get() {
		TestViewModel vm = new TestViewModel("Test");	
		BeanProperty prop = BeanProperty.create("name");
		assertEquals("Test", prop.getValue(vm));				
	}	
	
	@Test
	public void test_binding_read() {
		
		TestViewModel vm = new TestViewModel("Test");		
		BindingContext context = new BindingContext();
		JButton btn = new JButton();
		
		context.bind(vm, "name", btn, "text", UpdateStrategy.READ);		
		assertEquals("Test", btn.getText());
		
		vm.rename("Changed Text");
		assertEquals("Changed Text", vm.name.get());
		assertEquals("Changed Text", btn.getText());
		
		context.unbind();
	}	
	
	public static class TestViewModel extends PropertyChangeSupportBase {
		
		private final XProperty<String> _name = AutoProperty.ofType(String.class).name("name").notify(this::firePropertyChanged).createWithDefault("");
		public final XReadOnlyProperty<String> name = AutoProperty.readOnly(_name);
		
		public TestViewModel(String name) {
			_name.set(name);
		}
		
		/**
		 * Changes name but is not an "official" setter
		 */
		public void rename(String newName) {
			_name.set(newName);
		}
		
	}
	
}

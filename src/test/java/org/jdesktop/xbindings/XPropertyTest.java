package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import java.beans.IntrospectionException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.xbindings.properties.AutoProperty;
import org.jdesktop.xbindings.properties.XProperty;
import org.jdesktop.xbindings.properties.XReadOnlyProperty;
import org.junit.Test;

public class XPropertyTest {

	@Test
	public void test_getDefault() {		
		TestViewModel vm = new TestViewModel();
		assertEquals(Integer.valueOf(5), vm.number.get());	
	}
	
	@Test
	public void test_set() {
		
		TestViewModel vm = new TestViewModel();
		
		vm.number.set(10);
		assertEquals(Integer.valueOf(10), vm.number.get());
		
		vm.number.set(null);
		assertEquals(null, vm.number.get());
	}	
	
	@Test
	public void test_propertyChangeNotification() {
		
		TestViewModel vm = new TestViewModel();
		
		AtomicBoolean wasRaised = new AtomicBoolean(false);
		
		vm.addPropertyChangeListener((e) -> {
			wasRaised.set(true);
			assertEquals("number", e.getPropertyName());
		});
		
		vm.number.set(10);			
		assertTrue(wasRaised.get());
	}	
	
	@Test
	public void test_sameValue_noPropertyChangeNotification() {
		TestViewModel vm = new TestViewModel();		
		AtomicBoolean wasRaised = new AtomicBoolean(false);
		
		vm.addPropertyChangeListener((e) -> {
			wasRaised.set(true);			
		});
		
		vm.number.set(5);			
		assertFalse(wasRaised.get());
	}
	
	@Test
	public void test_BeanProperty_get() {
		TestViewModel vm = new TestViewModel();	
		BeanProperty prop = BeanProperty.create("number");
		assertEquals(Integer.valueOf(5), prop.getValue(vm));				
	}
	
	@Test
	public void test_BeanProperty_set() {
		TestViewModel vm = new TestViewModel();	
		BeanProperty prop = BeanProperty.create("number");
		prop.setValue(vm, new Integer(10));
		assertEquals(new Integer(10), vm.number.get());						
	}
	
	@Test
	public void test_BeanProperty_nestedXProperty_get() {
		TestViewModel vm = new TestViewModel();	
		BeanProperty prop = BeanProperty.create("child.childText");
		assertEquals("I am the child", prop.getValue(vm));			
	}
	
	@Test
	public void test_BeanProperty_nestedXProperty_set() {
		TestViewModel vm = new TestViewModel();	
		BeanProperty prop = BeanProperty.create("child.childText");
		prop.setValue(vm, "I am all grown up now");
		
		assertEquals("I am all grown up now", vm.child.get().childText.get());			
	}	
	
	@Test
	public void test_binding_read() {
		
		TestViewModel vm = new TestViewModel();		
		BindingContext context = new BindingContext();
		JButton btn = new JButton();
		
		context.bind(vm, "displayText", btn, "text", UpdateStrategy.READ);		
		assertEquals("Hello World", btn.getText());
		
		vm.displayText.set("Changed Text");
		assertEquals("Changed Text", btn.getText());
		
		context.unbind();
	}
	
	@Test
	public void test_binding_write() {
		
		TestViewModel vm = new TestViewModel();		
		BindingContext context = new BindingContext();
		JTextField txt = new JTextField();
		
		context.bind(vm, "displayText", txt, "text", UpdateStrategy.READ_WRITE);		
		assertEquals("Hello World", txt.getText());
		
		txt.setText("Changed Text");				
		assertEquals("Changed Text", vm.displayText.get());
		
		context.unbind();		
	}	

	public static class TestViewModel extends PropertyChangeSupportBase {		
		
		public final XProperty<Integer> number = AutoProperty.ofType(Integer.class)
												  .name("number")
												  .notify(this::firePropertyChanged)
												  .createWithDefault(5);
		
		public final XProperty<String> displayText = AutoProperty.ofType(String.class)
													 .name("displayText")
													 .notify(this::firePropertyChanged)
													 .createWithDefault("Hello World");
		
		public final XProperty<SubClass> child = AutoProperty.ofType(SubClass.class)
												  .name("child")
												  .notify(this::firePropertyChanged)
												  .createWithDefault(new SubClass());	
		
	}
	
	public static class SubClass extends PropertyChangeSupportBase {
		
		public final XProperty<String> childText = AutoProperty.ofType(String.class)
				  								   .name("childText")
				  								   .notify(this::firePropertyChanged)
				  								   .createWithDefault("I am the child");
		
	}
	
}

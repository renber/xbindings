package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import java.beans.PropertyChangeListener;

import javax.swing.JButton;

import org.junit.Test;

public class BindingContextTest {

	@Test
	public void testBind() {
		
		BindingContext context = new BindingContext();
		TestViewModel vm = new TestViewModel("MyText");
		
		JButton btnTest = new JButton();		
		context.bind(vm, "value", btnTest, "text");
		
		assertEquals("MyText", btnTest.getText());
		vm.setValue("SecondText");
		assertEquals("SecondText", btnTest.getText());
		
		context.unbind();
		vm.setValue("ThirdText");
		assertEquals("SecondText", btnTest.getText());
		
		context.bind();
		vm.setValue("ThirdText");
		assertEquals("ThirdText", btnTest.getText());
	}

	public static class TestViewModel extends PropertyChangeSupportBase {

		private String value;
		
		public TestViewModel(String value) {
			this.value = value;
		}
		
		public String getValue() {
			return value;
		}
		
		public void setValue(String newValue) {
			String oldValue = value;
			value = newValue;
			firePropertyChanged("value", oldValue, newValue);
		}
	}
}

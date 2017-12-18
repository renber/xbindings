package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import org.jdesktop.xbindings.properties.AutoProperty;
import org.jdesktop.xbindings.properties.XProperty;
import org.junit.Test;

public class PropertyOverrideTest {

	@Test
	public void test_propertyHide() {
		
		TestViewModel vm = new TestViewModel();
		HidingViewModel dvm = new HidingViewModel();
		
		assertEquals("Test Person", vm.name.get());
		assertEquals("Hidden", dvm.name.get());
		// test that field is hidden, not overridden
		assertEquals("Test Person", ((TestViewModel)dvm).name.get());
		
	}
	
	@Test
	public void test_propertyOverride() {
		
		TestViewModel vm = new TestViewModel();
		OverridenViewModel dvm = new OverridenViewModel();
		
		assertEquals("Test Person", vm.name.get());
		assertEquals("Overriden", dvm.name.get());
		// test that field is overridden, not hidden
		assertEquals("Overriden", ((TestViewModel)dvm).name.get());
		
	}
	
	public static class TestViewModel extends PropertyChangeSupportBase {		
		public final XProperty<String> name = AutoProperty.ofType(String.class).name("name").notify(this::firePropertyChanged).createWithDefault("Test Person");		
	}
	
	public static class HidingViewModel extends TestViewModel {
		// redeclaring the property of TestViewModel without override
		// -> field of super class is only hidden
		public final XProperty<String> name = AutoProperty.ofType(String.class).name("name").notify(this::firePropertyChanged).createWithDefault("Hidden");		
	}
	
	public static class OverridenViewModel extends TestViewModel {
		// redeclaring the property of TestViewModel with override
		// -> field of super class is set to this property instance as well (i.e. overridden)
		public final XProperty<String> name = AutoProperty.ofType(String.class).name("name").overrideOn(this).notify(this::firePropertyChanged).createWithDefault("Overriden");		
	}
	
}

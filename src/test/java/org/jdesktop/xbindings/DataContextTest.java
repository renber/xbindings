package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import javax.swing.JButton;
import javax.swing.JLabel;

import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.xbindings.BindingContextTest.TestViewModel;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;
import org.junit.Test;

public class DataContextTest {

	@Test
	public void test_dataContextToTargetObject() {
		
		TestPersonViewModel vm = new TestPersonViewModel("Peter", "Staengl");		
		DataContext dataContext = new BeansDataContext(vm);
		BindingContext bindingContext = new BindingContext(true);
		
		JLabel lbl = new JLabel();
		bindingContext.bind(dataContext.path("firstName"), lbl, "text");		
		
		assertEquals("Peter", lbl.getText());
		
		vm.setFirstName("Heinz");
		assertEquals("Heinz", lbl.getText());
	}
	
	@Test
	public void test_nestedDataContextToTargetObject() {
		
		PersonListViewModel listVm = new PersonListViewModel();
		TestPersonViewModel personOne = new TestPersonViewModel("Peter", "Staengl");
		TestPersonViewModel personTwo = new TestPersonViewModel("Engbert", "Norlisch");
		
		DataContext dataContext = new BeansDataContext(listVm);
		BindingContext bindingContext = new BindingContext(true);
		
		JLabel lbl = new JLabel();
		bindingContext.bind(dataContext.path("selectedPerson.firstName"), lbl, "text");
		
		assertEquals("", lbl.getText());
		listVm.setSelectedPerson(personOne);
		assertEquals("Peter", lbl.getText());
		listVm.setSelectedPerson(personTwo);
		assertEquals("Engbert", lbl.getText());
		
		personTwo.setFirstName("Heinz");
		assertEquals("Heinz", lbl.getText());
	}	
	
	public static class PersonListViewModel extends PropertyChangeSupportBase {
		
		TestPersonViewModel selectedPerson = null;
		
		public TestPersonViewModel getSelectedPerson() {
			return selectedPerson;
		}
		
		public void setSelectedPerson(TestPersonViewModel newValue) {
			TestPersonViewModel oldValue = selectedPerson;
			selectedPerson = newValue;
			firePropertyChanged("selectedPerson", oldValue, newValue);
		}		
	}

	public static class TestPersonViewModel extends PropertyChangeSupportBase {
		
		String firstName = "";
		
		public String getFirstName() {
			return firstName;
		}
		
		public void setFirstName(String newValue) {
			String oldValue = firstName;
			firstName = newValue;
			firePropertyChanged("firstName", oldValue, newValue);
		}
		
		String lastName = "";
		
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String newValue) {
			String oldValue = lastName;
			lastName = newValue;
			firePropertyChanged("lastName", oldValue, newValue);
		}	
		
		public TestPersonViewModel(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}
	}
	
}

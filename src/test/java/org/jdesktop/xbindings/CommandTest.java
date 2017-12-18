package org.jdesktop.xbindings;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.jdesktop.xbindings.commands.Command;
import org.jdesktop.xbindings.commands.CommandManager;
import org.jdesktop.xbindings.commands.RelayCommand;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;
import org.junit.Test;

public class CommandTest {

	@Test
	public void test_simpleBind() {
		CommandManager commandManager = new CommandManager();		
		CommandTestViewModel viewModel = new CommandTestViewModel(1);
		DataContext dataContext = new BeansDataContext(viewModel);
		JButton btn = new JButton();
		
		commandManager.bind(btn, dataContext.path("myCommand"));
		commandManager.start();
		
		CommandManager.suggestRequeryCommands(); // manually refresh command states		
		assertTrue(btn.isEnabled());
		
		// test bind button enabled <-> command canExecute
		viewModel.commandCanExecute = false;
		CommandManager.suggestRequeryCommands(); // manually refresh command states		
		assertFalse(btn.isEnabled());
		
		viewModel.commandCanExecute = true;
		CommandManager.suggestRequeryCommands(); // manually refresh command states		
		assertTrue(btn.isEnabled());
		
		// test execute
		assertFalse(viewModel.commandExecuted);
		btn.doClick();
		assertTrue(viewModel.commandExecuted);	
				
		// test unbind
		commandManager.clear();
		viewModel.commandExecuted = false;
		btn.doClick();
		assertFalse(viewModel.commandExecuted);
	}
	
	@Test
	public void test_changingCommandPath() {
		
		ParentViewModel parentViewModel = new ParentViewModel();
		CommandTestViewModel commandOne = new CommandTestViewModel(1);
		CommandTestViewModel commandTwo = new CommandTestViewModel(2);
		
		parentViewModel.setSelectedCommandViewModel(commandOne);
		
		CommandManager commandManager = new CommandManager();			
		DataContext dataContext = new BeansDataContext(parentViewModel);
		JButton btn = new JButton();
		
		commandManager.bind(btn, dataContext.path("selectedCommandViewModel.myCommand"));
		commandManager.start();
		
		btn.doClick();
		assertTrue(commandOne.commandExecuted);
		assertFalse(commandTwo.commandExecuted);
		
		// change datacontext object to command two		
		parentViewModel.setSelectedCommandViewModel(commandTwo);
		CommandManager.suggestRequeryCommands(); // manually refresh command states		
		commandOne.commandExecuted = false;

		btn.doClick();
		assertFalse(commandOne.commandExecuted);			
		assertTrue(commandTwo.commandExecuted);
		
		// test enabled
		commandTwo.commandCanExecute = false;
		CommandManager.suggestRequeryCommands(); // manually refresh command states
		assertFalse(btn.isEnabled());
		
		parentViewModel.setSelectedCommandViewModel(commandOne);
		CommandManager.suggestRequeryCommands(); // manually refresh command states
		assertTrue(btn.isEnabled());
		
		// test default value of executable (button should be disabled when no command can be resolved from the binding)
		parentViewModel.setSelectedCommandViewModel(null);
		assertFalse(btn.isEnabled());
	}
	
	public static class ParentViewModel extends PropertyChangeSupportBase {
		
		private CommandTestViewModel selectedCommandViewModel;
		
		public CommandTestViewModel getSelectedCommandViewModel() {
			return selectedCommandViewModel;
		}
		
		public void setSelectedCommandViewModel(CommandTestViewModel newValue) {
			CommandTestViewModel oldValue = selectedCommandViewModel;
			selectedCommandViewModel = newValue;
			firePropertyChanged("selectedCommandViewModel", oldValue, newValue);
		}
		
	}
	
	public static class CommandTestViewModel extends PropertyChangeSupportBase {
		
		boolean commandExecuted = false;
		boolean commandCanExecute = true;
		
		Command myCommand;			
		
		public CommandTestViewModel(int id) {			
			myCommand = new RelayCommand(() -> commandExecuted = true, () -> commandCanExecute);
		}
		
		public Command getMyCommand() {
			return myCommand;
		}
	}
	
}

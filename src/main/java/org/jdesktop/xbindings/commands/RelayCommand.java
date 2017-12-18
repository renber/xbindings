package org.jdesktop.xbindings.commands;

import java.util.function.Supplier;

/**
 * A command implementation which can be constructed using lambda functions
 * for execute and canExecute
 * @author berre
 */
public class RelayCommand extends Command {

	Runnable executeFunc;
	Supplier<Boolean> canExecuteFunc;
	
	/**
	 * Creates a new command instance which is always executable
	 * @param executeFunc The function to execute when this commands' execute method is called
	 */
	public RelayCommand(Runnable executeFunc) {
		this(executeFunc, null);
	}
	
	/**
	 * Creates a new command instance using the given two functions
	 * @param executeFunc The function to execute when this commands' execute method is called
	 * @param canExecuteFunc The predicate function to call in order to evaluate if this command can be executed currently
	 */
	public RelayCommand(Runnable executeFunc, Supplier<Boolean> canExecuteFunc) {
		if (executeFunc == null)
			throw new IllegalArgumentException("Parameter executeFunc mus tnot be null");
		
		this.executeFunc = executeFunc;
		this.canExecuteFunc = canExecuteFunc;
	}
	
	@Override
	protected void doExecute() {
		executeFunc.run();
	}

	@Override
	protected boolean canExecute() {
		if (canExecuteFunc == null)
			return true;
		
		return canExecuteFunc.get();
	}

}

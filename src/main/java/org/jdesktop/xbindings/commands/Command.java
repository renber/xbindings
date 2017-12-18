package org.jdesktop.xbindings.commands;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Base class for command pattern
 *
 * @author berre
 */
public abstract class Command {

	/**
	 * Should exceptions in the canExecute or Execute method be logged to the console?
	 */
	public static boolean LOG_EXCEPTIONS = false;

	/**
	 * The (optional) parameter to canExecute/execute
	 */
	private ThreadLocal<Object> currentParameter = new ThreadLocal<Object>();

    protected boolean executable = false;

    // PropertyChangeSupport
    protected PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener(listener);
    }

    public boolean isExecutable()
    {
        return executable;
    }

    /*
     * Execute the command with parameter = null
     */
    public void execute()
    {
        execute(null);
    }

    /*
     * Execute the command
     * @param parameter An optional parameter to the command's execute method
     */
    public void execute(Object parameter)
    {
    	setParameter(parameter);
        updateExecutable();

        if (isExecutable()) {
            doExecute();
        }
    }

    private void setParameter(Object parameter) {
    	currentParameter.set(parameter);
    }

    /**
     * Returns the parameter given to execute(...)
     */
    protected Object getParameter() {
    	return currentParameter.get();
    }

    /*
     * Updates the Executable-Property and informs listeners when the value has changed
     * (Usually only called by the CommandManager)
     */
    public void updateExecutable()
    {
        boolean oldValue = executable;
        boolean newValue;
        try {
        	newValue = canExecute();
        } catch (Exception exc) {
        	newValue = false; // disable the command, since we do not know what to do

        	if (LOG_EXCEPTIONS)
        		System.out.println("[Command: " + this.getClass().getName() + "] canExecute raised an Exception: " + exc.getMessage());
        }

        if (newValue != oldValue)
        {
            executable = newValue;
            support.firePropertyChange("executable", oldValue, newValue);
        }
    }

    /*
     * Code which shall be run when the command is executed
     * Check the parameter given to execute(...) with getParameter() if necessary
     */
    protected abstract void doExecute();

    /*
     * Called to determine if the command can be executed at the moment
     */
    protected abstract boolean canExecute();
}

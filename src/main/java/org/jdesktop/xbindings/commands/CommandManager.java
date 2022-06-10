package org.jdesktop.xbindings.commands;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ActionMap;
import javax.swing.ButtonModel;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.xbindings.BindingContext;
import org.jdesktop.xbindings.PropertyChangeSupportBase;
import org.jdesktop.xbindings.XBinding;
import org.jdesktop.xbindings.context.DataContext;

/**
 * Class which manages commands and checks if canExecute() changed, firing an
 * appropriate propertyChanged event
 *
 * @author berre
 */
public class CommandManager implements XBinding {

	/** Interval in which the commands canExecute() method is evaluated in milliseconds */
	private static final int COMMAND_CHECK_INTERVAL = 150;

	// we use one timer for all CommandManager instances -> less cpu load
	private static Timer refreshTimer;
	// the commands to refresh and their reference count
	private static HashMap<Command, RefCounter> monitoredCommands = new HashMap<Command, RefCounter>();

	// this command manager's commands
	private HashMap<Object, CommandObject> commands = new HashMap<Object, CommandObject>();	

	private CommandActionListener actionListener;
	private BindingContext bindingContext = new BindingContext(false);

	private boolean isStarted = false; // keep track if this CommandManager has been started (avoid ReferenceCounters falling when calling stop() consecutively)

	@SuppressWarnings("rawtypes")
	private BeanProperty executableProperty = BeanProperty.create("executable");
	@SuppressWarnings("rawtypes")
	private BeanProperty enabledProperty = BeanProperty.create("enabled");
	@SuppressWarnings("rawtypes")
	private BeanProperty visibleProperty = BeanProperty.create("visible");

	static {
		// install application-wide input listener
		// to refresh commands after user input
		/*long eventMask = AWTEvent.MOUSE_EVENT_MASK + AWTEvent.KEY_EVENT_MASK;
		Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
			@Override
			public void eventDispatched(AWTEvent e) {

				if (e.getID() == MouseEvent.MOUSE_ENTERED || e.getID() == MouseEvent.MOUSE_EXITED)
					return; // mouse motion is no real "interaction" so ignore
							// it

				// update commands after user input				
				SwingUtilities.invokeLater(() -> requeryCommands());
			}
		}, eventMask);*/
		// create the timer which refreshes the commands
		TimerTask task = new TimerTask() {

			@Override
			public void run() {
				synchronized (monitoredCommands) {
					requeryCommands();
					}
				}				
		};		

		refreshTimer = new Timer();
		refreshTimer.scheduleAtFixedRate(task, 0, COMMAND_CHECK_INTERVAL);		
	}
	
	/**
	 * Requery all registered commands to check whether
	 * their canExecute flag has changed
	 */
	private static void requeryCommands() {		
		try {
			for (Command cmd : monitoredCommands.keySet()) {
				if (cmd != null) {
					cmd.updateExecutable();
				}
			}	
		} catch (ConcurrentModificationException e)
		{
			// requery the new, modified list of commands
			requeryCommands();
		}
	}
	
	/**
	 * Indicate to the CommandManager that all the canExecute states
	 * of all Commands should be updated 
	 */
	public static void suggestRequeryCommands() {
		requeryCommands(); 		
	}

	public CommandManager() {
		actionListener = new CommandActionListener(this);
	}

	protected void publish(Component targetComponent, CommandObject commandObject) {
		commands.put(targetComponent, commandObject);
		
		if (commandObject.getCurrentCommand() != null) {
			commandObject.getCurrentCommand().updateExecutable();
			
			if (isStarted) {
				enqueueCommand(commandObject.getCurrentCommand());
			}
		}				
	}

	private Command getCommand(Object forObject) {
		if (commands.containsKey(forObject)) {
			return commands.get(forObject).getCurrentCommand();
		} else {
			return null;
		}
	}

	public void clear() {
		stop();
		commands.clear();
		bindingContext.clear();
	}

	public void tryExecute(Object sourceObject) {
		Command cmd = getCommand(sourceObject);
		if (cmd != null) {
			cmd.execute();
		}
	}

	/**
	 * Adds the given command to the list of commands
	 * which should be monitored or increases the reference counter
	 * if the command is already monitored
	 *
	 * ! Lock monitoredCommands before calling this !
	 */
	private static void enqueueCommand(Command command) {
		if (monitoredCommands.containsKey(command)) {
			monitoredCommands.get(command).inc();
		} else {
			RefCounter rc = new RefCounter(1);
			monitoredCommands.put(command, rc);
		}
	}

	/**
	 * Removes the given command from the list of commands
	 * which should be monitored or decreases the reference counter
	 * if the command is still monitored through other command managers
	 *
	 * ! Lock monitoredCommands before calling this !
	 */
	private static void dequeueCommand(Command command) {
		if (monitoredCommands.containsKey(command)) {
			if (monitoredCommands.get(command).dec()) {
				// reference counter reached 0 -> command does not need to be monitored anymore
				monitoredCommands.remove(command);				
			}
		}
	}

	/**
	 * Call this to let the CommandManager check the state of commands bound through this instance
	 * call stop() when the CommandManager is no longer needed (for instance
	 * when closing its window)
	 */
	public void start() {
		if (!isStarted) {
			bindingContext.bind();

			synchronized (monitoredCommands) {
				for (CommandObject c : commands.values()) {
					enqueueCommand(c.getCurrentCommand());
				}
			}

			isStarted = true;
		}
	}

	/**
	 * Stops this command manager instance and removes all bindings
	 */
	public void stop() {
		if (isStarted) {
			bindingContext.unbind();
			
			synchronized (monitoredCommands) {
				for (CommandObject c : commands.values())
					dequeueCommand(c.getCurrentCommand());
				
				// remove listeners from controls
				for(Object c: commands.keySet()) {
					if (c instanceof AbstractButton) {
						((AbstractButton)c).removeActionListener(actionListener);
					}
				}
			}
			
			isStarted = false;
		}
	}

	/**
	 * Binds the button to the command represented by the given data context
	 * @param btn The button to bind to the command
	 * @param dataContext The datacontext of the command to bind
	 */
	public void bind(AbstractButton btn, DataContext dataContext) { 
		bind(btn, dataContext, false);
	}
	
	/**
	 * Binds the abstract button to the command described by the given DataContext
	 * Changes in the data context are reflected at runtime
	 * @param btn The abstract button to bind to the command
	 * @param dataContext The data context of the command to bind
	 * @param canExecuteAffectsVisibility When the command cannot be executed the menu item will be hidden instead of being disabled
	 */
	public void bind(AbstractButton btn, DataContext dataContext, boolean canExecuteAffectsVisibility) {
		CommandObject cmdObject = new CommandObject(dataContext);		
		
		// bind the dynamic command to the CommandObject
		bindingContext.bind(dataContext, cmdObject, "currentCommand");		
		publish(btn, cmdObject);				
		
		// bind command to button		
		btn.addActionListener(actionListener);
				
		// set enabled / disabled state of button according to command's executable state
		// defaulting to false (when binding cannot be resolved)
		bindingContext.bind(dataContext.path("executable"), btn, "enabled").setSourceUnreadableValue(false);

		if (canExecuteAffectsVisibility)
			bindingContext.bind(dataContext.path("executable"), btn, "visible").setSourceUnreadableValue(false);		
	}	

	
	/**
	 * Binds the button to the given command using the command name
	 * @param btn The button to bind to the command
	 * @param cmd The command to bind
	 */
	public void bind(AbstractButton btn, Command cmd) {
		bind(btn, cmd, false);
	}	
	
	/**
	 * Binds the menu item to the given command using the command name
	 * @param btn The button to bind to the command
	 * @param cmd The command to bind
	 * @param canExecuteAffectsVisibility When the command cannot be executed the menu item will be hidden instead of being disabled
	 */
	public void bind(AbstractButton btn, Command cmd, boolean canExecuteAffectsVisibility) {
		publish(btn, new CommandObject(cmd));

		// bind command to button		
		btn.addActionListener(actionListener);		

		// set enabled / disabled state of button according to command's executable state
		bindingContext.bind(cmd, executableProperty, btn, enabledProperty);

		if (canExecuteAffectsVisibility)
			bindingContext.bind(cmd, executableProperty, btn, visibleProperty);
	}	

	/**
	 * Bind the components enabled property to the given command's canExecute
	 */
	public void bindEnabled(Component component, Command cmd) {
		publish(component, new CommandObject(cmd));

		// set enabled / disabled state of component according to command's executable state
		bindingContext.bind(cmd, executableProperty, component, enabledProperty);
	}	
	
	/** Bind the command to the given component and execute it, when the component is focused and the given key is pressed	
	 * 
	 * @param component
	 * @param cmd
	 * @param keyCode
	 * @param global -> should the binding react globally or only on component-focus?
	 */
	public void bindKey(Component component, final Command cmd, final int keyCode, boolean global) {
		
		publish(component, new CommandObject(cmd));
		
		if (global && component instanceof JComponent)
		{
			// for JComponents use InputMap 
			// works when the given component or any of its subcomponents has focus
			JComponent jComponent = (JComponent)component;
			
			InputMap inputMap; 
			ActionMap actionMap;
			AbstractAction action;

			inputMap  = jComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
			actionMap = jComponent.getActionMap();			
			
			action  = new AbstractAction()
			{
				@Override
				public void actionPerformed(ActionEvent e) {
					cmd.execute();
				}

			};

			String commandVerb = newCommandVerb(cmd);
			inputMap.put(KeyStroke.getKeyStroke(keyCode, 0), commandVerb);
			actionMap.put(commandVerb, action);	
		} else
		{
			// only works when the given component has focus
			component.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == keyCode)
						cmd.execute();				
			}});
		}
		
	}

	/**
	 * Free all resources of the command managers static timer task
	 * which is shared by all CommandManager instances
	 * (call this before exiting the application)
	 */
	public static void end() {
		// stop the timer, if this has not yet been done
		if (refreshTimer != null) {
			refreshTimer.cancel();
		}
	}

	@Override
	protected void finalize() throws Throwable {
		this.stop();

		super.finalize();
	}

	/**
	 * Returns a command verb for the given command
	 */
	private static String newCommandVerb(Command cmd) {				
		return cmd.getClass().getName() + "@" + System.identityHashCode(cmd); // generate an instance-unique identifier
		// registering the same command instance with different components always yields the same verb string
	}
	
	/**
	 * CommandObject which can be used as a binding target
	 * @author renber
	 */
	public class CommandObject {
		
		// the current command instance
		DataContext dataContext;
		Command currentCommand;
	
		public void setCurrentCommand(Command newValue) {
			// remove the old commands
			if (currentCommand != null)
				dequeueCommand(currentCommand);
			
			currentCommand = newValue;
			
			// register the new command
			if (currentCommand != null)
				enqueueCommand(currentCommand);
		}
		
		public Command getCurrentCommand() {
			if (dataContext == null)
				return currentCommand;
			else
				return (Command)dataContext.getValue();
		}
		
		public CommandObject(DataContext dataContext) {
			this.dataContext = dataContext;
		}
		
		public CommandObject(Command initialCommand) {
			currentCommand = initialCommand;			
		}
	}

	@Override
	public void bind() {
		start();
	}

	@Override
	public void unbind() {
		stop();
	}

	@Override
	public boolean isBound() {
		return isStarted;
	}
}

/**
 * A reference counter for commands
 * @author berre
 *
 */
class RefCounter {
	private int refCount = 0;

	public RefCounter(int _startCount) {
		refCount = _startCount;
	}

	/**
	 * Decreases the reference counter and returns true
	 * if the counter reached zero
	 */
	synchronized boolean dec() {
		refCount--;
		return refCount <= 0;
	}

	/**
	 * Increases the reference counter
	 */
	synchronized void inc() {
		refCount++;
	}
}

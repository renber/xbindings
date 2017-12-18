package org.jdesktop.xbindings.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ActionListener for controls which are bound to commands by the CommandManager class
 * Tries to execute the associated command of the control
 */
class CommandActionListener implements ActionListener
{
    private CommandManager commandManager;
    
    public CommandActionListener(CommandManager _commandManager)
    {
        commandManager = _commandManager;
    }    
    
    @Override
    public void actionPerformed(ActionEvent e) {
        // try to execute the command
        commandManager.tryExecute(e.getSource());
    }    
}  

/***********************************************************************************************************************
 *
 * BetterBeansBinding - keeping JavaBeans in sync
 * ==============================================
 *
 * Copyright (C) 2009 by Tidalwave s.a.s. (http://www.tidalwave.it)
 * http://betterbeansbinding.kenai.com
 *
 * This is derived work from BeansBinding: http://beansbinding.dev.java.net
 * BeansBinding is copyrighted (C) by Sun Microsystems, Inc.
 *
 ***********************************************************************************************************************
 *
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 *
 ***********************************************************************************************************************
 *
 * $Id: Logger.java 116 2009-07-09 08:21:46Z fabriziogiudici $
 *
 **********************************************************************************************************************/
package org.jdesktop.beansbinding.util.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;

/***********************************************************************************************************************
 *
 * @author Fabrizio Giudici
 * @version $Id: Logger.java 116 2009-07-09 08:21:46Z fabriziogiudici $
 *
 **********************************************************************************************************************/
public class Logger {

    private final static Map<String, Logger> loggerMapByName = new HashMap<String, Logger>();

    private final java.util.logging.Logger logger;

    Logger( final java.util.logging.Logger logger) {
        this.logger = logger;
    }


    public static synchronized Logger getLogger( final String name) {
        Logger logger = loggerMapByName.get(name);
        if (logger == null) {
            logger = new Logger(java.util.logging.Logger.getLogger(name));
            loggerMapByName.put(name, logger);
        }

        return logger;
    }

    public void throwing(String sourceClass, String sourceMethod, Throwable thrown) {
        logger.throwing(sourceClass, sourceMethod, thrown);
    }

    public void severe( final String string,  final Object... args) {
        log(Level.SEVERE, string, args);
    }

    public void warning( final String string,  final Object... args) {
        log(Level.WARNING, string, args);
    }

    public void info( final String string,  final Object... args) {
        log(Level.INFO, string, args);
    }

    public void fine( final String string,  final Object... args) {
        log(Level.FINE, string, args);
    }

    public void finer( final String string,  final Object... args) {
        log(Level.FINER, string, args);
    }

    public void finest( final String string,  final Object... args) {
        log(Level.FINEST, string, args);
    }

    private void log( Level level,  final String string,
             final Object... args) {
        if (logger.isLoggable(level)) {
            logger.log(level, String.format(string, args));
        }
    }

    public void setLevel(final Level level) throws SecurityException {
        logger.setLevel(level);
    }

    public boolean isLoggable(final Level level) {
        return logger.isLoggable(level);
    }

    public String getName() {
        return logger.getName();
    }

    public Level getLevel() {
        return logger.getLevel();
    }

    public synchronized Handler[] getHandlers() {
        return logger.getHandlers();
    }
}

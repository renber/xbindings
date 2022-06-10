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
 * $Id: JComboBoxAdapterProvider.java 60 2009-04-26 20:47:20Z fabriziogiudici $
 *
 **********************************************************************************************************************/
package org.jdesktop.swingbinding.adapters;

import org.jdesktop.beansbinding.ext.BeanAdapterProvider;

import java.awt.event.*;

import java.beans.*;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;


/**
 * @author Shannon Hickey
 */
public final class JComboBoxAdapterProvider implements BeanAdapterProvider {
    private static final String SELECTED_ITEM_P = "selectedItem";

    public boolean providesAdapter(Class<?> type, String property) {
        return JComboBox.class.isAssignableFrom(type) &&
        (property.intern() == SELECTED_ITEM_P);
    }

    public Object createAdapter(Object source, String property) {
        if (!providesAdapter(source.getClass(), property)) {
            throw new IllegalArgumentException();
        }

        return new Adapter((JComboBox) source);
    }

    public Class<?> getAdapterClass(Class<?> type) {
        return JList.class.isAssignableFrom(type)
        ? JComboBoxAdapterProvider.Adapter.class : null;
    }

    public static final class Adapter extends BeanAdapterBase {
        private JComboBox combo;
        private Handler handler;
        private Object cachedItem;
        private ListDataListener listDataListener;

        private Adapter(JComboBox combo) {
            super(SELECTED_ITEM_P);
            this.combo = combo;

            listDataListener = new ListDataListener() {
                @Override
                public void intervalAdded(ListDataEvent e) {
                }

                @Override
                public void intervalRemoved(ListDataEvent e) {
                }

                @Override
                public void contentsChanged(ListDataEvent e) {
                    // fix: when the items of the combobox change remove the cached selection
                    cachedItem = null;
                }
            };
        }

        public Object getSelectedItem() {
        	// added by renber
        	// do not change the source property to null
        	// when the combobox has no selection (most likely because its items are in the event of being swapped out)
        	// users cannot deliberately set a combobox to null
        	if (combo.getSelectedItem() == null)
        		return cachedItem;
        	
            return combo.getSelectedItem();
        }

        public void setSelectedItem(Object item) {
            combo.setSelectedItem(item);
        }

        protected void listeningStarted() {
            handler = new Handler();
            // fix by renber: set to null, do not assume the state of the ComboBox before binding
            cachedItem = null;
            combo.addActionListener(handler);
            combo.addPropertyChangeListener("model", handler);
            combo.getModel().addListDataListener(listDataListener);
        }

        protected void listeningStopped() {
            combo.removeActionListener(handler);
            combo.removePropertyChangeListener("model", handler);
            combo.getModel().removeListDataListener(listDataListener);
            handler = null;
            cachedItem = null;
        }

        private class Handler implements ActionListener, PropertyChangeListener {
            private void comboSelectionChanged() {
	           Object oldValue = cachedItem;
	           Object selected = getSelectedItem();
	           if (selected != null) {
	              	cachedItem = selected;
	           }
	           firePropertyChange(oldValue, selected);
            }

            public void actionPerformed(ActionEvent ae) {
                comboSelectionChanged();
            }

            public void propertyChange(PropertyChangeEvent pce) {
                comboSelectionChanged();
            }
        }
    }
}

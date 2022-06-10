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
 * $Id: SwingBindings.java 73 2009-06-12 19:50:33Z fabriziogiudici $
 *
 **********************************************************************************************************************/
package org.jdesktop.swingbinding;

import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JTable;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.ObjectProperty;
import org.jdesktop.beansbinding.Property;
import org.jdesktop.xbindings.context.BeansDataContext;
import org.jdesktop.xbindings.context.DataContext;

/**
 * A factory class for creating instances of the custom Swing {@code Binding}
 * implementations provided by this package. See the
 * <a href="package-summary.html">package summary</a> for full details on
 * binding to Swing components.
 *
 * @author Shannon Hickey
 * @author Fabrizio Giudici
 */
public class SwingBindings {

    private SwingBindings() {
    }

    /**
     * Creates a {@code JListBinding} from direct references to a {@code List} and {@code JList}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetJList the target {@code JList}
     * @return the {@code JTableBinding}
     */
    public static <E> JListBinding<E, List<E>, JList> createJListBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            JList targetJList) {
        return new JListBinding<E, List<E>, JList>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetJList,
                ObjectProperty.<JList>create(), null);
    }

    public static JListBinding createJListBinding(DataContext sourceListContext, JList targetJList) {
        return createJListBinding(AutoBinding.UpdateStrategy.READ, sourceListContext.getSource(), sourceListContext.getPropertyHelper(), targetJList);
    }

    /**
     * Creates a named {@code JListBinding} from direct references to a {@code List} and {@code JList}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetJList the target {@code JList}
     * @return the {@code JListBinding}
     */
    public static <E> JListBinding<E, List<E>, JList> createJListBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            JList targetJList, String name) {
        return new JListBinding<E, List<E>, JList>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetJList,
                ObjectProperty.<JList>create(), name);
    }

    /**
     * Creates a {@code JListBinding} from an object and property that resolves to a {@code List} and a direct reference to a {@code JList}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetJList the target {@code JList}
     * @return the {@code JListBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} is {@code null}
     */
    public static <E, SS> JListBinding<E, SS, JList> createJListBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, JList targetJList) {
        return new JListBinding<E, SS, JList>(strategy, sourceObject,
                sourceListProperty, targetJList, ObjectProperty.<JList>create(),
                null);
    }

    /**
     * Creates a named {@code JListBinding} from an object and property that resolves to a {@code List} and a direct reference to a {@code JList}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetJList the target {@code JList}
     * @return the {@code JListBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} is {@code null}
     */
    public static <E, SS> JListBinding<E, SS, JList> createJListBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, JList targetJList, String name) {
        return new JListBinding<E, SS, JList>(strategy, sourceObject,
                sourceListProperty, targetJList, ObjectProperty.<JList>create(),
                name);
    }

    /**
     * Creates a {@code JListBinding} from a direct reference to a {@code List} and an object and property that resolves to a {@code JList}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetObject the target object
     * @param targetJListProperty a property on the target object that resolves to a {@code JList}
     * @return the {@code JListBinding}
     * @throws IllegalArgumentException if {@code targetJListProperty} is {@code null}
     */
    public static <E, TS> JListBinding<E, List<E>, TS> createJListBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            TS targetObject, Property<TS, ? extends JList> targetJListProperty) {
        return new JListBinding<E, List<E>, TS>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetObject,
                targetJListProperty, null);
    }

    /**
     * Creates a named {@code JListBinding} from a direct reference to a {@code List} and an object and property that resolves to a {@code JList}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetObject the target object
     * @param targetJListProperty a property on the target object that resolves to a {@code JList}
     * @return the {@code JListBinding}
     * @throws IllegalArgumentException if {@code targetJListProperty} is {@code null}
     */
    public static <E, TS> JListBinding<E, List<E>, TS> createJListBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            TS targetObject, Property<TS, ? extends JList> targetJListProperty,
            String name) {
        return new JListBinding<E, List<E>, TS>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetObject,
                targetJListProperty, name);
    }

    /**
     * Creates a {@code JListBinding} from an object and property that resolves to a {@code List} and an object and property that resolves to a {@code JList}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetObject the target object
     * @param targetJListProperty a property on the target object that resolves to a {@code JList}
     * @return the {@code JListBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} or {@code targetJListProperty} is {@code null}
     */
    public static <E, SS, TS> JListBinding<E, SS, TS> createJListBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, TS targetObject,
            Property<TS, ? extends JList> targetJListProperty) {
        return new JListBinding<E, SS, TS>(strategy, sourceObject,
                sourceListProperty, targetObject, targetJListProperty, null);
    }

    /**
     * Creates a named {@code JListBinding} from an object and property that resolves to a {@code List} and an object and property that resolves to a {@code JList}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetObject the target object
     * @param targetJListProperty a property on the target object that resolves to a {@code JList}
     * @return the {@code JListBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} or {@code targetJListProperty} is {@code null}
     */
    public static <E, SS, TS> JListBinding<E, SS, TS> createJListBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, TS targetObject,
            Property<TS, ? extends JList> targetJListProperty, String name) {
        return new JListBinding<E, SS, TS>(strategy, sourceObject,
                sourceListProperty, targetObject, targetJListProperty, name);
    }

    /**
     * Creates a {@code JTableBinding} from direct references to a {@code List} and {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetJTable the target {@code JTable}
     * @return the {@code JTableBinding}
     */
    public static <E> JTableBinding<E, List<E>, JTable> createJTableBinding(
             AutoBinding.UpdateStrategy strategy,
             List<E> sourceList,
             JTable targetJTable) {
        return new JTableBinding<E, List<E>, JTable>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetJTable,
                ObjectProperty.<JTable>create(), null);
    }

    /**
     * Creates a named {@code JTableBinding} from direct references to a {@code List} and {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetJTable the target {@code JTable}
     * @return the {@code JTableBinding}
     */
    public static <E> JTableBinding<E, List<E>, JTable> createJTableBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            JTable targetJTable, String name) {
        return new JTableBinding<E, List<E>, JTable>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetJTable,
                ObjectProperty.<JTable>create(), name);
    }

    /**
     * Creates a {@code JTableBinding} from an object and property that resolves to a {@code List} and a direct reference to a {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetJTable the target {@code JTable}
     * @return the {@code JTableBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} is {@code null}
     */
    public static <E, SS> JTableBinding<E, SS, JTable> createJTableBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, JTable targetJTable) {
        return new JTableBinding<E, SS, JTable>(strategy, sourceObject,
                sourceListProperty, targetJTable, ObjectProperty.<JTable>create(),
                null);
    }

    /**
     * Creates a named {@code JTableBinding} from an object and property that resolves to a {@code List} and a direct reference to a {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetJTable the target {@code JTable}
     * @return the {@code JTableBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} is {@code null}
     */
    public static <E, SS> JTableBinding<E, SS, JTable> createJTableBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, JTable targetJTable,
            String name) {
        return new JTableBinding<E, SS, JTable>(strategy, sourceObject,
                sourceListProperty, targetJTable, ObjectProperty.<JTable>create(),
                name);
    }
    
    /**
     * Creates a named {@code JTableBinding} from a DataContext that resolves to a {@code List} and a direct reference to a {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceListContext a DataContext that resolves to a {@code List} 
     * @param targetJTable the target {@code JTable}
     * @return the {@code JTableBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} is {@code null}
     */
    public static JTableBinding createJTableBinding(
            AutoBinding.UpdateStrategy strategy, DataContext sourceListContext, JTable targetJTable) {
            return createJTableBinding(strategy, sourceListContext.getSource(), sourceListContext.getPropertyHelper(), targetJTable);
    }    

    /**
     * Creates a {@code JTableBinding} from a direct reference to a {@code List} and an object and property that resolves to a {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetObject the target object
     * @param targetJTableProperty a property on the target object that resolves to a {@code JTable}
     * @return the {@code JTableBinding}
     * @throws IllegalArgumentException if {@code targetJTableProperty} is {@code null}
     */
    public static <E, TS> JTableBinding<E, List<E>, TS> createJTableBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            TS targetObject, Property<TS, ? extends JTable> targetJTableProperty) {
        return new JTableBinding<E, List<E>, TS>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetObject,
                targetJTableProperty, null);
    }

    /**
     * Creates a named {@code JTableBinding} from a direct reference to a {@code List} and an object and property that resolves to a {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetObject the target object
     * @param targetJTableProperty a property on the target object that resolves to a {@code JTable}
     * @return the {@code JTableBinding}
     * @throws IllegalArgumentException if {@code targetJTableProperty} is {@code null}
     */
    public static <E, TS> JTableBinding<E, List<E>, TS> createJTableBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            TS targetObject, Property<TS, ? extends JTable> targetJTableProperty,
            String name) {
        return new JTableBinding<E, List<E>, TS>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetObject,
                targetJTableProperty, name);
    }

    /**
     * Creates a {@code JTableBinding} from an object and property that resolves to a {@code List} and an object and property that resolves to a {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetObject the target object
     * @param targetJTableProperty a property on the target object that resolves to a {@code JTable}
     * @return the {@code JTableBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} or {@code targetJTableProperty} is {@code null}
     */
    public static <E, SS, TS> JTableBinding<E, SS, TS> createJTableBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, TS targetObject,
            Property<TS, ? extends JTable> targetJTableProperty) {
        return new JTableBinding<E, SS, TS>(strategy, sourceObject,
                sourceListProperty, targetObject, targetJTableProperty, null);
    }

    /**
     * Creates a named {@code JTableBinding} from an object and property that resolves to a {@code List} and an object and property that resolves to a {@code JTable}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetObject the target object
     * @param targetJTableProperty a property on the target object that resolves to a {@code JTable}
     * @return the {@code JTableBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} or {@code targetJTableProperty} is {@code null}
     */
    public static <E, SS, TS> JTableBinding<E, SS, TS> createJTableBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, TS targetObject,
            Property<TS, ? extends JTable> targetJTableProperty, String name) {
        return new JTableBinding<E, SS, TS>(strategy, sourceObject,
                sourceListProperty, targetObject, targetJTableProperty, name);
    }

    /**
     * Creates a {@code JComboBoxBinding} from direct references to a {@code List} and {@code JComboBox}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetJComboBox the target {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     */
    public static <E> JComboBoxBinding<E, List<E>, JComboBox> createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            JComboBox targetJComboBox) {
        return new JComboBoxBinding<E, List<E>, JComboBox>(strategy,
                sourceList, ObjectProperty.<List<E>>create(), targetJComboBox,
                ObjectProperty.<JComboBox>create(), null);
    }

    /**
     * Creates a named {@code JComboBoxBinding} from direct references to a {@code List} and {@code JComboBox}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetJComboBox the target {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     */
    public static <E> JComboBoxBinding<E, List<E>, JComboBox> createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            JComboBox targetJComboBox, String name) {
        return new JComboBoxBinding<E, List<E>, JComboBox>(strategy,
                sourceList, ObjectProperty.<List<E>>create(), targetJComboBox,
                ObjectProperty.<JComboBox>create(), name);
    }

    /**
     * Creates a {@code JComboBoxBinding} from a DataContext that resolves to a {@code List} and a direct reference to a {@code JComboBox}.
     *
     * @param strategy the update strategy*
     * @param targetJComboBox the target {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     * @throws IllegalArgumentException if {@code dataContext} is {@code null}
     */
    public static JComboBoxBinding createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, DataContext dataContext, JComboBox targetJComboBox) {
    	if (dataContext == null)
    		throw new IllegalArgumentException("Parameter dataContetx must not be null.");
    	
        return new JComboBoxBinding(strategy, ((BeansDataContext)dataContext).getSource(), ((BeansDataContext)dataContext).getPropertyHelper(), targetJComboBox,
                ObjectProperty.<JComboBox>create(), null);
    }
    
    /**
     * Creates a {@code JComboBoxBinding} from an object and property that resolves to a {@code List} and a direct reference to a {@code JComboBox}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetJComboBox the target {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} is {@code null}
     */
    public static <E, SS> JComboBoxBinding<E, SS, JComboBox> createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, JComboBox targetJComboBox) {
        return new JComboBoxBinding<E, SS, JComboBox>(strategy, sourceObject,
                sourceListProperty, targetJComboBox,
                ObjectProperty.<JComboBox>create(), null);
    }

    /**
     * Creates a named {@code JComboBoxBinding} from an object and property that resolves to a {@code List} and a direct reference to a {@code JComboBox}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetJComboBox the target {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} is {@code null}
     */
    public static <E, SS> JComboBoxBinding<E, SS, JComboBox> createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, JComboBox targetJComboBox,
            String name) {
        return new JComboBoxBinding<E, SS, JComboBox>(strategy, sourceObject,
                sourceListProperty, targetJComboBox,
                ObjectProperty.<JComboBox>create(), name);
    }

    /**
     * Creates a {@code JComboBoxBinding} from a direct reference to a {@code List} and an object and property that resolves to a {@code JComboBox}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetObject the target object
     * @param targetJComboBoxProperty a property on the target object that resolves to a {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     * @throws IllegalArgumentException if {@code targetJComboBoxProperty} is {@code null}
     */
    public static <E, TS> JComboBoxBinding<E, List<E>, TS> createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            TS targetObject,
            Property<TS, ? extends JComboBox> targetJComboBoxProperty) {
        return new JComboBoxBinding<E, List<E>, TS>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetObject,
                targetJComboBoxProperty, null);
    }

    /**
     * Creates a named {@code JComboBoxBinding} from a direct reference to a {@code List} and an object and property that resolves to a {@code JComboBox}.
     *
     * @param strategy the update strategy
     * @param sourceList the source {@code List}
     * @param targetObject the target object
     * @param targetJComboBoxProperty a property on the target object that resolves to a {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     * @throws IllegalArgumentException if {@code targetJComboBoxProperty} is {@code null}
     */
    public static <E, TS> JComboBoxBinding<E, List<E>, TS> createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, List<E> sourceList,
            TS targetObject,
            Property<TS, ? extends JComboBox> targetJComboBoxProperty, String name) {
        return new JComboBoxBinding<E, List<E>, TS>(strategy, sourceList,
                ObjectProperty.<List<E>>create(), targetObject,
                targetJComboBoxProperty, name);
    }

    /**
     * Creates a {@code JComboBoxBinding} from an object and property that resolves to a {@code List} and an object and property that resolves to a {@code JComboBox}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetObject the target object
     * @param targetJComboBoxProperty a property on the target object that resolves to a {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} or {@code targetJComboBoxProperty} is {@code null}
     */
    public static <E, SS, TS> JComboBoxBinding<E, SS, TS> createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, TS targetObject,
            Property<TS, ? extends JComboBox> targetJComboBoxProperty) {
        return new JComboBoxBinding<E, SS, TS>(strategy, sourceObject,
                sourceListProperty, targetObject, targetJComboBoxProperty, null);
    }

    /**
     * Creates a named {@code JComboBoxBinding} from an object and property that resolves to a {@code List} and an object and property that resolves to a {@code JComboBox}.
     *
     * @param strategy the update strategy
     * @param sourceObject the source object
     * @param sourceListProperty a property on the source object that resolves to a {@code List}
     * @param targetObject the target object
     * @param targetJComboBoxProperty a property on the target object that resolves to a {@code JComboBox}
     * @return the {@code JComboBoxBinding}
     * @throws IllegalArgumentException if {@code sourceListProperty} or {@code targetJComboBoxProperty} is {@code null}
     */
    public static <E, SS, TS> JComboBoxBinding<E, SS, TS> createJComboBoxBinding(
            AutoBinding.UpdateStrategy strategy, SS sourceObject,
            Property<SS, List<E>> sourceListProperty, TS targetObject,
            Property<TS, ? extends JComboBox> targetJComboBoxProperty, String name) {
        return new JComboBoxBinding<E, SS, TS>(strategy, sourceObject,
                sourceListProperty, targetObject, targetJComboBoxProperty, name);
    }
}

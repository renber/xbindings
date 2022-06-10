package org.jdesktop.xbindings.templating;

import java.awt.Component;
import java.awt.Container;
import java.awt.Label;
import java.awt.LayoutManager;

import javax.swing.JLabel;

import org.jdesktop.xbindings.context.DataContext;

/**
 * The default TemplatingControlFactory used by ItemsControl and
 * ContentPresenter which creates a label for each child using the objects
 * toString() method for the label text or if the value itself is a Component just
 * returns this
 * 
 * @author renber
 */
class DefaultControlFactory implements TemplatingControlFactory {

	@Override
	public Component create(Container parent, DataContext itemDataContext) {
		Object v = itemDataContext.getValue();

		if (v instanceof Component) {			
			return (Component)v;
		} else {			
			JLabel lbl = new JLabel(v != null ? v.toString() : "");
			// inherit foreground color from container
			lbl.setForeground(null);
			return lbl;
		}
	}

	@Override
	public Object getLayoutData(LayoutManager parentLayout, Component itemControl, DataContext itemDataContext) {
		return null;
	}
}

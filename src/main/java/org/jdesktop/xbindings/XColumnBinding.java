package org.jdesktop.xbindings;

import java.awt.DisplayMode;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Converter;
import org.jdesktop.beansbinding.PropertyHelper;

/**
 * Represents a bound column for the XTableBinding
 * @author berre
 */
public class XColumnBinding {

	private String headerText;

	// Property to show in cell
	private PropertyHelper displayMember;
	
	// Converter to convert the property value to a display value
	private Converter displayValueConverter;

	private Class<?> cellClass;

	/**
	 * Create a new column binding descriptor (cell value will determined by calling the rowObject's toString() method)
	 * @param _headerText The text to show in the column header
	 */
	public XColumnBinding(String _headerText) {
		this(_headerText, null, String.class);
	}	
	
	/**
	 * Create a new column binding descriptor (cell value will be the row object itself)
	 * @param _headerText The text to show in the column header
	 */
	public XColumnBinding(String _headerText, Class<?> _cellClass) {
		this(_headerText, null, _cellClass);
	}	
	
	/**
	 * Create a new column binding descriptor
	 * @param _headerText The text to show in the column header
	 * @param _displayMember The property of row items to use in cells of this column
	 */
	public XColumnBinding(String _headerText, PropertyHelper _displayMember) {
		this(_headerText, _displayMember, String.class);
	}

	/**
	 * Create a new column binding descriptor
	 * @param _headerText The text to show in the column header
	 * @param _displayMember The property of row items to use in cells of this column
	 * @param _cellClass The object class of cells in this column
	 */
	public XColumnBinding(String _headerText, PropertyHelper _displayMember, Class<?> _cellClass) {			
		this(_headerText, _displayMember, _cellClass, null);
	}
	
	/**
	 * Create a new column binding descriptor
	 * @param _headerText The text to show in the column header
	 * @param _displayMember The property of row items to use in cells of this column
	 * @param _cellClass The object class of cells in this column
	 * @param _converter The converter to convert the value of displayMember to a display value
	 */
	public <TCell> XColumnBinding(String _headerText, PropertyHelper _displayMember, Class<TCell> _cellClass, Converter<?,TCell> _converter) {
		headerText = _headerText;
		displayMember = _displayMember;
		cellClass = _cellClass;
		displayValueConverter = _converter;
	}

	/**
	 * Return the header text for this column binding
	 * @return
	 */
	public String getHeaderText() {
		return headerText;
	}

	/**
	 * Get the display value for the given item for a cell in this column
	 * @param rowItem The object to get the display value from
	 * @return display value
	 */
	public Object getValue(Object rowItem) {
		if (displayMember != null) {			
			if (displayValueConverter != null)
				return displayValueConverter.convertForward(displayMember.getValue(rowItem));
			
			return displayMember.getValue(rowItem);
		}
		
		if (displayValueConverter != null)
			return displayValueConverter.convertForward(rowItem);
			
		return rowItem;
	}

	/**
	 * Returns if a cell of the given object in this column can be written to 
	 * @param rowItem
	 * @return
	 */
	public boolean isWritable(Object rowItem) {
		if (displayValueConverter == null && displayMember != null)
			return displayMember.isWriteable(rowItem);				
		
		return false;
	}

	/**
	 * Sets the value described by this column of the given object
	 * @param rowItem
	 * @param newValue
	 */
	public void setValue(Object rowItem, Object newValue) {
		if (displayValueConverter == null && displayMember != null)
			displayMember.setValue(rowItem, newValue);
	}

	/**
	 * Return the class type of the values in this column's cells
	 */
	public Class<?> getCellClass() {
		return cellClass;
	}

}

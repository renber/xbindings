package org.jdesktop.xbindings;

/**
 * Base interface for binding objects,
 * declaring functions needed by all Binding Objects 
 * @author berre
 *
 */
public interface XBinding {

	/**
	 * Establish the binding
	 */
	public void bind();
	/**
	 * Remove the binding
	 */
	public void unbind();

	/**
	 * Return if the binding has been established and is currently active	 
	 */
	public boolean isBound();

}

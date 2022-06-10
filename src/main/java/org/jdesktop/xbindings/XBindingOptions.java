package org.jdesktop.xbindings;

/**
 * Provides teh ability to enable/disable certain features of XBindings
 */
public class XBindingOptions {

    private static XBindingOptions instance = new XBindingOptions();

    private boolean enableXProperties = false;

    /**
     * Returns if the usage of XProperty classes has been enabled
     */
    public boolean areXPropertiesEnabled() {
        return enableXProperties;
    }

    /**
     * Enable or disable the usage of XProperty classes
     */
    public void setXPropertiesEnabled(boolean value) {
        enableXProperties = value;
    }

    /**
     * Return the XBindingOptions which are valid in the current context
     */
    public static XBindingOptions getActive() {
        return instance;
    }

    private XBindingOptions() {
        // --
    }

}

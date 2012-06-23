package net.pms.plugins;

/**
 * Plugins should never implement {@link PluginBase} directly because they won't
 * get loaded in the GUI. Use this interface instead if no additional
 * functionality is required.
 * 
 * @author pw
 * 
 */
public interface Plugin extends PluginBase {

}

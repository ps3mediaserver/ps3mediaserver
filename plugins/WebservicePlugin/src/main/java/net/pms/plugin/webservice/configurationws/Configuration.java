package net.pms.plugin.webservice.configurationws;

import java.util.List;

import net.pms.plugin.webservice.InvalidParameterException;

public interface Configuration {

	/**
	 * Get a configuration value by specifying the key
	 * 
	 * @param key
	 *            a valid parameter key
	 * @throws InvalidParameterException
	 *             if the specified key can't be found in the list of parameters
	 * @return the value corresponding to the key
	 */
	public String getValue(String key) throws InvalidParameterException;

	/**
	 * Sets a value for a key
	 * 
	 * @param key
	 *            a valid parameter key
	 * @param value
	 *            the value to set
	 * @throws InvalidParameterException
	 *             if either the key or the associated value isn't valid
	 */
	public void setValue(String key, String value)
			throws InvalidParameterException;

	/**
	 * Save the current configuration to file
	 * 
	 * @throws SaveException
	 *             thrown if an error occurs while trying to save the
	 *             configuration
	 */
	public void saveConfiguration() throws SaveException;

	/**
	 * Get the list of all available parameters to be used as keys in
	 * get/setValue
	 * 
	 * @return list of parameters
	 */
	public List<String> getParameters();
}

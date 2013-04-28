package net.pms.plugin.webservice.configurationws;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

import net.pms.PMS;
import net.pms.configuration.PmsConfiguration;
import net.pms.plugin.webservice.InvalidParameterException;
import net.pms.plugin.webservice.ServiceBase;

import org.apache.commons.configuration.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebService(serviceName = "Configure", targetNamespace = "http://ps3mediaserver.org/configure")
public class ConfigurationWebService extends ServiceBase implements
		Configuration {
	private static final Logger log = LoggerFactory.getLogger(ConfigurationWebService.class);

	@Override
	@WebMethod(operationName = "getValue")
	public String getValue(@WebParam(name = "key") String key)
			throws InvalidParameterException {
		if (!isInitialized) {
			log.warn("Trying to access getValue when it's not initialized. Abort");
			return null;
		}

		ConfigurationValue enumKey = getConfigurationValue(key);
		PmsConfiguration conf = PMS.getConfiguration();

		// TODO: add all the possible configuration values
		String res = "";
		switch (enumKey) {
		case Language:
			res = conf.getLanguage();
			break;
		default:
			throw new InvalidParameterException(String.format(
					"Retrieval of key %s hasn't been implemented yet", key));
		}
		return res;
	}

	@Override
	@WebMethod(operationName = "saveConfiguration")
	public void saveConfiguration() throws SaveException {
		if (!isInitialized) {
			log.warn("Trying to access saveConfiguration when it's not initialized. Abort");
			return;
		}

		try {
			PMS.getConfiguration().save();
		} catch (ConfigurationException e) {
			String msg = "Failed to save pms configuration";
			log.error(msg, e);
			throw new SaveException(msg, e);
		}
	}

	@Override
	@WebMethod(operationName = "setValue")
	public void setValue(@WebParam(name = "key") String key,
			@WebParam(name = "value") String value)
			throws InvalidParameterException {
		if (!isInitialized) {
			log.warn("Trying to access setValue when it's not initialized. Abort");
			return;
		}

		ConfigurationValue enumKey = getConfigurationValue(key);
		PmsConfiguration conf = PMS.getConfiguration();

		// TODO: add all the possible configuration values
		switch (enumKey) {
		case Language:
			conf.setLanguage(value);
			break;
		default:
			throw new InvalidParameterException(String.format(
					"Setting of key %s hasn't been implemented yet", key));
		}
	}

	@Override
	@WebMethod(operationName = "getParameters")
	public List<String> getParameters() {
		if (!isInitialized) {
			log.warn("Trying to access getParameters when it's not initialized. Abort");
			return null;
		}

		ArrayList<String> params = new ArrayList<String>();
		for (ConfigurationValue val : ConfigurationValue.values()) {
			params.add(val.name());
		}
		return params;
	}

	private ConfigurationValue getConfigurationValue(String key)
			throws InvalidParameterException {
		try {
			// try to convert string to ConfigurationValue
			return Enum.valueOf(ConfigurationValue.class, key);
		} catch (IllegalArgumentException ex) {
			// give a clear error message if conversion fails
			throw new InvalidParameterException(
					String.format(
							"Key %s doesn't exist in the list of parameters. Valid parameters are: %s",
							key, getParametersString()));
		}
	}

	private String getParametersString() {
		String validParams = "";
		for (String param : getParameters()) {
			validParams += param + ", ";
		}
		validParams = validParams.substring(0, validParams.length() - 2);
		return validParams;
	}
}

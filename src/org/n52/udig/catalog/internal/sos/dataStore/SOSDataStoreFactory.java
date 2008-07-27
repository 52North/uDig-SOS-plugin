/**********************************************************************************
Copyright (C) 2008
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under
the terms of the GNU General Public License version 2 as published by the
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

Author: Carsten Priess
Created: 15.01.2008
 *********************************************************************************/
package org.n52.udig.catalog.internal.sos.dataStore;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.ParameterConfiguration;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;
import org.n52.udig.catalog.internal.sos.ui.SOSPreferencePage;
import org.n52.udig.catalog.sos.internal.Messages;

public class SOSDataStoreFactory extends AbstractDataStoreFactory {

	public static class UDIGSOSDataStore extends SOSDataStore {
		/**
		 * the connection- and request-parameters
		 */
		private final Map<String, Serializable> params;

		/**
		 * Creates a new instance of {@link UDIGSOSDataStore} using
		 * {@link SOSDataStore}
		 * 
		 * @param params
		 *            the connection- and request-parameters
		 * @throws IOException
		 */
		UDIGSOSDataStore(final Map<String, Serializable> params)
				throws IOException {
			super(params);
			this.params = params;
		}

		/**
		 * Returns the capabilities for this Datastore
		 * 
		 * @return an instance of capabilities for this Datastore/ServiceURL
		 * @throws IOException
		 */
		public SOSCapabilities getCapabilities() throws IOException {
			return SOSDataStoreFactory.getInstance().getCapabilities(params);
		}
	}

	/**
	 * Singleton Pattern: Current instance of this {@link SOSDataStoreFactory}
	 * 
	 * @see SOSDataStoreFactory#getInstance()
	 */
	private static SOSDataStoreFactory instance = null;

	/**
	 * The Apache Log4J-Logger for this class. Use
	 * {@link GeneralConfigurationRegistry} to configure the logger.
	 * 
	 * @see GeneralConfigurationRegistry#getLog4jPropertiesFilename()
	 * @see SOSPreferencePage
	 */
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSDataStoreFactory.class);

	/**
	 * Represents an operation which should be sent to service
	 * 
	 * @see SOSOperations
	 */
	public static final Param OPERATION = new Param(
			"SOSDataStoreFactory:OPERATION", String.class,
			"Represents an operation which should be sent to service", true);
	/**
	 * The parameter configuration contains all parameters that should be send
	 * to the SOS.
	 * 
	 * @see SOSDataStoreFactory#getCapabilities(Map)
	 * @see SOSCapabilities#getOperations()
	 * @see SOSOperationType#getNewPreconfiguredConfiguration()
	 */
	public static final Param PARAMETERS = new Param(
			"SOSDataStoreFactory:PARAMETERCONFIGURATION",
			ParameterConfiguration.class, "", true);
	/**
	 * This parameter should contain the url the SOS may be found at
	 */
	public static final Param URL_SERVICE = new Param(
			"SOSDataStoreFactory:SERVICE_URL", URL.class,
			"Represents a basic URL to the server instance.", true);
	/**
	 * This parameter defines the service version of the sos
	 * 
	 * @see SOSDataStoreFactory#getSupportedVersions(URL)
	 * @see SOSDataStoreFactory#guessServiceVersion(URL)
	 */
	public static final Param SERVICE_VERSION = new Param(
			"SOSDataStoreFactory:SERVICE_VERSION", String.class,
			"Represents SOS-ServiceVersion which should be used for requests.",
			true);

	/**
	 * String representing a 0.0.0 SOS implementation
	 * 
	 * @see SOSDataStoreFactory#SERVICE_VERSION
	 */
	public static final String serviceVersion_000 = "0.0.0";

	/**
	 * String representing a 1.0.0 SOS implementation
	 * 
	 * @see SOSDataStoreFactory#SERVICE_VERSION
	 */
	public static final String serviceVersion_100 = "1.0.0";

	/**
	 * All supported ServiceVersions
	 */
	public static final String[] supportedVersions = { serviceVersion_000,
			serviceVersion_100 };

	/**
	 * Singleton pattern; returns the instance of this DataStoreFactory or
	 * creates a new one
	 * 
	 * @return current instance of SOSDataStoreFactory, or a new one if none
	 *         exists
	 */
	public static SOSDataStoreFactory getInstance() {
		if (instance == null) {
			instance = new SOSDataStoreFactory();
		}
		return instance;
	}

	/**
	 * Takes a small look to our parameters and tries to fix some obvious errors
	 * 
	 * @param params
	 *            the connection- and requestparameters
	 * @return a Map with fixed parameters, or null if an error occures
	 */
	public static Map<String, Serializable> workOnParams(
			final Map<String, Serializable> params) {
		if (params != null) {
			// final URL url_CAPS = (URL) params
			// .get(SOSDataStoreFactory.URL_CAPS.key);
			// if (url_CAPS != null) {
			// if (!params.containsKey(SOSDataStoreFactory.URL_SERVICE.key)) {
			// final String url_caps_s = url_CAPS.toExternalForm();
			// try {
			// // maybe the caps url is the service url?
			// if (url_caps_s.contains("?")) {
			// // no it isn't
			// params.put(SOSDataStoreFactory.URL_SERVICE.key,
			// new URL(url_caps_s.substring(0, url_caps_s
			// .indexOf('?'))));
			// } else {
			// // assume URL_CAPS == URL_service
			// params.put(SOSDataStoreFactory.URL_SERVICE.key,
			// new URL(url_caps_s));
			// }
			// } catch (final Exception e) {
			// e.printStackTrace();
			// }
			// }
			// }
			if (params.containsKey(SOSDataStoreFactory.PARAMETERS.key)
					&& params.get(SOSDataStoreFactory.PARAMETERS.key) != null) {
				if (params.get(SOSDataStoreFactory.PARAMETERS.key).getClass()
						.isAssignableFrom(String.class)) {
					LOGGER.warn(params.get(SOSDataStoreFactory.PARAMETERS.key));
					params.put(SOSDataStoreFactory.PARAMETERS.key,
							new ParameterConfiguration((String) params
									.get(SOSDataStoreFactory.PARAMETERS.key)));
				}
			}
			return params;
		}
		return null;
	}

	// public static final String serviceVersion = serviceVersion_100;
	private final HashMap<Map<String, Serializable>, SOSDataStore> dsCache = new HashMap<Map<String, Serializable>, SOSDataStore>();

	// Map<URL, List<String>> supportedVersionsCache = new HashMap<URL,
	// List<String>>();

	/**
	 * Creates a new instance of {@link SOSDataStoreFactory} calls parent
	 * constructor Singleton Pattern
	 * 
	 * @see SOSDataStoreFactory#getInstance()
	 */
	private SOSDataStoreFactory() {
		super();
	}

	@Override
	public boolean canProcess(final Map params) {
		if (params == null) {
			return false;
		}
		// if (!params.containsKey(URL_CAPS.key)
		// && !params.containsKey(URL_SERVICE.key)) {
		// return false;
		if (!params.containsKey(URL_SERVICE.key)) {
			return false;
		}
		if (!params.containsKey(SERVICE_VERSION.key)) {
			try {
				final List<String> versions = getSupportedVersions((URL) params
						.get(URL_SERVICE.key));
				if (versions == null || versions.size() == 0) {
					LOGGER
							.warn("No service version set, and I cannot guess one");
				} else {
					LOGGER.info("No service version set, guessing "
							+ versions.get(0));
					params.put(SERVICE_VERSION.key, versions.get(0));
				}
			} catch (final Exception e) {
				LOGGER.error(e);
			}
		}

		if (!params.containsKey(OPERATION.key)) {
			LOGGER.warn("No operation selected, cannot proceed");
			return false;
		}
		if (!params.containsKey(PARAMETERS.key)) {
			// TODO check PARAMETERS-content
			return false;
		}
		return true;
	}

	/**
	 * Checks if a service can be found at serviceUrl and if this is a SOS. Uses
	 * internal caching.
	 * 
	 * @param serviceURL
	 *            the URL where the service can be found
	 * @return a primitive boolean; true if service is a SOS, false if not
	 * @throws IOException
	 *             connection problems or a error which occured when parsing the
	 *             response
	 */
	public boolean checkForSOS(final URL serviceURL) throws IOException {

		return !getSupportedVersions(serviceURL).isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
	 */
	public DataStore createDataStore(final Map params) throws IOException {
		if (canProcess(params)) {
			// for (final Param p : getParametersInfo()) {
			// if (!params.containsKey(p.key)) {
			// // TODO throw Exception ... missing argument or sth. like this
			// }
			// }

			if (params.containsKey(PARAMETERS)) {
				if (params.get(PARAMETERS.key).getClass().isAssignableFrom(
						String.class)) {
				}
			}
			if (dsCache.containsKey(params)) {
				if (dsCache.get(params).getLastUpdated()
						- System.currentTimeMillis() > GeneralConfigurationRegistry
						.getInstance().getTimeToCacheDatastore()) {
					LOGGER
							.debug(
									"Cache hit, however max. cachetime exceeded, creating new instance",
									null);
					return createNewDataStore(params);
				}
				LOGGER.debug("Cache hit!", null);
				return dsCache.get(params);
			}
			return createNewDataStore(params);
		} else {
			throw new IOException(
					"Sorry, the parameters you delivered are not sufficient.");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
	 */
	public DataStore createNewDataStore(final Map params) throws IOException {
		if (canProcess(params)) {
			// for (final Param p : getParametersInfo()) {
			// }
			// URL host = null;
			// if (params.containsKey(URL_CAPS.key)) {
			// host = (URL) URL_CAPS.lookUp(params);
			// }

			final SOSDataStore lastDatastore = new UDIGSOSDataStore(params);
			dsCache.put(params, lastDatastore);
			return lastDatastore;
		} else {
			throw new IOException(
					"Sorry, the parameters you delivered are not sufficient.");
		}
	}

	/**
	 * Returns the capabilities for the SOS at the serviceURL defined in params
	 * 
	 * @param params
	 * @return an instance of capabilities for SOS at the serviceURL defined in
	 *         params
	 * @throws IOException
	 */
	public SOSCapabilities getCapabilities(
			final Map<String, Serializable> params) throws IOException {
		// serviceVersion delivered?
		if (params.containsKey(SOSDataStoreFactory.SERVICE_VERSION.key)) {
			return getCapabilities((URL) params.get(URL_SERVICE.key),
					(String) params
							.get(SOSDataStoreFactory.SERVICE_VERSION.key));
		} else {
			final String serviceV = guessServiceVersion((URL) params
					.get(URL_SERVICE.key));
			if (serviceV != null) {
				return getCapabilities((URL) params.get(URL_SERVICE.key),
						serviceV);
			}
		}
		throw new IOException("Missing ServiceVersion");
	}

	/**
	 * Get the capabilities for the SOS which can be found at URL
	 * 
	 * @see SOSDataStoreFactory#SERVICE_VERSION
	 * @param serviceURL
	 *            the URL the SOS can be found at
	 * @param serviceVersion
	 *            the SOSServiceVersion
	 * @return an instance of SOSCapabilities
	 * @throws IOException
	 *             connection problems or a error which occured during parsing
	 */
	public SOSCapabilities getCapabilities(final URL serviceURL,
			final String serviceVersion) throws IOException {
		return SOSCapabilities.getCapabilities(serviceURL);
	}

	/**
	 * returns the DataStoreFactory`s description as String
	 */
	public String getDescription() {
		return Messages.SOSDataStoreFactory_desc;
	}

	@Override
	public String getDisplayName() {
		return Messages.SOSDataStoreFactory_name;
	}

	@Override
	public Map getImplementationHints() {
		return super.getImplementationHints();
	}

	public Param[] getParametersInfo() {
		return new Param[] { URL_SERVICE, OPERATION, SERVICE_VERSION,
				PARAMETERS };
	}

	/**
	 * Checks which service versions can be used with this service, take care:
	 * if the service found at serviceURL is not useable, an empty List will be
	 * returned. Uses internal caching.
	 * 
	 * @param serviceURL
	 *            serviceURL The URL where the service can be found.
	 * @return a list with serviceVersions as Strings. see:
	 *         {@link SOSDataStoreFactory#serviceVersion_000} and
	 *         {@link SOSDataStoreFactory#serviceVersion_100}
	 * @throws IOException
	 *             connection problems or a error which occured when parsing the
	 *             response
	 */
	public List<String> getSupportedVersions(final URL serviceURL)
			throws IOException {
		// if (!supportedVersionsCache.containsKey(serviceURL)) {
		// supportedVersionsCache.put(serviceURL, SOSCapabilities
		// .checkForSOS(serviceURL));
		// }
		// return supportedVersionsCache.get(serviceURL);
		return SOSCapabilities.checkForSOS(serviceURL);
	}

	/**
	 * This method tries to guess a usable serviceversion with help of
	 * capabilities, if more than one version is available
	 * {@link SOSDataStoreFactory#getPreferredServiceVersion(List)} is used.
	 * 
	 * @param service_url
	 *            the URL where the service can be found
	 * @return a String with the guessed serviceversion or null if no guess is
	 *         possible
	 * @throws IOException
	 * 
	 * @see SOSDataStoreFactory#SERVICE_VERSION
	 */
	public String guessServiceVersion(final URL service_url) throws IOException {
		// try to find a proper serviceVersion
		final List<String> versions = getSupportedVersions(service_url);
		return getPreferredServiceVersion(versions);

	}

	/**
	 * Returns the first available SOS-Version String from versions in this
	 * order (1.) version set as preferred Version in
	 * {@link GeneralConfigurationRegistry#getPreferedSOSVersion()} (2.) 1.0.0
	 * (3.) 0.0.0 (4.) any other version in versions (last chance, don`t think
	 * this will work)
	 * 
	 * 
	 * @param versions
	 *            available versions to select from
	 * @return the selected version
	 */
	public String getPreferredServiceVersion(final List<String> versions) {
		if (versions != null) {
			if (!versions.isEmpty()) {
				String serviceV = null;
				if (versions.contains(GeneralConfigurationRegistry
						.getInstance().getPreferedSOSVersion())) {
					serviceV = GeneralConfigurationRegistry.getInstance()
							.getPreferedSOSVersion();
				}
				// prefer 1.0.0 SOS
				if (versions.contains(serviceVersion_100)) {
					serviceV = serviceVersion_100;
					// check for 0.0.0 SOS
				} else if (versions.contains(serviceVersion_000)) {
					serviceV = serviceVersion_000;
				} else {
					// running out of ideas
					if (versions.get(0) != null) {
						serviceV = versions.get(0);
					}
				}
				return serviceV;
			}
		}
		return null;
	}

	@Override
	public boolean isAvailable() {
		return true;
	}
}
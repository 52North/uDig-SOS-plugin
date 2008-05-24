/***************************************************************
Copyright © 2007 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Carsten Priess, 52°N

 Contact: Andreas Wytzisk,
 52°North Initiative for Geospatial Open Source SoftwareGmbH,
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany,
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.udig.catalog.internal.sos.dataStore;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataStore;
import org.n52.oxf.serviceAdapters.sos.SOSAdapter;
import org.n52.udig.catalog.internal.sos.SOSPlugin;
import org.n52.udig.catalog.internal.sos.dataStore.config.ParameterConfiguration;
import org.n52.udig.catalog.sos.internal.Messages;

public class SOSDataStoreFactory extends AbstractDataStoreFactory {

	private static SOSDataStoreFactory instance = null;
	// private UDIGSOSDataStore lastDatastore = null;
	public static final String serviceVersion_000 = "0.0.0";
	public static final String serviceVersion_100 = "1.0.0";
	public static final String serviceVersion = serviceVersion_100;
	private final HashMap<Map<String, Serializable>, SOSDataStore> dsCache = new HashMap<Map<String, Serializable>, SOSDataStore>();
	private final HashMap<URL, SOSCapabilities> capsCache = new HashMap<URL, SOSCapabilities>();

	/**
	 * Creates a new instance of {@link SOSDataStoreFactory} Singleton Pattern
	 */
	private SOSDataStoreFactory() {
		super();
	}

	public static Map<String, Serializable> workOnParams(
			final Map<String, Serializable> params) {
		if (params != null) {
			final URL url_CAPS = (URL) params
					.get(SOSDataStoreFactory.URL_CAPS.key);
			if (url_CAPS != null) {
				if (!params.containsKey(SOSDataStoreFactory.URL_SERVICE.key)) {
					final String url_caps_s = url_CAPS.toExternalForm();
					try {
						// maybe the caps url is the service url?
						if (url_caps_s.contains("?")) {
							// no it isn't
							params.put(SOSDataStoreFactory.URL_SERVICE.key,
									new URL(url_caps_s.substring(0, url_caps_s
											.indexOf('?'))));
						} else {
							// assume URL_CAPS == URL_service
							params.put(SOSDataStoreFactory.URL_SERVICE.key,
									new URL(url_caps_s));
						}
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
			return params;
		}
		return null;
	}

	// XXX if any parameters are changed getParametersInfo has to be updated
	public static final Param URL_CAPS = new Param(
			"SOSDataStoreFactory:GET_CAPABILITIES_URL",
			URL.class,
			"Represents a URL to the getCapabilities document or a server instance.",
			true);

	public static final Param PORT = new Param(
			"SOSDataStoreFactory:GET_CAPABILITIES_PORT", Integer.class,
			"Represents the port the server instance is reachable", true);

	public static final Param URL_SERVICE = new Param(
			"SOSDataStoreFactory:SERVICE_URL", URL.class,
			"Represents a basic URL to the server instance.", true);

	public static final Param OPERATION = new Param(
			"SOSDataStoreFactory:OPERATION", String.class,
			"Represents an operation which should be sent to service", true);

	public static final Param PARAMETERS = new Param(
			"SOSDataStoreFactory:PARAMETERCONFIGURATION",
			ParameterConfiguration.class, "", true);

	/**
	 * Singleton pattern; returns the instance of this DataStoreFactory
	 * 
	 * @return current instance of SOSDataStoreFactory
	 */
	public static SOSDataStoreFactory getInstance() {
		if (instance == null) {
			instance = new SOSDataStoreFactory();
		}
		return instance;
	}

	@Override
	public boolean canProcess(final Map params) {
		if (params == null) {
			return false;
		}
		if (!params.containsKey(URL_CAPS.key)
				&& !params.containsKey(URL_SERVICE.key)) {
			return false;
		}
		if (!params.containsKey(OPERATION.key)) {
			return false;
		}
		if (!params.containsKey(PARAMETERS.key)) {
			return false;
		}

		// TODO check PARAMETERS-content

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.DataStoreFactorySpi#createDataStore(java.util.Map)
	 */
	public DataStore createDataStore(final Map params) throws IOException {
		for (final Param p : getParametersInfo()) {
			if (!params.containsKey(p.key)) {
				// TODO throw Exception ... missing argument or sth. like this
			}
		}
		if (dsCache.containsKey(params)) {
			SOSPlugin.trace("Cache hit!", null);
			return dsCache.get(params);
		}
		return createNewDataStore(params);
	}

	// private void removeFromCache(final SOSDataStore ds) {
	// for (DataStore cached : dsCache.values()) {
	// if (cached == ds) {
	// cached = null;
	// return;
	// }
	// }
	// }

	// private void addToCache(final Map<String, Serializable> params, final
	// SOSDataStore ds) {
	// dsCache.put(params, ds);
	// }

	public DataStore createNewDataStore(final Map params) throws IOException {
		for (final Param p : getParametersInfo()) {
			if (!params.containsKey(p.key)) {
				// TODO throw Exception ... missing argument or sth. like this
			}
		}
		URL host = null;
		if (params.containsKey(URL_CAPS.key)) {
			host = (URL) URL_CAPS.lookUp(params);
		}

		final SOSDataStore lastDatastore = new UDIGSOSDataStore(params);
		dsCache.put(params, lastDatastore);
		return lastDatastore;
	}

	public String getDescription() {
		return Messages.SOSDataStoreFactory_desc;
	}

	@Override
	public String getDisplayName() {
		return Messages.SOSDataStoreFactory_name;
	}

	public Param[] getParametersInfo() {
		return new Param[] { URL_SERVICE, OPERATION, PARAMETERS };
	}

	@Override
	public boolean isAvailable() {
		// TODO check for installed oxf or other libs
		return true;
	}

	@Override
	public Map getImplementationHints() {
		return super.getImplementationHints();
	}

	public SOSCapabilities getCapabilities(
			final Map<String, Serializable> params) throws IOException {
		return getCapabilities((URL) params.get(URL_SERVICE.key));
	}

	public SOSCapabilities getCapabilities(final URL serviceURL)
			throws IOException {
		if (!capsCache.containsKey(serviceURL)) {
			capsCache.put(serviceURL, new SOSCapabilities(new SOSAdapter(
					serviceVersion), serviceURL));
		}
		return capsCache.get(serviceURL);
	}

	public static class UDIGSOSDataStore extends SOSDataStore {
		Map params;

		// /**
		// * Construct <code>UDIGWFSDataStore</code>.
		// *
		// * @param arg0
		// * @param arg1
		// * @param arg2
		// * @param arg3
		// * @param arg4
		// * @param arg5
		// * @param arg6
		// * @throws SAXException
		// * @throws IOException
		// */
		// UDIGSOSDataStore(URL host, Boolean protocol, String username,
		// String password, int timeout, int buffer) throws IOException {
		// super(host, protocol, username, password, timeout, buffer);
		// }

		UDIGSOSDataStore(final Map params) throws IOException {
			super(params);
			this.params = params;
		}

		public SOSCapabilities getCapabilities() throws IOException {
			return SOSDataStoreFactory.getInstance().getCapabilities(params);
		}
	}
}
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
import java.util.Map;

import javax.sql.DataSource;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureType;
import org.n52.oxf.serviceAdapters.sos.SOSAdapter;
import org.n52.udig.catalog.internal.sos.dataStore.config.ParameterConfiguration;

import com.vividsolutions.jts.geom.Geometry;

/**
 * This class represents a Sensor Observation Service DataStore, see Geotools
 * documentation for concepts of {@link DataStore} and {@link DataSource}
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSDataStore extends AbstractDataStore {

	/**
	 * the SOSAdapter is used to communicate with help of the OXFramework with
	 * the Sensor Observation Service
	 */
	private SOSAdapter adapter = null;
	/**
	 * The SOSCapabilities for the current DataStore
	 */
	private final SOSCapabilities capabilities;
	/**
	 * the SOS service version
	 * 
	 * @see SOSDataStoreFactory#supportedVersions
	 */
	private String serviceVersion = null;

	/**
	 * A cache that contains featureReaders to reduce the number of requests to
	 * the SOS.
	 */
	private final Map<String, SOSFeatureReader> featureReaderCache = new HashMap<String, SOSFeatureReader>();

	private final Map<String, FeatureType> featureTypeCache = new HashMap<String, FeatureType>();
	private Map<String, Serializable> params;
	private final String[] typeNames = null;
	/**
	 * @see SOSDataStore#getLastUpdated()
	 * @see SOSDataStore#setLastUpdated(long)
	 */
	private long lastUpdated;

	private SOSDataStore() throws IOException {
		super(false);
		capabilities = SOSDataStoreFactory.getInstance()
				.getCapabilities(params);
		// generateCapabilities();
	}

	/**
	 * Returns the OXF-SOS-Serviceadapter used for connections of this datastore
	 * 
	 * @return the adapter
	 */
	protected SOSAdapter getAdapter() {
		return adapter;
	}

	/**
	 * 
	 * @param isWriteable
	 */
	// XXX this cannot work; where are the params from?
	private SOSDataStore(final boolean isWriteable) throws IOException {
		super(isWriteable);
		capabilities = SOSDataStoreFactory.getInstance()
				.getCapabilities(params);
		adapter = new SOSAdapter((String) params
				.get(SOSDataStoreFactory.SERVICE_VERSION.key));
		lastUpdated = System.currentTimeMillis();
	}

	/**
	 * Creates a new instance of {@link SOSDataStore}. Called by
	 * {@link SOSDataStoreFactory#createDataStore(Map)} or
	 * {@link SOSDataStoreFactory#createNewDataStore(Map)}
	 * 
	 * @param params
	 *            the parameters for this SOSDataStore
	 * @throws IOException
	 *             connection and or network errors
	 * @see SOSDataStoreFactory#PARAMETERS
	 */
	public SOSDataStore(Map<String, Serializable> params) throws IOException {
		super(false);
		this.params = params;

		// TODO check params and throw Exception
		params = SOSDataStoreFactory.workOnParams(params);
		if (!params.containsKey(SOSDataStoreFactory.SERVICE_VERSION.key)) {
			serviceVersion = SOSDataStoreFactory.getInstance()
					.guessServiceVersion(
							(URL) params
									.get(SOSDataStoreFactory.URL_SERVICE.key));
		} else {
			serviceVersion = (String) params
					.get(SOSDataStoreFactory.SERVICE_VERSION.key);
		}
		adapter = new SOSAdapter(serviceVersion);
		capabilities = SOSDataStoreFactory.getInstance()
				.getCapabilities(params);
		lastUpdated = System.currentTimeMillis();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.AbstractDataStore#getFeatureReader(java.lang.String)
	 */
	@Override
	protected FeatureReader getFeatureReader(final String typeName)
			throws IOException {
		if (!featureReaderCache.containsKey(typeName)) {
			featureReaderCache.put(typeName, new SOSFeatureReader(params,
					adapter, typeName));
		} else if (featureReaderCache.get(typeName).isUsed()) {
			featureReaderCache.put(typeName, new SOSFeatureReader(params,
					adapter, typeName));
		}
		return featureReaderCache.get(typeName);
	}

	@Override
	public FeatureType getSchema(final String typeName) throws IOException {
		if (!featureTypeCache.containsKey(typeName)) {
			featureTypeCache.put(typeName, getFeatureReader(typeName)
					.getFeatureType());
		}
		return featureTypeCache.get(typeName);
	}

	public Geometry getBoundingBox(final String typeName) throws IOException {
		return ((SOSFeatureReader) getFeatureReader(typeName)).getBoundingBox();
	}

	@Override
	public String[] getTypeNames() throws IOException {
		if (typeNames == null) {
			final String op = (String) params
					.get(SOSDataStoreFactory.OPERATION.key);
			
			
			return ((ParameterConfiguration) params
					.get(SOSDataStoreFactory.PARAMETERS.key)).getTypeNames();
			// return ((SOSOperationType) capabilities.getOperations()
			// .getOperationTypeByName(op)).getTypeNames();
		}
		return typeNames;
	}

	/**
	 * Returns the serviceVersion of the SOS represented by this DataStore
	 * 
	 * @see SOSDataStoreFactory#SERVICE_VERSION
	 * @return the serviceVersion as String
	 */
	public String getServiceVersion() {
		return serviceVersion;
	}

	/**
	 * Get the last time this {@link SOSDataStore} was updated. Used for the
	 * caching mechanism in {@link SOSDataStoreFactory#createDataStore(Map)}
	 * 
	 * @return the lastUpdated
	 * @see System#currentTimeMillis()
	 */
	protected long getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * Set the last time this {@link SOSDataStore} was updated. Used for the
	 * caching mechanism in {@link SOSDataStoreFactory#createDataStore(Map)}
	 * 
	 * @param lastUpdated
	 *            the lastUpdated to set
	 * @see System#currentTimeMillis()
	 */
	protected void setLastUpdated(final long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}
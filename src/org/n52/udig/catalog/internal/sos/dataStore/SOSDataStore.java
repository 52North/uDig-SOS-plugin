/***************************************************************
Copyright © 2008 52°North Initiative for Geospatial Open Source Software GmbH

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
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.AbstractDataStore;
import org.geotools.data.FeatureReader;
import org.geotools.feature.FeatureType;
import org.n52.oxf.serviceAdapters.sos.SOSAdapter;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;

/**
 * @author 52n
 *
 */
public class SOSDataStore extends AbstractDataStore {

	private final SOSAdapter adapter = new SOSAdapter(
			SOSDataStoreFactory.serviceVersion);
	// TODO add serviceversion as parameter
	private final SOSCapabilities capabilities;
	private final Map<String, SOSFeatureReader> featureReaderCache = new HashMap<String, SOSFeatureReader>();
	private final Map<String, FeatureType> featureTypeCache = new HashMap<String, FeatureType>();
	private Map<String, Serializable> params;
	private final String[] typeNames = null;


	/**
	 *
	 */
	private SOSDataStore() throws IOException {
		super(false);
		capabilities = SOSDataStoreFactory.getInstance().getCapabilities(params);
//		generateCapabilities();
	}

	protected SOSAdapter getAdapter(){
		return adapter;
	}

	/**
	 * @param isWriteable
	 */
	public SOSDataStore(final boolean isWriteable) throws IOException {
		super(isWriteable);
		capabilities = SOSDataStoreFactory.getInstance().getCapabilities(params);
	}

	public SOSDataStore(Map<String, Serializable> params) throws IOException {
		super(false);
		this.params = params;
		// TODO check params an throw Exception
		params = SOSDataStoreFactory.workOnParams(params);
		capabilities = SOSDataStoreFactory.getInstance().getCapabilities(params);
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
//			featureReaderCache.put(typeName, new SOSFeatureReader(params,
//					adapter, this, typeName));
			featureReaderCache.put(typeName, new SOSFeatureReader(params,
					adapter, typeName));
		} else if (featureReaderCache.get(typeName).isUsed()){
			featureReaderCache.put(typeName, new SOSFeatureReader(params,
					adapter, typeName));
//			featureReaderCache.put(typeName, new SOSFeatureReader(params,
//					adapter, this, typeName));
		}
		return featureReaderCache.get(typeName);
	}

	@Override
	public FeatureType getSchema(final String typeName) throws IOException {
		if (!featureTypeCache.containsKey(typeName)){
			featureTypeCache.put(typeName, getFeatureReader(typeName).getFeatureType());
		}
		return featureTypeCache.get(typeName);
	}

	@Override
	public String[] getTypeNames() throws IOException {
		if (typeNames == null) {
			final String op = (String) params.get(SOSDataStoreFactory.OPERATION.key);
//			SOSCapabilities capabilities = SOSDataStoreFactory.getInstance().getCapabilities(adapter, params);
			// TODO this decision and result should be moved to operation Type
			if (op.equals("GetFeatureOfInterest")) {
				return ((SOSOperationType) capabilities.getOperations()
						.getGetFeatureOfInterest()).getTypeNames();
			} else if (op.equals("GetObservation")) {
				return ((SOSOperationType) capabilities.getOperations()
						.getGetObservation()).getTypeNames();
			} else if (op.equals("DescribeSensor")) {
				return ((SOSOperationType) capabilities.getOperations()
						.getDescribeSensor()).getTypeNames();
			} else if (op.equals("GetCapabilities")) {
				return ((SOSOperationType) capabilities.getOperations()
						.getGetCapabilities()).getTypeNames();
			} else if (op.equals("GetObservationById")) {
				return ((SOSOperationType) capabilities.getOperations()
						.getGetObservationById()).getTypeNames();
			}
		}
		return typeNames;
	}

//	public void updateParams(Map<String, Serializable> params) {
//		SOSDataStoreFactory.getInstance().removeFromCache(this);
//		this.params = params;
//		SOSDataStoreFactory.getInstance().addToCache(params, this);
//		// TODO eventually here capabilities should be generated again ...
//		// however this current breaks system because already configured operations will
//		// be overwritten
//		// TODO solution: Configure Operations with Map <Parameters> @see SOSDataStoreFactory
//	}
}
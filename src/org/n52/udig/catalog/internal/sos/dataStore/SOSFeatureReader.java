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
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.geotools.data.FeatureReader;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.n52.oxf.OXFException;
import org.n52.oxf.feature.IFeatureStore;
import org.n52.oxf.feature.OXFFeature;
import org.n52.oxf.feature.OXFFeatureCollection;
import org.n52.oxf.owsCommon.ExceptionReport;
import org.n52.oxf.owsCommon.OWSException;
import org.n52.oxf.serviceAdapters.OperationResult;
import org.n52.oxf.serviceAdapters.ParameterContainer;
import org.n52.oxf.serviceAdapters.sos.SOSAdapter;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.config.ParameterConfiguration;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSFeatureReader implements FeatureReader {

	private final Map<String, Serializable> params;
	private final SOSAdapter adapter;
	// private SOSDataStore datastore;
	public static final String serviceVersion = SOSDataStoreFactory.serviceVersion_100;
	// private Map<String, FeatureType> featureTypeCache = new HashMap<String,
	// FeatureType>();
	private OXFFeatureCollection featureCollection = null;
	private Iterator<OXFFeature> it;
	/**
	 * the current operationType
	 */
	private SOSOperationType opType;
	private final String typeName;
	/** true if the FeatureReader has been closed */
	private boolean closed;
	private boolean used;
	private FeatureType featureType;
	private OperationResult opResult;
	/** the Log4J Logger */
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSFeatureReader.class);
	
	public Geometry getBoundingBox() {
		Geometry g = featureCollection.getBoundingBox();
		return g; 
	}

	/**
	 * Sends the request to the SOS and prepares the resultiterator
	 * @throws OXFException
	 * @throws IOException
	 * @throws ExceptionReport
	 */
	private void create() throws OXFException, IOException, ExceptionReport {
		final ParameterConfiguration pc = ((ParameterConfiguration) params
				.get(SOSDataStoreFactory.PARAMETERS.key));

		opType = (SOSOperationType) SOSDataStoreFactory.getInstance()
				.getCapabilities(params).getOperations()
				.getOperationTypeByName(
						(String) params.get(SOSDataStoreFactory.OPERATION.key));

		if (pc != null) {
			if (!opType.getId().equals("GetObservation")){
				pc.setHeartParameter(pc, typeName);
			} else{
				
			}
		} else {
			// TODO what should be the default behaviour?
		}

		final ParameterContainer paramCon = pc
				.getConfiguredParameterContainer();

		// now use this ParameterContainer as an input for the 'doOperation'
		// method of your SOSAdapter. What
		// you receive is an OperationResult.
		opResult = adapter.doOperation(opType.getOperation(((URL) params
				.get(SOSDataStoreFactory.URL_SERVICE.key)).toExternalForm()),
				SOSOperationType.fixContainer(paramCon));

		final IFeatureStore featureStore = opType.getFeatureStore();

		if (LOGGER.isEnabledFor(Level.DEBUG)) {
			final StringBuffer buff = new StringBuffer();
			for (final byte c : opResult.getIncomingResult()) {
				buff.append((char) c);
			}
			LOGGER.debug(buff.toString());
		}

		// use the result as an input for the 'unmarshalFeatures' operation
		// to parse the returned O&M document and to build up OXFFeature
		// objects.
		// InputStream is = new
		// FileInputStream("D:\\Java\\workspace\\net.refractions.udig.catalog.sos\\observations\\aggregated.xml");
		// InputStream is = new
		// FileInputStream("D:\\Java\\workspace\\net.refractions.udig.catalog.sos\\observations\\OutgoingGTSObservation.xml");
		// InputStream is = new
		// FileInputStream("D:\\Java\\workspace\\net.refractions.udig.catalog.sos\\observations\\Outgoing_SeismicObservation.xml");
		// featureCollection = featureStore.unmarshalFeatures(new
		// OperationResult(is,SOSOperationType
		// .fixContainer(paramCon),null));
		// InputStream is = new
		// FileInputStream("D:\\Java\\workspace\\net.refractions.udig.catalog.sos\\testfiles\\observations\\sosSimulation.xml");
		// featureCollection = featureStore.unmarshalFeatures(new
		// OperationResult(is,SOSOperationType
		// .fixContainer(paramCon),null));

		featureCollection = featureStore.unmarshalFeatures(opResult);

		featureType = OXFFeatureConverter.convert(featureCollection.iterator()
				.next().getFeatureType(), typeName);
		it = featureCollection.iterator();
	}

	private void handleException(OXFException oxfe) throws IOException{
		if (LOGGER.isEnabledFor(Level.DEBUG)) {
			LOGGER.error(oxfe);
			oxfe.printStackTrace();
		}

		if (oxfe.getCause().getClass().isAssignableFrom(
				org.apache.xmlbeans.XmlException.class)) {
			throw new IOException(
					"SOS reported an error, could not open FeatureReader, the server returned no valid xml: "
							+ oxfe.getMessage()
							+ " ;"
							+ oxfe.getCause().getMessage());
		} else if (oxfe.getCause().getClass().isAssignableFrom(
				org.xml.sax.SAXParseException.class)) {
			throw new IOException(
					"SOS reported an error, could not open FeatureReader, the server returned no valid xml: "
							+ oxfe.getMessage()
							+ " ;"
							+ oxfe.getCause().getMessage());
		} else if (oxfe.getCause().getClass().isAssignableFrom(
				java.lang.NumberFormatException.class)) {
			throw new IOException(
					"SOS reported an error, could not open FeatureReader, the server returned an invalid value : "
							+ oxfe.getCause().getClass()
							+ " "
							+ oxfe.getCause().getMessage());
		} else {
			throw new IOException(
					"SOS reported an error, could not open FeatureReader: "
							+ oxfe.getMessage() + " "
							+ oxfe.getCause().getClass() + " "
							+ oxfe.getCause().getMessage());
		}
	}
	
	private void handleException(ExceptionReport e) throws IOException{
		LOGGER.error("The Server reported an error:", e);
		final Iterator<OWSException> it = e.getExceptionsIterator();
		while (it.hasNext()) {
			final OWSException e2 = it.next();
			LOGGER.error(e2.getExceptionCode());
			LOGGER.error(e2.getExceptionTexts());
		}
		LOGGER.error(e.getStackTrace());
		throw new IOException(
				"The Server reported at least one error, please check your logs for the error message "
						+ e.getMessage());
		// LOGGER.error(e.getMessage());
	}
	
	private SOSFeatureReader(final Map<String, Serializable> params,
			final SOSAdapter adapter, final String typeName, SOSOperationType opType, FeatureType featureType, OXFFeatureCollection featureCollection, OperationResult opResult){
		this.closed = false;
		this.used = false;
		this.params = params;
		this.adapter = adapter;
		// this.datastore = data;
		this.typeName = typeName;
		this.opType = opType;
		this.featureType = featureType;
		this.featureCollection = featureCollection;
		this.opResult = opResult;
		this.it = featureCollection.iterator();
	}
	
	/**
	 * Creates a new instance of SOSFeatureReader
	 * 
	 * @param params
	 * @param adapter
	 * @param typeName
	 */
	public SOSFeatureReader(final Map<String, Serializable> params,
			final SOSAdapter adapter, final String typeName) throws IOException {
		// public SOSFeatureReader(Map<String, Serializable> params,
		// SOSAdapter adapter, SOSDataStore data, String typeName) {
		this.closed = false;
		this.used = false;
		this.params = params;
		this.adapter = adapter;
		// this.datastore = data;
		this.typeName = typeName;
		try {
			create();

		} catch (final OXFException oxfe) {
			handleException(oxfe);

		} catch (final ExceptionReport e) {
			handleException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.FeatureReader#close()
	 */
	public void close() throws IOException {
		closed = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.FeatureReader#getFeatureType()
	 */
	public FeatureType getFeatureType() {
		return featureType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.FeatureReader#hasNext()
	 */
	public boolean hasNext() throws IOException {
		return it.hasNext();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.geotools.data.FeatureReader#next()
	 */
	public Feature next() throws IOException, IllegalAttributeException,
			NoSuchElementException {
		if (!closed) {
			used = true;
			final OXFFeature f = it.next();
			// convert oxffeature to geotoolsfeature and return
			if (f == null) {
				throw new IllegalAttributeException("Feature is null");
			}
			Feature f2 = OXFFeatureConverter.convert(f);
			return f2;
		} else {
			throw new IOException("Reader closed");
		}
	}

	/**
	 * Returns the used state of the SOSFeatureReader
	 * 
	 * @return true if featureReader.next has been called
	 */
	public boolean isUsed() {
		return used;
	}
	
	public SOSFeatureReader getThisRestarted(){
		return new SOSFeatureReader(params,adapter,typeName,opType, featureType, featureCollection, opResult);
	}
}
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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

import net.opengis.cat.csw.x202.ResultType;
import net.opengis.gml.ResultDocument;
import net.opengis.om.x00.AbstractObservationPropertyType;
import net.opengis.om.x00.AbstractObservationType;
import net.opengis.om.x00.ObservationCollectionDocument;
import net.opengis.om.x00.ObservationCollectionType;
import net.opengis.om.x00.ObservationType;
import net.opengis.sos.x00.GetObservationDocument.GetObservation.Result;
import net.opengis.swe.x00.SimpleDataRecordDocument;
import net.opengis.swe.x00.SimpleDataRecordType;

import org.apache.xmlbeans.XmlCursor;
import org.geotools.data.DefaultFeatureReader;
import org.geotools.data.FeatureReader;
import org.geotools.feature.DefaultFeature;
import org.geotools.feature.DefaultFeatureType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.n52.oxf.feature.IFeatureStore;
import org.n52.oxf.feature.OXFFeature;
import org.n52.oxf.feature.OXFFeatureCollection;
import org.n52.oxf.feature.sos.SOSObservationStore;
import org.n52.oxf.serviceAdapters.OperationResult;
import org.n52.oxf.serviceAdapters.ParameterContainer;
import org.n52.oxf.serviceAdapters.sos.ISOSRequestBuilder;
import org.n52.oxf.serviceAdapters.sos.SOSAdapter;
import org.n52.udig.catalog.internal.sos.dataStore.config.ParameterConfiguration;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;
import org.w3c.dom.NodeList;

/**
 * @author 52n
 *
 */
public class SOSFeatureReader implements FeatureReader {

	private final Map<String, Serializable> params;
	private final SOSAdapter adapter;
//	private SOSDataStore datastore;
	public static final String serviceVersion = SOSDataStoreFactory.serviceVersion_100;
	// private Map<String, FeatureType> featureTypeCache = new HashMap<String,
	// FeatureType>();
	private OXFFeatureCollection featureCollection = null;
	private Iterator<OXFFeature> it;
	private SOSOperationType opType;
	private final String typeName;
	private boolean closed;
	private boolean used;

public static void main(String[] args) {
	try {
		
//		pc.addParameterShell(parameterName, parameterValue)



		Map<String,Serializable> param = new HashMap<String, Serializable>();
		
		SOSFeatureReader fr = new SOSFeatureReader(null,new SOSAdapter(SOSDataStoreFactory.serviceVersion_100),"");
//		xmltester();
		fr.testcreate();
	} catch (Exception e) {
		e.printStackTrace();
	}
	
}
	private static void xmltester() throws Exception {
		InputStream is = new FileInputStream("SeismicObservation.xml");
//		InputStream is = new FileInputStream("Buoyobservation2008_03_07.xml");
		ObservationCollectionDocument obcol_doc = ObservationCollectionDocument.Factory.parse(is);
		ObservationCollectionType obscol = obcol_doc.getObservationCollection();
		System.out.println(obscol.validate());
		AbstractObservationPropertyType member = obscol.getMemberArray(0);
		System.out.println(member.validate());
		
		ObservationType obs = (ObservationType)member.getAbstractObservation();
		System.out.println(obs.getResultDefinition());
		
		XmlCursor c = obs.getResult().newCursor();
//		System.out.println(c.getDomNode().getTextContent());
//		c.
		org.w3c.dom.Node n = c.getDomNode();
		NodeList l = n.getChildNodes();
		org.w3c.dom.Node simpleDataRecordNode = null;
		for (int i = 0; i<l.getLength(); i++){
			if (l.item(i).getNodeName().equals("swe:SimpleDataRecord")){
				simpleDataRecordNode = l.item(i); 
			}
		}

		SimpleDataRecordDocument sdrd = SimpleDataRecordDocument.Factory.parse(simpleDataRecordNode);
		
		SimpleDataRecordType sdrt = sdrd.getSimpleDataRecord();
//		System.out.println(sdrt.getSimpleDataRecord());
		System.out.println(sdrt.getDescription());
		System.out.println(sdrt.getFieldArray(0));



		System.out.println(obs.validate());

		System.out.println("samplingtime valid: "+obs.getSamplingTime().validate());
		System.out.println("samplingtime valid: "+obs.getSamplingTime().getTimeObject().validate());
}
	private void testcreate() throws Exception{
		final IFeatureStore featureStore = new SOSObservationStore();
		InputStream is = new FileInputStream("SeismicObservation.xml");
//		InputStream is = new FileInputStream("Buoyobservation2008_03_07.xml");
ParameterContainer pc = new ParameterContainer();
		
		pc.addParameterShell(ISOSRequestBuilder.GET_OBSERVATION_VERSION_PARAMETER,
                SOSAdapter.SUPPORTED_VERSIONS[0]);

		featureCollection = featureStore.unmarshalFeatures(new OperationResult(is,pc,null));
		it = featureCollection.iterator();
	}
	
	
	
	private void create() {
		try {
//			opType = (SOSOperationType) datastore.getCapabilities()
			opType = (SOSOperationType) SOSDataStoreFactory.getInstance().getCapabilities(params)
					.getOperations().getOperationTypeByName(
							(String) params
									.get(SOSDataStoreFactory.OPERATION.key));
			opType.setParameterConfiguration(((ParameterConfiguration)params.get(SOSDataStoreFactory.PARAMETERS.key)));
			if (opType != null){
				opType.setHeartParameter(typeName);
			} else{
				// TODO what should be the default behaviour?
			}


			final ParameterContainer paramCon = opType.getParameterConfiguration().getConfiguredParameterContainer();

			// now use this ParameterContainer as an input for the 'doOperation'
			// method of your SOSAdapter. What
			// you receive is an OperationResult.
			final OperationResult opResult = adapter.doOperation(opType
					.getOperation(((URL) params
							.get(SOSDataStoreFactory.URL_SERVICE.key))
							.toExternalForm()), SOSOperationType.fixContainer(paramCon));


			final IFeatureStore featureStore = opType.getFeatureStore();

			System.out.println(opResult.getSendedRequest());
//			for (byte c : opResult.getIncomingResult()) {
//				System.out.print((char) c);
//			}

			// use the result as an input for the 'unmarshalFeatures' operation
			// to parse the returned O&M document and to build up OXFFeature
			// objects.
//			try {
			featureCollection = featureStore.unmarshalFeatures(opResult);
//			} catch (Exception e) {
//				// no featureCollection as result
//				if (e.getMessage().contains("valid FeatureCollection")){
//					((SOSFoiStore)featureStore).parseFoi(opResult.get)
//				}


//			}

			it = featureCollection.iterator();
		} catch (final Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * Creates a new instance of SOSFeatureReader
	 *
	 * @param params
	 * @param adapter
	 * @param typeName
	 */
	public SOSFeatureReader(final Map<String, Serializable> params,
			final SOSAdapter adapter, final String typeName) {
//		public SOSFeatureReader(Map<String, Serializable> params,
//				SOSAdapter adapter, SOSDataStore data, String typeName) {
		this.closed = false;
		this.used = false;
		this.params = params;
		this.adapter = adapter;
//		this.datastore = data;
		this.typeName = typeName;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.geotools.data.FeatureReader#close()
	 */
	public void close() throws IOException {
		closed = true;
	}

	// protected GeometricAttributeType getDefaultGeometry(){
	// GeometricAttributeType gat = null;
	// try {
	// CoordinateReferenceSystem crs = CRS.decode("EPSG:4326");
	// gat = new GeometricAttributeType("Punkt",Point.class,true,null,crs,new
	// FilterFactoryImpl().createNullFilter());
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// return gat;
	// }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.geotools.data.FeatureReader#getFeatureType()
	 */
	public FeatureType getFeatureType() {
		if (featureCollection == null) {
			create();
		}
		return OXFFeatureConverter.createFeatureType(featureCollection,
				typeName);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.geotools.data.FeatureReader#hasNext()
	 */
	public boolean hasNext() throws IOException {
		if (featureCollection == null) {
			create();
		}
		return it.hasNext();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.geotools.data.FeatureReader#next()
	 */
	public Feature next() throws IOException, IllegalAttributeException,
			NoSuchElementException {
		used = true;
		if (featureCollection == null) {
			create();
		}
		// convert oxffeature to geotoolsfeature and return
		return OXFFeatureConverter.convert(it.next());
	}

	/**
	 * @return the used
	 */
	public boolean isUsed() {
		return used;
	}

}

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


import org.geotools.data.DataStore;
import org.geotools.data.FeatureReader;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.DataStoreFactorySpi.Param;
import org.geotools.filter.Filter;
import org.geotools.filter.SortBy;
import org.n52.oxf.OXFException;
import org.n52.oxf.owsCommon.capabilities.IDiscreteValueDomain;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.SOSOperations;
import org.n52.udig.catalog.internal.sos.dataStore.config.ParameterConfiguration;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;



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
/**
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class ExampleAccessSOSProgramaticly {
	
//	static final String SOSURL = "http://mars.uni-muenster.de:8080/OWS5SOS/sos";
	static final String SOSURL = "http://192.168.1.2:8180/DLR_BUOY_SOS/sos?SERVICE=SOS&REQUEST=GetCapabilities";
	
	public static void main(String[] args) {
		example1();
	}
	
	static void example1() {
		// create an empty parameter Map for SOS parameters
		final Map<String, Serializable> params = new HashMap<String, Serializable>(
				4);

		// the SOS service url
		try {
			final URL sosUrl = new URL(SOSURL);

			// get descriptions of needed parameters
			final Param[] paramInfo = SOSDataStoreFactory.getInstance()
					.getParametersInfo();

			// fill parameter MAP
			// url
			params.put(SOSDataStoreFactory.URL_SERVICE.key, sosUrl);

			// name of operation
			params.put(SOSDataStoreFactory.OPERATION.key,
					SOSOperations.opName_GetObservation);

			// get preconfigured ParameterConfiguration from
			// sosConfiguration.xml
			final ParameterConfiguration pc = SOSConfigurationRegistry
					.getInstance("sosConfiguration.xml")
					.getParameterConfiguration(sosUrl.toExternalForm(),
							SOSOperations.opName_GetObservation);

			// get the operation
			final SOSOperationType sosopType = ((SOSOperationType) SOSDataStoreFactory
					.getInstance()
					.getCapabilities(params)
					.getOperations()
					.getOperationTypeByName(SOSOperations.opName_GetObservation));

			// check if parametercontainer contains all needed parameters
			boolean configured = sosopType.isOperationConfigured(pc);

			// if not fully configured
			if (!configured) {
				for (final Parameter p : pc.getUnconfiguredRequiredParameters()) {
					// do something to set missing parameters
					if (p.getValueDomain() instanceof IDiscreteValueDomain) {
						final IDiscreteValueDomain temp_param_domain = (IDiscreteValueDomain) p
								.getValueDomain();
						if (p.getServiceSidedName().equals("responseMode")) {
							pc.setParameterValue(p.getServiceSidedName(),
									"inline");
						} else {
							pc.setParameterValue(p.getServiceSidedName(),
									temp_param_domain.getPossibleValues()
											.get(0).toString());
						}
					}
				}
			}

			
			// put SOS parameters into Parameter-Map
			params.put(SOSDataStoreFactory.PARAMETERS.key, pc);


			// no UDIG running:
			// submit the request and get the DataStore from the SOSDatastoreFactory 
			final SOSDataStoreFactory dsf = SOSDataStoreFactory.getInstance();
			final DataStore ds = dsf.createDataStore(params);
			
			// create the query
			final Query q = new InternalQuery(ds.getTypeNames()[0]);

			// get the featurereader
			final FeatureReader fr = ds.getFeatureReader(q,
					Transaction.AUTO_COMMIT);

			// iterate through all Features and print a string representation
			while (fr.hasNext()) {
				try {
					System.out.println((fr.next()));
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}

		} catch (final MalformedURLException mue) {
			// TODO: handle exception
		} catch (final IOException ioe) {
			System.err.println(ioe);
		} catch (final OXFException oxfe) {
			System.err.println(oxfe);
		}
	}
}

class InternalQuery implements Query {

	CoordinateReferenceSystem crs;
	String typeName;

	public InternalQuery(final String typeName) {
		// this.crs = crs;
		this.typeName = typeName;
	}

	public final String[] getPropertyNames() {
		return null;
	}

	public final boolean retrieveAllProperties() {
		return true;
	}

	public final int getMaxFeatures() {
		return Integer.MAX_VALUE; // consider Integer.MAX_VALUE
	}

	public final Filter getFilter() {
		return Filter.NONE;
	}

	public final String getTypeName() {
		return typeName;
	}

	public URI getNamespace() {
		return null;
	}

	public final String getHandle() {
		return "Request All Features";
	}

	public final String getVersion() {
		return null;
	}

	/**
	 * Hashcode based on propertyName, maxFeatures and filter.
	 * 
	 * @return hascode for filter
	 */
	@Override
	public int hashCode() {
		final String[] n = getPropertyNames();

		return ((n == null) ? (-1) : ((n.length == 0) ? 0 : (n.length | n[0]
				.hashCode())))
				| getMaxFeatures()
				| ((getFilter() == null) ? 0 : getFilter().hashCode())
				| ((getTypeName() == null) ? 0 : getTypeName().hashCode())
				| ((getVersion() == null) ? 0 : getVersion().hashCode())
				| ((getCoordinateSystem() == null) ? 0 : getCoordinateSystem()
						.hashCode())
				| ((getCoordinateSystemReproject() == null) ? 0
						: getCoordinateSystemReproject().hashCode());
	}

	/**
	 * Equality based on propertyNames, maxFeatures, filter, typeName and
	 * version.
	 * 
	 * <p>
	 * Changing the handle does not change the meaning of the Query.
	 * </p>
	 * 
	 * @param obj
	 *            Other object to compare against
	 * 
	 * @return <code>true</code> if <code>obj</code> matches this filter
	 */
	@Override
	public boolean equals(final Object obj) {
		if ((obj == null) || !(obj instanceof Query)) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		final Query other = (Query) obj;

		return Arrays.equals(getPropertyNames(), other.getPropertyNames())
				&& (retrieveAllProperties() == other.retrieveAllProperties())
				&& (getMaxFeatures() == other.getMaxFeatures())
				&& ((getFilter() == null) ? (other.getFilter() == null)
						: getFilter().equals(other.getFilter()))
				&& ((getTypeName() == null) ? (other.getTypeName() == null)
						: getTypeName().equals(other.getTypeName()))
				&& ((getVersion() == null) ? (other.getVersion() == null)
						: getVersion().equals(other.getVersion()))
				&& ((getCoordinateSystem() == null) ? (other
						.getCoordinateSystem() == null) : getCoordinateSystem()
						.equals(other.getCoordinateSystem()))
				&& ((getCoordinateSystemReproject() == null) ? (other
						.getCoordinateSystemReproject() == null)
						: getCoordinateSystemReproject().equals(
								other.getCoordinateSystemReproject()));
	}

	@Override
	public String toString() {
		return "Query.ALL";
	}

	/**
	 * Return <code>null</code> as ALLQuery does not require a CS.
	 * 
	 * @return <code>null</code> as override is not required.
	 * 
	 * @see org.geotools.data.Query#getCoordinateSystem()
	 */
	public CoordinateReferenceSystem getCoordinateSystem() {
		return null;
	}

	/**
	 * Return <code>null</code> as ALLQuery does not require a CS.
	 * 
	 * @return <code>null</code> as reprojection is not required.
	 * 
	 * @see org.geotools.data.Query#getCoordinateSystemReproject()
	 */
	public CoordinateReferenceSystem getCoordinateSystemReproject() {
		return null;
	}

	public SortBy[] getSortBy() {
		return SortBy.UNSORTED;
	}

}

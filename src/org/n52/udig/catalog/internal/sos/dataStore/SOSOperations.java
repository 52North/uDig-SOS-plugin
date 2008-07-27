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

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.geotools.data.ows.OperationType;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;

/**
 * This class offers access for all operations supported by the SOS/DataStore
 * It's a static version. So this class needs to be extended if the
 * specification evolves and additional operations become available.
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSOperations {
	/**
	 * OperationType for getCapabilities-Operation
	 */
	private OperationType getCapabilities;

	/**
	 * OperationType for getObservation-Operation
	 */
	private OperationType getObservation;

	/**
	 * OperationType for describeSensor-Operation
	 */
	private OperationType describeSensor;

	/**
	 * OperationType for getFeatureOfInterest-Operation
	 */
	private OperationType getFeatureOfInterest;

	/**
	 * OperationType for getObservationById-Operation
	 */
	private OperationType getObservationById;

	/**
	 * String representation of the "GetCapabilities"-operation Used for
	 * internal operationselection and used as operationString in SOSRequests
	 */
	public static String opName_GetCapabilities = "GetCapabilities";

	/**
	 * String representation of the "GetObservation"-operation Used for internal
	 * operationselection and used as operationString in SOSRequests
	 */
	public static String opName_GetObservation = "GetObservation";

	/**
	 * String representation of the "GetObservationById"-operation Used for
	 * internal operationselection and used as operationString in SOSRequests
	 */
	public static String opName_GetObservationById = "GetObservationById";

	/**
	 * String representation of the "DescribeSensor"-operation Used for internal
	 * operationselection and used as operationString in SOSRequests
	 */
	public static String opName_DescribeSensor = "DescribeSensor";

	/**
	 * String representation of the "GetFeatureOfInterest"-operation Used for
	 * internal operationselection and used as operationString in SOSRequests
	 */
	public static String opName_GetFeatureOfInterest = "GetFeatureOfInterest";

	/**
	 * Returns the OperationType from a String representation of the operation
	 * 
	 * @see SOSOperations#opName_GetCapabilities
	 * @see SOSOperations#opName_DescribeSensor
	 * @see SOSOperations#opName_GetFeatureOfInterest
	 * @see SOSOperations#opName_GetObservation
	 * @see SOSOperations#opName_GetObservationById
	 * 
	 * @param opName
	 *            the operation's String representation
	 * @return the OperationType represented by opName or null if opName not
	 *         found/available/supported
	 */
	public OperationType getOperationTypeByName(final String opName) {
		if (opName.equalsIgnoreCase(SOSOperations.opName_DescribeSensor)) {
			return getDescribeSensor();
		} else if (opName
				.equalsIgnoreCase(SOSOperations.opName_GetCapabilities)) {
			return getGetCapabilities();
		} else if (opName
				.equalsIgnoreCase(SOSOperations.opName_GetFeatureOfInterest)) {
			return getGetFeatureOfInterest();
		} else if (opName.equalsIgnoreCase(SOSOperations.opName_GetObservation)) {
			return getGetObservation();
		} else if (opName
				.equalsIgnoreCase(SOSOperations.opName_GetObservationById)) {
			return getGetObservationById();
		}
		return null;
	}

	/**
	 * Returns a String[] for all String representations of all
	 * supported/available operations
	 * 
	 * @see SOSOperations#opName_GetCapabilities
	 * @see SOSOperations#opName_DescribeSensor
	 * @see SOSOperations#opName_GetFeatureOfInterest
	 * @see SOSOperations#opName_GetObservation
	 * @see SOSOperations#opName_GetObservationById
	 * @return a String[] for all String representations
	 */
	public String[] getAllOperationNames() {
		final List<String> op = new Vector<String>(5);
		if (getCapabilities != null) {
			op.add(opName_GetCapabilities);
		}
		if (getObservation != null) {
			op.add(opName_GetObservation);
		}
		if (describeSensor != null) {
			op.add(opName_DescribeSensor);
		}
		if (getFeatureOfInterest != null) {
			op.add(opName_GetFeatureOfInterest);
		}
		if (getObservationById != null) {
			op.add(opName_GetObservationById);
		}

		final String[] out = new String[op.size()];
		int i = 0;
		for (final String s : op) {
			out[i++] = s;
		}
		return out;
	}

	/**
	 * Returns a List containing all supported operations
	 * 
	 * @return a List of all supported operations or an empty list if none
	 *         supported/available
	 */
	public List<OperationType> getAllOperations() {
		final List<OperationType> ops = new LinkedList<OperationType>();
		for (final String s : getAllOperationNames()) {
			ops.add(getOperationTypeByName(s));
		}
		return ops;
	}

	/**
	 * 
	 * @return the getObservationById
	 */
	public OperationType getGetObservationById() {
		return getObservationById;
	}

	/**
	 * @param getObservationById
	 *            the getObservationById to set
	 */
	public void setGetObservationById(final OperationType getObservationById) {
		this.getObservationById = getObservationById;
	}

	/**
	 * @return the getCapabilities
	 */
	public OperationType getGetCapabilities() {
		return getCapabilities;
	}

	/**
	 * @param getCapabilities
	 *            the getCapabilities to set
	 */
	public void setGetCapabilities(final OperationType getCapabilities) {
		this.getCapabilities = getCapabilities;
	}

	/**
	 * @return the getObservation
	 */
	public OperationType getGetObservation() {
		return getObservation;
	}

	/**
	 * @param getObservation
	 *            the getObservation to set
	 */
	public void setGetObservation(final SOSOperationType getObservation) {
		this.getObservation = getObservation;
	}

	/**
	 * @return the describeSensor
	 */
	public OperationType getDescribeSensor() {
		throw new UnsupportedOperationException(
				"Operation getDescribeSensor not supported by plugin");
		// return describeSensor;
	}

	/**
	 * @param describeSensor
	 *            the describeSensor to set
	 */
	public void setDescribeSensor(final OperationType describeSensor) {
		throw new UnsupportedOperationException(
				"Operation setDescribeSensor not supported by plugin");
		// this.describeSensor = describeSensor;
	}

	/**
	 * @return the getFeatureOfInterest
	 */
	public OperationType getGetFeatureOfInterest() {
		return getFeatureOfInterest;
	}

	/**
	 * @param getFeatureOfInterest
	 *            the getFeatureOfInterest to set
	 */
	public void setGetFeatureOfInterest(final OperationType getFeatureOfInterest) {
		this.getFeatureOfInterest = getFeatureOfInterest;
	}
}
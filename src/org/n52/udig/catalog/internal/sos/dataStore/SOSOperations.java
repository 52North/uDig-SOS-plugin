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

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.geotools.data.ows.OperationType;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;

/**
 * @author 52n
 *
 */
public class SOSOperations {
	// TODO HASHTABLE o.ä. verwenden
	private OperationType getCapabilities;
	private OperationType getObservation;
	private OperationType describeSensor;
	private OperationType getFeatureOfInterest;
	private OperationType getObservationById;
	public static String opName_GetCapabilities = "GetCapabilities";
	public static String opName_GetObservation = "GetObservation";
	public static String opName_GetObservationById = "GetObservationById";
	public static String opName_DescribeSensor = "DescribeSensor";
	public static String opName_GetFeatureOfInterest = "GetFeatureOfInterest";

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

	public List<OperationType> getAllOperations(){
		final List<OperationType> ops = new LinkedList<OperationType>();
		for (final String s:getAllOperationNames()){
			ops.add(getOperationTypeByName(s));
		}
		return ops;
	}

	/**
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
		return describeSensor;
	}

	/**
	 * @param describeSensor
	 *            the describeSensor to set
	 */
	public void setDescribeSensor(final OperationType describeSensor) {
		this.describeSensor = describeSensor;
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

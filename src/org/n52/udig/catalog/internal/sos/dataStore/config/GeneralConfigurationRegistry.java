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
Created: 27.05.2008
 *********************************************************************************/
package org.n52.udig.catalog.internal.sos.dataStore.config;

import java.util.HashMap;

import org.n52.oxf.serviceAdapters.sos.ISOSRequestBuilder;

/**
 * General Configuration
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public abstract class GeneralConfigurationRegistry {

	private static GeneralConfigurationRegistry instance = null;

	/**
	 * Gets the current instance of GeneralConfigurationRegistry
	 * 
	 * @return
	 */
	public static GeneralConfigurationRegistry getInstance() {
		if (instance == null) {
			instance = new GeneralConfigurationRegistryProperties();
		}
		return instance;
	}

	public abstract String getSOSconfigurationFilename();

	public abstract String getProxyHost();

	public abstract String getProxyPort();

	public abstract void setProxyHost(String host);

	public abstract void setProxyPort(String port);

	public abstract String getLog4jPropertiesFilename();

	public abstract boolean isFixErrors();

	public abstract boolean isWriteable();

	public abstract void save();

	public abstract long getTimeToCacheCapabilities();

	public abstract long getTimeToCacheDatastore();

	public abstract String getPreferedSOSVersion();

	public abstract void setSOSconfigurationFilename(String s);

	public abstract void setLog4jPropertiesFilename(String s);

	public abstract void setFixErrors(boolean b);

	public abstract void setTimeToCacheCapabilities(long l);

	public abstract void setTimeToCacheDatastore(long l);

	public abstract void setPreferedSOSVersion(String s);

	private static HashMap<String, Character> parameterTypes = null;

	/**
	 * @return the parameterTypes
	 */
	public static final HashMap<String, Character> getParameterTypes() {
		if (parameterTypes == null) {
			parameterTypes = new HashMap<String, Character>();
			configureParameterTypes();
		}
		return parameterTypes;
	}

	private static void configureParameterTypes() {
		if (parameterTypes.isEmpty()) {
			// GetCapabilities
			parameterTypes.put(ISOSRequestBuilder.GET_CAPABILITIES_UPDATE_SEQUENCE_PARAMETER,
							's');
			parameterTypes.put(ISOSRequestBuilder.GET_CAPABILITIES_ACCEPT_VERSIONS_PARAMETER,
							'a');
			parameterTypes
					.put(
							ISOSRequestBuilder.GET_CAPABILITIES_SECTIONS_PARAMETER,
							'a');
			parameterTypes.put(
					ISOSRequestBuilder.GET_CAPABILITIES_SERVICE_PARAMETER, 's');

			// DescribeSensor
			parameterTypes.put(
					ISOSRequestBuilder.DESCRIBE_SENSOR_SERVICE_PARAMETER, 's');
			parameterTypes.put(
					ISOSRequestBuilder.DESCRIBE_SENSOR_VERSION_PARAMETER, 's');
			parameterTypes
					.put(
							ISOSRequestBuilder.DESCRIBE_SENSOR_PROCEDURE_PARAMETER,
							's');
			parameterTypes.put(
					ISOSRequestBuilder.DESCRIBE_SENSOR_OUTPUT_FORMAT, 's');

			// GetFOI
			parameterTypes.put(ISOSRequestBuilder.GET_FOI_SERVICE_PARAMETER,
					's');
			parameterTypes.put(ISOSRequestBuilder.GET_FOI_VERSION_PARAMETER,
					's');
			parameterTypes.put(ISOSRequestBuilder.GET_FOI_EVENT_TIME_PARAMETER,
					's');
			parameterTypes.put(ISOSRequestBuilder.GET_FOI_ID_PARAMETER, 's');
			parameterTypes.put(ISOSRequestBuilder.GET_FOI_LOCATION_PARAMETER,
					's');

			// GetObservation
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_SERVICE_PARAMETER, 's');
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_VERSION_PARAMETER, 's');
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_OFFERING_PARAMETER, 's');
			parameterTypes
					.put(
							ISOSRequestBuilder.GET_OBSERVATION_RESPONSE_FORMAT_PARAMETER,
							's');
			parameterTypes
					.put(
							ISOSRequestBuilder.GET_OBSERVATION_OBSERVED_PROPERTY_PARAMETER,
							'a');
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_EVENT_TIME_PARAMETER,
					's');
			parameterTypes
					.put(
							ISOSRequestBuilder.GET_OBSERVATION_PROCEDURE_PARAMETER,
							'a');
			parameterTypes
					.put(
							ISOSRequestBuilder.GET_OBSERVATION_FEATURE_OF_INTEREST_PARAMETER,
							'a');
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_RESULT_PARAMETER, 's');
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_RESULT_MODEL_PARAMETER,
					's');
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_RESPONSE_MODE_PARAMETER,
					's');

			// GetObservationByID
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_BY_ID_SERVICE_PARAMETER,
					's');
			parameterTypes.put(
					ISOSRequestBuilder.GET_OBSERVATION_BY_ID_VERSION_PARAMETER,
					's');
			parameterTypes
					.put(
							ISOSRequestBuilder.GET_OBSERVATION_BY_ID_OBSERVATION_ID_PARAMETER,
							's');
			parameterTypes
					.put(
							ISOSRequestBuilder.GET_OBSERVATION_BY_ID_RESPONSE_FORMAT_PARAMETER,
							's');
			parameterTypes
					.put(
							ISOSRequestBuilder.GET_OBSERVATION_BY_ID_RESPONSE_MODE_PARAMETER,
							's');
			parameterTypes.put(ISOSRequestBuilder.GET_OBSERVATION_BY_ID_RESULT_MODEL_PARAMETER,'s');
			parameterTypes.put("AcceptFormats",'s');
		}
	}
}
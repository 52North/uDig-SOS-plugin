/*
 * Copyright (C) 2008 - 2010 52°North Initiative for Geospatial Open Source Software GmbH
 * 
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 * 
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 * 
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 * 
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.udig.catalog.internal.sos.dataStore.config;

import java.util.HashMap;
import org.n52.oxf.serviceAdapters.sos.ISOSRequestBuilder;
import org.n52.udig.catalog.internal.sos.workarounds.IWorkaroundDescription;

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
	
	public abstract HashMap<String, IWorkaroundDescription> getWorkarounds();

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
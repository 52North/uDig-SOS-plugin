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

import org.apache.log4j.Logger;
import org.geotools.data.ows.OperationType;
import org.n52.oxf.OXFException;
import org.n52.oxf.feature.IFeatureStore;
import org.n52.oxf.feature.UDIGSOSFOIStore;
import org.n52.oxf.feature.sos.UDIGSOSObservationStore;
import org.n52.oxf.owsCommon.capabilities.Operation;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.oxf.serviceAdapters.ParameterContainer;
import org.n52.oxf.serviceAdapters.ParameterShell;
import org.n52.oxf.util.LoggingHandler;
import org.n52.oxf.valueDomains.StringValueDomain;
import org.n52.udig.catalog.internal.sos.dataStore.SOSOperations;
import org.n52.udig.catalog.internal.sos.ui.SOSPreferencePage;

/**
 * @author 52n
 * 
 */
public class SOSOperationType extends OperationType {

	/**
	 * the operation's service-id set by constructor
	 */
	private final String id;
	private ParameterConfiguration initialParamConf;
	private final ParameterConfiguration initialCapabilitiesParamConf;


	/**
	 * This method fixes a few errors known in the 52N-SOS, this may lead to
	 * errors with other SOS implementations, so enable/disable it via
	 * {@link GeneralConfigurationRegistry} or the corresponding GUI
	 * {@link SOSPreferencePage}
	 * 
	 * @param pc
	 *            the ParameterContainer
	 * @return a ParameterContainer with fixed values
	 * @throws OXFException
	 *             thrown on connection or parse errors
	 */
	public static ParameterContainer fixContainer(final ParameterContainer pc)
			throws OXFException {
		if (GeneralConfigurationRegistry.getInstance().isFixErrors()) {
			if (pc.containsParameterShellWithServiceSidedName("responseFormat")) {
				try {
					if (((String) pc.getParameterShellWithServiceSidedName(
							"responseFormat").getSpecifiedValue())
							.equals("text/xml;subtype=\"OM\"")) {
						pc.setParameterValue("responseFormat",
								"text/xml;subtype=\"om/1.0.0\"");
					}
				} catch (final UnsupportedOperationException uoe) {
					if (((String) pc.getParameterShellWithServiceSidedName(
							"responseFormat").getSpecifiedValueArray()[0])
							.equals("text/xml;subtype=\"OM\"")) {
						pc.setParameterValue("responseFormat",
								"text/xml;subtype=\"om/1.0.0\"");
					}
				}

				if (pc
						.containsParameterShellWithServiceSidedName("resultModel")) {
					try {
						if (((String) pc.getParameterShellWithServiceSidedName(
								"resultModel").getSpecifiedValue())
								.equals("om:Measurement")) {
							pc.setParameterValue("resultModel", "Measurement");
						}
					} catch (final UnsupportedOperationException uoe) {
						if (((String) pc.getParameterShellWithServiceSidedName(
								"resultModel").getSpecifiedValueArray()[0])
								.equals("om:Measurement")) {
							pc.setParameterValue("resultModel", "Measurement");
						}
					}

					try {
						if (((String) pc.getParameterShellWithServiceSidedName(
								"resultModel").getSpecifiedValue())
								.equals("om:Observation")) {
							pc.setParameterValue("resultModel", "Observation");
						}
					} catch (final UnsupportedOperationException uoe) {
						if (((String) pc.getParameterShellWithServiceSidedName(
								"resultModel").getSpecifiedValueArray()[0])
								.equals("om:Observation")) {
							pc.setParameterValue("resultModel", "Observation");
						}

					}
				}
			}
		}
		return pc;
	}
	
	

	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSOperationType.class);

	public SOSOperationType(final String id, final String url, org.n52.oxf.owsCommon.capabilities.Contents contents) {
		this.id = id;
//		this.url = url;
		initialParamConf = new ParameterConfiguration(id, url);
		initialCapabilitiesParamConf = new ParameterConfiguration(id, url);
		if (id.equals(SOSOperations.opName_GetObservationById)) {
			try {
				addParameterFromCaps(new Parameter("ObservationId", true,
						new StringValueDomain(), "ObservationId"));
			} catch (final Exception e) {
				// this should never happen
				LOGGER.fatal(e);
			}
		}
		setContents(contents);
	}

	public String getInfo() {
		final StringBuilder docSB = new StringBuilder();
		// String docS = new String("");
		docSB.append("ID:\n");
		docSB.append(getId());
		docSB.append("\n\nGet:\n");
		docSB.append(getGet());
		docSB.append("\n\nPost:\n");
		docSB.append(getPost());
		docSB.append("\n\nSupported formats:\n");
		docSB.append(getFormats());
		docSB.append("\n\nParameters:\n");
		docSB.append(getNewPreconfiguredConfiguration()
				.getParametersAsStrings());
		return docSB.toString();
	}

	private ParameterConfiguration mergeParametersFromCapabilities(
			final ParameterConfiguration pc) {
		final ParameterConfiguration out = new ParameterConfiguration(pc
				.getOperationId(), pc.getUrl());
		try {

			for (final Parameter p : initialCapabilitiesParamConf
					.getParameters()) {
				out.addParameter(p);
			}
			for (final Parameter p2 : pc.getParameters()) {
				out.addParameter(p2);
			}
			for (final ParameterShell ps : initialCapabilitiesParamConf
					.getConfiguredParameterContainer().getParameterShells()) {
				if (ps.hasMultipleSpecifiedValues()) {
					out.setParameterValue(ps.getParameter()
							.getServiceSidedName(), (String[]) ps
							.getSpecifiedValueArray());
				} else {
					out.setParameterValue(ps.getParameter()
							.getServiceSidedName(), (String) ps
							.getSpecifiedValue());
				}
			}
			for (final ParameterShell ps2 : initialCapabilitiesParamConf
					.getConfiguredParameterContainer().getParameterShells()) {
				if (ps2.hasMultipleSpecifiedValues()) {
					out.setParameterValue(ps2.getParameter()
							.getServiceSidedName(), (String[]) ps2
							.getSpecifiedValueArray());
				} else {
					out.setParameterValue(ps2.getParameter()
							.getServiceSidedName(), (String) ps2
							.getSpecifiedValue());
				}
			}
		} catch (final Exception e) {
			LOGGER.error(e);
		}
		return out;

	}
	
	public void setContents(org.n52.oxf.owsCommon.capabilities.Contents contents){
		initialCapabilitiesParamConf.setContents(contents);
		initialParamConf.setContents(contents);
	}

	public void addParameterFromCaps(final Parameter parameter)
			throws OXFException {
		initialCapabilitiesParamConf.addParameter(parameter);
		initialParamConf.addParameter(parameter);
	}
	
	public IFeatureStore getFeatureStore() {
		if (id.equals(SOSOperations.opName_GetObservation)
				|| id.equals(SOSOperations.opName_GetObservationById)) {
			return new UDIGSOSObservationStore();
		} else {
			return new UDIGSOSFOIStore();
		}
	}

	/**
	 * Returns the id of this operation
	 * 
	 * @return the id of the operation, for possible values see
	 *         {@link SOSOperations}
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns an Operation with the specified name and sets one DCP with the
	 * specified httpGetHref and httpPostHref. The constraints and parameters
	 * attributes will stay <code>null</code>.
	 * 
	 * @param url
	 *            the url of the sos this operation should connect to
	 * @return the operation
	 */
	public Operation getOperation(final String url) {
		return new Operation(id, "get_not_used", url);
	}

	public ParameterConfiguration getNewPreconfiguredConfiguration() {
		return new ParameterConfiguration(initialParamConf);
	}

	public ParameterConfiguration getCapabilitiesConfiguration() {
		return new ParameterConfiguration(initialCapabilitiesParamConf);
	}

	/**
	 * Checks if all required Parameters for this operation are set in the
	 * ParameterConfiguration
	 * 
	 * @param pc
	 *            the parameterConfiguration that should be checked
	 * @return true if all required Parameters for this operation are set, false
	 *         if not
	 */
	public boolean isOperationConfigured(final ParameterConfiguration pc) {
		for (final Parameter param : pc.getRequiredParameters()) {
			if (!pc.isParameterConfigured(param.getServiceSidedName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if all required Parameters except the "HeartParameter" for this
	 * operation are set
	 * 
	 * @return true if all required Parameters except the "HeartParameter" for
	 *         this operation are set, false if not
	 * @see ParameterConfiguration#setHeartParameter(String)
	 * @see ParameterConfiguration#getHeartParameter(String)
	 */
	public boolean isOperationNearlyConfigured(final ParameterConfiguration pc) {
		for (final Parameter param : pc.getRequiredParameters()) {
			// all required parameters except the typeName-Parameter are needed
			if (!pc.isParameterConfigured(param.getServiceSidedName())
					&& !param.getServiceSidedName().equals(
							pc.getHeartParameterName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Replaces the current ParameterConfiguration with a new one used for
	 * external configuration
	 * 
	 * @param pc
	 *            the new Parameterblock to set
	 */
	public void setParameterConfiguration(final ParameterConfiguration pc) {
		this.initialParamConf = mergeParametersFromCapabilities(pc);
	}

}
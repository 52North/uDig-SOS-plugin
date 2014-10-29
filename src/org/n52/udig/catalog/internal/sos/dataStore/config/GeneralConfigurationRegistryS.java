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

import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.workarounds.EastingFirstWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.FalseBoundingBoxWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.IWorkaroundDescription;
import org.n52.udig.catalog.internal.sos.workarounds.NoCRSWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.TransformCRSWorkaroundDesc;

/**
 * General Configuration Uses static strings load the configuration
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class GeneralConfigurationRegistryS extends GeneralConfigurationRegistry {
	private final static String SOSconfigurationFilename = "sosConfiguration.xml";
	private final static String log4jPropertiesFilename = "D:\\\\Java\\log4j.properties";

	private HashMap<String, IWorkaroundDescription> workarounds = new HashMap<String, IWorkaroundDescription>();
	
	protected GeneralConfigurationRegistryS() {
		EastingFirstWorkaroundDesc w1 = new EastingFirstWorkaroundDesc();
		workarounds.put(w1.getIdentifier(),w1);
		
		TransformCRSWorkaroundDesc w2 = new TransformCRSWorkaroundDesc();
		workarounds.put(w2.getIdentifier(),w2);
		
		FalseBoundingBoxWorkaroundDesc w3 = new FalseBoundingBoxWorkaroundDesc();
		workarounds.put(w3.getIdentifier(), w3);
		
		NoCRSWorkaroundDesc w4 = new NoCRSWorkaroundDesc();
		workarounds.put(w4.getIdentifier(), w4);

	}

	/**
	 * @return the sOSconfigurationFilename
	 */
	@Override
	public String getSOSconfigurationFilename() {
		return SOSconfigurationFilename;
	}

	@Override
	public String getLog4jPropertiesFilename() {
		return log4jPropertiesFilename;
	}

	@Override
	public boolean isWriteable() {
		return false;
	}

	@Override
	public boolean isFixErrors() {
		return true;
	}

	@Override
	public long getTimeToCacheCapabilities() {
		return 1000 * 60 * 5;
	}

	@Override
	public long getTimeToCacheDatastore() {
		return 1000 * 60 * 5;
	}

	@Override
	public String getPreferedSOSVersion() {
		return SOSDataStoreFactory.serviceVersion_100;
	}

	@Override
	public void save() {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");

	}

	@Override
	public void setFixErrors(final boolean b) {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");

	}

	@Override
	public void setLog4jPropertiesFilename(final String s) {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");

	}

	@Override
	public void setPreferedSOSVersion(final String s) {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");

	}

	@Override
	public void setSOSconfigurationFilename(final String s) {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");
	}

	@Override
	public void setTimeToCacheCapabilities(final long l) {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");
	}

	@Override
	public void setTimeToCacheDatastore(final long l) {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");
	}

	@Override
	public String getProxyHost() {
		return "";
	}

	@Override
	public String getProxyPort() {
		return "";
	}

	@Override
	public void setProxyHost(final String host) {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");
	}

	@Override
	public void setProxyPort(final String port) {
		throw new UnsupportedOperationException(
				"Configuration Registry does not support write");
	}
	
	@Override
	public HashMap<String, IWorkaroundDescription> getWorkarounds() {
		return workarounds;
	}

}

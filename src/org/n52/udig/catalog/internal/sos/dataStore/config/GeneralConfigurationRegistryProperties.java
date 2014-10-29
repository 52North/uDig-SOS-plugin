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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.workarounds.EastingFirstWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.FalseBoundingBoxWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.IWorkaroundDescription;
import org.n52.udig.catalog.internal.sos.workarounds.NoCRSWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.TransformCRSWorkaroundDesc;

/**
 * General Configuration Uses java-properties-file to load and store the
 * configuration
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class GeneralConfigurationRegistryProperties extends
		GeneralConfigurationRegistry {

	private static Logger LOGGER = LoggingHandler
			.getLogger(GeneralConfigurationRegistryProperties.class);

	private final Properties properties = new Properties();

	/**
	 * default value for the LOG4Jpath
	 */
	private final String defaultLOG4Jpath = "D:\\\\Java\\log4j.properties";
	/**
	 * default value for the prefered SOS Version
	 * 
	 * @see SOSDataStoreFactory#guessServiceVersion(java.net.URL)
	 */
	private final String defaultPreferedSOSVersion = SOSDataStoreFactory.serviceVersion_100;
	/**
	 * default value for the SOSConfigurationFilename
	 */
	private final String defaultSOSConfigurationFilename = "sosConfiguration.xml";
	/**
	 * default value for the time capabilities should be cached in ms
	 */
	private final String defaultTimeToCacheCapabilities = 1000 * 60 * 5 + "";
	/**
	 * default value for the time datastores should be cached in ms
	 */
	private final String defaultTimeToCacheDatastore = 1000 * 60 * 5 + "";
	/**
	 * default value for the hostname of the used proxy
	 */
	private final String defaultProxyHost = "";
	/**
	 * default value for the port of used proxy
	 */
	private final String defaultProxyPort = "";
	/**
	 * the filename of this configurationFile
	 */
	private static String filename;
	private InputStream is;

	private HashMap<String, IWorkaroundDescription> workarounds = new HashMap<String, IWorkaroundDescription>();
		
	public GeneralConfigurationRegistryProperties() {
		// load dynamic workaround descriptions
		EastingFirstWorkaroundDesc w1 = new EastingFirstWorkaroundDesc();
		workarounds.put(w1.getIdentifier(),w1);
		
		TransformCRSWorkaroundDesc w2 = new TransformCRSWorkaroundDesc();
		workarounds.put(w2.getIdentifier(),w2);
		
		FalseBoundingBoxWorkaroundDesc w3 = new FalseBoundingBoxWorkaroundDesc();
		workarounds.put(w3.getIdentifier(), w3);
		
		NoCRSWorkaroundDesc w4 = new NoCRSWorkaroundDesc();
		workarounds.put(w4.getIdentifier(), w4);
		
		try {
			filename = new StringBuffer().
			// append((new File("")).getAbsolutePath()).
					// append(System.getProperty("file.separator")).
					append(System.getProperty("user.dir")).
					// append("config").
					append(System.getProperty("file.separator")).append(
							"UDIG-SOSPlugin.properties").toString();
			is = new FileInputStream(filename);
			properties.load(is);

		} catch (final FileNotFoundException fnee) {
			LOGGER.info("Configfile " + filename
					+ " cannot be found, using default values");
		} catch (final Exception e) {
			LOGGER.warn(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry#getLog4jPropertiesFilename()
	 */
	@Override
	public String getLog4jPropertiesFilename() {
		return properties.getProperty("log4jpath", defaultLOG4Jpath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry#getPreferedSOSVersion()
	 */
	@Override
	public String getPreferedSOSVersion() {
		return properties.getProperty("preferedSOSVersion",
				defaultPreferedSOSVersion);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry#getSOSconfigurationFilename()
	 */
	@Override
	public String getSOSconfigurationFilename() {
		return properties.getProperty("sosConfigurationFilename",
				defaultSOSConfigurationFilename);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry#getTimeToCacheCapabilities()
	 */
	@Override
	public long getTimeToCacheCapabilities() {
		return Long.parseLong(properties.getProperty("timeToCacheCapabilities",
				defaultTimeToCacheCapabilities));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry#getTimeToCacheDatastore()
	 */
	@Override
	public long getTimeToCacheDatastore() {
		return Long.parseLong(properties.getProperty("timeToCacheDatastore",
				defaultTimeToCacheDatastore));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry#isFixErrors()
	 */
	@Override
	public boolean isFixErrors() {
		return Boolean
				.parseBoolean(properties.getProperty("fixErrors", "true"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry#isWriteable()
	 */
	@Override
	public boolean isWriteable() {
		return true;
	}

	@Override
	public void setFixErrors(final boolean b) {
		properties.setProperty("fixErrors", Boolean.toString(b));
	}

	@Override
	public void setLog4jPropertiesFilename(final String s) {
		properties.setProperty("log4jpath", s);
	}

	@Override
	public void setPreferedSOSVersion(final String s) {
		properties.setProperty("preferedSOSVersion", s);
	}

	@Override
	public void setSOSconfigurationFilename(final String s) {
		properties.setProperty("sosConfigurationFilename", s);
	}

	@Override
	public void setTimeToCacheCapabilities(final long l) {
		properties.setProperty("timeToCacheCapabilities", l + "");
	}

	@Override
	public void setTimeToCacheDatastore(final long l) {
		properties.setProperty("timeToCacheDatastore", l + "");
	}

	@Override
	public void save() {
		try {
			if (is != null) {
				is.close();
			}
			// File f = new File(filename);
			// if (!f.exists()){
			// new File(f.getAbsolutePath()).createNewFile();
			// // f.createNewFile();
			// }
			final OutputStream os = new FileOutputStream(filename);
			properties.store(os, "UDigSOS-Plugin Configuration");
		} catch (final Exception e) {

			LOGGER.error(e);
		}
	}

	public static void main(final String[] args) {
		System.out.println(System.getProperty("user.dir"));
		
		// create exampleconf 
		GeneralConfigurationRegistry.getInstance().setFixErrors(false);
		GeneralConfigurationRegistry.getInstance().setTimeToCacheCapabilities(
				40000);
		GeneralConfigurationRegistry.getInstance().setSOSconfigurationFilename(
				"C:\\config\\sosconf.xml");
		GeneralConfigurationRegistry.getInstance().save();
	}

	@Override
	public String getProxyHost() {
		return properties.getProperty("proxyHost", defaultProxyHost);
	}

	@Override
	public String getProxyPort() {
		return properties.getProperty("proxyPort", defaultProxyPort);
	}

	@Override
	public void setProxyHost(final String host) {
		properties.setProperty("proxyHost", host);

	}

	@Override
	public void setProxyPort(final String port) {
		properties.setProperty("proxyPort", port);

	}

	@Override
	public HashMap<String, IWorkaroundDescription> getWorkarounds() {
		return workarounds;
	}
	
}
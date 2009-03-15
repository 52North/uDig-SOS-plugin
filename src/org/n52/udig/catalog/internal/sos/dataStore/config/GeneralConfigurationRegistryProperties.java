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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
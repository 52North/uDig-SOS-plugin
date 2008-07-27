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

import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;

/**
 * General Configuration Uses static strings load the configuration
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class GeneralConfigurationRegistryS extends GeneralConfigurationRegistry {
	private final static String SOSconfigurationFilename = "sosConfiguration.xml";
	private final static String log4jPropertiesFilename = "D:\\\\Java\\log4j.properties";

	protected GeneralConfigurationRegistryS() {

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

}

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
package org.n52.udig.catalog.internal.sos.dataStore.config;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlString;
import org.n52.oxf.serviceAdapters.ParameterShell;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.SOSCapabilities;
import org.x52N.schema.xmlConfigSchema.UDigSOSPluginDocument;
import org.x52N.schema.xmlConfigSchema.UDigSOSPluginDocument.UDigSOSPlugin.SOS;
import org.x52N.schema.xmlConfigSchema.UDigSOSPluginDocument.UDigSOSPlugin.SOS.Operation;
import org.x52N.schema.xmlConfigSchema.UDigSOSPluginDocument.UDigSOSPlugin.SOS.Operation.Parameter;

/**
 * @author Carsten Priess
 * 
 */
public class SOSConfigurationRegistry {
	/**
	 * current instance; singleton pattern
	 */
	// private static SOSConfigurationRegistry instance = null;
	private static HashMap<String, SOSConfigurationRegistry> instances = new HashMap<String, SOSConfigurationRegistry>(
			1);

	// private final String filename = "sosConfiguration.xml";
	// private final static String defaultfilename = "sosConfiguration.xml";
	private UDigSOSPluginDocument doc;
	private final String filename;

	// private SOSConfigurationRegistry(){
	// this("sosConfiguration.xml");
	// }

	private SOSConfigurationRegistry(final String filename2) {
		this.filename = filename2;
		try {
			doc = org.x52N.schema.xmlConfigSchema.UDigSOSPluginDocument.Factory
					.parse(new File(filename2));
		} catch (final IOException ioe) {
			LOGGER.debug(
					"Configurationfile cannot be opened, creating a new one",
					null);
			doc = org.x52N.schema.xmlConfigSchema.UDigSOSPluginDocument.Factory
					.newInstance();
			doc.addNewUDigSOSPlugin();
			try {
				doc.save(new File(filename2));
			} catch (final IOException ioe2) {
				LOGGER
						.warn(
								"Error creating configuration, maybe file is a directory?",
								ioe2);
			}
		} catch (final XmlException xmle) {
			LOGGER.warn("Error parsing configuration", xmle);
		}
	}

	/**
	 * Singleton-pattern, Returns the current instance of
	 * {@link SOSConfigurationRegistry} or returns a new instance
	 * 
	 * @return an instance of {@link SOSConfigurationRegistry}
	 */
	public static SOSConfigurationRegistry getInstance() {
		if (GeneralConfigurationRegistry.getInstance()
				.getSOSconfigurationFilename() != null) {
			return getInstance(GeneralConfigurationRegistry.getInstance()
					.getSOSconfigurationFilename());
		}
		return getInstance("sosConfiguration.xml");
	}

	/**
	 * Singleton-pattern, Returns the current instance of
	 * {@link SOSConfigurationRegistry} or returns a new instance
	 * 
	 * @param filename
	 * @return an instance of {@link SOSConfigurationRegistry}
	 */
	public static SOSConfigurationRegistry getInstance(final String filename) {
		if (!instances.containsKey(filename)) {
			instances.put(filename, new SOSConfigurationRegistry(filename));
		}
		return instances.get(filename);
	}

	/**
	 * Get a list of all sos configured in this registry/xml
	 * 
	 * @return a List with String-Elements containing all SOS, which are
	 *         configured with this registry
	 */
	public List<String> getConfiguredSOSs() {
		final List<String> l = new LinkedList<String>();
		if (doc.getUDigSOSPlugin() == null) {
			return null;
		}
		final SOS sos[] = doc.getUDigSOSPlugin().getSOSArray();
		for (final SOS currentSOS : sos) {
			l.add(currentSOS.getUrl().getStringValue());
		}
		return l;
	}

	public boolean addNewSOS(final String url) {
		final List<String> sosList = getConfiguredSOSs();
		// if SOS is already included in configfile, refuse the addition of a
		// new SOS
		if (sosList.contains(url)) {
			return false;
		}
		final SOS sos = doc.getUDigSOSPlugin().addNewSOS();
		sos.setUrl(XmlString.Factory.newValue(url));
		return true;
	}

	/**
	 * Write the current xml-doc to filesystem
	 */
	public void save() {
		try {
			doc.save(new File(filename));
		} catch (final IOException ioe) {
			LOGGER.warn("Error saving xml-configuration", ioe);
		}
	}

	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSConfigurationRegistry.class);

	private Operation createOperation(final SOS sos, final String operationID) {
		for (final Operation op : sos.getOperationArray()) {
			if (op.getName().equals(operationID)) {
				LOGGER.warn("This shouldn't happen");
				return op;
			}
		}
		final Operation op = sos.addNewOperation();
		op.setName(XmlString.Factory.newValue(operationID));
		return op;
	}

	private Operation getOperation(final SOS sos, final String operationID)
			throws NoSuchElementException {

		final Operation opArray[] = sos.getOperationArray();
		for (final Operation op : opArray) {
			if (op.getName().getStringValue().equals(operationID)) {
				return op;
			}
		}
		return createOperation(sos, operationID);
	}

	private SOS getSOS(final String url) throws NoSuchElementException {
		final SOS sosArray[] = doc.getUDigSOSPlugin().getSOSArray();
		for (final SOS sos : sosArray) {
			if (sos.getUrl().getStringValue().equals(url)) {
				return sos;
			}
		}
		return createSOS(url);
	}

	private Parameter getParameter(final Operation operation,
			final String parameterID) throws NoSuchElementException {
		final Parameter parameterArray[] = operation.getParameterArray();
		for (final Parameter p : parameterArray) {
			if (p.getId().getStringValue().equals(parameterID)) {
				return p;
			}
		}
		throw new NoSuchElementException("Parameter with ID " + parameterID
				+ " cannot be found in configfile");
	}

	private Parameter createParameter(final Operation operation,
			final String parameterID) {
		final Parameter p = operation.addNewParameter();
		p.setId(XmlString.Factory.newValue(parameterID));
		return p;
	}

	private SOS createSOS(final String url) {
		final SOS s = doc.getUDigSOSPlugin().addNewSOS();
		s.setUrl(XmlString.Factory.newValue(url));
		return s;
	}

	public boolean getOmitParameter(final String url, final String operationID,
			final String parameterID) {
		final SOS sos = getSOS(url);
		Operation op = null;

		try {
			op = getOperation(sos, operationID);
		} catch (final NoSuchElementException nsee1) {
			op = createOperation(sos, operationID);
		}

		Parameter p = null;
		try {
			p = getParameter(op, parameterID);
		} catch (final NoSuchElementException nsee2) {
			p = createParameter(op, parameterID);
		}

		if (p.getOmit() != null) {
			return p.getOmit().getStringValue().equals("true");
		}

		return false;
	}

	public boolean getShowOperation(final String url, final String operationID) {
		SOS sos;
		try {
			sos = getSOS(url);
		} catch (final NoSuchElementException nsee1) {
			sos = createSOS(url);
		}

		Operation op = null;

		try {
			op = getOperation(sos, operationID);
		} catch (final NoSuchElementException nsee1) {
			op = createOperation(sos, operationID);
		}

		if (op.getShowOperation() != null) {
			return op.getShowOperation().getStringValue().equals("true");
		}

		return true;
	}

	public void setShowOperation(final String url, final String operationID,
			final boolean show) {
		Operation op = null;
		try {
			op = getOperation(getSOS(url), operationID);
		} catch (final NoSuchElementException nseee1) {
			op = createOperation(getSOS(url), operationID);
		}
		if (op.getShowOperation() == null) {
			op.addNewShowOperation();
		}
		if (show) {
			op.getShowOperation().setStringValue("true");
		} else {
			op.getShowOperation().setStringValue("false");
		}
	}

	public void setOmitParameter(final String url, final String operationID,
			final String parameterID, final boolean omit) {
		Operation op = null;
		try {
			op = getOperation(getSOS(url), operationID);
		} catch (final NoSuchElementException nseee1) {
			op = createOperation(getSOS(url), operationID);
		}

		Parameter p = null;
		try {
			p = getParameter(op, parameterID);
		} catch (final NoSuchElementException nseee1) {
			p = createParameter(op, parameterID);
		}

		if (p.getOmit() == null) {
			p.addNewOmit();
		}

		if (omit) {
			p.getOmit().setStringValue("true");
		} else {
			p.getOmit().setStringValue("false");
		}
	}

	public void setParameter(final String url, final String operationID,
			final String parameterID, final Object values[]) {
		// if (values.length == 0){
		removeParameter(url, operationID, parameterID);
		// }
		for (final Object s : values) {
			setParameter(url, operationID, parameterID, s);
		}
	}

	public void removeParameter(final String url, final String operationID,
			final String parameterID) {
		Operation op = null;
		try {
			op = getOperation(getSOS(url), operationID);
		} catch (final NoSuchElementException nseee1) {
			op = createOperation(getSOS(url), operationID);
		}

		for (int i = 0; i < op.getParameterArray().length; i++) {
			if (op.getParameterArray(i).getId().getStringValue().equals(
					parameterID)) {
				op.removeParameter(i);
				return;
			}
		}
	}

	public void setParameter(final String url, final String operationID,
			final String parameterID, final Object value) {
		Operation op = null;
		try {
			op = getOperation(getSOS(url), operationID);
		} catch (final NoSuchElementException nseee1) {
			op = createOperation(getSOS(url), operationID);
		}

		Parameter p = null;
		try {
			p = getParameter(op, parameterID);
		} catch (final NoSuchElementException nseee1) {
			p = createParameter(op, parameterID);
		}

		p.addValue((String) value);
	}

	// public Object[] getParameters(final String url, final String
	// operationID){
	// Operation op = null;
	// try {
	// op = getOperation(getSOS(url), operationID);
	// } catch (final NoSuchElementException nseee1) {
	// op = createOperation(getSOS(url), operationID);
	// }
	//
	// return op.get
	//
	// org.n52.oxf.owsCommon.capabilities.Parameter pout[] = new
	// org.n52.oxf.owsCommon.capabilities.Parameter[op.getParameterArray().length];
	// int i =0;
	// for (Parameter p:op.getParameterArray()){
	// pout[i++] = new org.n52.oxf.owsCommon.capabilities.Parameter()
	// }

	// }

	public String[] getParameter(final String url, final String operationID,
			final String parameterID) {
		Operation op = null;
		try {
			op = getOperation(getSOS(url), operationID);
		} catch (final NoSuchElementException nseee1) {
			op = createOperation(getSOS(url), operationID);
		}

		Parameter p = null;
		try {
			p = getParameter(op, parameterID);
		} catch (final NoSuchElementException nseee1) {
			p = createParameter(op, parameterID);
		}

		// String returnArray[] = new String[p.getValueArray().length];
		// int i=0;
		// for(String value:p.getValueArray()){
		// returnArray[i++] = value;
		// }
		// return returnArray;
		return p.getValueArray();
	}

	public void removeSOS(final String url) {
		final SOS sosArray[] = doc.getUDigSOSPlugin().getSOSArray();
		int i = 0;
		for (final SOS sos : sosArray) {
			if (sos.getUrl().getStringValue().equals(url)) {
				doc.getUDigSOSPlugin().removeSOS(i);
				return;
			}
			i++;
		}
	}

	// public void configureWithParameterConfiguration(ParameterConfiguration
	// pc){
	// for (ParameterShell ps :
	// pc.getConfiguredParameterContainer().getParameterShells()){
	// setParameter(pc.getUrl(), pc.getOperationId(),
	// ps.getParameter().getServiceSidedName(), ps.getSpecifiedValueArray());
	// }
	// }

	public void setParameterConfiguration(final String url,
			final String operation, final ParameterConfiguration pc) {
		removeSOS(url);
		createSOS(url);
		createOperation(getSOS(url), operation);

		for (final ParameterShell ps : pc.getConfiguredParameterContainer()
				.getParameterShells()) {
			if (ps.hasMultipleSpecifiedValues()) {
				setParameter(url, operation, ps.getParameter()
						.getServiceSidedName(), ps.getSpecifiedValueArray());
			} else {
				setParameter(url, operation, ps.getParameter()
						.getServiceSidedName(), ps.getSpecifiedValue());
			}

		}

	}

	public ParameterConfiguration updateParameterConfiguration(
			final String url, final String operation,
			final ParameterConfiguration pc) {
		final Operation op = getOperation(getSOS(url), operation);
		try {
			for (final Parameter p : op.getParameterArray()) {
				if (p.getValueArray().length > 0) {
					if (p.getValueArray().length == 1) {
						pc.setParameterValue(p.getId().getStringValue(), p
								.getValueArray()[0]);
					} else {
						pc.setParameterValue(p.getId().getStringValue(), p
								.getValueArray());
					}

				}

			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return pc;
	}

	public ParameterConfiguration getParameterConfiguration(final String url,
			final String operation) throws IOException {
		SOSCapabilities caps = null;
		// try {
		// caps = new SOSCapabilities(new
		// SOSAdapter(SOSDataStoreFactory.serviceVersion_100),new URL(url));
		caps = SOSCapabilities.getCapabilities(new URL(url));
		// } catch (final IOException ioe) {
		// ioe.printStackTrace();
		// }
		final ParameterConfiguration pc = ((SOSOperationType) caps
				.getOperations().getOperationTypeByName(operation))
				.getCapabilitiesConfiguration();
		final Operation op = getOperation(getSOS(url), operation);

		try {
			for (final Parameter p : op.getParameterArray()) {
				if (p.getValueArray().length > 0) {
					if (p.getValueArray().length == 1) {
						pc.setParameterValue(p.getId().getStringValue(), p
								.getValueArray()[0]);
					} else {
						pc.setParameterValue(p.getId().getStringValue(), p
								.getValueArray());
					}
				}

			}
		} catch (final Exception e) {
			LOGGER.fatal(e);
		}
		return pc;
	}
}
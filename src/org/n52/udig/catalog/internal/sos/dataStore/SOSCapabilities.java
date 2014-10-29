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
package org.n52.udig.catalog.internal.sos.dataStore;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.opengis.sos.x00.CapabilitiesDocument;
import net.opengis.sos.x10.GetCapabilitiesDocument;

import org.apache.log4j.Logger;
import org.geotools.data.ows.Capabilities;
import org.geotools.data.ows.Service;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.n52.oxf.OXFException;
import org.n52.oxf.owsCommon.ExceptionReport;
import org.n52.oxf.owsCommon.ServiceDescriptor;
import org.n52.oxf.owsCommon.capabilities.Dataset;
import org.n52.oxf.owsCommon.capabilities.Operation;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.oxf.owsCommon.capabilities.ServiceProvider;
import org.n52.oxf.serviceAdapters.IServiceAdapter;
import org.n52.oxf.serviceAdapters.OperationResult;
import org.n52.oxf.serviceAdapters.ParameterContainer;
import org.n52.oxf.serviceAdapters.sos.ISOSRequestBuilder;
import org.n52.oxf.serviceAdapters.sos.SOSAdapter;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;
import org.n52.udig.catalog.internal.sos.workarounds.EastingFirstWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.FalseBoundingBoxWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.NoCRSWorkaroundDesc;
import org.n52.udig.catalog.internal.sos.workarounds.TransformCRSWorkaroundDesc;
import org.opengis.metadata.citation.Address;
import org.opengis.metadata.citation.Contact;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.Telephone;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.InternationalString;

import com.vividsolutions.jts.geom.Coordinate;


class CapabilitiesCreator implements Callable<OperationResult> {
	private final SOSAdapter adapter;
	private final ParameterContainer paramCon;
	private final URL serviceURL;

	public CapabilitiesCreator(final SOSAdapter adapter, final URL serviceURL,
			final ParameterContainer paramCon) {
		this.adapter = adapter;
		this.serviceURL = serviceURL;
		this.paramCon = paramCon;
	}

	public OperationResult call() throws Exception {
		return adapter.doOperation(new Operation("GetCapabilities", //$NON-NLS-1$
				serviceURL.toExternalForm() + "?", //$NON-NLS-1$
				serviceURL.toExternalForm()), paramCon);
	}
}

/**
 * This class offers an object that holds and distributes the information from
 * the SensorObservation Service GetCapabilites-Request
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSCapabilities extends Capabilities {
	class MyInternationalString implements InternationalString {

		String s;

		public MyInternationalString(final String s) {
			this.s = s;
		}

		public char charAt(final int arg0) {
			return s.charAt(arg0);
		}

		public int compareTo(final Object arg0) {
			return s.compareTo((String) arg0);
		}

		public int length() {
			return s.length();
		}

		public CharSequence subSequence(final int beginIndex, final int endIndex) {
			return s.subSequence(beginIndex, endIndex);
		}

		public String toString(final Locale arg0) {
			return s;
		}

	}

	class SOSOperationsCreator implements Callable<SOSOperations> {
		ServiceDescriptor oxfService;

		// List<Dataset> observationOffering;

		public SOSOperationsCreator(final ServiceDescriptor oxfService,
				final List<Dataset> observationOffering) {
			this.oxfService = oxfService;
			// this.observationOffering = observationOffering;
		}

		public SOSOperations call() throws Exception {
			final SOSOperations operations = new SOSOperations();

			final Operation op[] = oxfService.getOperationsMetadata()
					.getOperations();
			// check all available operations
			
			for (final Operation element : op) {
				final SOSOperationType opType = new SOSOperationType(element
						.getName(), serviceURL.toExternalForm(), oxfService.getContents());
				opType.getFormats();

				// all operations have DCP-Elements
				for (int j = 0; j < element.getDcps().length; j++) {
					try {
						opType.setGet(new URL(element.getDcps()[j]
								.getHTTPGetRequestMethods().get(0)
								.getOnlineResource().getHref()));
					} catch (final MalformedURLException e) {
						// fehlerhandling nicht notwendig
					} catch (final IndexOutOfBoundsException npe) {
						// IndexOutOfBounds just means, that
						// getHTTPGetRequestMethods() empty list -> no get
						// available
					}
					try {
						opType.setPost(new URL(element.getDcps()[j]
								.getHTTPPostRequestMethods().get(0)
								.getOnlineResource().getHref()));
					} catch (final MalformedURLException e) {
						// fehlerhandling nicht notwendig
					} catch (final IndexOutOfBoundsException npe) {
						// IndexOutOfBounds just means, that
						// getHTTPPostRequestMethods() empty list -> no post
						// available
					}

					try {
						// Get required and optional parameters
						final List<Parameter> parameters = element
								.getParameters();
						for (final Parameter parameter : parameters) {
							opType.addParameterFromCaps(parameter);
						}

						if (element.getName().equals(
								SOSOperations.opName_GetCapabilities)) {
							operations.setGetCapabilities(opType);
							LOGGER.debug("Added operation: "
									+ element.getName());
						} else if (element.getName().equals(
								SOSOperations.opName_DescribeSensor)) {
							operations.setDescribeSensor(opType);
							LOGGER.debug("Added operation: "
									+ element.getName());
						} else if (element.getName().equals(
								SOSOperations.opName_GetFeatureOfInterest)) {
							operations.setGetFeatureOfInterest(opType);
							LOGGER.debug("Added operation: "
									+ element.getName());
						} else if (element.getName().equals(
								SOSOperations.opName_GetObservation)) {
							operations.setGetObservation(opType);
							LOGGER.debug("Added operation: "
									+ element.getName());
						} else if (element.getName().equals(
								SOSOperations.opName_GetObservationById)) {
							operations.setGetObservationById(opType);
							LOGGER.debug("Added operation: "
									+ element.getName());
						} else {
							LOGGER.info("Operation " + element.getName()
									+ " not supported by plugin");
						}

					} catch (final OXFException e) {
						e.printStackTrace();
					} catch (final UnsupportedOperationException e) {
						LOGGER.info(e.getMessage());
					}
				}
			}
			return operations;
		}

	}

	class SOSResponsibleParty implements ResponsibleParty {

		class SOSContact implements Contact {

			InternationalString contactInstructions = null;

			protected SOSContact(final ServiceProvider provider) {
				if (provider.getServiceContact() != null) {
					if (provider.getServiceContact().getContactInfo() != null) {
						contactInstructions = new MyInternationalString(
								provider.getServiceContact().getContactInfo()
										.getContactInstructions());
						posName = provider.getServiceContact()
								.getPositionName();
					}
				}
			}

			public Address getAddress() {
				// TODO Auto-generated method stub
				return null;
			}

			public InternationalString getContactInstructions() {
				return contactInstructions;
			}

			public InternationalString getHoursOfService() {
				// TODO Auto-generated method stub
				return null;
			}

			public OnLineResource getOnLineResource() {
				// TODO Auto-generated method stub
				return null;
			}

			public Telephone getPhone() {
				return null;
			}
		}

		Contact contact = null;
		String individualname = null;
		String orgName = null;
		String posName = null;

		Role role = null;

		public SOSResponsibleParty(final ServiceProvider provider) {
			if (provider != null) {
				this.contact = new SOSContact(provider);
				if (provider.getProviderName() != null) {
					this.orgName = provider.getServiceContact()
							.getIndividualName();
				}
				if (provider.getServiceContact() != null) {
					this.individualname = provider.getServiceContact()
							.getIndividualName();
					this.posName = provider.getServiceContact()
							.getPositionName();
				}
			}
			// if (provider.getProviderName() != null)
		}

		public Contact getContactInfo() {
			return contact;
		}

		public String getIndividualName() {
			return individualname;
		}

		public InternationalString getOrganisationName() {
			return new MyInternationalString(orgName);
		}

		public InternationalString getPositionName() {
			return new MyInternationalString(posName);
		}

		public Role getRole() {
			return role;
		}
	}

	class SOSServiceInformationCreator implements Callable<Service> {

		ServiceDescriptor oxfService;
		Service service;

		public SOSServiceInformationCreator(final ServiceDescriptor oxfService) {
			this.oxfService = oxfService;
		}

		public Service call() throws Exception {
			final Service service = new Service();
			// title

			// LOGGER.info("Service title: "
			// + oxfService.getServiceIdentification().getTitle());
			// service.setTitle(oxfService.getServiceIdentification().getTitle());
			// abstract
			// LOGGER.info("Service abstract: "
			// + oxfService.getServiceIdentification()
			// .getAbstractDescription());
			service.set_abstract(oxfService.getServiceIdentification()
					.getAbstractDescription());

			// keywords
			for (int j = 0; j < oxfService.getServiceIdentification()
					.getKeywords().length; j++) {
				// LOGGER.info("Service keyword: "
				// + oxfService.getServiceIdentification().getKeywords()[j]);
			}
			service.setKeywordList(oxfService.getServiceIdentification()
					.getKeywords());

			// service.
			// LOGGER.info("Service
			// type"+oxfService.getServiceIdentification().getServiceType());

//			service.setContactInformation(new SOSResponsibleParty(oxfService
//					.getServiceProvider()));

			// ResponsibleParty resParty = new SOSResponsibleParty();
			// service.setContactInformation(oxfService.getServiceProvider().)
			// oxfService.getServiceIdentification().getAccessConstraints()
			// oxfService.getServiceIdentification().getFees()

			// keywords

			// oxfService.getServiceIdentification().getServiceType()
			// oxfService.getServiceIdentification().getServiceTypeVersion()

			return service;
		}

	}

	private static HashMap<URL, SOSCapabilities> capsCache = new HashMap<URL, SOSCapabilities>();

	private static void addToCache(final URL serviceURL,
			final SOSCapabilities soscaps) {
		soscaps.setLastUpdated(System.currentTimeMillis());
		capsCache.put(serviceURL, soscaps);
	}

	private static SOSCapabilities getFromCache(final URL serviceURL)
			throws IOException {
		if (!capsCache.containsKey(serviceURL)
				|| capsCache.get(serviceURL).serviceVersions.isEmpty()
				|| capsCache.get(serviceURL).getLastUpdated()
						- System.currentTimeMillis() > GeneralConfigurationRegistry
						.getInstance().getTimeToCacheCapabilities()) {
			addToCache(serviceURL, new SOSCapabilities(null, serviceURL));
			// final SOSCapabilities caps = new SOSCapabilities(null,
			// serviceURL);
		}
		return capsCache.get(serviceURL);
	}

	/**
	 * The Apache Log4j logger, which can be configured in
	 * {@link GeneralConfigurationRegistry}
	 */
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSCapabilities.class);

	private final ExecutorService threadPool = Executors.newFixedThreadPool(3);

	/**
	 * Checks if serviceURL is an URL to a SensorObservationService and returns
	 * a list of all supported versions
	 * 
	 * @param serviceURL
	 *            the service's url
	 * @return a List<String> of all supported versions
	 * @see SOSDataStoreFactory#SERVICE_VERSION
	 * @throws IOException
	 */
	public static List<String> checkForSOS(final URL serviceURL)
			throws IOException {
		// if (cache.containsKey(serviceURL)) {
		// if (!cache.get(serviceURL).serviceVersions.isEmpty()){
		// return cache.get(serviceURL).serviceVersions;
		// }
		// }
		return identifyVersion(serviceURL);
	}

	private long lastUpdated;

	public static SOSCapabilities getCapabilities(final URL serviceURL)
			throws IOException {
		// try {

		if (!(((HttpURLConnection) serviceURL.openConnection())
				.getResponseCode() == HttpURLConnection.HTTP_OK)) {
			throw new IOException("Could not connect to SOS at " + serviceURL
					+ ". Check your connection, settings and the URL");
		}
		// } catch ( IOException e ) {
		// throw new IOException("Could not connect to SOS", e);
		// }
		return getFromCache(serviceURL);
	}

	protected static List<String> identifyVersion(final URL serviceURL) {
		if (!capsCache.containsKey(serviceURL)
				|| capsCache.get(serviceURL).serviceVersions.isEmpty()
				|| capsCache.get(serviceURL).getLastUpdated()
						- System.currentTimeMillis() > GeneralConfigurationRegistry
						.getInstance().getTimeToCacheCapabilities()) {
			final ExecutorService threadPool = Executors.newFixedThreadPool(3);
			final SOSAdapter adapter_000 = new SOSAdapter(
					SOSDataStoreFactory.serviceVersion_000);
			final SOSAdapter adapter_100 = new SOSAdapter(
					SOSDataStoreFactory.serviceVersion_100);
			// final ExecutorService tp = Executors.newFixedThreadPool(2);
			// try 1.0 and 0.0
			final List<String> supportedVersions = new LinkedList<String>();
			final ParameterContainer paramCon_000 = new ParameterContainer();
			Future<OperationResult> result100Future = null;
			Future<OperationResult> result000Future = null;
			try {
				// 0.0
				paramCon_000
						.addParameterShell(
								ISOSRequestBuilder.GET_CAPABILITIES_ACCEPT_VERSIONS_PARAMETER,
								SOSAdapter.SUPPORTED_VERSIONS[0]);
				paramCon_000.addParameterShell(
						ISOSRequestBuilder.GET_CAPABILITIES_SERVICE_PARAMETER,
						SOSAdapter.SERVICE_TYPE);
				// paramCon_000.addParameterShell(
				// ISOSRequestBuilder.GET_CAPABILITIES_SECTIONS_PARAMETER,
				// "ServiceIdentification");

				result000Future = threadPool.submit(new CapabilitiesCreator(
						adapter_000, serviceURL, paramCon_000));

			} catch (final Exception e) {
				// ignore
			}

			try {
				// 1.0
				final ParameterContainer paramCon_100 = new ParameterContainer();
				paramCon_100
						.addParameterShell(
								ISOSRequestBuilder.GET_CAPABILITIES_ACCEPT_VERSIONS_PARAMETER,
								SOSAdapter.SUPPORTED_VERSIONS[1]);
				paramCon_100.addParameterShell(
						ISOSRequestBuilder.GET_CAPABILITIES_SERVICE_PARAMETER,
						SOSAdapter.SERVICE_TYPE);
				// paramCon_100.addParameterShell(
				// ISOSRequestBuilder.GET_CAPABILITIES_SECTIONS_PARAMETER,
				// "ServiceIdentification");

				result100Future = threadPool.submit(new CapabilitiesCreator(
						adapter_100, serviceURL, paramCon_100));

			} catch (final Exception e) {
				e.printStackTrace();
			}
			OperationResult opRes_000 = null;
			if (result000Future != null) {

				try {
					opRes_000 = result000Future.get();
					final CapabilitiesDocument caps_000 = CapabilitiesDocument.Factory
							.parse(opRes_000.getIncomingResultAsStream());
					if (caps_000.getCapabilities().getServiceIdentification()
							.getServiceType().getStringValue().contains("SOS")) {
						final String[] versions = caps_000.getCapabilities()
								.getServiceIdentification()
								.getServiceTypeVersionArray();
						for (final String s : versions) {
							if (!supportedVersions.contains(s)) {
								supportedVersions.add(s);
							}
						}
					}

				} catch (final Exception e) {
					LOGGER.info("SOS-version 0.0.0 not supported");
					// LOGGER.debug(e);
				}
			}
			OperationResult opRes_100 = null;
			if (result100Future != null) {

				try {
					opRes_100 = result100Future.get();
					GetCapabilitiesDocument.Factory.parse(opRes_100
							.getSendedRequest());
					// LOGGER.debug(opRes_100.getSendedRequest());
					final net.opengis.sos.x10.CapabilitiesDocument caps_100 = net.opengis.sos.x10.CapabilitiesDocument.Factory
							.parse(opRes_100.getIncomingResultAsStream());
					if (caps_100.getCapabilities().getServiceIdentification()
							.getServiceType().getStringValue().contains("SOS")) {
						final String[] versions = caps_100.getCapabilities()
								.getServiceIdentification()
								.getServiceTypeVersionArray();
						for (final String s : versions) {
							if (!supportedVersions.contains(s)) {
								supportedVersions.add(s);
							}
						}
					}
				} catch (final Exception e) {
					LOGGER.info("SOS-version 1.0.0 not supported");
					// LOGGER.debug(e);
				}
			}

			try {
				if (supportedVersions
						.contains(SOSDataStoreFactory.serviceVersion_100)
						&& opRes_100 != null) {

					addToCache(serviceURL, new SOSCapabilities(adapter_100,
							SOSDataStoreFactory.serviceVersion_100, serviceURL,
							opRes_100, supportedVersions));
				} else if (supportedVersions
						.contains(SOSDataStoreFactory.serviceVersion_000)
						&& opRes_000 != null) {
					addToCache(serviceURL, new SOSCapabilities(adapter_000,
							SOSDataStoreFactory.serviceVersion_000, serviceURL,
							opRes_000, supportedVersions));
				}
			} catch (final Exception e) {
				LOGGER.debug(e);
			}
			threadPool.shutdown();
			return supportedVersions;
		} else {
			return capsCache.get(serviceURL).serviceVersions;
		}
	}

	private SOSAdapter adapter;

	private List<Dataset> observationOffering;

	Future<SOSOperations> operationsFuture = null;

	private OperationResult opResult;

	// // for testing purposes
	// private OperationResult generateCapabilitiesFromFile() {
	// try {
	// // InputStream is = new FileInputStream(new File("sosCapabilities_"
	// // + SOSDataStoreFactory.serviceVersion + ".xml"));
	// final InputStream is = new FileInputStream("D:\\Java\\sos.xml");
	// final OperationResult opRes = new OperationResult(is,
	// createGenerateCapsParameterContainer(), "GetCapabilities");
	// return opRes;
	// } catch (final Exception e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	private ServiceDescriptor oxfService;

	Future<Service> serviceFuture = null;

	private URL serviceURL;

	private List<String> serviceVersions = new LinkedList<String>();

	/**
	 * Creates a new instance of SOSCapabilities. It calls threads to get and
	 * parse capabilities in the background.
	 * 
	 * @param adapter2
	 *            SOS-Adapter for the OX-Framework
	 * @param serviceURL
	 *            Service url for the sos
	 * @throws IOException
	 */
	private SOSCapabilities(final SOSAdapter adapter2, final URL serviceURL)
			throws IOException {
		try {
			this.serviceURL = serviceURL;
			this.adapter = adapter2;

			if (adapter == null) {
				this.adapter = new SOSAdapter(SOSDataStoreFactory.getInstance()
						.guessServiceVersion(serviceURL));
			}
			// generate and send request
			opResult = generateCapabilitiesFromSOS(adapter);

			if (adapter.getServiceVersion() != null) {
				this.oxfService = adapter.initService(opResult, adapter
						.getServiceVersion());
			} else {
				this.oxfService = adapter.initService(opResult,
						SOSDataStoreFactory.getInstance().guessServiceVersion(
								serviceURL));
			}

			LOGGER.debug(URLDecoder
					.decode(opResult.getSendedRequest(), "UTF-8"));
			// LOGGER.debug(message)
			// parse it
			parseCapabilities();
			lastUpdated = System.currentTimeMillis();

		} catch (final OXFException oxfe) {
			final IOException iox = new IOException(oxfe.getMessage());
			iox.initCause(oxfe.getCause());
			iox.setStackTrace(oxfe.getStackTrace());
			throw iox;
		} catch (final ExceptionReport er) {
			final IOException iox = new IOException(er.getMessage());
			iox.setStackTrace(er.getStackTrace());
			throw iox;
		}
	}

	/**
	 * Creates a new instance of SOSCapabilities. (if serviceVersion == null)
	 * serviceVersion is guessed automatically by
	 * {@link SOSDataStoreFactory#guessServiceVersion(URL)} (if versions != null &&
	 * !versions.isEmpty()) service version is set wit
	 * {@link SOSDataStoreFactory#getPreferredServiceVersion(List)}
	 * 
	 * @param adapter2
	 *            the SOSAdapter
	 * @param serviceVersion
	 *            the version that should be used;
	 * @param serviceURL
	 *            the URL the service is found at
	 * @param opResult
	 * @param versions
	 *            a list of all supported versions, to allow switching between
	 *            versions
	 * @throws IOException
	 * 
	 * @see IServiceAdapter
	 * @see SOSDataStoreFactory#SERVICE_VERSION
	 */
	private SOSCapabilities(final SOSAdapter adapter2, String serviceVersion,
			final URL serviceURL, final OperationResult opResult,
			final List<String> versions) throws IOException {
		try {
			this.serviceURL = serviceURL;
			this.adapter = adapter2;
			this.opResult = opResult;

			if (serviceVersion == null) {
				if (versions != null && !versions.isEmpty()) {
					serviceVersion = SOSDataStoreFactory.getInstance()
							.getPreferredServiceVersion(versions);
				} else {
					serviceVersion = SOSDataStoreFactory.getInstance()
							.guessServiceVersion(serviceURL);
				}
			} else {
				if ((this.adapter == null)
						|| (!this.adapter.getServiceVersion().equals(
								serviceVersion))) {
					this.adapter = new SOSAdapter(serviceVersion);
				}

				this.oxfService = adapter.initService(opResult, adapter
						.getServiceVersion());
			}

			// parse it
			parseCapabilities();

			this.serviceVersions = versions;
			lastUpdated = System.currentTimeMillis();

		} catch (final OXFException oxfe) {
			final IOException iox = new IOException(oxfe.getMessage());
			iox.initCause(oxfe.getCause());
			iox.setStackTrace(oxfe.getStackTrace());
			throw iox;
		} catch (final ExceptionReport er) {
			final IOException iox = new IOException(er.getMessage());
			iox.setStackTrace(er.getStackTrace());
			throw iox;
		}
	}

	/**
	 * Creates a Parameter container for GetCapabilitiesRequest
	 * 
	 * @param serviceVersion
	 *            the SOS service version
	 * @return ParameterContainer for the GetCapabilities request
	 * @throws OXFException
	 *             connection error
	 * @see SOSDataStoreFactory#SERVICE_VERSION
	 */
	private ParameterContainer createGenerateCapsParameterContainer(
			final String serviceVersion) throws OXFException {
		final ParameterContainer paramCon = new ParameterContainer();
		if (serviceVersion.equals(SOSDataStoreFactory.serviceVersion_000)) {
			paramCon
					.addParameterShell(
							ISOSRequestBuilder.GET_CAPABILITIES_ACCEPT_VERSIONS_PARAMETER,
							SOSAdapter.SUPPORTED_VERSIONS[0]);
			paramCon.addParameterShell(
					ISOSRequestBuilder.GET_CAPABILITIES_SERVICE_PARAMETER,
					SOSAdapter.SERVICE_TYPE);
		} else if (serviceVersion
				.equals(SOSDataStoreFactory.serviceVersion_100)) {
			paramCon
					.addParameterShell(
							ISOSRequestBuilder.GET_CAPABILITIES_ACCEPT_VERSIONS_PARAMETER,
							SOSAdapter.SUPPORTED_VERSIONS[1]);
			paramCon.addParameterShell(
					ISOSRequestBuilder.GET_CAPABILITIES_SERVICE_PARAMETER,
					SOSAdapter.SERVICE_TYPE);
		}
		return paramCon;
	}

	private OperationResult generateCapabilitiesFromSOS(final SOSAdapter adapter)
			throws ExceptionReport, OXFException, IOException {
		LOGGER.info("Giving GetCapabilities-Request to OxF with " + serviceURL);
		String serviceVersion = null;
		if (adapter.getServiceVersion() != null) {
			serviceVersion = adapter.getServiceVersion();
		} else {
			serviceVersion = SOSDataStoreFactory.getInstance()
					.guessServiceVersion(serviceURL);
			if (serviceVersion == null) {
				throw new IOException(
						"Could not guess service version, check URL and or connection settings");
			} else {
				this.oxfService = adapter.initService(opResult, serviceVersion);
			}
		}
		// DO IT
		// return threadPool.submit(new CapabilitiesCreator(adapter, serviceURL,
		// createGenerateCapsParameterContainer(serviceVersion)));
		return adapter.doOperation(new Operation("GetCapabilities", //$NON-NLS-1$
				serviceURL.toExternalForm() + "?", //$NON-NLS-1$
				serviceURL.toExternalForm()),
				createGenerateCapsParameterContainer(serviceVersion));
	}

	/**
	 * @return the adapter
	 */
	public SOSAdapter getAdapter() {
		return adapter;
	}

	private ReferencedEnvelope getBBoxFromOffering(final String typeName) {
		for (int i = 0; i < oxfService.getContents()
		.getDataIdentificationCount(); i++) {
			if (oxfService.getContents().getDataIdentification(i)
					.getIdentifier().equals(typeName)) {
				String crs = oxfService.getContents().getDataIdentification(i).getAvailableCRSs()[0].toUpperCase();

				try {
					if (crs.contains("EPSG:")) {
						crs = crs.substring(crs.indexOf("EPSG:"));
						if (crs.equals("EPSG:0")){
							//WORKAROUND NO CRS
							NoCRSWorkaroundDesc noCRSWorkaroundDesc = (NoCRSWorkaroundDesc)GeneralConfigurationRegistry.getInstance().getWorkarounds().get(NoCRSWorkaroundDesc.identifier);
							crs = noCRSWorkaroundDesc.workaround(crs);
						}			
					}

					
					ReferencedEnvelope tempbbox;
					if (SOSConfigurationRegistry.getInstance().getWorkaroundState(serviceURL.toExternalForm(), EastingFirstWorkaroundDesc.identifier)){
//						tempbbox = EastingFirstWorkaroundDesc.workaround(tempbbox);
						tempbbox = new ReferencedEnvelope(
								oxfService.getContents().getDataIdentification(i)
								.getBoundingBoxes()[0].getLowerCorner()[1],
								oxfService.getContents().getDataIdentification(i)
								.getBoundingBoxes()[0].getUpperCorner()[1],
								oxfService.getContents().getDataIdentification(i)
								.getBoundingBoxes()[0].getLowerCorner()[0],
								oxfService.getContents().getDataIdentification(i)
								.getBoundingBoxes()[0].getUpperCorner()[0],
								CRS.decode(crs));
						// WORKAROUND

						tempbbox.expandToInclude(new Coordinate(
								tempbbox.getMaxX() + 0.02,
								tempbbox.getMaxY() + 0.02));
						tempbbox.expandToInclude(new Coordinate(
								tempbbox.getMinX() - 0.02,
								tempbbox.getMinY() - 0.02));
					} else{
						tempbbox = new ReferencedEnvelope(
								oxfService.getContents().getDataIdentification(i)
								.getBoundingBoxes()[0].getLowerCorner()[0],
								oxfService.getContents().getDataIdentification(i)
								.getBoundingBoxes()[0].getUpperCorner()[0],
								oxfService.getContents().getDataIdentification(i)
								.getBoundingBoxes()[0].getLowerCorner()[1],
								oxfService.getContents().getDataIdentification(i)
								.getBoundingBoxes()[0].getUpperCorner()[1],
								CRS.decode(crs));
						tempbbox.expandToInclude(new Coordinate(
								tempbbox.getMaxX() + 0.02,
								tempbbox.getMaxY() + 0.02));
						tempbbox.expandToInclude(new Coordinate(
								tempbbox.getMinX() - 0.02,
								tempbbox.getMinY() - 0.02));
					}

					if (SOSConfigurationRegistry.getInstance().getWorkaroundState(serviceURL.toExternalForm(), TransformCRSWorkaroundDesc.identifier)){
						// WORKAROUND -> TRANSFORM CRS -> WGS84targetCRS
						// TODO use parameters from settings
//						TransformCRSWorkaroundDesc transformWorkaroundDesc = (TransformCRSWorkaroundDesc)GeneralConfigurationRegistry.getInstance().getWorkarounds().get(TransformCRSWorkaroundDesc.identifier);
						
//						EastingFirstWorkaroundDesc eastingFirstWorkaroundDesc = (EastingFirstWorkaroundDesc)GeneralConfigurationRegistry.getInstance().getWorkarounds().get(EastingFirstWorkaroundDesc.identifier);

						TransformCRSWorkaroundDesc transcrsworkDesc = (TransformCRSWorkaroundDesc)GeneralConfigurationRegistry.getInstance().getWorkarounds().get(TransformCRSWorkaroundDesc.identifier);
//						CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:4326");
//						CoordinateReferenceSystem targetCRS = CRS.decode(SOSConfigurationRegistry.getInstance().getWorkaroundParameter(
//								serviceURL.toExternalForm(),
//								EastingFirstWorkaroundDesc.identifier, 
//								eastingFirstWorkaroundDesc.getParameters()[0].key));
//						tempbbox = TransformCRSWorkaroundDesc.workaround(tempbbox, targetCRS);
						CoordinateReferenceSystem targetCRS = null;
						try {
								targetCRS = CRS.decode(SOSConfigurationRegistry.getInstance().getWorkaroundParameter(
								serviceURL.toExternalForm(),
								TransformCRSWorkaroundDesc.identifier, 
								transcrsworkDesc.getParameters()[0].key));
						} catch (org.opengis.referencing.NoSuchAuthorityCodeException e) {
							targetCRS = CRS.decode(
									(String)transcrsworkDesc.getDefaultValue(transcrsworkDesc.getParameters()[0]));
						}
						
						tempbbox = TransformCRSWorkaroundDesc.workaround(tempbbox, targetCRS);
					}

					if (SOSConfigurationRegistry.getInstance().getWorkaroundState(serviceURL.toExternalForm(), FalseBoundingBoxWorkaroundDesc.identifier)){
						// WORKAROUND Change BBOX
						// TODO use parameters from settings
//						FalseBoundingBoxWorkaroundDesc falseBBoxWorkaroundDesc = (FalseBoundingBoxWorkaroundDesc)GeneralConfigurationRegistry.getInstance().getWorkarounds().get(FalseBoundingBoxWorkaroundDesc.identifier);
						tempbbox = FalseBoundingBoxWorkaroundDesc.workaround(tempbbox);
					}

					return tempbbox;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
		LOGGER.warn("No BoundingBox Found");
		return null;
	}

	public ReferencedEnvelope getBoundingBox(final String operation, final String typeName){
		if (operation.equals(SOSOperations.opName_GetObservation)) {
//			final ReferencedEnvelope re = getBBoxFromOffering(typeName.substring(0,typeName.indexOf("#")));
			final ReferencedEnvelope re = getBBoxFromOffering(typeName);
			if (re == null) {
				LOGGER.warn("No BoundingBox Found");
			}
			return re;
		}

		LOGGER.warn("No BoundingBox Found");
		return null;
	}

	/**
	 * Returns the capabilities document as a String
	 * 
	 * @see String
	 * @return a String containing the text of the incoming capabilities-doc
	 */
	public String getCapabilitiesString() {
		return new String(opResult.getIncomingResult());
	}

	private SOSOperations operations = null;

	/**
	 * Returns the operations supported by this SOS
	 * 
	 * @return the supported Operations as SOSOperations
	 * @see SOSOperations
	 */
	public SOSOperations getOperations() {
		if (operations == null) {

			try {
				operations = operationsFuture.get();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return operations;
	}

	@Override
	public Service getService() {
		try {
			if (super.getService() == null) {
				setService(serviceFuture.get());
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return super.getService();
	}

	private void parseCapabilities() {
		observationOffering = new Vector<Dataset>(oxfService.getContents()
				.getDataIdentificationCount());

		for (int i = 0; i < oxfService.getContents()
				.getDataIdentificationCount(); i++) {
			// LOGGER.info("Observation Offering: "
			// + oxfService.getContents().getDataIdentification(i));
			observationOffering.add(oxfService.getContents()
					.getDataIdentification(i));
		}

		serviceFuture = threadPool.submit(new SOSServiceInformationCreator(
				oxfService));
		operationsFuture = threadPool.submit(new SOSOperationsCreator(
				oxfService, observationOffering));
		threadPool.shutdown();
	}

	/**
	 * Get the last time this {@link SOSCapabilities} was updated. Used by the
	 * caching mechanism in {@link SOSCapabilities#getCapabilities(URL)}
	 * 
	 * @return the lastUpdated
	 * @see java.lang.System#currentTimeMillis()
	 * 
	 */
	protected long getLastUpdated() {
		return lastUpdated;
	}

	/**
	 * Set the last time this {@link SOSCapabilities} was updated. Used by the
	 * caching mechanism in {@link SOSCapabilities#getCapabilities(URL)}
	 * 
	 * @param lastUpdated
	 *            the lastUpdated to set
	 * @see System#currentTimeMillis()
	 */
	protected void setLastUpdated(final long lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	public final URL getServiceURL() {
		return serviceURL;
	}
}
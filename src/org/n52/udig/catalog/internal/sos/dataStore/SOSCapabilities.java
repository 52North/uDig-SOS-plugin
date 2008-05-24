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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
import org.n52.oxf.serviceAdapters.OperationResult;
import org.n52.oxf.serviceAdapters.ParameterContainer;
import org.n52.oxf.serviceAdapters.sos.ISOSRequestBuilder;
import org.n52.oxf.serviceAdapters.sos.SOSAdapter;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.SOSPlugin;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;
import org.opengis.metadata.citation.Address;
import org.opengis.metadata.citation.Contact;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.citation.Role;
import org.opengis.metadata.citation.Telephone;
import org.opengis.util.InternationalString;

/**
 * @author Carsten Priess
 *
 */
public class SOSCapabilities extends Capabilities {
	private final ExecutorService threadPool = Executors.newFixedThreadPool(2);
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSCapabilities.class);
	private URL serviceURL;
	private SOSAdapter adapter;

	private OperationResult opResult;

	public String getCapabilitiesString(){
		return new String(opResult.getIncomingResult());
	}

	public SOSCapabilities(final SOSAdapter adapter, final URL serviceURL)
			throws IOException {
		try {
			this.serviceURL = serviceURL;
			this.adapter = adapter;
			// generate request
			opResult = generateCapabilitiesFromSOS(adapter);
			LOGGER.debug(new String(opResult.getIncomingResult()));
			this.oxfService = adapter.initService(opResult,
					SOSDataStoreFactory.serviceVersion);
			// send request
			parseCapabilities();
			// parse it

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

	private ParameterContainer createGenerateCapsParameterContainer()
			throws OXFException {
		final ParameterContainer paramCon = new ParameterContainer();
		if (SOSDataStoreFactory.serviceVersion
				.equals(SOSDataStoreFactory.serviceVersion_000)) {
			paramCon.addParameterShell(
							ISOSRequestBuilder.GET_CAPABILITIES_ACCEPT_VERSIONS_PARAMETER,
							SOSAdapter.SUPPORTED_VERSIONS[0]);
			paramCon.addParameterShell(
					ISOSRequestBuilder.GET_CAPABILITIES_SERVICE_PARAMETER,
					SOSAdapter.SERVICE_TYPE);
		} else if (SOSDataStoreFactory.serviceVersion
				.equals(SOSDataStoreFactory.serviceVersion_100)) {
			paramCon.addParameterShell(
							ISOSRequestBuilder.GET_CAPABILITIES_ACCEPT_VERSIONS_PARAMETER,
							SOSAdapter.SUPPORTED_VERSIONS[1]);
			paramCon.addParameterShell(
					ISOSRequestBuilder.GET_CAPABILITIES_SERVICE_PARAMETER,
					SOSAdapter.SERVICE_TYPE);
		}
		return paramCon;
	}

//	// for testing purposes
//	private OperationResult generateCapabilitiesFromFile() {
//		try {
////			InputStream is = new FileInputStream(new File("sosCapabilities_"
////					+ SOSDataStoreFactory.serviceVersion + ".xml"));
//			final InputStream is = new FileInputStream("D:\\Java\\sos.xml");
//			final OperationResult opRes = new OperationResult(is,
//					createGenerateCapsParameterContainer(), "GetCapabilities");
//			return opRes;
//		} catch (final Exception e) {
//			e.printStackTrace();
//		}
//		return null;
//	}

	private OperationResult generateCapabilitiesFromSOS(final SOSAdapter adapter)
			throws ExceptionReport, OXFException {
		LOGGER.info("Giving GetCapabilities-Request to OxF with " + serviceURL);
		// DO IT
		return adapter.doOperation(new Operation("GetCapabilities", //$NON-NLS-1$
				serviceURL.toExternalForm() + "?", //$NON-NLS-1$
				serviceURL.toExternalForm()), createGenerateCapsParameterContainer());
	}

	/**
	 *
	 * @return the request
	 */
	public SOSOperations getOperations() {
		try {
			return operationsFuture.get();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private ReferencedEnvelope getBBoxFromOffering(final String typeName) {
		for (int i = 0; i < oxfService.getContents()
				.getDataIdentificationCount(); i++) {
			if (oxfService.getContents().getDataIdentification(i)
					.getIdentifier().equals(typeName)) {
				String crs = oxfService.getContents().getDataIdentification(i)
						.getAvailableCRSs()[0].toUpperCase();
				// TODO what about other codes?
				if (crs.contains("EPSG:")) {
					crs = crs.substring(crs.indexOf("EPSG:"));
				}

				try {
					final ReferencedEnvelope tempbbox = new ReferencedEnvelope(
							oxfService.getContents().getDataIdentification(i)
									.getBoundingBoxes()[0].getLowerCorner()[0],
							oxfService.getContents().getDataIdentification(i)
									.getBoundingBoxes()[0].getUpperCorner()[0],
							oxfService.getContents().getDataIdentification(i)
									.getBoundingBoxes()[0].getLowerCorner()[1],
							oxfService.getContents().getDataIdentification(i)
									.getBoundingBoxes()[0].getUpperCorner()[1],
							CRS.decode(crs));

					return tempbbox;
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
		LOGGER.warn("No BoundingBox Found");
		return null;
	}

	public ReferencedEnvelope getBoundingBox(final String operation, final String typeName) {
		if (operation.equals(SOSOperations.opName_GetObservation)) {
			return getBBoxFromOffering(typeName);
		}
		LOGGER.warn("No BoundingBox Found");
		return null;
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

	Future<Service> serviceFuture = null;
	Future<SOSOperations> operationsFuture = null;

	private List<Dataset> observationOffering;
	private ServiceDescriptor oxfService;

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
		// now the envelope
		// boundingBox = createBounds(oxfService);

		// FeatureSetDescription fsd;
		//
		// for (int i = 0; i < array.length; i++) {
		// fsd= new FeatureSetDescription();
		//
		// Envelope bbox = new Envelope(
		// oxfService.getContents().getDataIdentification(0).getBoundingBoxes()[0].getLowerCorner()[0],
		// oxfService.getContents().getDataIdentification(0).getBoundingBoxes()[0].getLowerCorner()[1],
		// oxfService.getContents().getDataIdentification(0).getBoundingBoxes()[0].getUpperCorner()[0],
		// oxfService.getContents().getDataIdentification(0).getBoundingBoxes()[0].getUpperCorner()[1]
		// );
		//
		// fsd.setLatLongBoundingBox(bbox);
		// fsd.setSRS(oxfService.getContents().getDataIdentification(0).getBoundingBoxes()[0].getCRS());
		// fsd.setName(oxfService.getContents().getDataIdentification(0).getIdentifier());
		// fsd.setTitle(oxfService.getContents().getDataIdentification(0).getTitle());
		//
		// SimpleFeatureType sft = new SimpleFeatureType()
		//
		// dataset = new
		// ArrayList<Dataset>(oxfService.getContents().getDataIdentificationCount());
		// for (int i = 0; i <
		// oxfService.getContents().getDataIdentificationCount(); i++) {
		// dataset.add(oxfService.getContents().getDataIdentification(i));
		// }
		// }

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

			final Operation op[] = oxfService.getOperationsMetadata().getOperations();
			// check all available operations
			for (final Operation element : op) {
				final SOSOperationType opType = new SOSOperationType(element.getName(), serviceURL.toExternalForm());
				opType.getFormats();

				// all operations have DCP-Elements
				for (int j = 0; j < element.getDcps().length; j++) {
					// TODO check for null
					try {
						opType.setGet(new URL(element.getDcps()[j]
								.getHTTPGetRequestMethods().get(0)
								.getOnlineResource().getHref()));
					} catch (final MalformedURLException e) {
						// TODO fehlerhandling überhaupt notwendig?
						e.printStackTrace();
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
						// TODO fehlerhandling überhaupt notwendig?
						e.printStackTrace();
					} catch (final IndexOutOfBoundsException npe) {
						// IndexOutOfBounds just means, that
						// getHTTPPostRequestMethods() empty list -> no post
						// available
					}

					try {
						// Get required and optional parameters
						final List<Parameter> parameters = element.getParameters();
						for (final Parameter parameter : parameters) {
							opType.addParameter(parameter);
						}
//						opType.setParameterConfiguration(SOSConfigurationRegistry.getInstance().updateParameterConfiguration(serviceURL.toExternalForm(), op[i].getName(), opType.getParameterConfiguration()));
//						opType.setParameterConfiguration(SOSConfigurationRegistry.getInstance().updateParameterConfiguration(serviceURL.toExternalForm(), op[i].getName(), opType.getParameterConfiguration()));
						// get operationsname and decide which operation is
						// represented
						// TODO this decisions could be made in SOSOperations
						if (element.getName().equals(
								SOSOperations.opName_GetCapabilities)) {
							operations.setGetCapabilities(opType);
							// LOGGER.info("Added operation: " +
							// op[i].getName());
						} else if (element.getName().equals(
								SOSOperations.opName_DescribeSensor)) {
							operations.setDescribeSensor(opType);
							// LOGGER.info("Added operation: " +
							// op[i].getName());
						} else if (element.getName().equals(
								SOSOperations.opName_GetFeatureOfInterest)) {
							operations.setGetFeatureOfInterest(opType);
							// LOGGER.info("Added operation: " +
							// op[i].getName());
						} else if (element.getName().equals(
								SOSOperations.opName_GetObservation)) {
							operations.setGetObservation(opType);
							SOSPlugin.trace("Added operation: " +
									element.getName(), null);
						} else if (element.getName().equals(
								SOSOperations.opName_GetObservationById)) {
							operations.setGetObservationById(opType);
							SOSPlugin.trace("Added operation: " +
									element.getName(), null);
						}

					} catch (final OXFException e) {
						e.printStackTrace();
					}
				}
			}

			return operations;
		}

	}

	class SOSServiceInformationCreator implements Callable<Service> {

		Service service;
		ServiceDescriptor oxfService;

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

			service.setContactInformation(new SOSResponsibleParty(oxfService
					.getServiceProvider()));

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

	class SOSResponsibleParty implements ResponsibleParty {

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
	}

	class MyInternationalString implements InternationalString {

		String s;

		public MyInternationalString(final String s) {
			this.s = s;
		}

		public String toString(final Locale arg0) {
			return s;
		}

		public char charAt(final int arg0) {
			return s.charAt(arg0);
		}

		public int length() {
			return s.length();
		}

		public CharSequence subSequence(final int beginIndex, final int endIndex) {
			return s.subSequence(beginIndex, endIndex);
		}

		public int compareTo(final Object arg0) {
			return s.compareTo((String) arg0);
		}

	}

	/**
	 * @return the adapter
	 */
	public SOSAdapter getAdapter() {
		return adapter;
	}
}

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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.n52.oxf.OXFException;
import org.n52.oxf.owsCommon.capabilities.IDiscreteValueDomain;
import org.n52.oxf.owsCommon.capabilities.IRangeValueDomain;
import org.n52.oxf.owsCommon.capabilities.ITime;
import org.n52.oxf.owsCommon.capabilities.IValueDomain;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.oxf.serviceAdapters.ParameterContainer;
import org.n52.oxf.serviceAdapters.ParameterShell;
import org.n52.oxf.serviceAdapters.sos.caps.ObservationOffering;
import org.n52.oxf.util.LoggingHandler;
import org.n52.oxf.valueDomains.StringValueDomain;
import org.n52.oxf.valueDomains.time.TemporalValueDomain;
import org.n52.udig.catalog.internal.sos.dataStore.SOSCapabilities;
import org.n52.udig.catalog.internal.sos.dataStore.SOSOperations;

/**
 * This class holds the parameters and parameter values for a given
 * SensorObservationService operation. It may be preconfigured with the
 * {@link SOSCapabilities} or {@link SOSConfigurationRegistry}
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class ParameterConfiguration implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5935103077342801799L;

	/**
	 * the Log4j-logger, configured with {@link GeneralConfigurationRegistry}
	 */
	private static final Logger LOGGER = LoggingHandler
			.getLogger(ParameterConfiguration.class);

	private org.n52.oxf.owsCommon.capabilities.Contents contents = null;

	private final String entryEND = "@";

	private final String lineEND = "\n";

	/**
	 * The ID of the operation configured with this class
	 * 
	 * @see SOSOperations
	 * @see SOSOperationType
	 */
	private String operationId;

	// static private HashMap<String, Character> parameterType = new
	// HashMap<String, Character>();

	/**
	 * A list with all optinalParameters
	 */
	private List<Parameter> optionalParameters = new Vector<Parameter>();

	/**
	 * The parameter container that will be send to the OXFFramework
	 */
	private ParameterContainer paramCon = new ParameterContainer();
	
	/**
	 * A list with all requiredParameters
	 */
	private List<Parameter> requiredParameters = new Vector<Parameter>();
	
	String selectedObservationID = "";

	private String[] typeNames = null;

	/**
	 * The url of the sos
	 */
	private String url;

	public ParameterConfiguration(final ParameterConfiguration p) {
		this.operationId = p.getOperationId();
		this.url = p.getUrl();
		this.setContents(p.getContents());
		this.paramCon = new ParameterContainer();

		for (final ParameterShell ps : p.getConfiguredParameterContainer()
				.getParameterShells()) {
			ParameterShell psneu = null;
			try {
				if (ps.hasMultipleSpecifiedValues()) {
					psneu = new ParameterShell(ps.getParameter(), ps
							.getSpecifiedValueArray());
				} else {
					psneu = new ParameterShell(ps.getParameter(), ps
							.getSpecifiedValue());
				}
			} catch (final Exception e) {
				LOGGER.error(e);
			}
			this.paramCon.addParameterShell(psneu);
		}

		this.optionalParameters = new ArrayList<Parameter>();
		for (final Parameter p1 : p.getOptionalParameters()) {
			optionalParameters.add(new Parameter(p1.getServiceSidedName(), p1
					.isRequired(), p1.getValueDomain(), p1.getCommonName()));
		}

		this.requiredParameters = new ArrayList<Parameter>();
		for (final Parameter p1 : p.getRequiredParameters()) {
			requiredParameters.add(new Parameter(p1.getServiceSidedName(), p1
					.isRequired(), p1.getValueDomain(), p1.getCommonName()));
		}
	}

	/**
	 * Creates a new instance of {@link ParameterConfiguration} UDig saves map
	 * and layer connection parameters as Strings. So use this constructor to
	 * create a new instance of {@link ParameterConfiguration} from the string
	 * representation created by {@link ParameterConfiguration}{@link #toString()}.
	 * Note: The values are not crosschecked with Capabilities
	 * 
	 * @param classRepresentation
	 */
	public ParameterConfiguration(final String classRepresentation) {
		try {

			final String[] cr = classRepresentation.split("\n");
			if (cr.length > 2) {
				for (final String s : cr) {
					if (s.contains(("opID"))) {
						operationId = s.substring(s.indexOf("opID") + 5, s
								.length() - 1);
					} else if (s.contains("url")) {
						url = s.substring(s.indexOf("url") + 4, s.length() - 1);
					} else {
						final String[] s2 = s.split(entryEND);
						final String name = s2[0];
						if (s2.length > 2) {
							final String[] values = new String[s2.length - 1];
							for (int i = 1; i < s2.length; i++) {
								values[i - 1] = s2[i];
							}
							addParameter(new Parameter(name, false,
									new StringValueDomain(), name));
							setParameterValue(name, values);
						} else {
							addParameter(new Parameter(name, false,
									new StringValueDomain(), name));
							setParameterValue(name, s2[1]);
							// paramCon.addParameterShell(name, s2[1]);
						}
					}
				}
			}
		} catch (final Exception e) {
			LOGGER.fatal("Could not load parameters", e);
		}
		// } else throw new Exception("was los=!");

	}

	/**
	 * Creates a new Instance of ParameterConfiguration
	 * 
	 * @param operation
	 *            the operation's service id this ParameterConfiguration is
	 *            assigned to
	 * @param url
	 *            the sos's URL
	 */
	public ParameterConfiguration(final String operation, final String url) {
		// configureParameterTypes();

		this.operationId = operation;
		this.url = url;
	}

	/**
	 * Adds a new parameter to the lists of optional parameters.
	 * 
	 * @param parameter
	 *            the parameter to add
	 * @throws OXFException
	 */
	@Deprecated
	private void addOptionalParameter(final Parameter parameter)
			throws OXFException {
		if (optionalParameters.contains(parameter)) {
			optionalParameters.remove(parameter);
		}
		optionalParameters.add(parameter);
		configureParameter(parameter);
	}

	/**
	 * Adds a new parameter to the lists of possible parameters. The method will
	 * decide if the parameter is required or optional using
	 * {@link Parameter#isRequired()}. In addition the Parameter will be
	 * preconfigured if it`s required and has only one possible value.
	 * 
	 * @param parameter
	 *            the parameter to add
	 * @throws OXFException
	 */
	public void addParameter(final Parameter parameter) throws OXFException {
		if (parameter.isRequired()) {
			addRequiredParameter(parameter);
		} else {
			addOptionalParameter(parameter);
		}
	}

	/**
	 * Adds a new parameter to the lists of possible required parameters. The
	 * method will decide if the parameter is required or optional using
	 * {@link Parameter#isRequired()}. In addition the Parameter will be
	 * preconfigured if it`s required and has only one possible value.
	 * 
	 * @param parameter
	 *            the parameter to add
	 * @throws OXFException
	 */
	@Deprecated
	private void addRequiredParameter(final Parameter parameter)
			throws OXFException {
		if (requiredParameters.contains(parameter)) {
			requiredParameters.remove(parameter);
		}
		requiredParameters.add(parameter);
		configureParameter(parameter);
	}

	private void addToList(String[] sArray, List<String> l){
		for (int i = 0; i < sArray.length; i++) {
			l.add(sArray[i]);
		}
	}

	/**
	 * Preconfigures a given parameter. If only one value is possible and the
	 * parameter is required, to value will be autoset.
	 * 
	 * @param parameter
	 * @throws OXFException
	 */
	private void configureParameter(final Parameter parameter)
			throws OXFException {
		// TODO this one needs to be extended to configureParameterTypes
		if (paramCon.containsParameterShellWithServiceSidedName(parameter
				.getServiceSidedName())) {
			paramCon.removeParameterShell(paramCon
					.getParameterShellWithServiceSidedName(parameter
							.getServiceSidedName()));
		}

		if (parameter.getValueDomain() instanceof IRangeValueDomain) {
			LOGGER
					.debug("IRangeValueDomain "
							+ parameter.getServiceSidedName());
			LOGGER.info("Parameter " + parameter.getServiceSidedName()
					+ " with IRangeValueDomain not supported");
		} else {
			if (parameter.isRequired()) {
				if (parameter.getValueDomain() instanceof IDiscreteValueDomain) {
					if (parameter.getServiceSidedName().equals("responseMode")){
						setParameterValue(parameter
								.getServiceSidedName(),
								"inline");
					}
					else if (parameter.getServiceSidedName().equals("responseFormat")){
						setParameterValue(parameter
								.getServiceSidedName(),
								"text/xml;subtype=\"OM/1.0.0\"");
					} else{
					// System.out.println("VD.
					// :"+parameter.getValueDomain().getDomainDescription());

					// LOGGER.debug(parameter.getServiceSidedName()
					// + " is part of IDiscreteValueDomain.");
					final IDiscreteValueDomain temp_param_domain = (IDiscreteValueDomain) parameter
							.getValueDomain();
					// if only one value is allowed, configure the parameter
					// according to it
					if (temp_param_domain.getPossibleValues().size() == 1) {
						LOGGER.debug("only a single value allowed for parameter \""
										+ parameter.getServiceSidedName()
										+ "\": autoset to "
										+ temp_param_domain.getPossibleValues()
												.get(0));
						if (parameter.getValueDomain() instanceof StringValueDomain) {
							try {
								setParameterValue(parameter
										.getServiceSidedName(),
										(String) temp_param_domain
												.getPossibleValues().get(0));
							} catch (final Exception e) {
								LOGGER.error(e);
							}

							// paramCon.addParameterShell(parameter
							// .getServiceSidedName(),
							// (String) temp_param_domain.getPossibleValues()
							// .get(0));
						} else {
							LOGGER
									.warn("could not set single value for parameter \""
											+ parameter.getServiceSidedName()
											+ "\"");
						}
						// } else if (parameter.getValueDomain() instanceof
						// TemporalValueDomain) {
						// paramCon.addParameterShell(new ParameterShell(
						// parameter, temp_param_domain
						// .getPossibleValues().get(0)));
						// } else if (parameter.getValueDomain() instanceof
						// TimePeriod) {
						// paramCon.addParameterShell(new ParameterShell(
						// parameter, temp_param_domain
						// .getPossibleValues().get(0)));
						// } else if (parameter.getValueDomain() instanceof
						// TimePosition) {
						// paramCon.addParameterShell(new ParameterShell(
						// parameter, temp_param_domain
						// .getPossibleValues().get(0)));
					}
					}
				}
			}
		}
	}

	/**
	 * Returns the current ParameterConfiguration in a ParameterContainer
	 * 
	 * @return the current ParameterConfiguration in a ParameterContainer
	 */
	public ParameterContainer getConfiguredParameterContainer() {
		return paramCon;
	}

	public org.n52.oxf.owsCommon.capabilities.Contents getContents(){
		return this.contents;
	}

	public String getHeartParameterName() {
		if (operationId.equals("GetFeatureOfInterest")) {
			return "featureOfInterestId";
		} else if (operationId.equals("GetObservation")) {
			return "offering";
		} else if (operationId.equals("DescribeSensor")) {
			return "procedure";
		} else if (operationId.equals("GetCapabilities")) {

		} else if (operationId.equals("GetObservationById")) {
			return "ObservationId";
		}
		return null;
	}

	private List<String> getOfferingItems(String offeringS){
		ObservationOffering offering = (ObservationOffering)contents.getDataIdentification(offeringS);
		List<String> l = new Vector<String>();
		addToList(offering.getAvailableCRSs(), l);
		addToList(offering.getFeatureOfInterest(), l);
		addToList(offering.getObservedProperties(), l);
		addToList(offering.getOutputFormats(), l);
		addToList(offering.getProcedures(), l);
		addToList(offering.getResponseModes(), l);
		addToList(offering.getResultModels(), l);

		return l;
	}

	public List<String> getOfferingsFromContents(){
		List<String> returnList = new Vector<String>();
		for (int i = 0; i < contents.getDataIdentificationCount(); i++) {
			returnList.add(contents.getDataIdentification(i).getIdentifier());
		}
		return returnList;
	}
	
	/**
	 * Returns the operation's Service-ID this {@link ParameterConfiguration} is
	 * assigned to
	 * 
	 * @return the operation's Service-ID as {@link String}
	 * @see ParameterConfiguration#operationId
	 */
	public String getOperationId() {
		return operationId;
	}
	
	

	/**
	 * Returns a list of all optional Parameters.
	 * 
	 * @return a list of all optional Parameters as List<Parameter>
	 */
	public List<Parameter> getOptionalParameters() {
		return optionalParameters;
	}

	/**
	 * Returns a list of all optional Parameters as Strings with
	 * serviceSidedName.
	 * 
	 * @return a list of all optional Parameters as List<String>
	 * @see Parameter#getServiceSidedName()
	 */
	public List<String> getOptionalParametersAsStrings() {
		final List<String> out = new Vector<String>(optionalParameters.size());
		for (final Parameter param : optionalParameters) {
			out.add(param.getServiceSidedName());
		}
		return out;
	}

	public Parameter getParameterByID(final String id) {
		for (final Parameter param : requiredParameters) {
			if (param.getServiceSidedName().equals(id)) {
				return param;
			}
		}
		for (final Parameter param : optionalParameters) {
			if (param.getServiceSidedName().equals(id)) {
				return param;
			}
		}
		return null;
	}

	public List<Parameter> getParameters() {
		final List<Parameter> out = new Vector<Parameter>(requiredParameters
				.size()
				+ optionalParameters.size());
		out.addAll(getRequiredParameters());
		out.addAll(getOptionalParameters());
		return out;
	}

	/**
	 * Returns a list of all (optional and required) Parameters as Strings with
	 * serviceSidedName.
	 * 
	 * @return a list of all Parameters as List<String>
	 * @see Parameter#getServiceSidedName()
	 */
	public List<String> getParametersAsStrings() {
		final List<String> out = new Vector<String>(requiredParameters.size()
				+ optionalParameters.size());
		out.addAll(getRequiredParametersAsStrings());
		out.addAll(getOptionalParametersAsStrings());
		return out;
	}

	/**
	 * Returns a list of all required Parameters as List<Parameter>
	 * 
	 * @return a list of all required Parameters as List<Parameter>
	 */
	public List<Parameter> getRequiredParameters() {
		return requiredParameters;
	}

	/**
	 * Returns a list of all required Parameters as Strings with
	 * serviceSidedName.
	 * 
	 * @return a list of all required Parameters as List<String>
	 * @see Parameter#getServiceSidedName()
	 */
	public List<String> getRequiredParametersAsStrings() {
		final List<String> out = new Vector<String>(requiredParameters.size());
		for (final Parameter param : requiredParameters) {
			out.add(param.getServiceSidedName());
		}
		return out;
	}

	@SuppressWarnings("unchecked")
	public String[] getTypeNames() {
		if (typeNames != null
				&& !operationId.equals(SOSOperations.opName_GetObservationById)) {
			return typeNames;
		}
		List<String> l = null;
		// TODO tuning needed

		// featureofinterest
		if (operationId.equals(SOSOperations.opName_GetFeatureOfInterest)) {
			l = ((IDiscreteValueDomain) getParameterByID("featureOfInterestId")
					.getValueDomain()).getPossibleValues();
			// typeNames = new String[l.size()];
			// for (int i = 0; i < l.size(); i++) {
			// typeNames[i] = l.get(i);
			// }
			// return typeNames;
		}
		// ask for offerings
		else if (operationId.equals(SOSOperations.opName_GetObservation)) {
//			l = ((IDiscreteValueDomain) getParameterByID("offering")
//					.getValueDomain()).getPossibleValues();
			l = new LinkedList<String>();
//			Random r = new Random();
//			String typeName = (String)paramCon.getParameterShellWithCommonName("offering").getSpecifiedValue()+"#"+r.nextInt();
			String typeName = (String)paramCon.getParameterShellWithCommonName("offering").getSpecifiedValue();
			l.add(typeName);
			
			// TODO this will be deprecated
			// take care, not every offering works with every procedure
//			String[] configuredprocedures = (String[])paramCon.getParameterShellWithCommonName("procedure").getSpecifiedValueArray();
//			String[] configuredObservedProperties = (String[])paramCon.getParameterShellWithCommonName("observedProperty").getSpecifiedValueArray();
//			String[] configuredFOI = null;
////			String[] configuredFOI = (String[])paramCon.getParameterShellWithCommonName("featureOfInterest").getSpecifiedValueArray();
//			List<String> removeOfferings = new Vector<String>();
//			
//			for (String s : l){
//				if (!offeringCheck(s, configuredprocedures, configuredObservedProperties, configuredFOI)){
//					removeOfferings.add(s);
//				}
//			}
//			
//			for (String s:removeOfferings){
//				l.remove(s);
//			}
		} else if (operationId.equals(SOSOperations.opName_DescribeSensor)) {
			l = ((IDiscreteValueDomain) getParameterByID("procedure")
					.getValueDomain()).getPossibleValues();
		} else if (operationId.equals(SOSOperations.opName_GetCapabilities)) {
			return null;
		} else if (operationId.equals(SOSOperations.opName_GetObservationById)) {
			// XXX this won`t work
			LOGGER
					.info("SETTING OF HEART GetObservationById NOT SUPPORTED, because SOS offers no list with all available featureids");
			l = new LinkedList<String>();
			l.add((String) paramCon.getParameterShellWithServiceSidedName(
					"ObservationId").getSpecifiedValue());
		}
		if (l != null) {
			typeNames = new String[l.size()];
			for (int i = 0; i < l.size(); i++) {
				typeNames[i] = l.get(i);
			}
			return typeNames;
		}
		return null;
	}

	/**
	 * Returns a list of all unconfigured (parameters with no value set)
	 * optional Parameters.
	 * 
	 * @return a list of all unconfigured optional Parameters as List<Parameter>
	 */
	public List<Parameter> getUnconfiguredOptionalParameters() {
		final List<Parameter> out = new Vector<Parameter>();
		for (final Parameter p : optionalParameters) {
			if (!paramCon.containsParameterShellWithServiceSidedName(p
					.getServiceSidedName())) {
				out.add(p);
			}
		}
		return out;
	}
	
	/**
	 * Returns a list of all unconfigured (parameters with no value set)
	 * optional Parameters.
	 * 
	 * @return a list of all unconfigured optional Parameters as List<String>
	 * @see Parameter#getServiceSidedName()
	 */
	public List<String> getUnconfiguredOptionalParametersAsStrings() {
		final List<String> out = new Vector<String>();

		for (final Parameter param : getUnconfiguredOptionalParameters()) {
			// XXX WORKAROUND!
			if (!param.getServiceSidedName().equalsIgnoreCase("bbox")) {
				out.add(param.getServiceSidedName());
			} else {
				LOGGER.warn("REMOVED BBOX FROM OUTPUT!");
			}
		}
		return out;
	}
	
	public List<String> getUnconfiguredOptionalParametersAsStringsForOffering(String offeringS) {
		List<String> l = getOfferingItems(offeringS);

		final List<String> out = new Vector<String>();
		for (final Parameter param : getUnconfiguredOptionalParameters()) {
			// XXX WORKAROUND!
			if (param.getServiceSidedName().equalsIgnoreCase("bbox")){
				
			} else if (param.getServiceSidedName().equalsIgnoreCase("EventTime")){
				if (!out.contains(param.getServiceSidedName())){
					out.add(param.getServiceSidedName());
				}
			}
			// NOT BBOX or EventTime
			else {
				// check if parameter has valid values for this offering
				boolean added = false;
				for (Object o:((IDiscreteValueDomain)param.getValueDomain()).getPossibleValues()){
					if (!added && l.contains(o)){
						added = true;
						if (!out.contains(param.getServiceSidedName())){
							out.add(param.getServiceSidedName());
						}
					}
				}
			} 
		}
		return out;
	}
	
	/**
	 * Returns a list of all unconfigured (parameters with no value set)
	 * required Parameters.
	 * 
	 * @return a list of all unconfigured required Parameters as List<Parameter>
	 */
	public List<Parameter> getUnconfiguredRequiredParameters() {
		final List<Parameter> out = new Vector<Parameter>();
		for (final Parameter p : requiredParameters) {
			// if (!configuredParameter.containsKey(p.getServiceSidedName())){
			if (!paramCon.containsParameterShellWithServiceSidedName(p
					.getServiceSidedName())) {
				out.add(p);
			}
		}
		return out;
	}
	
	/**
	 * Returns a list of all unconfigured (parameters with no value set)
	 * required Parameters.
	 * 
	 * @return a list of all unconfigured required Parameters as List<String>
	 * @see Parameter#getServiceSidedName()
	 */
	public List<String> getUnconfiguredRequiredParametersAsStrings() {
		final List<String> out = new Vector<String>();
		for (final Parameter param : getUnconfiguredRequiredParameters()) {
			// XXX WORKAROUND!
			if (!param.getServiceSidedName().equalsIgnoreCase("bbox")) {
				out.add(param.getServiceSidedName());
			}
		}
		return out;
	}
	
	

	public List<String> getUnconfiguredRequiredParametersAsStringsForOffering(String offeringS) {
		List<String> l = getOfferingItems(offeringS);
		
		l.addAll(((IDiscreteValueDomain)getParameterByID("resultModel").getValueDomain()).getPossibleValues());
		
		final List<String> out = new Vector<String>();
		for (final Parameter param : getUnconfiguredRequiredParameters()) {
			// XXX WORKAROUND!
			if (param.getServiceSidedName().equalsIgnoreCase("bbox")){
				
			} else if (param.getServiceSidedName().equalsIgnoreCase("EventTime")){
				if (!out.contains(param.getServiceSidedName())){
					out.add(param.getServiceSidedName());
				}
			}
			// NOT BBOX or EventTime
			else {
				boolean added = false;
				for (Object o:((IDiscreteValueDomain)param.getValueDomain()).getPossibleValues()){
					if (!added && l.contains(o)){
						added = true;
						out.add(param.getServiceSidedName());
					}
				}
			}
		}
		return out;
	}

	/**
	 * Returns the url of the used SOS
	 * 
	 * @return the url as {@link String}
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Returns the {@link IValueDomain} for the parameter with paramID
	 * 
	 * @param paramID
	 * @return the IValueDomain
	 */
	public IValueDomain getValueDomainForParameter(final String paramID) {
		for (final Parameter param : requiredParameters) {
			if (param.getServiceSidedName().equals(paramID)) {
				return param.getValueDomain();
			}
		}
		for (final Parameter param : optionalParameters) {
			if (param.getServiceSidedName().equals(paramID)) {
				return param.getValueDomain();
			}
		}
		return null;
	}

	/**
	 * Checks if Strings in configured are contained in inOffering. Used to check if a given ParameterSet can be used for special Offerings
	 * @param inOffering
	 * @param configured
	 * @return
	 * @deprecated
	 */
	@Deprecated
	private boolean isContained(String[] inOffering, String[] configured){
		List<String> inOfferingList = new Vector<String>();
		for (int i = 0; i < inOffering.length; i++) {
			inOfferingList.add(inOffering[i]);
		}
		
		for (int j = 0; j < configured.length; j++) {
			String configuredObservedProperty = configured[j];
			if (!inOfferingList.contains(configuredObservedProperty)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if a parameter with given id is already configured
	 * 
	 * @param id
	 *            the parameterID that should be checked
	 * @return true if parameter is properly configured, false if not
	 */
	public boolean isParameterConfigured(final String id) {
		// TODO check value domain; if only a single value allowed, don't ask
		// for and just add it
		if (paramCon.containsParameterShellWithServiceSidedName(id)) {
			return true;
		}
		return false;
	}
	
	public boolean isValueAllowedInOffering(String value, String offeringS){
		ObservationOffering offering = (ObservationOffering)contents.getDataIdentification(offeringS);
		
		boolean returnbool = false;
		
		List<String> l = new LinkedList<String>();
		l.addAll(((IDiscreteValueDomain)getParameterByID("resultModel").getValueDomain()).getPossibleValues());
		
		String[] lAnew = new String[l.size()];
		int i = 0;
		for (String s: l){
			lAnew[i++] = s;
		}
		
		String[] valueAnew = new String[1];
		valueAnew[0] = value;
		returnbool = returnbool | isContained(offering.getObservedProperties(), valueAnew);
		returnbool = returnbool | isContained(offering.getOutputFormats(), valueAnew);
		returnbool = returnbool | isContained(offering.getProcedures(), valueAnew);
		returnbool = returnbool | isContained(offering.getResponseModes(), valueAnew);
		returnbool = returnbool | isContained(offering.getResultModels(), valueAnew);
		returnbool = returnbool | isContained(lAnew, valueAnew);
		return returnbool;
	}
	
	private boolean offeringCheck(String s, String[] configuredprocedures, String[] configuredObservedProperties, String[] configuredFOIs){
		// TODO there are more possibillities
		ObservationOffering dataset = (ObservationOffering)contents.getDataIdentification(s);

		// check contents for observedproperties
		if (configuredObservedProperties!= null){
			String[] observedpropertiesInOffering = dataset.getObservedProperties();
			if (!isContained(observedpropertiesInOffering, configuredObservedProperties)){
				return false;
			}
		}

		// check contents for FOIs
		if (configuredFOIs!= null){
			String[] fOIs = dataset.getFeatureOfInterest();
			if (!isContained(fOIs, configuredFOIs)){
				return false;
			}
		}

		// check contents for procedures
		if (configuredprocedures!= null){
			String[] procedures = dataset.getProcedures();
			if (!isContained(procedures, configuredprocedures)){
				return false;
			}
		}
		return true;
	}
	
	public void setContents(org.n52.oxf.owsCommon.capabilities.Contents contentss){
		this.contents = contentss;
	}

	public void setHeartParameter(final ParameterConfiguration pc,
			final String typename) throws OXFException {
		// initialParamConf.setParameter(getHeartParameterName(), typename);
		pc.setParameterValue(getHeartParameterName(), typename);

		// WORKAROUND
		if (getHeartParameterName().equals("ObservationId")) {
			selectedObservationID = typename;
		}
	}

	/**
	 * Sets the value of a parameter to value
	 * 
	 * @param paramID
	 *            the paramID to set
	 * @param value
	 *            the inputvalue as ITime
	 * @return null if everything is ok, an errormessage as String
	 * @throws IllegalArgumentException
	 * @throws OXFException
	 */
	public String setParameterValue(final String paramID, final ITime value)
			throws Exception {
		if (getParameterByID(paramID) == null) {
			throw new Exception("ID " + paramID + " is no known parameter id");
		}

		if (!((TemporalValueDomain) getParameterByID(paramID).getValueDomain())
				.containsValue(value)) {
			return "Value " + value + " is not contained in valueDomain "
					+ (getParameterByID(paramID).getValueDomain());
		}

		if (paramCon.containsParameterShellWithServiceSidedName(paramID)) {
			paramCon.setParameterValue(paramID, value);
		} else {
			paramCon.addParameterShell(paramID, value);
		}
		return null;
	}
	/**
	 * Sets the value of a parameter to value
	 * 
	 * @param paramID
	 *            the paramID to set
	 * @param value
	 *            the inputvalue as String
	 * @return null if everything is ok, an errormessage as String
	 * @throws IllegalArgumentException
	 * @throws OXFException
	 */
	public String setParameterValue(final String paramID, final String value)
			throws IllegalArgumentException, OXFException {
		if (getParameterByID(paramID) == null) {
			throw new IllegalArgumentException("ID " + paramID
					+ " is no known parameter id");
		}
		// TODO valueDomain
		// if (getParameterByID(paramID).getValueDomain().containsValue(value)){
		// throw new InvalidValueException("The value "+ value +" is no part of
		// "+getParameterByID(paramID).getValueDomain().getDomainDescription());
		// }else{

		if (GeneralConfigurationRegistry.getParameterTypes().get(paramID) != null) {
			if (GeneralConfigurationRegistry.getParameterTypes().get(paramID)
					.equals('a')) {
				final String[] sa = new String[1];
				sa[0] = value;
				return setParameterValue(paramID, sa);
			} else if (GeneralConfigurationRegistry.getParameterTypes().get(
					paramID).equals('s')) {
				if (paramCon.containsParameterShellWithServiceSidedName(paramID)) {
					paramCon.setParameterValue(paramID, value);
				} else {
					paramCon.addParameterShell(paramID, value);
				}
				return null;
			}
		}
		LOGGER
				.warn("ParameterType of "
						+ paramID
						+ " unknown trying with singleValue, however this may lead to an error. Try to extend ParameterConfiguration#configureParameterTypes()");
		if (paramCon.containsParameterShellWithServiceSidedName(paramID)) {
			paramCon.setParameterValue(paramID, value);
		} else {
			paramCon.addParameterShell(paramID, value);
		}
		return null;
	}

	/**
	 * Sets the value of a parameter to values[]
	 * 
	 * @param paramID
	 *            the paramID to set
	 * @param values
	 *            the valuearray as String[]
	 * @return null if everything is ok, an errormessage as String
	 * @throws IllegalArgumentException
	 * @throws OXFException
	 */
	public String setParameterValue(final String paramID, final String[] values)
			throws IllegalArgumentException, OXFException {
		if (getParameterByID(paramID) == null) {
			throw new IllegalArgumentException("ID " + paramID
					+ " is no known parameter id");
		}
		// TODO check value in value domain?
		// configuredParameter.put(paramID, value);
		if (paramID.equals("responseMode")) {
			if (values.length > 1) {
				return "Only a single value is allowed for parameter "
						+ paramID + " ";
			} else {
				if (values.length > 0) {
					return setParameterValue(paramID, values[0]);
				}
			}
		}
		if (paramCon.containsParameterShellWithServiceSidedName(paramID)) {
			paramCon.setParameterValueArray(paramID, values);
		} else {
			paramCon.addParameterShell(paramID, values);
		}
		return null;
	}

	/**
	 * UDig saves map and layer connection parameters as Strings. This class
	 * offers a way to create a stringrepresentation which can be used to create
	 * a new instance of {@link ParameterConfiguration} with help of the
	 * constructor {@link ParameterConfiguration#ParameterConfiguration(String)}
	 * Format: entryNAME+*entryEND+value+lineEND example: opID@GetObservation@\n
	 * url@GetObservation@\n
	 * 
	 */
	@Override
	public String toString() {
		// take care, change this and you`ll have to change
		// ParameterConfiguration#ParameterConfiguration(String)

		final StringBuilder sb = new StringBuilder();
		sb.append("opID");
		sb.append(entryEND);
		sb.append(operationId);
		sb.append(entryEND);
		sb.append(lineEND);

		sb.append("url");
		sb.append(entryEND);
		sb.append(url);
		sb.append(entryEND);

		for (final ParameterShell p1 : paramCon.getParameterShells()) {
			sb.append(lineEND);
			sb.append(p1.getParameter().getServiceSidedName());
			sb.append(entryEND);
			if (p1.hasMultipleSpecifiedValues()) {
				for (final Object o : p1.getSpecifiedValueArray()) {
					sb.append(o);
					sb.append(entryEND);
				}
			} else {
				sb.append(p1.getSpecifiedValue());
				sb.append(entryEND);
			}

		}
		return sb.toString();
	}

}
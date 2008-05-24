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
package org.n52.udig.catalog.internal.sos.dataStore.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.n52.oxf.OXFException;
import org.n52.oxf.owsCommon.capabilities.IDiscreteValueDomain;
import org.n52.oxf.owsCommon.capabilities.IRangeValueDomain;
import org.n52.oxf.owsCommon.capabilities.ITime;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.oxf.serviceAdapters.ParameterContainer;
import org.n52.oxf.serviceAdapters.ParameterShell;
import org.n52.oxf.valueDomains.StringValueDomain;
import org.n52.oxf.valueDomains.time.TimePeriod;
import org.n52.oxf.valueDomains.time.TimePosition;

/**
 * @author 52n
 *
 */
public class ParameterConfiguration implements Serializable {
	private final String operationId;
	private final List<Parameter> optionalParameters = new ArrayList<Parameter>();
	private final List<Parameter> requiredParameters = new ArrayList<Parameter>();
	private final ParameterContainer paramCon = new ParameterContainer();
	private final String url;

	/**
	 * @param opoeration
	 *            the operationsname these parameters are assigned to
	 */
	public ParameterConfiguration(final String operation, final String url) {
		this.operationId = operation;
		this.url = url;
	}

	@Deprecated
	private void addOptionalParameter(final Parameter parameter)
			throws OXFException {
		optionalParameters.add(parameter);
		configureParameter(parameter);
	}

	public void addParameter(final Parameter parameter) throws OXFException {
		if (parameter.isRequired()){
			addRequiredParameter(parameter);
		} else {
			addOptionalParameter(parameter);
		}
	}

	@Deprecated
	private void addRequiredParameter(final Parameter parameter)
			throws OXFException {
		requiredParameters.add(parameter);
		configureParameter(parameter);
	}

	private void configureParameter(final Parameter parameter)
			throws OXFException {

		if (paramCon.containsParameterShellWithServiceSidedName(parameter.getServiceSidedName())){
			paramCon.removeParameterShell(paramCon.getParameterShellWithServiceSidedName(parameter.getServiceSidedName()));
		}

		if (parameter.getValueDomain() instanceof IRangeValueDomain){
			System.out.println("IRangeValueDomain "+parameter.getServiceSidedName());
		}

		if (parameter.getValueDomain() instanceof IDiscreteValueDomain) {
//			System.out.println("VD. :"+parameter.getValueDomain().getDomainDescription());

			System.out.println("IDiscreteValueDomain "+parameter.getServiceSidedName());
			final IDiscreteValueDomain temp_param_domain = (IDiscreteValueDomain) parameter
					.getValueDomain();
			// Parameter param;
			// if only one value is allowed, configure the parameter
			// according to it
				if (temp_param_domain.getPossibleValues().size() == 1) {
					if (parameter.getValueDomain() instanceof StringValueDomain){
						paramCon.addParameterShell(parameter.getServiceSidedName(),
								(String) temp_param_domain.getPossibleValues().get(
										0));
					} else if (parameter.getValueDomain() instanceof TimePeriod){
						paramCon.addParameterShell(new ParameterShell(parameter, temp_param_domain.getPossibleValues().get(0)));
					} else if (parameter.getValueDomain() instanceof TimePosition){
						paramCon.addParameterShell(new ParameterShell(parameter, temp_param_domain.getPossibleValues().get(0)));
					}
				}
			}

	}


	public ParameterContainer getConfiguredParameterContainer() {

		// try {
		// for (Parameter rp:requiredParameters){
		// if (!isParameterConfigured(rp.getServiceSidedName())){
		// // if a required parameter is not configured, we cant do the request
		// // TODO exception handling
		// // TODO assume default values, like first possible?
		// return null;
		// }
		// paramCon.addParameterShell(rp.getServiceSidedName(),
		// // configuredParameter.get(rp.getServiceSidedName()));
		// paramCon.getParameterShellWithServiceSidedName(serviceSidedName)(rp.getServiceSidedName()));
		// }
		// for (Parameter op:optionalParameters){
		// //XXX TIME is out!
		// if (!op.getServiceSidedName().equals("eventTime")){
		// if (isParameterConfigured(op.getServiceSidedName())){
		// // if an optional parameter is configured, we can add it to the
		// request
		// paramCon.addParameterShell(op.getServiceSidedName(),
		// configuredParameter.get(op.getServiceSidedName()));
		// }
		// }}
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		return paramCon;
	}

	public String getOperationId() {
		return operationId;
	}

	/**
	 * @return the optionalParameters
	 */
	public List<Parameter> getOptionalParameters() {
		return optionalParameters;
	}

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

	public List<String> getParametersAsStrings() {
		final List<String> out = new Vector<String>(requiredParameters.size()
				+ optionalParameters.size());
		out.addAll(getRequiredParametersAsStrings());
		out.addAll(getOptionalParametersAsStrings());
		return out;
	}

	/**
	 * @return the requiredParameters
	 */
	public List<Parameter> getRequiredParameters() {
		return requiredParameters;
	}

	public List<String> getRequiredParametersAsStrings() {
		final List<String> out = new Vector<String>(requiredParameters.size());
		for (final Parameter param : requiredParameters) {
			out.add(param.getServiceSidedName());
		}
		return out;
	}

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

	public List<String> getUnconfiguredOptionalParametersAsStrings() {
		final List<String> out = new Vector<String>();

		for (final Parameter param : getUnconfiguredOptionalParameters()) {
			// XXX WORKAROUND!
			if (!param.getServiceSidedName().equalsIgnoreCase("bbox")
					&& !param.getServiceSidedName().equalsIgnoreCase(
							"EventTime")) {
				out.add(param.getServiceSidedName());
			}
		}
		return out;
	}

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

	public List<String> getUnconfiguredRequiredParametersAsStrings() {
		final List<String> out = new Vector<String>();
		for (final Parameter param : getUnconfiguredRequiredParameters()) {
			// XXX WORKAROUND!
			if (!param.getServiceSidedName().equalsIgnoreCase("bbox")
					&& !param.getServiceSidedName().equalsIgnoreCase(
							"EventTime")) {
				out.add(param.getServiceSidedName());
			}
		}
		return out;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	public boolean isParameterConfigured(final String key) {
		// TODO check value domain; if only a single value allowed, don't ask
		// for and just add it
		if (paramCon.containsParameterShellWithServiceSidedName(key)) {
			return true;
		}
		return false;
	}

	public String setParameter(final String paramID, final ITime value) throws Exception{
		if (getParameterByID(paramID) == null) {
			throw new Exception("ID " + paramID + " is no known parameter id");
		}
		paramCon.setParameterValue(paramID, value);
		return null;
	}

	public String setParameter(final String paramID, final String value)
			throws Exception {
		if (getParameterByID(paramID) == null) {
			throw new Exception("ID " + paramID + " is no known parameter id");
		}
		// TODO check value in value domain?
		if (paramID.equals("procedure")) {
			final String[] sa = new String[1];
			sa[0] = value;
			return setParameter(paramID, sa);
		}
		if (paramCon.containsParameterShellWithServiceSidedName(paramID)) {
			paramCon.setParameterValue(paramID, value);
		} else {
			paramCon.addParameterShell(paramID, value);
		}
		return null;
	}

	public String setParameter(final String paramID, final String[] values)
			throws Exception {
		if (getParameterByID(paramID) == null) {
			throw new Exception("ID " + paramID + " is no known parameter id");
		}
		// TODO check value in value domain?
		// configuredParameter.put(paramID, value);
		if (paramID.equals("responseMode")) {
			if (values.length > 1) {
				return "Only a single value is allowed for parameter "
						+ paramID + " ";
			} else {
				if (values.length > 0){
					return setParameter(paramID, values[0]);
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
}

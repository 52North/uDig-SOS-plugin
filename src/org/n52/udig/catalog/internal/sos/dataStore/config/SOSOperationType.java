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

import java.util.List;

import org.geotools.data.ows.OperationType;
import org.n52.oxf.OXFException;
import org.n52.oxf.feature.IFeatureStore;
import org.n52.oxf.feature.sos.SOSFoiStore;
import org.n52.oxf.feature.sos.SOSObservationStore;
import org.n52.oxf.owsCommon.capabilities.IDiscreteValueDomain;
import org.n52.oxf.owsCommon.capabilities.Operation;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.oxf.serviceAdapters.ParameterContainer;
import org.n52.udig.catalog.internal.sos.dataStore.SOSOperations;

/**
 * @author 52n
 *
 */
public class SOSOperationType extends OperationType {

	private final String id;
	private ParameterConfiguration paramConf;
	private String[] typeNames = null;
	private final String url;

	public static ParameterContainer fixContainer(final ParameterContainer pc)
			throws OXFException {
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


			if (pc.containsParameterShellWithServiceSidedName("resultModel")) {
				try {
					if (((String) pc.getParameterShellWithServiceSidedName(
					"resultModel").getSpecifiedValue())
					.equals("om:Measurement")) {
						pc.setParameterValue("resultModel",
							"Measurement");
					}
				} catch (final UnsupportedOperationException uoe) {
					if (((String) pc.getParameterShellWithServiceSidedName(
					"resultModel").getSpecifiedValueArray()[0])
					.equals("om:Measurement")) {
						pc.setParameterValue("resultModel",
							"Measurement");
					}
				}

				try {
					if (((String) pc.getParameterShellWithServiceSidedName(
					"resultModel").getSpecifiedValue())
					.equals("om:Observation")) {
				pc.setParameterValue("resultModel",
					"Observation");
					}} catch (final UnsupportedOperationException uoe) {
						if (((String) pc.getParameterShellWithServiceSidedName(
						"resultModel").getSpecifiedValueArray()[0])
						.equals("om:Observation")) {
					pc.setParameterValue("resultModel",
						"Observation");
				}

		}
			}

		}
		return pc;
	}

	// private Map<String, String> configuredParameter = new HashMap<String,
	// String>();
	// private List<Dataset> offerings;
	/**
	 *
	 */

	public SOSOperationType(final String id, final String url) {
		this.id = id;
		this.url = url;
		paramConf = new ParameterConfiguration(id, url);
	}

	public void addParameter(final Parameter parameter) throws OXFException {
		paramConf.addParameter(parameter);
	}

	public IFeatureStore getFeatureStore() {
		if (id.equals(SOSOperations.opName_GetObservation) || id.equals(SOSOperations.opName_GetObservationById)) {
			return new SOSObservationStore();
		} else {
			return new SOSFoiStore();
		}

	}

	public String getHeartParameterName(){
		if (id.equals("GetFeatureOfInterest")) {
			return "featureOfInterestId";
		} else if (id.equals("GetObservation")) {
			return "offering";
		} else if (id.equals("DescribeSensor")) {

		} else if (id.equals("GetCapabilities")) {

		} else if (id.equals("GetObservationById")) {

		}
		return null;
	}

	/**
	 * Returns the id of this operation
	 * @return the id of the operation, for possible values see {@link SOSOperations}
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns an Operation with the specified name and sets one DCP with the
     * specified httpGetHref and httpPostHref. The constraints and parameters attributes will stay
     * <code>null</code>.
     * 
	 * @param url the url of the sos this operation should connect to
	 * @return the operation
	 */
	public Operation getOperation(final String url) {
		return new Operation(id, "get_not_used", url);
	}

	public ParameterConfiguration getParameterConfiguration(){
		return paramConf;
	}

	public String[] getTypeNames() {
		// TODO these are userreadable?
		if (typeNames != null) {
			return typeNames;
		}
		List<String> l;
		// TODO tuning needed

		// featureofinterest
		if (id.equals("GetFeatureOfInterest")) {
			l = ((IDiscreteValueDomain) paramConf.getParameterByID("featureOfInterestId")
					.getValueDomain()).getPossibleValues();
			typeNames = new String[l.size()];
			for (int i = 0; i < l.size(); i++) {
				typeNames[i] = l.get(i);
			}
			return typeNames;
		}
		// ask for offerings
		else if (id.equals("GetObservation")) {
			l = ((IDiscreteValueDomain) paramConf.getParameterByID("offering")
					.getValueDomain()).getPossibleValues();
			typeNames = new String[l.size()];
			for (int i = 0; i < l.size(); i++) {
				typeNames[i] = l.get(i);
			}
			// typeNames = new String[offerings.size()];
			//
			// for (int i = 0; i < offerings.size(); i++) {
			// typeNames[i] = offerings.get(i).getIdentifier();
			// }
			return typeNames;
		} else if (id.equals("DescribeSensor")) {
			l = ((IDiscreteValueDomain) paramConf.getParameterByID("procedure").getValueDomain()).getPossibleValues();
			typeNames = new String[l.size()];
			for (int i = 0; i < l.size(); i++) {
				typeNames[i] = l.get(i);
			}
			return typeNames;
		} else if (id.equals("GetCapabilities")) {
			return null;
		} else if (id.equals("GetObservationById")) {
			return null;
		}
		return null;
	}

	public boolean isOperationConfigured() {
		for (final Parameter param : paramConf.getRequiredParameters()) {
			if (!paramConf.isParameterConfigured(param.getServiceSidedName())) {
				return false;
			}
		}
		return true;
	}

	public boolean isOperationNearlyConfigured() {
		for (final Parameter param : paramConf.getRequiredParameters()) {
			// all required parameters except the typeName-Parameter are needed
			if (!paramConf.isParameterConfigured(param.getServiceSidedName()) && !param.getServiceSidedName().equals(getHeartParameterName())) {
				return false;
			}
		}
		return true;
	}




	public void setHeartParameter(final String typename) throws Exception {
		paramConf.setParameter(getHeartParameterName(), typename);
	}

	public void setParameterConfiguration(final ParameterConfiguration pc){
		this.paramConf = pc;
	}
}
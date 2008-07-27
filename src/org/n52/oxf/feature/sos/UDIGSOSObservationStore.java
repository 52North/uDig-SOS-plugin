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
Created: 10.04.2008
 *********************************************************************************/
package org.n52.oxf.feature.sos;

import java.io.InputStream;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.n52.oxf.OXFException;
import org.n52.oxf.feature.OXFFeatureCollection;
import org.n52.oxf.feature.OXFObservationCollectionType;
import org.n52.oxf.owsCommon.capabilities.Parameter;
import org.n52.oxf.serviceAdapters.OperationResult;
import org.n52.oxf.util.LoggingHandler;

/**
 * @author <a href="mailto:broering@52north.org">Arne Broering</a>
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class UDIGSOSObservationStore extends SOSObservationStore {

	private static Logger LOGGER = LoggingHandler
			.getLogger(UDIGSOSObservationStore.class);

	/**
	 * 
	 */
	@Override
	public OXFFeatureCollection unmarshalFeatures(
			final OperationResult dataToUnmarshal) throws OXFException {

		final String version = (String) dataToUnmarshal.getUsedParameters()
				.getParameterShellWithCommonName(Parameter.COMMON_NAME_VERSION)
				.getSpecifiedValue();

		OXFFeatureCollection featureCollection = null;

		if (version.equals("1.0.0")) {
			featureCollection = unmarshalFeatures100(dataToUnmarshal);
		} else if (version.equals("0.0.0")) {
			featureCollection = unmarshalFeatures000(dataToUnmarshal);
		}

		return featureCollection;
	}

	@Override
	protected OXFFeatureCollection unmarshalFeatures000(
			final OperationResult dataToUnmarshal) throws OXFException {

		try {
			final InputStream in = dataToUnmarshal.getIncomingResultAsStream();

			final net.opengis.om.x00.ObservationCollectionDocument xb_obsCollectionDoc = net.opengis.om.x00.ObservationCollectionDocument.Factory
					.parse(in);

			// final DLRObservationCollectionType obsCollectionType = new
			// DLRObservationCollectionType();
			final OXFObservationCollectionType obsCollectionType = new OXFObservationCollectionType();

			// create empty OXFFeatureCollection-object:
			final OXFFeatureCollection featureCollection = new OXFFeatureCollection(
					xb_obsCollectionDoc.getObservationCollection().getId(),
					obsCollectionType);

			// initialize the OXFFeatureCollection-object:
			obsCollectionType.initializeFeature(featureCollection,
					xb_obsCollectionDoc.getObservationCollection());

			return featureCollection;
		} catch (final Exception e) {
			throw new OXFException(e);
		}
	}

	@Override
	protected OXFFeatureCollection unmarshalFeatures100(
			final OperationResult dataToUnmarshal) throws OXFException {

		try {
			final InputStream in = dataToUnmarshal.getIncomingResultAsStream();
			if (LOGGER.isEnabledFor(Level.DEBUG)) {
				final StringBuffer buff = new StringBuffer();
				for (final byte c : dataToUnmarshal.getIncomingResult()) {
					buff.append((char) c);
				}
				LOGGER.debug(buff.toString());
			}

			final net.opengis.om.x10.ObservationCollectionDocument xb_obsCollectionDoc = net.opengis.om.x10.ObservationCollectionDocument.Factory
					.parse(in);

			// final DLRObservationCollectionType obsCollectionType = new
			// DLRObservationCollectionType();
			final OXFObservationCollectionType obsCollectionType = new OXFObservationCollectionType();

			// create empty OXFFeatureCollection-object:
			final OXFFeatureCollection featureCollection = new OXFFeatureCollection(
					xb_obsCollectionDoc.getObservationCollection().getId(),
					obsCollectionType);

			// initialize the OXFFeatureCollection-object:
			obsCollectionType.initializeFeature(featureCollection,
					xb_obsCollectionDoc.getObservationCollection());

			return featureCollection;
		} catch (final Exception e) {
			throw new OXFException(e);
		}
	}
}
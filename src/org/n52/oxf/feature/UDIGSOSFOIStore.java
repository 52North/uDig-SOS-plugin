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
package org.n52.oxf.feature;

import java.io.ByteArrayInputStream;

import net.opengis.sampling.x10.SamplingPointDocument;

import org.n52.oxf.OXFException;
import org.n52.oxf.feature.sos.SOSFoiStore;
import org.n52.oxf.serviceAdapters.OperationResult;

/**
 * @author 52n
 * 
 */
public class UDIGSOSFOIStore extends SOSFoiStore {

	public UDIGSOSFOIStore() {
		super();
	}

	@Override
	public OXFFeatureCollection unmarshalFeatures(final OperationResult opsRes)
			throws OXFException {
		ByteArrayInputStream is = opsRes.getIncomingResultAsStream();
		try {
			final net.opengis.gml.FeatureCollectionDocument2 xb_featureCollDoc = net.opengis.gml.FeatureCollectionDocument2.Factory
					.parse(is);
			final net.opengis.gml.AbstractFeatureCollectionType xb_collection = xb_featureCollDoc
					.getFeatureCollection();
			return unmarshalFeatures(xb_collection);
		} catch (final Exception e1) {
			try {
				is = opsRes.getIncomingResultAsStream();
				final SamplingPointDocument spd = SamplingPointDocument.Factory
						.parse(is);

				final OXFAbstractFeatureCollectionType oxf_abstFeatureCollType = new OXFAbstractFeatureCollectionType();
				// create empty OXFFeatureCollection-object:
				final OXFFeatureCollection oxf_featureCollection = new OXFFeatureCollection(
						"any_ID", oxf_abstFeatureCollType);
				oxf_featureCollection.add(OXFSamplingPointType.create(spd));
				return oxf_featureCollection;

			} catch (final Exception e2) {
				throw new OXFException(e2);
			}
		}
	}
}

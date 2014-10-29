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

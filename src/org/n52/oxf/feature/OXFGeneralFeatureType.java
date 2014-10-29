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

import java.util.Map;
import java.util.Map.Entry;

import org.opengis.feature.FeatureAttributeDescriptor;



import com.vividsolutions.jts.geom.Geometry;

/**
 * @author 52n
 * 
 */
public class OXFGeneralFeatureType extends OXFAbstractFeatureType {

	public OXFGeneralFeatureType() {
		// super("OXFGeneralFeature");
		featureAttributeDescriptors = generateAttributeDescriptors();
	}

	public void initializeFeature(final OXFFeature feature,
			final String[] nameValue, final String descriptionValue,
			final Geometry locationValue, final Map<String, Object> attributes,
			final Object NODATA) {
		super.initializeFeature(feature, nameValue, descriptionValue,
				locationValue);
		for (final Entry<String, Object> o : attributes.entrySet()) {
			if (o.getValue() == null) {
				// TODO NODATA!!!
				// feature.setAttribute(o.getKey(), NODATA);
			} else {
				feature.setAttribute(o.getKey(), o.getValue());
			}
		}
	}

	public void addAttributeDescription(final FeatureAttributeDescriptor desc) {
		featureAttributeDescriptors.add(desc);
	}

}

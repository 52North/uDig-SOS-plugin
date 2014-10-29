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
package org.geotools.feature.sos;

import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

/**
 * @author 52n
 * 
 */
public class SOSFeatureCollections extends DefaultFeatureCollections {

	/**
	 * Creates a new instance of DefaultFeatureCollections
	 */
	public SOSFeatureCollections() {
		super();
	}

	/**
	 * create a new FeatureCollection using the current default factory.
	 * 
	 * @return A FeatureCollection instance.
	 */
	public static FeatureCollection newCollection() {
		return new SOSFeatureCollections().createCollection();
	}

	/**
	 * Creates a new DefaultFeatureCollection.
	 * 
	 * @return A new, empty DefaultFeatureCollection.
	 */
	@Override
	protected FeatureCollection createCollection() {
		return new SOSFeatureCollection(null, null);
	}

	@Override
	protected FeatureCollection createCollection(final String id,
			final FeatureType ft) {
		return new SOSFeatureCollection(id, ft);
	}
}

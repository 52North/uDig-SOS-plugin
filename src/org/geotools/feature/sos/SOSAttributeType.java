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

import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.n52.oxf.feature.Duplicatable;
import org.n52.oxf.owsCommon.capabilities.ITime;
import org.n52.oxf.util.LoggingHandler;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.udig.catalog.internal.sos.dataStore.OXFFeatureConverter;

/**
 * @author Carsten Priess
 * 
 */
public class SOSAttributeType extends DefaultAttributeType {
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSAttributeType.class);

	/**
	 * @param copy
	 */
	public SOSAttributeType(final AttributeType copy) {
		super(copy);
	}

	/**
	 * @param name
	 * @param type
	 * @param nillable
	 * @param defaultValue
	 */
	@SuppressWarnings("unchecked")
	public SOSAttributeType(final String name, final Class type,
			final boolean nillable, final Object defaultValue) {
		super(name, type, nillable, defaultValue);
	}

	/**
	 * @param name
	 * @param type
	 * @param nillable
	 * @param min
	 * @param max
	 * @param defaultValue
	 */
	@SuppressWarnings("unchecked")
	public SOSAttributeType(final String name, final Class type,
			final boolean nillable, final int min, final int max,
			final Object defaultValue) {
		super(name, type, nillable, min, max, defaultValue);
	}

	/**
	 * @param name
	 * @param type
	 * @param nillable
	 * @param min
	 * @param max
	 * @param defaultValue
	 * @param f
	 */
	public SOSAttributeType(final String name, final Class type,
			final boolean nillable, final int min, final int max,
			final Object defaultValue, final Filter f) {
		super(name, type, nillable, min, max, defaultValue, f);
	}

	@Override
	public Object duplicate(final Object src) throws IllegalAttributeException {
		try {

			if (src instanceof Duplicatable) {
				return ((Duplicatable) src).duplicate();
			} else if (src instanceof String[]) {
				return (src);
			} else if (src instanceof ITime) {
				return TimeFactory.createTime(((ITime) src)
						.toISO8601FormatWithMillies());
			} else if (src instanceof java.util.List) {
				return OXFFeatureConverter.cloneList((List)src);
			} else {
				return super.duplicate(src);
			}

		} catch (final IllegalAttributeException e) {
			LOGGER
					.error("Could not duplicate " + src + " -> Returning null",
							e);
		}
		return null;
	}

}

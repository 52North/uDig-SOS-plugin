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
Created: 20.03.2008
 *********************************************************************************/
package org.geotools.feature.sos;

import java.util.List;

import org.apache.log4j.Logger;
import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultAttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.n52.oxf.feature.Duplicatable;
import org.n52.oxf.owsCommon.capabilities.ITime;
import org.n52.oxf.util.LoggingHandler;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.udig.catalog.internal.sos.dataStore.OXFFeatureConverter;

/**
 * @author Carsten Prieß
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

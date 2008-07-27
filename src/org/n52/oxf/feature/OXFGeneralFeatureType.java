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
package org.n52.oxf.feature;

import java.util.Map;
import java.util.Map.Entry;

import geoapi20.org.opengis.feature.FeatureAttributeDescriptor;

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

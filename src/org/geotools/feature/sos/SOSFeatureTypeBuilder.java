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

import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.FeatureTypeFactory;

/**
 * @author 52n
 * 
 */
public abstract class SOSFeatureTypeBuilder extends FeatureTypeBuilder {

	/**
	 * Create a new FeatureTypeFactory with the given typeName.
	 * 
	 * @param name
	 *            The typeName of the feature to create.
	 * 
	 * @return A new FeatureTypeFactory instance.
	 * 
	 * @throws FactoryConfigurationError
	 *             If there exists a configuration error.
	 */
	public static FeatureTypeFactory newInstance(final String name)
			throws FactoryConfigurationError {
		// final SOSFeatureTypeFactory factory = (SOSFeatureTypeFactory)
		// FactoryFinder
		// .findFactory("org.geotools.feature.sos.SOSFeatureTypeFactory",
		// "org.geotools.feature.sos.SOSFeatureTypeFactory");
		final SOSFeatureTypeFactory factory = new SOSFeatureTypeFactory();
		factory.setName(name);

		return factory;
	}

}

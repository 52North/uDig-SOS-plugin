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

//uses code from
/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.feature.sos;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.geotools.factory.FactoryCreator;
import org.geotools.factory.FactoryRegistry;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.SchemaException;
import org.geotools.xml.gml.GMLSchema;

/**
 * @author 52n
 * 
 */
public class SOSFeatureTypeFactory extends FeatureTypeFactory {
	private final List<AttributeType> attributeTypes = new ArrayList<AttributeType>();

	@Override
	protected void add(final AttributeType type)
			throws IllegalArgumentException {
		attributeTypes.add(type);
	}

	@Override
	protected void add(final int idx, final AttributeType type)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		attributeTypes.add(idx, type);
	}

	@Override
	protected FeatureType createFeatureType() throws SchemaException {
		if (isAbstract()) {
			return createAbstractType();
		}

		return new SOSFeatureType(getName(), getNamespace(), attributeTypes,
				getSuperTypes(), getDefaultGeometry());
	}

	@Override
	public AttributeType get(final int idx)
			throws ArrayIndexOutOfBoundsException {
		return attributeTypes.get(idx);
	}

	@Override
	public int getAttributeCount() {
		return attributeTypes.size();
	}

	@Override
	protected AttributeType remove(final int idx)
			throws ArrayIndexOutOfBoundsException {
		return attributeTypes.remove(idx);
	}

	@Override
	protected AttributeType remove(final AttributeType type) {
		if (attributeTypes.remove(type)) {
			return type;
		}

		return null;
	}

	@Override
	protected AttributeType set(final int idx, final AttributeType type)
			throws ArrayIndexOutOfBoundsException, IllegalArgumentException {
		final AttributeType former = get(idx);
		attributeTypes.set(idx, type);

		return former;
	}

	protected FeatureType createAbstractType() throws SchemaException {
		return new SOSFeatureType.Abstract(getName(), getNamespace(),
				attributeTypes, getSuperTypes(), getDefaultGeometry());
	}

	/**
	 * The service registry for this manager. Will be initialized only when
	 * first needed.
	 */
	private static FactoryRegistry registry;

	/**
	 * Returns the service registry. The registry will be created the first time
	 * this method is invoked.
	 */
	private static FactoryRegistry getServiceRegistry() {
		assert Thread.holdsLock(FeatureTypeFactory.class);
		if (registry == null) {
			registry = new FactoryCreator(Arrays
					.asList(new Class[] { FeatureTypeFactory.class }));
		}
		return registry;
	}

	/**
	 * The most specific way to create a new FeatureType.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * @param isAbstract
	 *            True if this created type should be abstract.
	 * @param superTypes
	 *            A Collection of types the FeatureType will inherit from.
	 *            Currently, all types inherit from feature in the opengis
	 *            namespace.
	 * 
	 * @return A new FeatureType created from the given arguments.
	 * 
	 * @throws FactoryConfigurationError
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 */
	public static FeatureType newFeatureType(final AttributeType[] types,
			final String name, final URI ns, final boolean isAbstract,
			final FeatureType[] superTypes) throws FactoryRegistryException,
			SchemaException {
		return newFeatureType(types, name, ns, isAbstract, superTypes, null);
	}

	/**
	 * The most specific way to create a new FeatureType.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * @param isAbstract
	 *            True if this created type should be abstract.
	 * @param superTypes
	 *            A Collection of types the FeatureType will inherit from.
	 *            Currently, all types inherit from feature in the opengis
	 *            namespace.
	 * 
	 * @return A new FeatureType created from the given arguments.
	 * 
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 */
	public static FeatureType newFeatureType(final AttributeType[] types,
			final String name, final URI ns, final boolean isAbstract,
			final FeatureType[] superTypes, final AttributeType defaultGeometry)
			throws FactoryRegistryException, SchemaException {
		final FeatureTypeFactory factory = newInstance(name);
		factory.addTypes(types);
		factory.setNamespace(ns);
		factory.setAbstract(isAbstract);
		if (defaultGeometry != null) {
			factory.setDefaultGeometry((GeometryAttributeType) defaultGeometry);
		}

		if (superTypes != null) {
			factory.setSuperTypes(Arrays.asList(superTypes));
		}

		return factory.getFeatureType();
	}

	/**
	 * The most specific way to create a new FeatureType.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * @param isAbstract
	 *            True if this created type should be abstract.
	 * @param superTypes
	 *            A Collection of types the FeatureType will inherit from.
	 *            Currently, all types inherit from feature in the opengis
	 *            namespace.
	 * 
	 * @return A new FeatureType created from the given arguments.
	 * 
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 */
	public static FeatureType newFeatureType(final AttributeType[] types,
			final String name, final URI ns, final boolean isAbstract,
			final FeatureType[] superTypes,
			final GeometryAttributeType defaultGeometry)
			throws FactoryRegistryException, SchemaException {
		final FeatureTypeFactory factory = newInstance(name);
		factory.addTypes(types);
		factory.setNamespace(ns);
		factory.setAbstract(isAbstract);

		if (superTypes != null) {
			factory.setSuperTypes(Arrays.asList(superTypes));
		}

		if (defaultGeometry != null) {
			factory.setDefaultGeometry(defaultGeometry);
		}

		return factory.getFeatureType();
	}

	/**
	 * Create a new FeatureType with the given AttributeTypes. A short cut for
	 * calling <code>newFeatureType(types,name,ns,isAbstract,null)</code>.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * @param isAbstract
	 *            True if this created type should be abstract.
	 * 
	 * @return A new FeatureType created from the given arguments.
	 * 
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 */
	public static FeatureType newFeatureType(final AttributeType[] types,
			final String name, final URI ns, final boolean isAbstract)
			throws FactoryRegistryException, SchemaException {
		return newFeatureType(types, name, ns, isAbstract, null);
	}

	/**
	 * Create a new FeatureType with the given AttributeTypes. A short cut for
	 * calling <code>newFeatureType(types,name,ns,false,null)</code>.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * @param ns
	 *            The namespace of the FeatureType. Optional, may be null.
	 * 
	 * @return A new FeatureType created from the given arguments.
	 * 
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 */
	public static FeatureType newFeatureType(final AttributeType[] types,
			final String name, final URI ns) throws FactoryRegistryException,
			SchemaException {
		return newFeatureType(types, name, ns, false);
	}

	/**
	 * Create a new FeatureType with the given AttributeTypes. A short cut for
	 * calling <code>newFeatureType(types,name,null,false,null)</code>.
	 * Useful for test cases or datasources which may not allow a namespace.
	 * 
	 * @param types
	 *            The AttributeTypes to create the FeatureType with.
	 * @param name
	 *            The typeName of the FeatureType. Required, may not be null.
	 * 
	 * @return A new FeatureType created from the given arguments.
	 * 
	 * @throws FactoryRegistryException
	 *             If there are problems creating a factory.
	 * @throws SchemaException
	 *             If the AttributeTypes provided are invalid in some way.
	 */
	public static FeatureType newFeatureType(final AttributeType[] types,
			final String name) throws FactoryRegistryException, SchemaException {
		return newFeatureType(types, name, GMLSchema.NAMESPACE, false);
	}

	/**
	 * Create a FeatureTypeFactory which contains all of the AttributeTypes from
	 * the given FeatureType. This is simply a convenience method for<br>
	 * <code><pre>
	 * FeatureTypeFactory factory = FeatureTypeFactory.newInstace();
	 * factory.importType(yourTypeHere);
	 * factory.setName(original.getName());
	 * factory.setNamespace(original.getNamespace());
	 * factory.setNillable(original.isNillable());
	 * factory.setDefaultGeometry(original.getDefaultGeometry());
	 * </pre></code>
	 * 
	 * @param original
	 *            The FeatureType to obtain information from.
	 * 
	 * @return A new FeatureTypeFactory which is initialized with the state of
	 *         the original FeatureType.
	 * 
	 * @throws FactoryRegistryException
	 *             If a FeatureTypeFactory cannot be found.
	 */
	public static FeatureTypeFactory createTemplate(final FeatureType original)
			throws FactoryRegistryException {

		final FeatureTypeFactory builder = FeatureTypeFactory
				.newInstance(original.getTypeName());
		builder.importType(original);
		builder.setNamespace(original.getNamespace());
		builder.setDefaultGeometry(original.getDefaultGeometry());

		final FeatureType[] ancestors = original.getAncestors();

		if (ancestors != null) {
			builder.setSuperTypes(Arrays.asList(ancestors));
		}

		return builder;
	}

	/**
	 * Returns a string representation of this factory.
	 * 
	 * @return The string representing this factory.
	 */
	@Override
	public String toString() {
		String types = "";

		for (int i = 0, ii = getAttributeCount(); i < ii; i++) {
			types += get(i);

			if (i < ii) {
				types += " , ";
			}
		}

		return "FeatureTypeFactory(" + getClass().getName() + ") [ " + types
				+ " ]";
	}
}

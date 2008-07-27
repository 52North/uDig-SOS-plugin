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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Iterator;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.xml.gml.GMLSchema;

/**
 * @author 52n
 * 
 */
public class SOSFeatureType implements FeatureType {

	/** attname:string -> position:int */
	private final java.util.Map attLookup;

	/** An feature type with no attributes */
	public static final FeatureType EMPTY = new SOSFeatureType();

	/** The name of this FeatureType. */
	private final String typeName;

	/** The namespace to uniquely identify this FeatureType. */
	private final URI namespace;

	/** The array of types that this FeatureType can have as attributes. */
	private final AttributeType[] types;

	/** The FeatureTypes this is descended from. */
	private final FeatureType[] ancestors;

	/** The default geometry AttributeType. */
	private final GeometryAttributeType defaultGeom;

	private final int hashCode;

	/**
	 * The position of the default Geometry Leave as package protected for use
	 * by DefaultFeature
	 */
	final int defaultGeomIdx;

	public SOSFeatureType(final String typeName, final String namespace,
			final Collection types, final Collection superTypes,
			final GeometryAttributeType defaultGeom) throws SchemaException,
			NullPointerException {
		this(typeName, toURI(namespace), types, superTypes, defaultGeom);
	}

	/**
	 * Constructs a new SOSeatureType.
	 * 
	 * <p>
	 * Attributes from the superTypes will be copied to the list of attributes
	 * for this feature type.
	 * 
	 * @param typeName
	 *            The name to give this FeatureType.
	 * @param namespace
	 *            The namespace of the new FeatureType.
	 * @param types
	 *            The attributeTypes to use for validation.
	 * @param superTypes
	 *            The ancestors of this FeatureType.
	 * @param defaultGeom
	 *            The attributeType to set as the defaultGeometry.
	 * 
	 * @throws SchemaException
	 *             For problems making the FeatureType.
	 * @throws NullPointerException
	 *             If typeName is null.
	 */
	public SOSFeatureType(final String typeName, final URI namespace,
			final Collection types, final Collection superTypes,
			final GeometryAttributeType defaultGeom)
			throws NullPointerException {
		if (typeName == null) {
			throw new NullPointerException(typeName);
		}

		this.typeName = typeName;
		this.namespace = namespace == null ? GMLSchema.NAMESPACE : namespace;
		this.ancestors = (FeatureType[]) superTypes
				.toArray(new FeatureType[superTypes.size()]);

		final Collection attributes = new java.util.ArrayList(types);
		for (final FeatureType ancestor : ancestors) {
			for (int j = 0, jj = ancestor.getAttributeCount(); j < jj; j++) {
				attributes.add(ancestor.getAttributeType(j));
			}
		}
		if (attributes.size() != 0) {
			this.types = (AttributeType[]) attributes
					.toArray(new AttributeType[attributes.size()]);
		} else {
			this.types = new AttributeType[0];
		}

		this.defaultGeom = defaultGeom;

		attLookup = new java.util.HashMap(this.types.length);
		for (int i = 0, ii = this.types.length; i < ii; i++) {
			attLookup.put(this.types[i].getName(), new Integer(i));
		}

		this.defaultGeomIdx = find(defaultGeom);

		hashCode = computeHash();
	}

	private SOSFeatureType() {
		this.typeName = "emptyFeatureType";
		namespace = GMLSchema.NAMESPACE;
		this.types = new SOSAttributeType[0];
		this.ancestors = new FeatureType[0];
		this.defaultGeomIdx = -1;
		this.defaultGeom = null;
		hashCode = computeHash();
		attLookup = java.util.Collections.EMPTY_MAP;
	}

	public Feature duplicate(final Feature original)
			throws IllegalAttributeException {
		if (original == null) {
			return null;
		}
		final FeatureType featureType = original.getFeatureType();
		if (!featureType.equals(this)) {
			throw new IllegalAttributeException("Feature type " + featureType
					+ " does not match " + this);
		}
		final String id = original.getID();
		final int numAtts = featureType.getAttributeCount();
		final Object attributes[] = new Object[numAtts];
		for (int i = 0; i < numAtts; i++) {
			final AttributeType curAttType = getAttributeType(i);
			attributes[i] = curAttType.duplicate(original.getAttribute(i));
		}
		return featureType.create(attributes, id);
	}

	/**
	 * Creates a new feature, with the proper featureID, using this FeatureType.
	 * 
	 * @param attributes
	 *            the array of attribute values.
	 * @param featureID
	 *            the feature ID.
	 * 
	 * @return the created feature.
	 * 
	 * @throws IllegalAttributeException
	 *             if this FeatureType does not validate the attributes.
	 */
	public Feature create(final Object[] attributes, final String featureID)
			throws IllegalAttributeException {
		return new SOSFeature(this, attributes, featureID);
	}

	private final static URI toURI(final String namespace)
			throws SchemaException {
		try {
			return new URI(namespace);
		} catch (final URISyntaxException badNamespace) {
			throw new SchemaException(badNamespace);
		}
	}

	/**
	 * Creates a new feature, with a generated unique featureID. This is less
	 * than ideal, as a FeatureID should be persistant over time, generally
	 * created by a datasource. This method is more for testing that doesn't
	 * need featureID.
	 * 
	 * @param attributes
	 *            the array of attribute values
	 * 
	 * @return The created feature with this as its feature type.
	 * 
	 * @throws IllegalAttributeException
	 *             if this FeatureType does not validate the attributes.
	 */
	public Feature create(final Object[] attributes)
			throws IllegalAttributeException {
		return create(attributes, null);
	}

	/**
	 * Gets the default geometry AttributeType. If the FeatureType has more one
	 * geometry it is up to the implementor to determine which geometry is the
	 * default. If working with multiple geometries it is best to get the
	 * attributeTypes and iterate through them, checking isGeometry on each.
	 * This should just be used a convenience method when it is known that the
	 * features are flat.
	 * 
	 * @return The attribute type of the default geometry, which will contain
	 *         the position.
	 */
	public GeometryAttributeType getDefaultGeometry() {
		return defaultGeom;
	}

	/**
	 * Gets the attributeType at this xPath, if the specified attributeType does
	 * not exist then null is returned.
	 * 
	 * @param xPath
	 *            XPath pointer to attribute type.
	 * 
	 * @return True if attribute exists.
	 */
	public AttributeType getAttributeType(final String xPath) {
		AttributeType attType = null;
		final int idx = find(xPath);
		if (idx >= 0) {
			attType = types[idx];
		}
		return attType;
	}

	/**
	 * Find the position of a given AttributeType.
	 * 
	 * @param type
	 *            The type to search for.
	 * 
	 * @return -1 if not found, a zero-based index if found.
	 */
	public int find(final AttributeType type) {
		if (type == null) {
			return -1;
		}
		int idx = find(type.getName());
		if (idx < 0 || !types[idx].equals(type)) {
			idx = -1;
		}
		return idx;
	}

	/**
	 * Find the position of an AttributeType which matches the given String.
	 * 
	 * @param attName
	 *            the name to look for
	 * @return -1 if not found, zero-based index otherwise
	 */
	public int find(final String attName) {
		final Integer idx = (Integer) attLookup.get(attName);
		return idx == null ? -1 : idx.intValue();
	}

	/**
	 * Gets the attributeType at the specified index.
	 * 
	 * @param position
	 *            the position of the attribute to check.
	 * 
	 * @return The attribute type at the specified position.
	 */
	public AttributeType getAttributeType(final int position) {
		return types[position];
	}

	public AttributeType[] getAttributeTypes() {
		return types.clone();
	}

	/**
	 * Gets the global schema namespace.
	 * 
	 * @return Namespace of schema.
	 */
	public URI getNamespace() {
		return namespace;
	}

	/**
	 * Gets the type name for this schema.
	 * 
	 * @return Namespace of schema.
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * This is only used twice in the whole geotools code base, and one of those
	 * is for a test, so we're removing it from the interface. If
	 * getAttributeType does not have the AttributeType it will just return
	 * null. Gets the number of occurrences of this attribute.
	 * 
	 * @param xPath
	 *            XPath pointer to attribute type.
	 * 
	 * @return Number of occurrences.
	 */
	public boolean hasAttributeType(final String xPath) {
		return getAttributeType(xPath) != null;
	}

	/**
	 * Returns the number of attributes at the first 'level' of the schema.
	 * 
	 * @return the total number of first level attributes.
	 */
	public int getAttributeCount() {
		return types.length;
	}

	public boolean equals(final FeatureType other) {
		if (other == this) {
			return true;
		}

		if (other == null) {
			return false;
		}

		if ((typeName == null) && (other.getTypeName() != null)) {
			return false;
		} else if (!typeName.equals(other.getTypeName())) {
			return false;
		}

		if ((namespace == null) && (other.getNamespace() != null)) {
			return false;
		} else if (!namespace.equals(other.getNamespace())) {
			return false;
		}

		if (types.length != other.getAttributeCount()) {
			return false;
		}

		for (int i = 0, ii = types.length; i < ii; i++) {
			if (!types[i].equals(other.getAttributeType(i))) {
				return false;
			}
		}

		return true;
	}

	private int computeHash() {
		int hash = typeName.hashCode() ^ namespace.hashCode();
		for (final AttributeType element : types) {
			hash ^= element.hashCode();
		}
		return hash;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public String toString() {
		String info = "name=" + typeName;
		info += (" , namespace=" + namespace);
		info += (" , abstract=" + isAbstract());

		String types1 = "types=(";

		for (int i = 0, ii = this.types.length; i < ii; i++) {
			types1 += this.types[i].toString();

			if (i < ii) {
				types1 += ",";
			}
		}

		types1 += ")";
		info += (" , " + types1);

		return "SOSFeatureType [" + info + "]";
	}

	@Override
	public boolean equals(final Object other) {
		if (other instanceof FeatureType) {
			return equals((FeatureType) other);
		}
		return false;
	}

	/**
	 * Obtain an array of this FeatureTypes ancestors. Implementors should
	 * return a non-null array (may be of length 0).
	 * 
	 * @return An array of ancestors.
	 */
	public FeatureType[] getAncestors() {
		return ancestors;
	}

	/**
	 * Is this FeatureType an abstract type?
	 * 
	 * @return true if abstract, false otherwise.
	 */
	public boolean isAbstract() {
		return false;
	}

	/**
	 * A convenience method for calling<br>
	 * <code> FeatureType f1; FeatureType f2;
	 * f1.isDescendedFrom(f2.getNamespace(),f2.getName()); </code>
	 * 
	 * @param type
	 *            The type to compare to.
	 * 
	 * @return true if descendant, false otherwise.
	 */
	public boolean isDescendedFrom(final FeatureType type) {
		return isDescendedFrom(type.getNamespace(), type.getTypeName());
	}

	/**
	 * Test to determine whether this FeatureType is descended from the given
	 * FeatureType. Think of this relationship likes the "extends" relationship
	 * in java.
	 * 
	 * @param nsURI
	 *            The namespace URI to use.
	 * @param typeName1
	 *            The typeName.
	 * 
	 * @return true if descendant, false otherwise.
	 * 
	 * @task HACK: if nsURI is null only typeName is tested.
	 */
	public boolean isDescendedFrom(final URI nsURI, final String typeName1) {
		for (final FeatureType element : ancestors) {
			if (((nsURI == null) || element.getNamespace().equals(nsURI))
					&& element.getTypeName().equals(typeName1)) {
				return true;
			}
		}
		return false;
	}

	static final class Abstract extends SOSFeatureType {
		public Abstract(final String typeName, final URI namespace,
				final Collection types, final Collection superTypes,
				final GeometryAttributeType defaultGeom) throws SchemaException {
			super(typeName, namespace, types, superTypes, defaultGeom);

			final Iterator st = superTypes.iterator();

			while (st.hasNext()) {
				final FeatureType ft = (FeatureType) st.next();

				if (!ft.isAbstract()) {
					throw new SchemaException(
							"Abstract type cannot descend from no abstract type : "
									+ ft);
				}
			}
		}

		@Override
		public final boolean isAbstract() {
			return true;
		}

		@Override
		public Feature create(final Object[] atts)
				throws IllegalAttributeException {
			throw new UnsupportedOperationException("Abstract Type");
		}

		@Override
		public Feature create(final Object[] atts, final String id)
				throws IllegalAttributeException {
			throw new UnsupportedOperationException("Abstract Type");
		}

	}
}

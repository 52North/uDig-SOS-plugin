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

import java.rmi.server.UID;
import java.util.List;

import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.GeometryAttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SimpleFeature;
import org.opengis.util.Cloneable;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author 52n
 * 
 */
public class SOSFeature implements SimpleFeature, Cloneable {

	/**
	 * @param schema
	 * @param attributes
	 * @throws IllegalAttributeException
	 */
	public SOSFeature(final SOSFeatureType schema, final Object[] attributes)
			throws IllegalAttributeException {
		this(schema, attributes, null);
	}

	/**
	 * @param schema
	 * @param attributes
	 * @param featureID
	 * @throws IllegalAttributeException
	 * @throws NullPointerException
	 */
	public SOSFeature(final SOSFeatureType schema, final Object[] attributes,
			final String featureID) throws IllegalAttributeException,
			NullPointerException {
		if (schema == null) {
			throw new NullPointerException("schema");
		}

		this.schema = schema;
		this.featureId = (featureID == null) ? defaultID() : featureID;
		this.attributes = new Object[schema.getAttributeCount()];

		setAttributes(attributes);
	}

	/** The unique id of this feature */
	protected String featureId;

	/** Flat feature type schema for this feature. */
	private final SOSFeatureType schema;

	/** Attributes for the feature. */
	private final Object[] attributes;

	/** The bounds of this feature. */
	private Envelope bounds;

	/** The collection that this Feature is a member of */
	private FeatureCollection parent;

	/**
	 * Creates an ID from a hashcode.
	 * 
	 * @return an id for the feature.
	 */
	String defaultID() {
		// According to GML and XML schema standards, FID is a XML ID
		// (http://www.w3.org/TR/xmlschema-2/#ID), whose acceptable values are
		// those that match an
		// NCNAME production
		// (http://www.w3.org/TR/1999/REC-xml-names-19990114/#NT-NCName):
		// NCName ::= (Letter | '_') (NCNameChar)* /* An XML Name, minus the ":"
		// */
		// NCNameChar ::= Letter | Digit | '.' | '-' | '_' | CombiningChar |
		// Extender
		// We have to fix the generated UID replacing all non word chars with an
		// _ (it seems
		// they area all ":")
		return "fid-" + new UID().toString().replaceAll("\\W", "_");
	}

	/**
	 * Gets a reference to the feature type schema for this feature.
	 * 
	 * @return A copy of this feature's metadata in the form of a feature type
	 *         schema.
	 */
	public FeatureType getFeatureType() {
		return schema;
	}

	/**
	 * Gets the unique indentification string of this Feature.
	 * 
	 * @return The unique id.
	 */
	public String getID() {
		return featureId;
	}

	/**
	 * Copy all the attributes of this Feature into the given array. If the
	 * argument array is null, a new one will be created. Gets all attributes
	 * from this feature, returned as a complex object array. This array comes
	 * with no metadata, so to interpret this collection the caller class should
	 * ask for the schema as well.
	 * 
	 * @param array
	 *            The array to copy the attributes into.
	 * 
	 * @return The array passed in, or a new one if null.
	 */
	public Object[] getAttributes(final Object[] array) {
		Object[] retArray;

		if (array == null) {
			retArray = new Object[attributes.length];
		} else {
			retArray = array;
		}

		System.arraycopy(attributes, 0, retArray, 0, attributes.length);

		return retArray;
	}

	/**
	 * Gets an attribute for this feature at the location specified by xPath.
	 * 
	 * @param xPath
	 *            XPath representation of attribute location.
	 * 
	 * @return Attribute.
	 */
	public Object getAttribute(final String xPath) {
		final int idx = schema.find(xPath);

		if (idx == -1) {
			return null;
		}

		return attributes[idx];
	}

	/**
	 * Gets an attribute by the given zero-based index.
	 * 
	 * @param index
	 *            the position of the attribute to retrieve.
	 * 
	 * @return The attribute at the given index.
	 */
	public Object getAttribute(final int index) {
		return attributes[index];
	}

	/**
	 * Sets the attribute at position to val.
	 * 
	 * @param position
	 *            the index of the attribute to set.
	 * @param val
	 *            the new value to give the attribute at position.
	 * 
	 * @throws IllegalAttributeException
	 *             if the passed in val does not validate against the
	 *             AttributeType at that position.
	 */
	public void setAttribute(final int position, Object val)
			throws IllegalAttributeException {
		final AttributeType type = schema.getAttributeType(position);

		try {
			if ((val == null) && !type.isNillable()) {
				val = type.createDefaultValue();
			}
			final Object parsed = type.parse(val);
			type.validate(parsed);
			setAttributeValue(position, parsed);
		} catch (final IllegalArgumentException iae) {
			throw new IllegalAttributeException(type, val, iae);
		}
	}

	/**
	 * Sets the attribute value at a given position, performing no parsing or
	 * validation. This is so subclasses can have access to setting the array,
	 * without opening it up completely.
	 * 
	 * @param position
	 *            the index of the attribute to set.
	 * @param val
	 *            the new value to give the attribute at position.
	 */
	protected void setAttributeValue(final int position, final Object val) {
		attributes[position] = val;
	}

	/**
	 * Sets all attributes for this feature, passed as an array. All attributes
	 * are checked for validity before adding.
	 * 
	 * @param attributes
	 *            All feature attributes.
	 * 
	 * @throws IllegalAttributeException
	 *             Passed attributes do not match feature type.
	 */
	public void setAttributes(final Object[] attributes)
			throws IllegalAttributeException {
		// the passed in attributes were null, lets make that a null array
		Object[] newAtts = attributes;

		if (attributes == null) {
			newAtts = new Object[this.attributes.length];
		}

		if (newAtts.length != this.attributes.length) {
			throw new IllegalAttributeException(
					"Wrong number of attributes expected "
							+ schema.getAttributeCount() + " got "
							+ newAtts.length);
		}

		for (int i = 0, ii = newAtts.length; i < ii; i++) {
			setAttribute(i, newAtts[i]);
		}
	}

	/**
	 * Sets a single attribute for this feature, passed as a complex object. If
	 * the attribute does not exist or the object does not conform to the
	 * internal feature type, an exception is thrown.
	 * 
	 * @param xPath
	 *            XPath representation of attribute location.
	 * @param attribute
	 *            Feature attribute to set.
	 * 
	 * @throws IllegalAttributeException
	 *             Passed attribute does not match feature type
	 */
	public void setAttribute(final String xPath, final Object attribute)
			throws IllegalAttributeException {
		final int idx = schema.find(xPath);

		if (idx < 0) {
			throw new IllegalAttributeException("No attribute named " + xPath);
		}

		setAttribute(idx, attribute);
	}

	/**
	 * Gets the geometry for this feature.
	 * 
	 * @return Geometry for this feature.
	 */
	public Geometry getDefaultGeometry() {
		final int idx = schema.defaultGeomIdx;

		if (idx == -1) {
			return null;
		}

		return (Geometry) attributes[idx];
	}

	/**
	 * Modifies the geometry.
	 * 
	 * @param geometry
	 *            All feature attributes.
	 * 
	 * @throws IllegalAttributeException
	 *             if the feature does not have a geometry.
	 */
	public void setDefaultGeometry(final Geometry geometry)
			throws IllegalAttributeException {
		final int idx = schema.defaultGeomIdx;

		if (idx < 0) {
			throw new IllegalAttributeException(
					"Feature does not have geometry");
		}

		attributes[idx] = geometry;
		bounds = null;
	}

	/**
	 * Get the number of attributes this feature has. This is simply a
	 * convenience method for calling getFeatureType().getNumberOfAttributes();
	 * 
	 * @return The total number of attributes this Feature contains.
	 */
	public int getNumberOfAttributes() {
		return attributes.length;
	}

	/**
	 * Get the total bounds of this feature which is calculated by doing a union
	 * of the bounds of each geometry this feature is associated with.
	 * 
	 * @return An Envelope containing the total bounds of this Feature.
	 * 
	 * @task REVISIT: what to return if there are no geometries in the feature?
	 *       For now we'll return a null envelope, make this part of interface?
	 *       (IanS - by OGC standards, all Feature must have geom)
	 */
	public Envelope getBounds() {
		if (bounds == null) {
			bounds = new Envelope();

			for (int i = 0, n = schema.getAttributeCount(); i < n; i++) {
				if (schema.getAttributeType(i) instanceof GeometryAttributeType) {
					final Geometry g = (Geometry) attributes[i];

					// IanS - check for null geometry!
					if (g == null) {
						continue;
					}

					final Envelope e = g.getEnvelopeInternal();

					// IanS
					// as of JTS 1.3, expandToInclude does not check to see if
					// Envelope is "null", and simply adds the flagged values.
					// This ensures that this behavior does not occur.
					if (!e.isNull()) {
						bounds.expandToInclude(e);
					}
				}
			}
		}

		// lets be defensive
		return new Envelope(bounds);
	}

	/**
	 * Creates an exact copy of this feature.
	 * 
	 * @return A default feature.
	 * 
	 * @throws RuntimeException
	 *             DOCUMENT ME!
	 */
	@Override
	public Object clone() {
		try {
			final SOSFeature clone = (SOSFeature) super.clone();

			for (int i = 0; i < attributes.length; i++) {
				try {
					clone.setAttribute(i, attributes[i]);
				} catch (final IllegalAttributeException e1) {
					throw new RuntimeException("The impossible has happened",
							e1);
				}
			}

			return clone;
		} catch (final CloneNotSupportedException e) {
			throw new RuntimeException("The impossible has happened", e);
		}
	}

	/**
	 * Returns a string representation of this feature.
	 * 
	 * @return A representation of this feature as a string.
	 */
	@Override
	public String toString() {
		String retString = "Feature[ id=" + getID() + " , ";
		final FeatureType featType = getFeatureType();

		for (int i = 0, n = attributes.length; i < n; i++) {
			retString += (featType.getAttributeType(i).getName() + "=");
			retString += attributes[i];

			if ((i + 1) < n) {
				retString += " , ";
			}
		}

		return retString += " ]";
	}

	/**
	 * returns a unique code for this feature
	 * 
	 * @return A unique int
	 */
	@Override
	public int hashCode() {
		return featureId.hashCode() * schema.hashCode();
	}

	/**
	 * override of equals. Returns if the passed in object is equal to this.
	 * 
	 * @param obj
	 *            the Object to test for equality.
	 * 
	 * @return <code>true</code> if the object is equal, <code>false</code>
	 *         otherwise.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj == this) {
			return true;
		}

		if (!(obj instanceof Feature)) {
			return false;
		}

		final Feature feat = (Feature) obj;

		if (!feat.getFeatureType().equals(schema)) {
			return false;
		}

		// this check shouldn't exist, by contract,
		// all features should have an ID.
		if (featureId == null) {
			if (feat.getID() != null) {
				return false;
			}
		}

		if (!featureId.equals(feat.getID())) {
			return false;
		}

		for (int i = 0, ii = attributes.length; i < ii; i++) {
			final Object otherAtt = feat.getAttribute(i);

			if (attributes[i] == null) {
				if (otherAtt != null) {
					return false;
				}
			} else {
				if (!attributes[i].equals(otherAtt)) {
					if (attributes[i] instanceof Geometry
							&& otherAtt instanceof Geometry) {
						// we need to special case Geometry
						// as JTS is broken
						// Geometry.equals( Object ) and Geometry.equals(
						// Geometry )
						// are different
						// (We should fold this knowledge into AttributeType...)
						// 
						if (!((Geometry) attributes[i])
								.equals((Geometry) otherAtt)) {
							return false;
						}
					} else {
						return false;
					}
				}
			}
		}

		return true;
	}

	/**
	 * Gets the feature collection this feature is stored in.
	 * 
	 * @return the collection that is the parent of this feature.
	 */
	public FeatureCollection getParent() {
		return parent;
	}

	/**
	 * Sets the parent collection this feature is stored in, if it is not
	 * already set. If it is set then this method does nothing.
	 * 
	 * @param collection
	 *            the collection to be set as parent.
	 */
	public void setParent(final FeatureCollection collection) {
		if (parent == null) {
			parent = collection;
		}
	}

	public Feature toComplex() {
		try {
			return new ComplexWrapper(this);
		} catch (final IllegalAttributeException iae) {
			throw new RuntimeException("the impossible has happened: ", iae);
		}
	}

	/**
	 * This class will wrap a DefaultFeature (which is a {@link SimpleFeature}
	 * into a Feature with multiplicity - which is to say it will always return
	 * a List of its attributes when they are requested. These will always be
	 * singleton Lists, since the min/max of attributes in a DefaultFeature is
	 * 1. But this is important so that clients can deal with all Features in
	 * the same way - always expecting Lists.
	 * 
	 * @author Chris Holmes, Fulbright
	 */
	static final class ComplexWrapper extends SOSFeature {
		/**
		 * Private constructor to wrap the attributes in list. Could consider
		 * making this public, but for now it seems better to keep it private
		 * since we do no check to make sure tha attribute array isn't already
		 * complex - and thus if it was we would wrap it in Lists again.
		 * 
		 * @param fType
		 *            DOCUMENT ME!
		 * @param atts
		 *            DOCUMENT ME!
		 * @param fid
		 *            DOCUMENT ME!
		 * 
		 * @throws IllegalAttributeException
		 *             DOCUMENT ME!
		 */
		private ComplexWrapper(final SOSFeatureType fType, final Object[] atts,
				final String fid) throws IllegalAttributeException {
			super(fType, wrapInList(atts, fType.getAttributeCount()), fid);
		}

		public ComplexWrapper(final SOSFeatureType fType, final Object[] atts)
				throws IllegalAttributeException {
			this(fType, atts, null);
		}

		// This could be problematic, not sure if all SimpleFeatures will have
		// DefaultFeatureTypes.
		public ComplexWrapper(final SimpleFeature feature)
				throws IllegalAttributeException {
			this((SOSFeatureType) feature.getFeatureType(), feature
					.getAttributes(null), feature.getID());
		}

		/**
		 * Sets the attribute Object at the given index. As this is a complex
		 * feature - one with multiplicity - then all attributes passed in must
		 * be Lists. Since this is just a wrapped SimpleFeature, the List passed
		 * in will only ever be a singleton, but we must abide by the contract
		 * of a ComplexFeature.
		 * 
		 * @param index
		 *            The attribute to set.
		 * @param value
		 *            A Singleton List of the attribute to set.
		 * 
		 * @throws IllegalAttributeException
		 *             If the value is not a singleton List.
		 * 
		 * @task REVISIT: Make List explicit in Java 1.5
		 * @task REVISIT: We could consider accepting none list objects, and
		 *       justify it as part of the parse method - that is to say that
		 *       most calls to setAttribute will turn the Object into the proper
		 *       form, so why not here? For a true Complex Feature this has
		 *       implications of letting people blow away their multiple
		 *       attributes, but for here there are no such dangers. -ch
		 */
		@Override
		public void setAttribute(final int index, final Object value)
				throws IllegalAttributeException {
			checkList(value);

			final List valList = (List) value;
			final int listSize = valList.size();

			if (listSize == 0) {
				super.setAttribute(index, wrapInList(null));
			} else {
				final AttributeType type = super.getFeatureType()
						.getAttributeType(index);
				final Object val = valList.get(0);

				try {
					final Object parsed = type.parse(val);
					type.validate(parsed);
					setAttributeValue(index, wrapInList(parsed));
				} catch (final IllegalArgumentException iae) {
					throw new IllegalAttributeException(type, val, iae);
				}
			}
		}

		private void checkList(final Object value)
				throws IllegalAttributeException {
			if (value instanceof List) {
				final List valList = (List) value;
				final int listSize = valList.size();

				if (listSize > 1) {
					final String errMsg = "The attribute: " + valList
							+ " has more " + "attributes (" + listSize
							+ ") than is allowed by an "
							+ " attributeType in a Simple Feature (1)";
					throw new IllegalAttributeException(errMsg);
				}
			} else {
				final String errMsg = "All objects set in a ComplexFeature must be "
						+ "Lists, to account for multiplicity";
				throw new IllegalAttributeException(errMsg);
			}
		}

		/**
		 * Sets the attribute at the given xPath. Note that right now this just
		 * does the name, and will fail on anything other than the name.
		 * 
		 * @param xPath
		 *            The name of the attribute to Set.
		 * @param attribute
		 *            The value to set - must be a List, for this Complex
		 *            Feature.
		 * 
		 * @throws IllegalAttributeException
		 *             DOCUMENT ME!
		 * 
		 * @task TODO: Revisit xPath stuff - get it working or do external
		 *       implementation.
		 */
		@Override
		public void setAttribute(final String xPath, final Object attribute)
				throws IllegalAttributeException {
			final int idx = super.getFeatureType().find(xPath);

			if (idx < 0) {
				throw new IllegalAttributeException("No attribute named "
						+ xPath);
			}

			setAttribute(idx, attribute);
		}

		protected static List wrapInList(final Object attribute) {
			return java.util.Collections.singletonList(attribute);
		}

		protected static Object[] wrapInList(final Object[] attributes,
				final int defaultSize) {
			Object[] retArray = attributes;

			if (attributes == null) {
				retArray = new Object[defaultSize];
			} else {
				retArray = attributes;
			}

			for (int i = 0; i < attributes.length; i++) {
				retArray[i] = wrapInList(attributes[i]);
			}

			return retArray;
		}
	}

}

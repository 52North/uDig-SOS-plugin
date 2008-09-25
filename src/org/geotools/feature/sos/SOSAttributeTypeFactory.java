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

import org.geotools.feature.AttributeType;
import org.geotools.feature.DefaultAttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.type.FeatureAttributeType;
import org.geotools.feature.type.GeometricAttributeType;
import org.geotools.feature.type.NumericAttributeType;
import org.geotools.feature.type.TemporalAttributeType;
import org.geotools.feature.type.TextualAttributeType;
import org.geotools.filter.CompareFilter;
import org.geotools.filter.Expression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.FilterType;
import org.geotools.filter.IllegalFilterException;
import org.geotools.filter.LengthFunction;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author 52n
 * 
 */
public class SOSAttributeTypeFactory extends DefaultAttributeTypeFactory {
	private FilterFactory ff;

	/**
	 * 
	 */
	public SOSAttributeTypeFactory() {
		super();
	}

	private static SOSAttributeTypeFactory instance = null;

	/**
	 * Returns the default attribute factory for the system - constucting a new
	 * one if this is first time the method has been called.
	 * 
	 * @return the default instance of AttributeTypeFactory.
	 */
	public static SOSAttributeTypeFactory defaultInstance() {
		if (instance == null) {
			instance = newInstance();
		}

		return instance;
	}

	/**
	 * @param factory
	 */
	public SOSAttributeTypeFactory(final FilterFactory factory) {
		ff = factory;
	}

	/**
	 * Returns a new instance of the current AttributeTypeFactory. If no
	 * implementations are found then DefaultAttributeTypeFactory is returned.
	 * 
	 * @return A new instance of an AttributeTypeFactory.
	 */
	public static SOSAttributeTypeFactory newInstance() {
		return new SOSAttributeTypeFactory();
	}

	/**
	 * Implementation of AttributeType creation.
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected AttributeType createAttributeType(final String name,
			final Class clazz, final boolean isNillable, final int fieldLength,
			final Object defaultValue) {
		final Filter f = length(fieldLength, name);

		if (Number.class.isAssignableFrom(clazz)) {
			return new NumericAttributeType(name, clazz, isNillable, 1, 1,
					defaultValue, f);
		} else if (CharSequence.class.isAssignableFrom(clazz)) {
			return new TextualAttributeType(name, isNillable, 1, 1,
					defaultValue, f);
		} else if (java.util.Date.class.isAssignableFrom(clazz)) {
			return new TemporalAttributeType(name, isNillable, 1, 1,
					defaultValue, f);
		} else if (Geometry.class.isAssignableFrom(clazz)) {
			return new GeometricAttributeType(name, clazz, isNillable, 1, 1,
					defaultValue, null, f);
		}
		return new SOSAttributeType(name, clazz, isNillable, 1, 1,
				defaultValue, f);
	}

	/**
	 * Implementation of AttributeType creation.
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected AttributeType createAttributeType(final String name,
			final Class clazz, final boolean isNillable, final Filter filter,
			final Object defaultValue, final Object metadata) {

		final LengthFunction length = (LengthFunction) ff
				.createFunctionExpression("LengthFunction");
		length.setArgs(new Expression[] { this.ff
				.createAttributeExpression(name) });
		if (Number.class.isAssignableFrom(clazz)) {
			return new NumericAttributeType(name, clazz, isNillable, 1, 1,
					defaultValue, filter);
		} else if (CharSequence.class.isAssignableFrom(clazz)) {
			return new TextualAttributeType(name, isNillable, 1, 1,
					defaultValue, filter);
		} else if (java.util.Date.class.isAssignableFrom(clazz)) {
			return new TemporalAttributeType(name, isNillable, 1, 1,
					defaultValue, filter);
		} else if (Geometry.class.isAssignableFrom(clazz)) {
			if (metadata instanceof CoordinateReferenceSystem) {
				return new GeometricAttributeType(name, clazz, isNillable, 1,
						1, defaultValue, (CoordinateReferenceSystem) metadata,
						filter);
			} else {
				return new GeometricAttributeType(name, clazz, isNillable, 1,
						1, defaultValue, null, filter);
			}
		}
		return new SOSAttributeType(name, clazz, isNillable, 1, 1,
				defaultValue, filter);
	}

	/**
	 * Creates a new AttributeType with the given name, class and nillable
	 * values.
	 * 
	 * @param name
	 *            The name of the AttributeType to be created.
	 * @param clazz
	 *            The class that objects will validate against.
	 * @param isNillable
	 *            if nulls are allowed in the new type.
	 * 
	 * @return A new AttributeType of name, clazz and isNillable.
	 */
	@SuppressWarnings("unchecked")
	public static AttributeType newAttributeType(final String name,
			final Class clazz, final boolean isNillable, final int fieldLength,
			final Object defaultValue) {
		return defaultInstance().createAttributeType(name, clazz, isNillable,
				fieldLength, defaultValue);
	}

	/**
	 * Creates the DefaultAttributeType.Feature
	 * 
	 * @param name
	 *            The name of the AttributeType to create.
	 * @param type
	 *            To use for validation.
	 * @param isNillable
	 *            whether the AttributeType should allow nulls.
	 * 
	 * @return the newly created feature AttributeType.
	 */
	@Override
	protected AttributeType createAttributeType(final String name,
			final FeatureType type, final boolean isNillable) {

		return new FeatureAttributeType(name, type, isNillable, 1, 1);
	}

	/**
	 * Create an AttributeType with the given name, Class, nillability, and
	 * fieldLength meta-data. This method will itself call <code>
	 * createAttributeType(String,Class,boolean,int,Object) </code>
	 * with null as the default value. To use your own default value, use the
	 * above method, providing your default value.
	 * 
	 * @param name
	 *            The name of the AttributeType to create.
	 * @param clazz
	 *            the class of the AttributeType to create.
	 * @param isNillable
	 *            whether the AttributeType should allow nulls.
	 * 
	 * @return the newly created AttributeType
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected AttributeType createAttributeType(final String name,
			final Class clazz, final boolean isNillable, final int fieldLength) {

		return createAttributeType(name, clazz, isNillable, fieldLength, null);
	}

	@Override
	@SuppressWarnings("unchecked")
	protected AttributeType createAttributeType(final String name,
			final Class clazz, final boolean isNillable, final int fieldLength,
			final Object defaultValue, final Object metaData) {

		if (Geometry.class.isAssignableFrom(clazz)
				&& metaData instanceof CoordinateReferenceSystem) {
			final LengthFunction length = (LengthFunction) ff
					.createFunctionExpression("LengthFunction");
			length.setArgs(new Expression[] { ff
					.createAttributeExpression(name) });
			CompareFilter cf = null;
			try {
				cf = ff.createCompareFilter(FilterType.COMPARE_LESS_THAN_EQUAL);
				cf.addLeftValue(length);
				cf.addRightValue(ff.createLiteralExpression(fieldLength));
			} catch (final IllegalFilterException e) {
				// TODO something
			}
			final Filter f = cf == null ? Filter.ALL : cf;
			return new GeometricAttributeType(name, clazz, isNillable, 1, 1,
					defaultValue, (CoordinateReferenceSystem) metaData, f);
		}
		return createAttributeType(name, clazz, isNillable, fieldLength,
				defaultValue);
	}

	/**
	 * Creates a new AttributeType with the addition of MetaData.
	 * <p>
	 * Currently MetaData is used to supply the CoordinateSequence when making a
	 * GeometryAttributeType.
	 * </p>
	 * 
	 * @param name
	 * @param clazz
	 * @param isNillable
	 * @param fieldLength
	 * @param defaultValue
	 * @param metaData
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static AttributeType newAttributeType(final String name,
			final Class clazz, final boolean isNillable, final int fieldLength,
			final Object defaultValue, final Object metaData) {
		return defaultInstance().createAttributeType(name, clazz, isNillable,
				fieldLength, defaultValue, metaData);
	}
	@SuppressWarnings("unchecked")
	public static AttributeType newAttributeType(final String name,
			final Class clazz, final boolean isNillable,
			final Filter restriction, final Object defaultValue,
			final Object metaData) {
		return defaultInstance().createAttributeType(name, clazz, isNillable,
				restriction, defaultValue, metaData);
	}

	/**
	 * Creates a new AttributeType with the given name, class and nillable
	 * values.
	 * 
	 * @param name
	 *            The name of the AttributeType to be created.
	 * @param clazz
	 *            The class that objects will validate against.
	 * @param isNillable
	 *            if nulls are allowed in the new type.
	 * 
	 * @return A new AttributeType of name, clazz and isNillable.
	 */
	@SuppressWarnings("unchecked")
	public static AttributeType newAttributeType(final String name,
			final Class clazz, final boolean isNillable, final int fieldLength) {
		return defaultInstance().createAttributeType(name, clazz, isNillable,
				fieldLength);
	}

	/**
	 * Creates a new AttributeType with the given name, class and nillable
	 * values.
	 * 
	 * @param name
	 *            The name of the AttributeType to be created.
	 * @param clazz
	 *            The class that objects will validate against.
	 * @param isNillable
	 *            if nulls are allowed in the new type.
	 * 
	 * @return A new AttributeType of name, clazz and isNillable.
	 */
	@SuppressWarnings("unchecked")
	public static AttributeType newAttributeType(final String name,
			final Class clazz, final boolean isNillable) {
		return defaultInstance().createAttributeType(name, clazz, isNillable,
				Integer.MAX_VALUE);
	}

	/**
	 * Convenience method to just specify name and class. Nulls are allowed as
	 * attributes by default (isNillable = <code>true</code>).
	 * 
	 * @param name
	 *            The name of the AttributeType to be created.
	 * @param clazz
	 *            The class that objects will validate against.
	 * 
	 * @return A new AttributeType of name and clazz.
	 */
	@SuppressWarnings("unchecked")
	public static AttributeType newAttributeType(final String name,
			final Class clazz) {
		return newAttributeType(name, clazz, true);
	}

	/**
	 * Constucts a new AttributeType that accepts Features (specified by a
	 * FeatureType)
	 * 
	 * @param name
	 *            The name of the AttributeType to be created.
	 * @param type
	 *            the FeatureType that features will validate agist
	 * @param isNillable
	 *            true iff nulls are allowed.
	 * 
	 * @return A new AttributeType of name, type, and isNillable.
	 */
	public static AttributeType newAttributeType(final String name,
			final FeatureType type, final boolean isNillable) {
		return defaultInstance().createAttributeType(name, type, isNillable);
	}

	/**
	 * Constucts a new AttributeType that accepts Feature (specified by a
	 * FeatureType). Nulls are allowed as attributes by default (isNillable =
	 * <code>true</code>).
	 * 
	 * @param name
	 *            The name of the AttributeType to be created.
	 * @param type
	 *            the FeatureType that features will validate agist
	 * 
	 * @return A new AttributeType of name and type.
	 */
	public static AttributeType newAttributeType(final String name,
			final FeatureType type) {
		return newAttributeType(name, type, true);
	}

}

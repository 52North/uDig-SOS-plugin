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
package org.n52.udig.catalog.internal.sos.dataStore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.geotools.feature.AttributeType;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeBuilder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.sos.SOSAttributeTypeFactory;
import org.geotools.feature.sos.SOSFeatureCollections;
import org.geotools.feature.sos.SOSFeatureTypeBuilder;
import org.n52.oxf.feature.GenericObservationParser;
import org.n52.oxf.feature.MeasureType;
import org.n52.oxf.feature.OXFFeature;
import org.n52.oxf.feature.OXFFeatureCollection;
import org.n52.oxf.feature.OXFFeatureType;
import org.n52.oxf.feature.PhenomenonPropertyType;
import org.n52.oxf.feature.ScopedName;
import org.n52.oxf.feature.dataTypes.OXFMeasureType;
import org.n52.oxf.feature.dataTypes.OXFPhenomenonPropertyType;
import org.n52.oxf.feature.dataTypes.OXFScopedName;
import org.n52.oxf.util.LoggingHandler;
import org.opengis.feature.FeatureAttributeDescriptor;

/**
 * A FeatureConverter for OXFFeatures and FeatureTypes. This class converts
 * OXFFeatures and OXFFeatureTypes into {@link org.geotools.feature.Feature} and
 * 
 * @link {@link org.geotools.feature.FeatureType}
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class OXFFeatureConverter {

	static Map<OXFFeature, Feature> featureCache = new HashMap<OXFFeature, Feature>(
			50);
	// static Map<OXFFeatureType, FeatureType> featureTypeCache = new
	// HashMap<OXFFeatureType, FeatureType>(10);

	private static final Logger LOGGER = LoggingHandler
			.getLogger(OXFFeatureConverter.class);

	public static List<Feature> cloneList(List<Feature> lin){
		List<Feature> lout = new ArrayList<Feature>();
		for (Feature f : lin){
			lout.add(f);
		}
		return lout;
	}
	
	public static List<Feature> convert(final List<OXFFeature> inList) throws IllegalAttributeException{
		List<Feature> outList = new ArrayList<Feature>();
		
		for (OXFFeature f : inList){
			outList.add(convert(f));
		}
		return outList;		
	}
	
	/**
	 * Converts a given {@link OXFFeature} to an
	 * {@link org.geotools.feature.Feature} Uses a caching mechanism to avoid
	 * converting of the same Features over and over again.
	 * 
	 * @param oxffeature
	 *            the inputfeature
	 * @return the converted {@link Feature}
	 * @throws IllegalAttributeException
	 *             Input Feature is null or it`s FeatureType is null, most
	 *             likely the {@link GenericObservationParser} had problems with
	 *             the xml.
	 */
	public static Feature convert(final OXFFeature oxffeature)
			throws IllegalAttributeException {
		if (featureCache.containsKey(oxffeature)) {
//			LOGGER.debug("Feature already converted, using cached Version");
			return featureCache.get(oxffeature);
		}

		if (oxffeature == null || oxffeature.getFeatureType() == null) {
			throw new IllegalAttributeException(
					"Sorry, seems like the feature is damaged. Check your request and the resulting xml.");
		}

		if (oxffeature instanceof OXFFeatureCollection) {
			final FeatureCollection fcol = SOSFeatureCollections
					.newCollection();
			;

			final Iterator<OXFFeature> it = ((OXFFeatureCollection) oxffeature)
					.iterator();
			while (it.hasNext()) {
				fcol.add(convert(it.next()));
			}
			return fcol;
		} else {

			// if (oxffeature instanceof OXFFeatureCollection) {
			// ((OXFFeatureCollection)oxffeature).
			// }
			boolean isGeometrySet = false;
			// oxffeature.getSpecifiedAttributes()
			// Object attributes[] = new
			// Object[oxffeature.getSpecifiedAttributes().length];
			final Object attributes[] = new Object[oxffeature.getFeatureType()
					.getAttributeDescriptors().size()];

			// for (FeatureAttributeDescriptor
			// fad:oxffeature.getFeatureType().getAttributeDescriptors()) {
			// attributes[i] = oxffeature.getAttribute(fad.getName());
			// }

			Feature f = null;

			try {
				// udig does not allow different features with equalID
				// drop OXFdefaultID and use a unique id generated by udig
				if (oxffeature.getID() == null
						|| oxffeature.getID().equals("anyID")
						|| oxffeature.getID().equals("anyId")) {
					f = convert(oxffeature.getFeatureType()).create(attributes);
				} else {
					f = convert(oxffeature.getFeatureType()).create(attributes,
							oxffeature.getID());
				}

				// Geometry
				if (oxffeature.getGeometry() != null) {
					f.setDefaultGeometry(oxffeature.getGeometry());
					isGeometrySet = true;
				}

				for (int j = 0; j < attributes.length; j++) {
					final String name = f.getFeatureType().getAttributeType(j)
							.getName();

					// TODO try to find a useable geometry
					// at first check attributes with
					// com.vividsolutions.jts.geom.Geometry.class
					// check featureofinterest
					// XXX workaround: today i know where the geometries are
					// stored
					// f.setDefaultGeometry(((OXFFeature)oxffeature.getAttribute("featureOfInterest")).getGeometry());
					if (!isGeometrySet && name.equals("featureOfInterest")) {
						f.setDefaultGeometry(((OXFFeature) oxffeature
								.getAttribute("featureOfInterest"))
								.getGeometry());
						isGeometrySet = true;
					}
//					if (!isGeometrySet && name.equals("featureOfInterest")) {
//						f.setDefaultGeometry(((OXFFeature) oxffeature
//								.getAttribute("featureOfInterest"))
//								.getGeometry());
//						isGeometrySet = true;
//					}

					final AttributeType at = f.getFeatureType()
							.getAttributeType(j);
					final FeatureAttributeDescriptor atd = oxffeature
							.getFeatureType().getAttributeDescriptor(name);

					if (atd.getName().equalsIgnoreCase("result")) {
						if (atd.getObjectClass().isAssignableFrom(
								OXFMeasureType.class)) {
							final OXFMeasureType oxftmt = ((OXFMeasureType) oxffeature
									.getAttribute(name));
							f.setAttribute(j, new MeasureType(oxftmt));
						} else if (atd.getObjectClass().isAssignableFrom(
								OXFScopedName.class)) {
							final OXFScopedName oxfsn = ((OXFScopedName) oxffeature
									.getAttribute(name));
							f.setAttribute(j, new ScopedName(oxfsn));
						} else {
							// take care, this may lead to a validation error
							f.setAttribute(j, oxffeature.getAttribute(name));
						}

					} else if (atd.getName().equalsIgnoreCase("name")) {
						if (oxffeature.getAttribute(name) != null) {
							f.setAttribute(j, ((String[]) oxffeature
									.getAttribute(name))[0]);
						}
					} else {
						if (atd.getObjectClass().isAssignableFrom(
								OXFMeasureType.class)) {
							final OXFMeasureType oxftmt = ((OXFMeasureType) oxffeature
									.getAttribute(name));
							f.setAttribute(j, new MeasureType(oxftmt));

						} else if (atd.getObjectClass().isAssignableFrom(
								OXFScopedName.class)) {
							final OXFScopedName oxfsn = ((OXFScopedName) oxffeature
									.getAttribute(name));
							f.setAttribute(j, new ScopedName(oxfsn));
						} else if (at.getType().isAssignableFrom(Feature.class)) {
							f.setAttribute(j, convert((OXFFeature) oxffeature
									.getAttribute(name)));
						} else if (at.getType().isAssignableFrom(List.class)) {
							f.setAttribute(j, convert((List) oxffeature
									.getAttribute(name)));
						} else if (at.getType().isAssignableFrom(
								PhenomenonPropertyType.class)) {
							if (oxffeature.getFeatureType()
									.getAttributeDescriptor(name)
									.getObjectClass().isAssignableFrom(
											OXFPhenomenonPropertyType.class)) {
								final OXFPhenomenonPropertyType oxfppt = ((OXFPhenomenonPropertyType) oxffeature
										.getAttribute(name));
								f.setAttribute(j, new PhenomenonPropertyType(
										oxfppt));
							}
//						} else if (at.getType().isAssignableFrom(
//								IQuantity.class)) {
//							if (oxffeature.getFeatureType()
//									.getAttributeDescriptor(name)
//									.getObjectClass().isAssignableFrom(
//											OXFQuantityRange.class)) {
//								if (oxffeature.getAttribute(name) != null) {
//									final OXFQuantityRange oxfqr = ((OXFQuantityRange) oxffeature
//											.getAttribute(name));
//									f.setAttribute(j, new QuantityRange(oxfqr));
//								} else if (oxffeature.getAttribute(name) != null) {
//									final OXFQuantity oxfq = ((OXFQuantity) oxffeature
//											.getAttribute(name));
//									f.setAttribute(j, new Quantity(oxfq));
//								}
//							}
						} else if (at.getType().isAssignableFrom(
								com.vividsolutions.jts.geom.Geometry.class)) {
							if (oxffeature.getAttribute(name) != null) {
								f.setAttribute(j, (oxffeature.getAttribute(name)));
								// place where geometries could be stored
								if (!isGeometrySet) {
									f.setDefaultGeometry(((com.vividsolutions.jts.geom.Geometry) oxffeature.getAttribute(name)));
									isGeometrySet = true;
								}
							}

						} else if (at.getType()
								.isAssignableFrom(String[].class)) {
							f.setAttribute(j, oxffeature.getAttribute(name));
						} else if (at.getType().isAssignableFrom(String.class)) {
							f.setAttribute(j, oxffeature.getAttribute(name));
						} else {
							// take care, this may lead to a validation error
							f.setAttribute(j, oxffeature.getAttribute(name));
						}
					}
				}
			} catch (final IllegalAttributeException iae) {
				LOGGER.error(iae.getMessage());
				LOGGER.error("At least one feature is invalid. I`ll set this to null and try to proceed, however your data cannot be trusted anymore. Check your request and the resulting xml.");
				f = null;
				throw iae;
			} catch (final Exception e) {
				LOGGER.error("Something unexpected happened", e);
				f = null;
			}
			if (f != null) {
				featureCache.put(oxffeature, f);
			}
			return f;
		}
	}

	private static FeatureType convert(final OXFFeatureType oxfft,
			final FeatureTypeBuilder build) {
		if (oxfft != null) {
			// if (featureTypeCache.containsKey(oxfft)){
			// LOGGER.info("FeatureType already converted, using cached
			// version");
			// return featureTypeCache.get(oxfft);
			// }
			try {

				// build.setName(oxfft.getTypeName());

				final List<FeatureAttributeDescriptor> oxffads = oxfft
						.getAttributeDescriptors();

				for (final FeatureAttributeDescriptor oxffad : oxffads) {
					// LOGGER.debug("Received " +
					// oxffad.getObjectClass().toString()
					// + " from OXF");
					final boolean nillable = true;
					if (oxffad.getName().equalsIgnoreCase("name")) {
						build.addType(SOSAttributeTypeFactory.newAttributeType(
								oxffad.getName(), String.class, nillable));
					} else if (oxffad.getName().equalsIgnoreCase("result")) {
						build.addType(SOSAttributeTypeFactory.newAttributeType(
								oxffad.getName(), Object.class, nillable));
					} else {
						if (oxffad.getObjectClass().isAssignableFrom(
								OXFFeature.class)) {
							build.addType(SOSAttributeTypeFactory
									.newAttributeType(oxffad.getName(),
											Feature.class, nillable));
						} else if (oxffad.getObjectClass().isAssignableFrom(
								OXFPhenomenonPropertyType.class)) {
							build.addType(SOSAttributeTypeFactory
									.newAttributeType(oxffad.getName(),
											PhenomenonPropertyType.class,
											nillable));
//						} else if (oxffad.getObjectClass().isAssignableFrom(
//								OXFIQuantity.class)) {
//							build.addType(SOSAttributeTypeFactory
//									.newAttributeType(oxffad.getName(),
//											IQuantity.class, nillable));
						} else if (oxffad.getObjectClass().isAssignableFrom(
								List.class)) {
							build.addType(SOSAttributeTypeFactory
									.newAttributeType(oxffad.getName(),
											List.class, nillable));
						} else {
							// primitive, String, ITime should be assigned here
							build.addType(SOSAttributeTypeFactory
									.newAttributeType(oxffad.getName(), oxffad
											.getObjectClass(), nillable));
						}
					}
					// if (oxffad.getMinimumOccurrences()>0){
					// nillable = false;
					// }
					// a feature ii a feature ... this has to be a feature

					// if
					// (oxffad.getObjectClass().isAssignableFrom(OXFFeature.class))
					// {
					// build.addType(SOSAttributeTypeFactory.newAttributeType(
					// oxffad.getName(), Feature.class, nillable));
					// } else if (oxffad.getObjectClass().isAssignableFrom(
					// OXFMeasureType.class)) {
					// build.addType(SOSAttributeTypeFactory.newAttributeType(
					// oxffad.getName(), MeasureType.class, nillable));
					// } else if (oxffad.getObjectClass().isAssignableFrom(
					// OXFScopedName.class)) {
					// build.addType(SOSAttributeTypeFactory.newAttributeType(
					// oxffad.getName(), ScopedName.class, nillable));
					// } else if (oxffad.getObjectClass().isAssignableFrom(
					// OXFPhenomenonPropertyType.class)) {
					// build.addType(SOSAttributeTypeFactory.newAttributeType(
					// oxffad.getName(), PhenomenonPropertyType.class,
					// nillable));
					// }
					// else
					// if(oxffad.getObjectClass().isAssignableFrom(OXFScopedName.class))
					// {
					// build.addType(AttributeTypeFactory.newAttributeType(oxffad
					// .getName(), Measure.class, nillable));
					// }
					// else {
					// // primitive, String, ITime should be assigned here
					// build.addType(SOSAttributeTypeFactory
					// .newAttributeType(oxffad.getName(), oxffad
					// .getObjectClass(), nillable));
					// }
				}
				if (build.getFeatureType() != null) {
					// featureTypeCache.put(oxfft, build.getFeatureType());
				}
				return build.getFeatureType();
			} catch (final Exception e) {
				LOGGER.fatal(e);
			}
		}
		throw new IllegalArgumentException("Input null not allowed");

	}

	public static FeatureType convert(final OXFFeatureType oxfft)
			throws IllegalArgumentException {
		if (oxfft != null) {
			// if (featureTypeCache.containsKey(oxfft)){
			// LOGGER.info("FeatureType already converted, using cached
			// version");
			// return featureTypeCache.get(oxfft);
			// }
			final String typeName = oxfft.getTypeName();
			final FeatureTypeBuilder build = SOSFeatureTypeBuilder
					.newInstance(typeName);
			return convert(oxfft, build);
		}
		throw new IllegalArgumentException("Input null not allowed");
	}

	public static FeatureType convert(final OXFFeatureType oxfft,
			final String typeName) throws IllegalArgumentException {
		if (oxfft != null) {
			// if (featureTypeCache.containsKey(oxfft)){
			// LOGGER.info("FeatureType already converted, using cached
			// version");
			// return featureTypeCache.get(oxfft);
			// }
			FeatureTypeBuilder build;
			if (typeName == null) {
				build = SOSFeatureTypeBuilder.newInstance(oxfft.getTypeName());
			} else {
				build = SOSFeatureTypeBuilder.newInstance(typeName);
			}
			return convert(oxfft, build);
		}
		throw new IllegalArgumentException("Input null not allowed");
	}

	public static FeatureType createFeatureType(
			final OXFFeatureCollection oxffc, final String typename) {
		try {
			if (oxffc != null) {
				final OXFFeatureType oxfft = oxffc.toList().get(0)
						.getFeatureType();

				FeatureTypeBuilder build;
				if (typename == null) {
					build = SOSFeatureTypeBuilder.newInstance(oxfft
							.getTypeName());
				} else {
					build = SOSFeatureTypeBuilder.newInstance(typename);
				}
				convert(oxfft, build);

				final FeatureType ft = build.getFeatureType();

				return ft;
			} else {
				LOGGER.debug("FeatureType is null");
			}
		} catch (final Exception e) {
			LOGGER.error(e);
		}
		return null;
		// build.setDefaultGeometry(defaultGeometry)
	}
}
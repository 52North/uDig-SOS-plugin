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
package org.n52.udig.catalog.internal.sos.workarounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.geotools.data.DataStoreFactorySpi.Param;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.spatialschema.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class EastingFirstWorkaroundDesc implements IWorkaroundDescription {

	public final static String identifier = "EPSG4326: Easting First";

	public static final Param[] parameters  = {};
	
	public String getDescription() {
		return "Some SOS-implementations change the order of x,y in EPSG4326 CRS. Enable this workaround to change the order.";
	}

	public String getIdentifier() {
		return identifier;
	}

	public Param[] getParameters(){
		return parameters;
	}

	private static List<Geometry> alreadyConverted = Collections.synchronizedList(new ArrayList<Geometry>());
	
	
//	public static void workaround(ReferencedEnvelope env){
//		if (!alreadyConverted.contains(env)){
//			env = new ReferencedEnvelope(
//					env.getMinY(), env.getMaxY(),
//					env.getMinX(), env.getMaxX(), env.getCoordinateReferenceSystem());
//			alreadyConverted.add(env);
//		}
//	}
	
//	public Feature workaround(Feature f2){
//	if (alreadyConverted.contains(f2)){
//		return f2;
//	} else{
//		Object location = f2.getAttribute("location");
//		if (alreadyConverted.contains(location)){
//			return f2;
//		} else{
//
//		if (location instanceof Point) {
//					Point new_name = (Point) location;
//					workaround(new_name.getCoordinates());
//					new_name.geometryChanged();
//					alreadyConverted.add(location);
//					alreadyConverted.add(f2);
//					return f2;
//				}
//			}}
//	return null;
//}
	
	public static void workaround(Feature f2){
		Object location = f2.getAttribute("location");

		if (location instanceof Point) {
					workaround((Point)location);
		}
}


	private static void workaround(Coordinate c){
		double x = c.x;
		double y = c.y;

		c.x = y;
		c.y = x;
	}
	
	private static void workaround(Coordinate[] cs){
		for (Coordinate c: cs){
			workaround(c);
		}
	}
	
	public static void workaround(Geometry g){
		if (!alreadyConverted.contains(g)){
			workaround(g.getCoordinates());
			g.geometryChanged();
			alreadyConverted.add(g);
		}
	}

	public Object getDefaultValue(Param p) {
		// TODO Auto-generated method stub
		return null;
	}
}
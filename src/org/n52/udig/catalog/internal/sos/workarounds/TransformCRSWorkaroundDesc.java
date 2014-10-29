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

import java.util.HashMap;

import org.geotools.data.DataStoreFactorySpi.Param;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class TransformCRSWorkaroundDesc implements IWorkaroundDescription {

	// TODO this workaround needs additional parameters like the targetCRS -> disable booleanType;
	
	public final static String identifier = "TransformCRS";
	
	private final static String defaultValue = "EPSG:4326";
	
private HashMap<Param, Object> defaultValues = new HashMap<Param, Object>();
	
	public TransformCRSWorkaroundDesc() {
		defaultValues.put(PARAM_ENV, defaultValue);
	}
	
	public Object getDefaultValue(Param p){
		return defaultValues.get(p);
	}
	
	public final static Param PARAM_ENV = new Param(
			"WORKAROUND:"+identifier+":x1,x2,y1,y2", String.class,
			"Defines the CRS that should be used. Format: 'EPSG:xxxx' . Default value: "+defaultValue,
			true);
	
	public Param[] getParameters() {
		return parameters;
	}
	 
	public static final Param[] parameters  = { 
		PARAM_ENV
	};

	public String getDescription() {
		return "UDig does not like different CRS at the same time. If a SOS uses different CRS in a reply, this workaround converts the data before forwarding it to UDIG";
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public static ReferencedEnvelope workaround(ReferencedEnvelope env, CoordinateReferenceSystem targetCRS) throws TransformException, FactoryException{
		if (targetCRS != null)		{
			return env.transform(targetCRS, true);
		} else{
			return env.transform(CRS.decode(defaultValue), true);
		}
		
	}
	
//	public ReferencedEnvelope workaround(ReferencedEnvelope env) throws TransformException, FactoryException{
//		return env.transform(CRS.decode(defaultValue), true);
//	}

}

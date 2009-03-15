package org.n52.udig.catalog.internal.sos.workarounds;

import org.geotools.data.DataStoreFactorySpi.Param;
import org.geotools.geometry.jts.ReferencedEnvelope;

public class FalseBoundingBoxWorkaroundDesc implements IWorkaroundDescription {

	// TODO this workaround needs additional parameters like the targetCRS -> disable booleanType;
	
	public final static String identifier = "FalseBoundingBox";
	
private final static String defaultValue = "-89,89,-89,89";
	
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
		return "Some SOS implementations return FOIs not included in the offering`s BoundingBoxes. Use this workaround to expand the BoundingBox.";
	}

	public String getIdentifier() {
		return identifier;
	}

	public static ReferencedEnvelope workaround(ReferencedEnvelope env){
		return new ReferencedEnvelope(-89,89,-89,89,
				env.getCoordinateReferenceSystem());
	}

	public Object getDefaultValue(Param p) {
		if (p.equals(PARAM_ENV)){
			return defaultValue; 
		}
		return null;
	}
}

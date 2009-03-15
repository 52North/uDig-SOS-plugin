package org.n52.udig.catalog.internal.sos.workarounds;

import org.geotools.data.DataStoreFactorySpi.Param;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class NoCRSWorkaroundDesc implements IWorkaroundDescription {

	// TODO this workaround needs additional parameters like the targetCRS -> disable booleanType;
	public final static String identifier = "NoCRSDelivered";
	
	private final static String defaultCRS = "EPSG:4326";
	
	public final static Param PARAM_CRS = new Param(
			"WORKAROUND:"+identifier+":CRS", String.class,
			"Defines the CRS that should be used. Format: 'EPSG:xxxx' . Default value: "+defaultCRS,
			true);
	
	public static final Param[] parameters  = { 
		PARAM_CRS};
	
	public String getDescription() {
		return "False SOS implementations do not advertise their CRS. This workaround allows the user to set a default";
	}

	public String getIdentifier() {
		return identifier;
	}

	public String workaround(String crs) throws TransformException, FactoryException{
		return defaultCRS;
	}

	public Param[] getParameters() {
		return parameters;
	}

	public Object getDefaultValue(Param p) {
		if (p.equals(PARAM_CRS)){
			return defaultCRS; 
		}
		return null;
	}
}
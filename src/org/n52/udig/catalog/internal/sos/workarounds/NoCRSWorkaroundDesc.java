package org.n52.udig.catalog.internal.sos.workarounds;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

public class NoCRSWorkaroundDesc implements IWorkaroundDescription {

	// TODO this workaround needs additional parameters like the targetCRS -> disable booleanType;
	
	public final static String identifier = "NoCRSDelivered";
	
	public boolean booleanType() {
		return true;
	}

	public String getDescription() {
		return "False SOS implementations do not advertise their CRS. This workaround allows the user to set a default";
	}

	public String getIdentifier() {
		return identifier;
	}

	public String workaround(String crs) throws TransformException, FactoryException{
		return "EPSG:4326";
	}
}

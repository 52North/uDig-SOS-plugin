package org.n52.udig.catalog.internal.sos.workarounds;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class TransformCRSWorkaroundDesc implements IWorkaroundDescription {

	// TODO this workaround needs additional parameters like the targetCRS -> disable booleanType;
	
	public final static String identifier = "TransformCRS";
	
	public boolean booleanType() {
		return true;
	}

	public String getDescription() {
		return "UDig does not like different CRS at the same time. If a SOS uses different CRS in a reply, this workaround converts the data before forwarding it to UDIG";
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public ReferencedEnvelope workaround(ReferencedEnvelope env, CoordinateReferenceSystem targetCRS) throws TransformException, FactoryException{
		return env.transform(targetCRS, true);
	}

}

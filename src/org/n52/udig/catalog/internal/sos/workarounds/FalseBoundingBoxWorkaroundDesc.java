package org.n52.udig.catalog.internal.sos.workarounds;

import org.geotools.geometry.jts.ReferencedEnvelope;

public class FalseBoundingBoxWorkaroundDesc implements IWorkaroundDescription {

	// TODO this workaround needs additional parameters like the targetCRS -> disable booleanType;
	
	public final static String identifier = "FalseBoundingBox";
	
	public boolean booleanType() {
		return true;
	}

	public String getDescription() {
		return "Some SOS implementations return FOIs not included in the offering`s BoundingBoxes. Use this workaround to expand the BoundingBox.";
	}

	public String getIdentifier() {
		return identifier;
	}

	public ReferencedEnvelope workaround(ReferencedEnvelope env){
		return new ReferencedEnvelope(-89,89,-89,89,
				env.getCoordinateReferenceSystem());
	}
}

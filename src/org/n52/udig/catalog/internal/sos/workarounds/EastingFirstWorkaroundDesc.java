package org.n52.udig.catalog.internal.sos.workarounds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.spatialschema.geometry.DirectPosition;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class EastingFirstWorkaroundDesc implements IWorkaroundDescription {

	public final static String identifier = "EPSG4326: Easting First"; 
	
	public String getDescription() {
		return "Some SOS-implementations change the order of x,y in EPSG4326 CRS. Enable this workaround to change the order.";
	}

	public String getIdentifier() {
		return identifier;
	}
	
	public boolean booleanType(){
		return true;
	}
	
	private List<Object> alreadyConverted = Collections.synchronizedList(new ArrayList<Object>());
	
	
	public ReferencedEnvelope workaround(ReferencedEnvelope env){
		if (alreadyConverted.contains(env)){
			return env;
		} else{
			alreadyConverted.add(env);
			return new ReferencedEnvelope(
					env.getMinY(), env.getMaxY(),
					env.getMinX(), env.getMaxX(), env.getCoordinateReferenceSystem());
		}
	}
	
	public Feature workaround(Feature f2){
		if (alreadyConverted.contains(f2)){
			return f2;
		} else{
			Object location = f2.getAttribute("location");
			if (alreadyConverted.contains(location)){
				return f2;
			} else{

			if (location instanceof Point) {
						Point new_name = (Point) location;
						workaround(new_name.getCoordinates());
						new_name.geometryChanged();
						alreadyConverted.add(location);
						alreadyConverted.add(f2);
						return f2;
					}
				}}
		return null;
	}
	
	
	
	private void workaround(Coordinate c){
		double x = c.x;
		double y = c.y;

		c.x = y;
		c.y = x;
	}
	
	private void workaround(Coordinate[] cs){
		for (Coordinate c: cs){
			workaround(c);
		}
	}
	
	public Geometry workaround(Geometry g){
		if (alreadyConverted.contains(g)){
			return g;
		} else{
			workaround(g.getCoordinates());
			g.geometryChanged();
			alreadyConverted.add(g);
			return g;
		}
	}
}
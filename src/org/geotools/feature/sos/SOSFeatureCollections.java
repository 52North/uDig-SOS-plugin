/**
 * 
 */
package org.geotools.feature.sos;

import org.geotools.feature.DefaultFeatureCollections;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;

/**
 * @author 52n
 * 
 */
public class SOSFeatureCollections extends DefaultFeatureCollections {

	/**
	 * Creates a new instance of DefaultFeatureCollections
	 */
	public SOSFeatureCollections() {
		super();
	}

	/**
	 * create a new FeatureCollection using the current default factory.
	 * 
	 * @return A FeatureCollection instance.
	 */
	public static FeatureCollection newCollection() {
		return new SOSFeatureCollections().createCollection();
	}

	/**
	 * Creates a new DefaultFeatureCollection.
	 * 
	 * @return A new, empty DefaultFeatureCollection.
	 */
	@Override
	protected FeatureCollection createCollection() {
		return new SOSFeatureCollection(null, null);
	}

	@Override
	protected FeatureCollection createCollection(final String id,
			final FeatureType ft) {
		return new SOSFeatureCollection(id, ft);
	}
}

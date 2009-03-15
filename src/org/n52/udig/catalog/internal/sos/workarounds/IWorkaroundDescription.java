package org.n52.udig.catalog.internal.sos.workarounds;

import org.geotools.data.DataStoreFactorySpi.Param;

public interface IWorkaroundDescription {

	public String getIdentifier();

	public String getDescription();
	
	public Param[] getParameters();
	
	public Object getDefaultValue(Param p);
	
}

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
package org.n52.udig.catalog.internal.sos;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import net.refractions.udig.catalog.IGeoResource;
import net.refractions.udig.catalog.IGeoResourceInfo;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.ui.graphics.Glyph;

import org.eclipse.core.runtime.IProgressMonitor;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureStore;
import org.geotools.feature.FeatureType;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.n52.udig.catalog.internal.sos.dataStore.SOSCapabilities;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory.UDIGSOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.workarounds.EastingFirstWorkaroundDesc;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSGeoResourceImpl extends IGeoResource {

	private URL identifier;
	private IGeoResourceInfo info;
	private SOSServiceImpl parent;
	private String typename;

	private SOSGeoResourceImpl() {
		// empty and never used
	}

	// public SOSGeoResourceImpl(SOSServiceImpl parent, Layer layer ) {
	// this.parent = parent;
	// this.layer = layer;
	// try {
	// identifier=new URL(parent.getIdentifier().toString() + "#" +
	// layer.getName()); //$NON-NLS-1$
	//
	// } catch (Throwable e) {
	// SOSPlugin.log( null, e);
	// identifier = parent.getIdentifier();
	// }
	// }

	public SOSGeoResourceImpl(final SOSServiceImpl parent, final String typename) {
		this.parent = parent;
		this.typename = typename;
		try {
			identifier = new URL(parent.getIdentifier().toString()
					+ "#" + typename); //$NON-NLS-1$
		} catch (final MalformedURLException e) {
			identifier = parent.getIdentifier();
		}
	}

	/*
	 * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
	 */
	@Override
	public <T> boolean canResolve(final Class<T> adaptee) {
		if (adaptee == null) {
			return false;
		}
		return (adaptee.isAssignableFrom(IGeoResourceInfo.class)
				|| adaptee.isAssignableFrom(FeatureStore.class)
				|| adaptee.isAssignableFrom(FeatureSource.class)
				|| adaptee.isAssignableFrom(SOSDataStore.class)
				|| adaptee.isAssignableFrom(UDIGSOSDataStore.class) || adaptee
				.isAssignableFrom(IService.class))
				|| super.canResolve(adaptee);
	}

	/*
	 * Required adaptions: <ul> <li>IGeoResourceInfo.class <li>IService.class
	 * </ul>
	 * 
	 * @see net.refractions.udig.catalog.IResolve#resolve(java.lang.Class,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public <T> T resolve(final Class<T> adaptee, final IProgressMonitor monitor)
			throws IOException {
		if (adaptee == null) {
			return null;
		}
		// if(adaptee.isAssignableFrom(IService.class))
		// return adaptee.cast( parent );
		if (adaptee.isAssignableFrom(SOSDataStore.class)) {
			return parent.resolve(adaptee, monitor);
		}
		if (adaptee.isAssignableFrom(UDIGSOSDataStore.class)) {
			return parent.resolve(adaptee, monitor);
		}
		if (adaptee.isAssignableFrom(IGeoResource.class)) {
			return adaptee.cast(this);
		}
		if (adaptee.isAssignableFrom(IGeoResourceInfo.class)) {
			return adaptee.cast(getInfo(monitor));
		}
		if (adaptee.isAssignableFrom(FeatureStore.class)) {
			final FeatureSource fs = parent.getDS(monitor).getFeatureSource(
					typename);
			if (fs instanceof FeatureStore) {
				return adaptee.cast(fs);
			}
			if (adaptee.isAssignableFrom(FeatureSource.class)) {
				return adaptee.cast(parent.getDS(monitor).getFeatureSource(
						typename));
			}
		}
		return super.resolve(adaptee, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.refractions.udig.catalog.IGeoResource#getIdentifier()
	 */
	@Override
	public URL getIdentifier() {
		return identifier;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.refractions.udig.catalog.IGeoResource#getInfo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IGeoResourceInfo getInfo(final IProgressMonitor monitor)
			throws IOException {
		if (info == null) {
			parent.rLock.lock();
			try {
				if (info == null) {
					// info = new SOSResourceInfo( monitor );
					info = new SOSResourceInfo();
				}
			} finally {
				parent.rLock.unlock();
			}
		}
		return info;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.refractions.udig.catalog.IGeoResource#service(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IService service(final IProgressMonitor monitor) throws IOException {
		return parent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.refractions.udig.catalog.IResolve#getMessage()
	 */
	public Throwable getMessage() {
		return parent.getMessage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.refractions.udig.catalog.IResolve#getStatus()
	 */
	public Status getStatus() {
		return parent.getStatus();
	}

	class SOSResourceInfo extends IGeoResourceInfo {
		// CoordinateReferenceSystem crs = null;
		SOSCapabilities caps;
		UDIGSOSDataStore ds;

		SOSResourceInfo() throws IOException {
			ds = parent.getDS(null);
			caps = ds.getCapabilities();

			final FeatureType ft = parent.getDS(null).getSchema(typename);
			if (ft == null) {
				throw new IOException(
						"FeatureType of observation is null. This is most likely a server error. Check your logs for additional information.");
			}

			// title = caps.get
			keywords = caps.getService().getKeywordList();
			// description = caps.get

			bounds = caps.getBoundingBox((String) parent.getParams().get(
					SOSDataStoreFactory.OPERATION.key), typename);
			if (bounds == null) {
				Geometry g = ds.getBoundingBox(typename);
				if (g != null) {
					try {
						if (SOSConfigurationRegistry.getInstance().getWorkaroundState(caps.getServiceURL().toExternalForm(), EastingFirstWorkaroundDesc.identifier)){
//							EastingFirstWorkaroundDesc eastingFirstWorkaroundDesc = (EastingFirstWorkaroundDesc)GeneralConfigurationRegistry.getInstance().getWorkarounds().get(EastingFirstWorkaroundDesc.identifier);
							// OXFSamplingPointType umgedreht
							EastingFirstWorkaroundDesc .workaround(g); 
						}
						
						// TODO change this to g.getSRID
						final String srid = "EPSG:" + 4326;
						bounds = new ReferencedEnvelope(
								new com.vividsolutions.jts.geom.Envelope(g
										.getCoordinate()), CRS.decode(srid));
						// WORKAROUND
						bounds.expandToInclude(new Coordinate(
								bounds.getMaxX() + 0.02,
								bounds.getMaxY() + 0.02));
						bounds.expandToInclude(new Coordinate(
								bounds.getMinX() - 0.02,
								bounds.getMinY() - 0.02));

						// g.getEnvelope();
					} catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}
			// crs = bounds.getCoordinateReferenceSystem();
			name = typename;
			schema = ft.getNamespace();
			icon = Glyph.icon(ft);
		}

		// /*
		// * @see net.refractions.udig.catalog.IGeoResourceInfo#getCRS()
		// */
		// public CoordinateReferenceSystem getCRS() {
		// if(crs != null)
		// return crs;
		// return super.getCRS();
		// }
	}
}

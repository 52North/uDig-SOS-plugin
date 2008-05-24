/***************************************************************
Copyright © 2008 52°North Initiative for Geospatial Open Source Software GmbH

 Author: Carsten Priess, 52°N

 Contact: Andreas Wytzisk,
 52°North Initiative for Geospatial Open Source SoftwareGmbH,
 Martin-Luther-King-Weg 24,
 48155 Muenster, Germany,
 info@52north.org

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 version 2 as published by the Free Software Foundation.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; even without the implied WARRANTY OF
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program (see gnu-gpl v2.txt). If not, write to
 the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 Boston, MA 02111-1307, USA or visit the Free
 Software Foundation’s web page, http://www.fsf.org.

 ***************************************************************/
package org.n52.udig.catalog.internal.sos.ui;

import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ui.UDIGConnectionFactory;

import org.n52.udig.catalog.internal.sos.SOSGeoResourceImpl;
import org.n52.udig.catalog.internal.sos.SOSServiceExtension;
import org.n52.udig.catalog.internal.sos.SOSServiceImpl;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;

/**
 * @author 52n
 *
 */
public class SOSConnectionFactory extends UDIGConnectionFactory {

	/*
	 * (non-Javadoc)
	 *
	 * @see net.refractions.udig.catalog.ui.UDIGConnectionFactory#canProcess(java.lang.Object)
	 */
	@Override
	public boolean canProcess(final Object context) {
		if (context instanceof IResolve) {
			final IResolve resolve = (IResolve) context;
			return resolve.canResolve(SOSDataStore.class);
		}
		return toCapabilitiesURL(context) != null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.refractions.udig.catalog.ui.UDIGConnectionFactory#createConnectionParameters(java.lang.Object)
	 */
	@Override
	public Map<String, Serializable> createConnectionParameters(final Object context) {
		if (context == null) {
			return null;
		}
		if (context instanceof SOSServiceImpl) {
			final SOSServiceImpl sos = (SOSServiceImpl) context;
			return sos.getConnectionParams();
		}
		URL url = toCapabilitiesURL(context);
		if (url == null) {
			// so we are not sure it is a wms url
			// lets guess
			url = CatalogPlugin.locateURL(context);
		}
		if (url != null) {
			// well we have a url - lets try it!
			final List<IResolve> list = CatalogPlugin.getDefault().getLocalCatalog()
					.find(url, null);
			for (final IResolve resolve : list) {
				if (resolve instanceof SOSServiceImpl) {
					// got a hit!
					final SOSServiceImpl sos = (SOSServiceImpl) resolve;
					return sos.getConnectionParams();
				} else if (resolve instanceof SOSGeoResourceImpl) {
					final SOSGeoResourceImpl layer = (SOSGeoResourceImpl) resolve;
					SOSServiceImpl sos;
					try {
						sos = (SOSServiceImpl) layer.parent(null);
						return sos.getConnectionParams();
					} catch (final IOException e) {
						checkedURL(layer.getIdentifier());
					}
				}
			}
			return createParams(url);
		}
		return Collections.emptyMap();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.refractions.udig.catalog.ui.UDIGConnectionFactory#createConnectionURL(java.lang.Object)
	 */
	@Override
	public URL createConnectionURL(final Object context) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Convert "data" to a wfs capabilities url
	 * <p>
	 * Candidates for conversion are:
	 * <ul>
	 * <li>URL - from browser DnD
	 * <li>URL#layer - from browser DnD
	 * <li>WFSService - from catalog DnD
	 * <li>IService - from search DnD
	 * </ul>
	 * </p>
	 * <p>
	 * No external processing should be required here, it is enough to guess and
	 * let the ServiceFactory try a real connect.
	 * </p>
	 *
	 * @param data
	 *            IService, URL, or something else
	 * @return URL considered a possibility for a WFS Capabilities, or null
	 */
	URL toCapabilitiesURL(final Object data) {
		if (data instanceof IResolve) {
			return toCapabilitiesURL((IResolve) data);
		} else if (data instanceof URL) {
			return toCapabilitiesURL((URL) data);
		} else if (CatalogPlugin.locateURL(data) != null) {
			return toCapabilitiesURL(CatalogPlugin.locateURL(data));
		} else {
			return null; // no idea what this should be
		}
	}

	protected URL toCapabilitiesURL(final IResolve resolve) {
		if (resolve instanceof IService) {
			return toCapabilitiesURL((IService) resolve);
		}
		return toCapabilitiesURL(resolve.getIdentifier());
	}

	protected URL toCapabilitiesURL(final IService resolve) {
		if (resolve instanceof SOSServiceImpl) {
			return toCapabilitiesURL((SOSServiceImpl) resolve);
		}
		return toCapabilitiesURL(resolve.getIdentifier());
	}

	protected URL toCapabilitiesURL(final SOSServiceImpl wfs) {
		return wfs.getIdentifier();
	}

	protected URL toCapabilitiesURL(final URL url) {
		// TODO SOSServiceExtension#isSOS does something similar; check if it
		// may fit together
		if (url == null) {
			return null;
		}

		final String path = url.getPath();
		final String query = url.getQuery();
		final String protocol = (url.getProtocol() != null) ? url.getProtocol()
				.toLowerCase() : null;

		if (!"http".equals(protocol) && !"https".equals(protocol)) { //$NON-NLS-1$ //$NON-NLS-2$
			return null;

		}
		if (query != null && query.indexOf("service=SOS") != -1) { //$NON-NLS-1$
			return checkedURL(url);
		}
		if (url.toExternalForm().indexOf("SOS") != -1) { //$NON-NLS-1$
			return checkedURL(url);
		}
		return null;
	}

	/** Check that any trailing #layer is removed from the url */
	static public URL checkedURL(final URL url) {
		final String check = url.toExternalForm();
		final int hash = check.indexOf('#');
		if (hash == -1) {
			return url;
		}
		try {
			return new URL(check.substring(0, hash));
		} catch (final MalformedURLException e) {
			return null;
		}
	}

	/** 'Create' params given the provided url, no magic occurs */
	protected Map<String, Serializable> createParams(final URL url) {
		final SOSServiceExtension factory = new SOSServiceExtension();
		final Map<String, Serializable> params = factory.createParams(url);
		if (params != null) {
			return params;
		}

		final Map<String, Serializable> params2 = new HashMap<String, Serializable>();
		params2.put(SOSDataStoreFactory.URL_CAPS.key, url);
		return params2;
	}

}

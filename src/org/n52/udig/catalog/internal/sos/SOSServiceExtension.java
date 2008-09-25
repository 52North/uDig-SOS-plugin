/**********************************************************************************
Copyright (C) 2008
by 52 North Initiative for Geospatial Open Source Software GmbH

Contact: Andreas Wytzisk
52 North Initiative for Geospatial Open Source Software GmbH
Martin-Luther-King-Weg 24
48155 Muenster, Germany
info@52north.org

This program is free software; you can redistribute and/or modify it under
the terms of the GNU General Public License version 2 as published by the
Free Software Foundation.

This program is distributed WITHOUT ANY WARRANTY; even without the implied
WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program (see gnu-gpl v2.txt). If not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
visit the Free Software Foundation web page, http://www.fsf.org.

Author: Carsten Priess
Created: 15.01.2008
 *********************************************************************************/
package org.n52.udig.catalog.internal.sos;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.refractions.udig.catalog.AbstractDataStoreServiceExtension;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.ServiceExtension;

import org.apache.log4j.Logger;
import org.geotools.data.DataStoreFactorySpi;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.SOSCapabilities;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;

/**
 * @author 52n
 * 
 */
public class SOSServiceExtension extends AbstractDataStoreServiceExtension
		implements ServiceExtension {

	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSServiceExtension.class);

	private static String reasonForFailure = null;

	public Map<String, Serializable> createParams(final URL url) {
		// check if url leads to a SOS
		if (!isSOS(url)) {
			return null;
		}

		final Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put(SOSDataStoreFactory.URL_SERVICE.key, url);
		// params.put(SOSDataStoreFactory.PORT.key, url.getPort());
		// params.put(SOSDataStoreFactory.OPERATION.key, );
		try {
			params.put(SOSDataStoreFactory.URL_SERVICE.key, new URL(url
					.toExternalForm().substring(0,
							url.toExternalForm().indexOf('?'))));
		} catch (final MalformedURLException e) {
			LOGGER.info(e);
			reasonForFailure = e.getMessage();
		}

		// LOGGER.info("Generated parameters: \n " +
		// "URL_CAPS: "+params.get(SOSDataStoreFactory.URL_CAPS.key)+"\n"+
		// "PORT: "+params.get(SOSDataStoreFactory.PORT.key)+"\n"+
		// "URL_SERVICE: "+params.get(SOSDataStoreFactory.URL_SERVICE.key));
		//
		// params.put(WFSDataStoreFactory.BUFFER_SIZE.key, 100);
		// params.put(WFSDataStoreFactory.LENIENT.key, true);
		// params.put(WFSDataStoreFactory.TRY_GZIP.key, true);

		// don't check ... it blocks
		// (XXX: but we are using that to figure out if the service will work?)
		return params;
	}

	@Override
	public String reasonForFailure(final URL url) {
		// return processURL(url);
		return reasonForFailure;
	}

	// private static String processURL( URL url ) {
	// if (url == null) {
	// return Messages.WMSServiceExtension_nullURL;
	// }
	//
	// String PATH = url.getPath();
	// String QUERY = url.getQuery();
	// String PROTOCOL = url.getProtocol();
	// if (PROTOCOL==null || PROTOCOL.indexOf("http") == -1) { //$NON-NLS-1$
	// supports 'https' too.
	// return Messages.WMSServiceExtension_protocol + "'"+PROTOCOL+"'";
	// //$NON-NLS-1$ //$NON-NLS-2$
	// }
	// if( QUERY != null && QUERY.toUpperCase().indexOf( "SERVICE=" ) != -1){
	// //$NON-NLS-1$
	// int indexOf = QUERY.toUpperCase().indexOf( "SERVICE=" ); //$NON-NLS-1$
	// // we have a service! it better be wfs
	// if( QUERY.toUpperCase().indexOf( "SERVICE=WMS") == -1 ){ //$NON-NLS-1$
	// int endOfExp = QUERY.indexOf('&', indexOf);
	// if( endOfExp == -1 )
	// endOfExp=QUERY.length();
	// if( endOfExp>indexOf+8)
	// return Messages.WMSServiceExtension_badService+QUERY.substring(indexOf+8,
	// endOfExp );
	// else{
	// return Messages.WMSServiceExtension_badService+""; //$NON-NLS-1$
	// }
	// }
	// } else if (PATH != null && PATH.toUpperCase().indexOf("GEOSERVER/WMS") !=
	// -1) { //$NON-NLS-1$
	// return null;
	// }
	// return null; // try it anyway
	// }

	// public String reasonForFailure( URL url ) {
	// String result=processURL(url);
	// if( result!=null )
	// return result;
	// // String rea = reasonForFailure(createParams(url));
	// return reasonForFailure(createParams(url));
	// }

	// private static String processURL(final URL url) {
	// if (url == null) {
	// return Messages.SOSServiceExtension_nullURL;
	// }
	// final String PATH = url.getPath();
	// final String QUERY = url.getQuery();
	// final String PROTOCOL = url.getProtocol();
	//
	// if (PROTOCOL.indexOf("http") == -1) { //$NON-NLS-1$
	// return Messages.SOSServiceExtension_protocolfailure
	// + "'" + PROTOCOL + "'"; //$NON-NLS-1$//$NON-NLS-2$
	// }
	// if (QUERY != null && QUERY.toUpperCase().indexOf("SERVICE=") != -1) {
	// //$NON-NLS-1$
	// final int indexOf = QUERY.toUpperCase().indexOf("SERVICE=");
	// //$NON-NLS-1$
	// // we have a service! it better be sos
	// if (QUERY.toUpperCase().indexOf("SERVICE=SOS") == -1) { //$NON-NLS-1$
	// int endOfExp = QUERY.indexOf('&', indexOf);
	// if (endOfExp == -1) {
	// endOfExp = QUERY.length();
	// }
	// if (endOfExp > indexOf + 8) {
	// return Messages.SOSServiceExtension_badService
	// + QUERY.substring(indexOf + 8, endOfExp);
	// } else {
	// return Messages.SOSServiceExtension_badService + ""; //$NON-NLS-1$
	// }
	// }
	// } else if (PATH != null
	// && PATH.toUpperCase().indexOf("GEOSERVER/SOS") != -1) { //$NON-NLS-1$
	// return null;
	// }
	// return null; // try it anyway
	// }

	private static final boolean isSOS(final URL url) {
		try {
			return !SOSCapabilities.checkForSOS(url).isEmpty();
		} catch (final Exception e) {
			LOGGER.debug("Not a SOS!", e);
			reasonForFailure = e.getMessage();
			return false;
		}

		// return processURL(url) == null;
	}

	public IService createService(final URL id,
			final Map<String, Serializable> params) {
		if (params == null
				|| !params.containsKey(SOSDataStoreFactory.URL_SERVICE.key)) {
			return null;
		}

		if (id == null) {
			try {
				return SOSServiceImpl.getInstance((URL) (params
						.get(SOSDataStoreFactory.URL_SERVICE.key)), params);
//				return new SOSServiceImpl((URL) (params
//						.get(SOSDataStoreFactory.URL_SERVICE.key)), params);
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		return SOSServiceImpl.getInstance(id, params);
//		return new SOSServiceImpl(id, params);
	}

	@Override
	protected String doOtherChecks(final Map<String, Serializable> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected DataStoreFactorySpi getDataStoreFactory() {
		return SOSDataStoreFactory.getInstance();
	}
}

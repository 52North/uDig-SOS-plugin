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
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import net.refractions.udig.catalog.CatalogPlugin;
import net.refractions.udig.catalog.IResolve;
import net.refractions.udig.catalog.IResolveChangeEvent;
import net.refractions.udig.catalog.IResolveDelta;
import net.refractions.udig.catalog.IService;
import net.refractions.udig.catalog.IServiceInfo;
import net.refractions.udig.catalog.internal.CatalogImpl;
import net.refractions.udig.catalog.internal.ResolveChangeEvent;
import net.refractions.udig.catalog.internal.ResolveDelta;
import net.refractions.udig.ui.ErrorManager;
import net.refractions.udig.ui.UDIGDisplaySafeLock;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.SOSCapabilities;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory.UDIGSOSDataStore;
import org.n52.udig.catalog.sos.internal.Messages;

/**
 * @author 52n
 * 
 */
public class SOSServiceImpl extends IService {
	public static final String SOS_URL_KEY = "net.refractions.udig.catalog.internal.wms.SOSServiceImpl.SOS_URL_KEY"; //$NON-NLS-1$
	public static final String SOS_SOS_KEY = "net.refractions.udig.catalog.internal.sos.SOSServiceImpl.SOS_SOS_KEY"; //$NON-NLS-1$
	private static final Lock dsLock = new UDIGDisplaySafeLock();
	private URL url = null;
	private Map<String, Serializable> params = null;
	protected Lock rLock = new UDIGDisplaySafeLock();
	private volatile List<SOSGeoResourceImpl> members = null;
	private volatile IServiceInfo info = null;
	private Throwable msg = null;
	private volatile UDIGSOSDataStore ds = null;
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSServiceImpl.class);
	private static HashMap<Map<String, Serializable>, SOSServiceImpl> cache = new HashMap<Map<String,Serializable>, SOSServiceImpl>();

	public static SOSServiceImpl getInstance(URL url, final Map<String, Serializable> params) {
		if (!cache.containsKey(params)){
			cache.put(params, new SOSServiceImpl(url, params));
		}
		return cache.get(params);
	}
	
	private SOSServiceImpl(URL url, final Map<String, Serializable> params) {
		
		if (url == null) {
			url = (URL) params.get(SOSDataStoreFactory.URL_SERVICE.key);
		}
		this.url = url;
//		try {
//				Random r = new Random();
//				this.url = new URL(url.toExternalForm()+r.nextInt());	
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
		this.params = params;
	}

	public Map<String, Serializable> getParams() {
		return params;
	}

	/*
	 * @see net.refractions.udig.catalog.IResolve#canResolve(java.lang.Class)
	 */
	@Override
	public <T> boolean canResolve(final Class<T> adaptee) {
		if (adaptee == null) {
			return false;
		}
		return adaptee.isAssignableFrom(UDIGSOSDataStore.class)
				|| super.canResolve(adaptee);
	}

	@Override
	public void dispose(final IProgressMonitor monitor) {
		if (members == null) {
			return;
		}

		final int steps = (int) ((double) 99 / (double) members.size());
		for (final IResolve resolve : members) {
			try {
				final SubProgressMonitor subProgressMonitor = new SubProgressMonitor(
						monitor, steps);
				resolve.dispose(subProgressMonitor);
				subProgressMonitor.done();
			} catch (final Throwable e) {
				ErrorManager
						.get()
						.displayException(
								e,
								"Error disposing members of service: " + getIdentifier(), CatalogPlugin.ID); //$NON-NLS-1$
			}
		}
	}

	/*
	 * @see net.refractions.udig.catalog.IService#getConnectionParams()
	 */
	@Override
	public Map<String, Serializable> getConnectionParams() {
		return params;
	}

	UDIGSOSDataStore getDS(IProgressMonitor monitor) throws IOException {
		if (ds == null) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			monitor.beginTask(Messages.SOSServiceImpl_task_name, 3);
			dsLock.lock();
			monitor.worked(1);
			try {
				if (ds == null) {
					final SOSDataStoreFactory dsf = SOSDataStoreFactory
							.getInstance();
					monitor.worked(1);
					if (dsf.canProcess(params)) {
						monitor.worked(1);
						try {
							ds = (UDIGSOSDataStore) dsf.createDataStore(params);
							monitor.worked(1);
						} catch (final IOException e) {
							msg = e;
							throw e;
						}
					}
				}
			} finally {
				dsLock.unlock();
				monitor.done();
			}
			final IResolveDelta delta = new ResolveDelta(this,
					IResolveDelta.Kind.CHANGED);
			((CatalogImpl) CatalogPlugin.getDefault().getLocalCatalog())
					.fire(new ResolveChangeEvent(this,
							IResolveChangeEvent.Type.POST_CHANGE, delta));
		}
		return ds;
	}

	/*
	 * @see net.refractions.udig.catalog.IResolve#getIdentifier()
	 */
	public URL getIdentifier() {
//		Random r = new Random(); 
		try {
			return new URL(url.toExternalForm()+"@"+params.hashCode());
		} catch (Exception e) {
			e.printStackTrace();
		}
		return url;
	}

	/*
	 * @see net.refractions.udig.catalog.IService#getInfo(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IServiceInfo getInfo(final IProgressMonitor monitor)
			throws IOException {
		getDS(monitor); // load ds
		if (info == null && ds != null) {
			rLock.lock();
			try {
				if (info == null) {
					info = new SOSServiceInfo(ds);
//					 final IResolveDelta delta = new ResolveDelta(this,
//					 IResolveDelta.Kind.CHANGED);
//					 ((CatalogImpl)
//					 CatalogPlugin.getDefault().getLocalCatalog())
//					 .fire(new ResolveChangeEvent(this,
//					 IResolveChangeEvent.Type.POST_CHANGE, delta));
				}
			} finally {
				rLock.unlock();
			}
		}
		return info;
	}

	/*
	 * @see net.refractions.udig.catalog.IResolve#getMessage()
	 */
	public Throwable getMessage() {
		return msg;
	}

	/*
	 * @see net.refractions.udig.catalog.IResolve#getStatus()
	 */
	public Status getStatus() {
		return msg != null ? Status.BROKEN : ds == null ? Status.NOTCONNECTED
				: Status.CONNECTED;
	}

	/*
	 * Required adaptions: <ul> <li>IServiceInfo.class <li>List.class
	 * <IGeoResource> </ul>
	 * 
	 * @see net.refractions.udig.catalog.IService#resolve(java.lang.Class,
	 *      org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public <T> T resolve(final Class<T> adaptee, final IProgressMonitor monitor)
			throws IOException {
		if (adaptee == null) {
			return null;
		}
		if (adaptee.isAssignableFrom(SOSDataStore.class)) {
			return adaptee.cast(getDS(monitor));
		}
		return super.resolve(adaptee, monitor);
	}

	@Override
	public List<SOSGeoResourceImpl> resources(final IProgressMonitor monitor)
			throws IOException {
		if (members == null) {
			rLock.lock();
			try {
				if (members == null) {
					getDS(monitor); // load ds
					members = new LinkedList<SOSGeoResourceImpl>();
					final String[] typenames = ds.getTypeNames();
					if (typenames != null) {
						for (final String element : typenames) {
							try {
								members.add(new SOSGeoResourceImpl(this,
										element));
							} catch (final Exception e) {
								LOGGER.fatal("", e); //$NON-NLS-1$
							}
						}
					}
				}
			} finally {
				rLock.unlock();
			}
		}
		return members;
	}

	class SOSServiceInfo extends IServiceInfo {
		SOSCapabilities capabilities;
		SOSDataStore datastore;

		public SOSServiceInfo(final SOSDataStore ds) throws IOException {
			this.datastore = ds;
			
			capabilities = SOSDataStoreFactory.getInstance().getCapabilities(
					url, ds.getServiceVersion());
			keywords = capabilities.getService().getKeywordList();
		}

		/*
		 * @see net.refractions.udig.catalog.IServiceInfo#getAbstract()
		 */
		@Override
		public String getAbstract() {
			return capabilities == null ? null
					: capabilities.getService() == null ? null : capabilities
							.getService().get_abstract();
		}

		@Override
		public String getDescription() {
			return getIdentifier().toString();
		}

		/*
		 * @see net.refractions.udig.catalog.IServiceInfo#getIcon()
		 */
		@Override
		public ImageDescriptor getIcon() {
			// // TODO ICON
			return null;
		}

		/*
		 * @see net.refractions.udig.catalog.IServiceInfo#getKeywords()
		 */
		@Override
		public String[] getKeywords() {
			return capabilities == null ? null
					: capabilities.getService() == null ? null : capabilities
							.getService().getKeywordList();
		}

		@Override
		public URL getSource() {
			return getIdentifier();
		}

		@Override
		public String getTitle() {
			return (capabilities == null || capabilities.getService() == null) ? (getIdentifier() == null ? Messages.SOSServiceImpl_broken
					: getIdentifier().toString())
					: capabilities.getService().getTitle();
		}
	}
}

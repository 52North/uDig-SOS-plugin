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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry;
import org.osgi.framework.BundleContext;

/**
 * Activator Class; defined in MANIFEST.MF This class represents the main
 * SOS-plugin class
 * 
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSPlugin extends AbstractUIPlugin {
	// The static instance.
	/**
	 * represents the static instance; may be modified by
	 * {@link SOSPlugin#start(BundleContext)} and
	 * {@link SOSPlugin#stop(BundleContext)}
	 * 
	 */
	private static SOSPlugin instance;
	public static final String ID = "org.n52.udig.catalog.internal.sos"; //$NON-NLS-1$
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSPlugin.class);

	/**
	 * Returns the plugin`s instance; singleton pattern
	 * 
	 * @return x Returns the plugin's instance.
	 */
	public static SOSPlugin getDefault() {
		if (instance == null) {
			return new SOSPlugin();
		}
		return instance;
	}

	/**
	 * @param key
	 * @return x Returns the string from the plugin's resource bundle, or 'key'
	 *         if not found.
	 */
	public static String getResourceString(final String key) {
		final ResourceBundle bundle = SOSPlugin.getDefault()
				.getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (final MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Performs the Platform.getDebugOption true check on the provided trace
	 * <p>
	 * Note: ProjectUIPlugin.getDefault().isDebugging() must also be on.
	 * <ul>
	 * <li>Trace.RENDER - trace rendering progress
	 * </ul>
	 * </p>
	 * 
	 * @param trace
	 *            currently only RENDER is defined
	 * @return true if -debug is on for this plugin
	 */
	public static boolean isDebugging(final String trace) {
		return getDefault().isDebugging()
				&& "true".equalsIgnoreCase(Platform.getDebugOption(trace)); //$NON-NLS-1$
	}

	/**
	 * Logs the Throwable in the plugin's log.
	 * <p>
	 * This will be a user visable ERROR iff:
	 * <ul>
	 * <li>t is an Exception we are assuming it is human readable or if a
	 * message is provided
	 * </ul>
	 * </p>
	 * 
	 * @param message
	 * @param t
	 */
	public static void log(final String message, final Throwable t) {
		final int status = t instanceof Exception || message != null ? IStatus.ERROR
				: IStatus.WARNING;
		getDefault().getLog().log(
				new Status(status, ID, IStatus.OK, message, t));
		t.printStackTrace();
	}

	/**
	 * Messages that only engage if getDefault().isDebugging()
	 * <p>
	 * It is much prefered to do this:
	 * 
	 * <pre><code>
	 * private static final String RENDERING = &quot;net.refractions.udig.project/render/trace&quot;;
	 * if (ProjectUIPlugin.getDefault().isDebugging()
	 * 		&amp;&amp; &quot;true&quot;.equalsIgnoreCase(RENDERING)) {
	 * 	System.out.println(&quot;your message here&quot;);
	 * }
	 * </code></pre>
	 * 
	 * </p>
	 * 
	 * @param message
	 * @param e
	 */
	public static void trace(final String message, final Throwable e) {
		if (getDefault().isDebugging()) {
			if (message != null) {
				LOGGER.debug(message);
			}
			if (e != null) {
				e.printStackTrace();
			}
		}
	}

	// Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * creates a new instance of SOSPlugin.java modifies
	 * {@link SOSPlugin#instance}
	 */
	public SOSPlugin() {
		super();
		instance = this;
		// try {
		// File f = new File("datei.txt");
		// f.createNewFile();
		// } catch (Exception e) {
		// }
		PropertyConfigurator.configure(GeneralConfigurationRegistry
				.getInstance().getLog4jPropertiesFilename());
		LOGGER.info("Plugin SOSPlugin loaded");

		System.setProperty("http.proxyHost", GeneralConfigurationRegistry
				.getInstance().getProxyHost());
		System.setProperty("http.proxyPort", GeneralConfigurationRegistry
				.getInstance().getProxyPort());
	}

	/**
	 * @return x Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null) {
				resourceBundle = ResourceBundle
						.getBundle("org.n52.udig.catalog.sos.internal.SOSPluginRessources"); //$NON-NLS-1$
			}
		} catch (final MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		final ClassLoader current = Thread.currentThread()
				.getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(
					SOSDataStore.class.getClassLoader());
		} finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	private void setLoggerLevel(final String loggerID, final String traceID) {
		// final Logger logger = Logger.getLogger(loggerID);
		// if (isDebugging(traceID)) {
		// logger.setLevel(Level.FINE);
		// } else {
		// logger.setLevel(Level.SEVERE);
		// }
		// final ConsoleHandler consoleHandler = new ConsoleHandler();
		// consoleHandler.setLevel(Level.ALL);
		// logger.addHandler(consoleHandler);
	}

	/**
	 * This method is called when the plug-in is stopped nullifies
	 * {@link SOSPlugin#instance} and {@link SOSPlugin#resourceBundle}
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
		instance = null;
		resourceBundle = null;
		super.stop(context);
	}
}
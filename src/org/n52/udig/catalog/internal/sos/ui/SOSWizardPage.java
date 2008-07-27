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
package org.n52.udig.catalog.internal.sos.ui;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.geotools.data.DataStore;
import org.n52.oxf.util.LoggingHandler;
import org.n52.udig.catalog.internal.sos.SOSPlugin;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory.UDIGSOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.sos.internal.Messages;

// TODO use net.refractions.udig.catalog.sos.SOSPreferencePage1 to configure
// this GUI
/**
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSWizardPage extends AbstractUDIGImportPage implements
		ModifyListener, UDIGConnectionPage, SelectionListener {

	protected Combo urlCombo = null;
	protected Combo serviceVersionCombo = null;
	private Button displayCapabilities;
	private Map<String, Serializable> params;
	private static final String SOS_WIZARD_ID = "SOSWizard"; //$NON-NLS-1$
	private static final String SOS_RECENTLY_USED_ID = "RecentlyUsed"; //$NON-NLS-1$
	private IDialogSettings settings;
	private String url = ""; //$NON-NLS-1$
	private static final String[] empty = new String[0];

	// // @Override
	// protected IDialogSettings getDialogSettings() {
	// return SOSPlugin.getDefault().getDialogSettings();
	// }

	/**
	 * the Apache Log4J-logger. Configured via .xml-file. Set path with
	 * {@link GeneralConfigurationRegistry}.
	 */
	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSWizardPage.class);

	/**
	 * @return the dataStore
	 */
	public DataStore getDataStore() {
		// .createDatastore uses a cache,
		try {
			return SOSDataStoreFactory.getInstance().createDataStore(
					getParameters());
		} catch (final IOException ioe) {
			LOGGER.fatal("Error creating datastore", ioe);
		}
		return null;
	}

	/**
	 * Creates a new instance of SOSWizardPage
	 * 
	 * @param pageName
	 *            the title of the created page
	 */
	public SOSWizardPage(final String pageName) {
		this(pageName, "title", null);
	}

	/**
	 * Creates a new instance of SOSWizardPage
	 */
	public SOSWizardPage() {
		this(""); //$NON-NLS-1$
	}

	/**
	 * Creates a new instance of SOSWizardPAge
	 * 
	 * @param pageName
	 *            the pagename
	 * @param title
	 *            the title
	 * @param titleImage
	 *            ImageDescriptor for an icon
	 */
	public SOSWizardPage(final String pageName, final String title,
			final ImageDescriptor titleImage) {
		super(pageName);
		settings = SOSPlugin.getDefault().getDialogSettings().getSection(
				SOS_WIZARD_ID);
		if (settings == null) {
			settings = SOSPlugin.getDefault().getDialogSettings()
					.addNewSection(SOS_WIZARD_ID);
		}
		// setDescription(SOSPlugin.getResourceString(Messages.SOSWizardPage_wizarddescription));
		params = new HashMap<String, Serializable>();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (red != null) {
			red.dispose();
		}
	}

	private Color red;

	protected Map<String, Serializable> getParameters() {
		if (urlCombo == null) {
			return new HashMap<String, Serializable>();
		}
		// boolean error = false;
		try {
			final URL url = new URL(urlCombo.getText());
			params.put(SOSDataStoreFactory.URL_SERVICE.key, url);

			if (serviceVersionCombo.getText() != null
					&& !serviceVersionCombo.getText().trim().equals("")) {
				params.put(SOSDataStoreFactory.SERVICE_VERSION.key,
						serviceVersionCombo.getText().trim());
			}
			setErrorMessage(null);
		} catch (final Exception e) {
			setErrorMessage(e.getMessage());
		}

		// return error ? new HashMap<String, Serializable> : params;
		return params;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.refractions.udig.catalog.ui.UDIGConnectionPage#getParams()
	 */
	public Map<String, Serializable> getParams() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(final Composite arg0) {
		final Composite composite = new Group(arg0, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));

		// urllabel
		Label label = new Label(composite, SWT.NONE);
		label.setText(Messages.SOSWizardPage_label_url_text);
		label.setToolTipText(Messages.SOSWizardPage_label_url_tooltip);
		label.setLayoutData(new GridData(SWT.END, SWT.DEFAULT, false, false));

		/*
		 * url = new Text( composite, SWT.BORDER | SWT.SINGLE );
		 * url.setLayoutData( new GridData(GridData.FILL_HORIZONTAL) );
		 * url.setText( "http://" ); url.addModifyListener(this);
		 */

		String[] temp = settings.getArray(SOS_RECENTLY_USED_ID);
		if (temp == null) {
			temp = new String[0];
		}

		final List<String> recent = Arrays.asList(temp);
		final GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.widthHint = 400;

		// For Drag 'n Drop as well as for general selections
		// look for a url as part of the selction
		final Map<String, Serializable> params = new HashMap<String, Serializable>(
				1); // based on selection
		URL selectedURL;
		try {
			selectedURL = (URL) SOSDataStoreFactory.URL_SERVICE.lookUp(params);
		} catch (final IOException e) {
			selectedURL = null;
		}

		// urlComboBox
		urlCombo = new Combo(composite, SWT.BORDER);
		urlCombo.setItems(recent.toArray(new String[recent.size()]));
		urlCombo.setVisibleItemCount(15);
		urlCombo.setLayoutData(gridData);
		if (selectedURL != null) {
			urlCombo.setText(selectedURL.toExternalForm());
		} else if (url != null && url.length() != 0) {
			urlCombo.setText(url);
		} else {
			for (final String s : SOSConfigurationRegistry.getInstance()
					.getConfiguredSOSs()) {
				urlCombo.add(s);
			}
		}
		urlCombo.addModifyListener(this);
		urlCombo.addSelectionListener(this);

		// add a button to display capabilities document
		(new Label(composite, SWT.LEFT)).setText("Version");
		serviceVersionCombo = new Combo(composite, SWT.BORDER | SWT.DROP_DOWN
				| SWT.READ_ONLY);
		serviceVersionCombo.setVisibleItemCount(3);
		serviceVersionCombo.setToolTipText("Select service version");
		serviceVersionCombo.setEnabled(false);
		// serviceVersionCombo.addModifyListener(this);
		serviceVersionCombo.addSelectionListener(this);

		// add a button to display capabilities document
		new Label(composite, SWT.LEFT);
		displayCapabilities = new Button(composite, SWT.PUSH);
		displayCapabilities.setText("GetCapabilities");
		displayCapabilities.addSelectionListener(this);

		// add spacer
		label = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3,
				3));

		label = new Label(composite, SWT.NONE);
		label
				.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
						false));

		label = new Label(composite, SWT.NONE);
		label
				.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
						false));

		urlCombo.addModifyListener(this);

		setControl(composite);
		// setPageComplete(true);
	}

	public void modifyText(final ModifyEvent e) {
		if (e.widget != null) {

			if (e.widget instanceof Text) {
				((Text) e.widget).setForeground(null);
			}
			if (e.widget instanceof Combo) {
				if (e.widget == urlCombo) {
					((Combo) e.widget).setForeground(null);
					// setErrorMessage(null);
					url = urlCombo.getText();
					// is the service a SOS?
					try {
						SOSDataStoreFactory.getInstance().checkForSOS(
								new URL(url));
						final List<String> suppVersions = SOSDataStoreFactory
								.getInstance().getSupportedVersions(
										new URL(url));
						if (suppVersions != null && !suppVersions.isEmpty()) {
							displayCapabilities.setEnabled(true);
							final String[] out = new String[suppVersions.size()];
							int i = 0;
							for (final String s : suppVersions) {
								out[i++] = s;
							}
							serviceVersionCombo.setItems(out);
							serviceVersionCombo.select(0);
							serviceVersionCombo.setEnabled(true);
							if (serviceVersionCombo.getText() != null
									&& !serviceVersionCombo.getText().trim()
											.equals("")) {
								params
										.put(
												SOSDataStoreFactory.SERVICE_VERSION.key,
												serviceVersionCombo.getText()
														.trim());
								setErrorMessage(null);
							}

						} else {
							// disable serviceVersionCombobox and set
							// serviceVersion to null
							params
									.remove(SOSDataStoreFactory.SERVICE_VERSION.key);
							serviceVersionCombo.setItems(empty);
							serviceVersionCombo.setEnabled(false);
							displayCapabilities.setEnabled(false);
							setErrorMessage("URL offers no SOS or a not supported SOS");
						}
					} catch (final IOException ioe) {
						setErrorMessage("URL offers no SOS or a not supported SOS");
						// setErrorMessage(ioe.getMessage());
						LOGGER.debug(ioe);
						serviceVersionCombo.setEnabled(false);
					}
					setPageComplete(isPageComplete());
				}
				getContainer().updateButtons();
			}
		}
	}

	public void widgetSelected(final SelectionEvent e) {
		if (e.widget instanceof Button) {
			final Button b = (Button) e.widget;

			// open a new shell and display Capabilities
			// creates a new Datastore
			if (b.equals(displayCapabilities)) {
				final Shell xmlViewer = new Shell(b.getShell());
				xmlViewer.setLayout(new FillLayout());
				xmlViewer.setText("Capabilities for " + urlCombo.getText());
				final StyledText t = new StyledText(xmlViewer, SWT.MULTI
						| SWT.V_SCROLL | SWT.H_SCROLL);
				try {
					t.setText(((UDIGSOSDataStore) getDataStore())
							.getCapabilities().getCapabilitiesString());
				} catch (final IOException ie) {
					LOGGER.error(ie);
					setErrorMessage(ie.getMessage());
				}
				xmlViewer.open();
			}
		} else if (e.widget instanceof Combo) {
			final Combo c = (Combo) e.widget;
			if (c.equals(serviceVersionCombo)) {
				if (serviceVersionCombo.getText() != null
						&& !serviceVersionCombo.getText().trim().equals("")) {
					params.put(SOSDataStoreFactory.SERVICE_VERSION.key,
							serviceVersionCombo.getText().trim());
				}
			}
		}
		getWizard().getContainer().updateButtons();
	}

	@Override
	public boolean isPageComplete() {
		params = new HashMap<String, Serializable>();
		params = getParameters();
		if (params == null || params.isEmpty()) {
			return false;
		}
		URL url;
		url = ((URL) params.get(SOSDataStoreFactory.URL_SERVICE.key));
		final String trim = url.getHost().trim();
		if (trim.length() == 0) {
			return false;
		}

		String version;
		version = (String) params.get(SOSDataStoreFactory.SERVICE_VERSION.key);
		if (version == null) {
			return false;
		}
		if (version.trim().length() == 0) {
			return false;
		}

		// is the service a SOS?
		try {
			setErrorMessage(null);
			return SOSDataStoreFactory.getInstance().checkForSOS(url);
		} catch (final IOException e) {
			setErrorMessage(e.getMessage());
			return false;
		}

		// if (!SOSDataStoreFactory.getInstance().canProcess(params)) {
		// return false;
		// }
	}

	@Override
	public boolean leavingPage() {
		try {
			getParameters();
			SOSDataStoreFactory.getInstance().getCapabilities(
					SOSDataStoreFactory.workOnParams(params));
			return true;
		} catch (final Exception e) {
			// SOSPlugin.log("Error creating DataStore", e);
			setErrorMessage(e.getMessage());
			LOGGER.fatal("Error leaving wizardpage", e);
			return false;
		}
	}

	public void widgetDefaultSelected(final SelectionEvent arg0) {
		// TODO Auto-generated method stub
	}
}
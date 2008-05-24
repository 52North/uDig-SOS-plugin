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
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;

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
import org.n52.udig.catalog.internal.sos.SOSPlugin;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory.UDIGSOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.sos.internal.Messages;
//TODO use net.refractions.udig.catalog.sos.SOSPreferencePage1 to configure this GUI
/**
 * @author 52n
 *
 */
public class SOSWizardPage extends AbstractUDIGImportPage implements
		ModifyListener, UDIGConnectionPage, SelectionListener {

	protected Combo urlCombo = null;
	private Button displayCapabilities;
	private final Map<String, Serializable> params;
	private static final String SOS_WIZARD_ID = "SOSWizard"; //$NON-NLS-1$
	private static final String SOS_RECENTLY_USED_ID = "RecentlyUsed"; //$NON-NLS-1$
	private IDialogSettings settings;
	private String url = ""; //$NON-NLS-1$
//	private static final Logger LOGGER = LoggingHandler
//			.getLogger(SOSWizardPage.class);

	@Override
	protected IDialogSettings getDialogSettings() {
		return SOSPlugin.getDefault().getDialogSettings();
	}

	/**
	 * @return the dataStore
	 */
	public DataStore getDataStore() {
		// .createDatastore uses a cache,
		try {
			return SOSDataStoreFactory.getInstance().createDataStore(
					getParameters());
		} catch (final IOException ioe) {
			SOSPlugin.log("Error creating datastore", ioe);
		}
		return null;
	}

	/**
	 * Creates a new instance of SOSWizardPage
	 * @param pageName the title of the created page
	 */
	public SOSWizardPage(final String name) {
		this(name, "title", null);
	}

	/**
	 * Creates a new instance of SOSWizardPage
	 */
	public SOSWizardPage() {
		this(""); //$NON-NLS-1$
	}

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public SOSWizardPage(final String pageName, final String title,
			final ImageDescriptor titleImage) {
		// super(pageName, title, titleImage);
		super(pageName);
		settings = SOSPlugin.getDefault().getDialogSettings().getSection(
				SOS_WIZARD_ID);
		if (settings == null) {
			settings = SOSPlugin.getDefault().getDialogSettings()
					.addNewSection(SOS_WIZARD_ID);
		}
//		setDescription(SOSPlugin.getResourceString(Messages.SOSWizardPage_wizarddescription));
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
			return null;
		}
		boolean error = false;
		try {
			final URL url = new URL(urlCombo.getText());
			params.put(SOSDataStoreFactory.URL_CAPS.key, url);

		} catch (final Exception e) {
			if (red == null) {
				red = new Color(null, 255, 0, 0);
			}
			urlCombo.setForeground(red);
			error = true;
		}
		return error ? null : params;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.refractions.udig.catalog.ui.UDIGConnectionPage#getParams()
	 */
	public Map<String, Serializable> getParams() {
		return null;
	}

	// /** Can be called during createControl */
	// protected Map<String,Serializable> defaultParams(){
	// IStructuredSelection selection = (IStructuredSelection)PlatformUI
	// .getWorkbench() .getActiveWorkbenchWindow().getSelectionService()
	// .getSelection();
	// return toParams( selection );
	// }

	// /** Retrieve "best" WFS guess of parameters based on provided context */
	// protected Map<String,Serializable> toParams( IStructuredSelection
	// context){
	// if( context == null ) {
	// // lets go with the defaults then
	// return Collections.emptyMap();
	// }
	// // for( Iterator itr = context.iterator(); itr.hasNext(); ) {
	// // Map<String,Serializable> params
	// // = wpsConnFactory.createConnectionParameters( itr.next() );
	// // if( !params.isEmpty() ) return params;
	// // }
	// return Collections.emptyMap();
	// }

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
		final Map<String, Serializable> params = new HashMap<String, Serializable>(1); // based on selection
		URL selectedURL;
		try {
			selectedURL = (URL) SOSDataStoreFactory.URL_CAPS.lookUp(params);
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
//TODO urls in urlCombo should be set from properties or a central registry
			for (final String s:SOSConfigurationRegistry.getInstance().getConfiguredSOSs()){
				urlCombo.add(s);
			}

//			urlCombo.setText(
//					"http://mars.uni-muenster.de:8080/OWS5SOS/sos?Service=SOS&Version=1.0&Request=GetCapabilities"
//					);
		}
		urlCombo.addModifyListener(this);

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
		label.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
						false));

		label = new Label(composite, SWT.NONE);
		label.setLayoutData(new GridData(SWT.DEFAULT, SWT.DEFAULT, false,
						false));

		setControl(composite);
		setPageComplete(true);

		urlCombo.addModifyListener(this);
	}

	public void modifyText(final ModifyEvent e) {
		if (e.widget != null && e.widget instanceof Text) {
			((Text) e.widget).setForeground(null);
		}
		if (e.widget == urlCombo) {
			((Combo) e.widget).setForeground(null);
			setErrorMessage(null);
			url = urlCombo.getText();
		}
		getContainer().updateButtons();
	}

	// public void widgetDefaultSelected(SelectionEvent arg0) {
	// if( getWizard().canFinish() ){
	// getWizard().performFinish();
	// }
	// }

	public void widgetSelected(final SelectionEvent e) {
		if (e.widget instanceof Button) {
			final Button b = (Button) e.widget;

			if (b.equals(displayCapabilities)){
				final Shell xmlViewer = new Shell(b.getShell());
				xmlViewer.setLayout(new FillLayout());
				xmlViewer.setText("Capabilities for "+urlCombo.getText());
//				new Browser(xmlViewer,SWT.MOZILLA);
				final StyledText t = new StyledText(xmlViewer, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
				try {
					t.setText(((UDIGSOSDataStore)getDataStore()).getCapabilities().getCapabilitiesString());
				} catch (final IOException ie) {
					SOSPlugin.log(ie.getMessage(), ie);
				}

//				xmlViewer.pack();
				xmlViewer.open();

			}
		}
		// Button b = (Button)e.widget;
		// if(b.equals(advancedTag)){
		// advanced.setVisible(advancedTag.getSelection());
		// }

		getWizard().getContainer().updateButtons();
	}

	@Override
	public boolean isPageComplete() {
		final Map<String, Serializable> params = getParameters();
		if (params == null) {
			return false;
		}
		URL url;
		url = ((URL) params.get(SOSDataStoreFactory.URL_CAPS.key));
		final String trim = url.getHost().trim();
		if (trim.length() == 0) {
			return false;
		}

//		if (!SOSDataStoreFactory.getInstance().canProcess(params)) {
//			return false;
//		}
		return true;
	}



	@Override
	public boolean leavingPage() {
		try {
//			SOSPlugin.trace("Creating Datastore", null);
//			// fire up a getDataStore to collect data
//			getDataStore();
//			SOSDataStoreFactory.getInstance().getCapabilities(new URL(urlCombo.getText()));
			return true;
		} catch (final Exception e) {
//			SOSPlugin.log("Error creating DataStore", e);
			SOSPlugin.log("Error leaving wizardpage", e);
			return false;
		}
	}

	public void widgetDefaultSelected(final SelectionEvent arg0) {
		// TODO Auto-generated method stub
	}
}
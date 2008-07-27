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
Created: 23.04.2008
 *********************************************************************************/
package org.n52.udig.catalog.internal.sos.ui;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.config.GeneralConfigurationRegistry;

/**
 * This class offers a way to configure the plugin.
 * 
 * Enable this via plugin.xml <extension point="org.eclipse.ui.preferencePages">
 * <page
 * category="net.refractions.udig.catalog.ui.preferences.CatalogPreferencePage"
 * class="org.n52.udig.catalog.internal.sos.ui.SOSPreferencePage"
 * id="org.n52.udig.catalog.internal.sos.ui.SOSPreferencePage" name="SOS">
 * </page> </extension>
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * @see GeneralConfigurationRegistry
 */
public class SOSPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage, SelectionListener, ModifyListener {

	/**
	 * 
	 */
	public SOSPreferencePage() {
		// no constructor needed
	}

	/**
	 * @param title
	 */
	public SOSPreferencePage(final String title) {
		super(title);
	}

	/**
	 * @param title
	 * @param image
	 */
	public SOSPreferencePage(final String title, final ImageDescriptor image) {
		super(title, image);
	}

	Composite composite;
	Text inputLOG4J;
	Text inputProxyHost;
	Text inputProxyPort;
	Text inputTimeToCacheCapabilities;
	Text inputTimeToCacheDatastore;
	Text inputSOSFilename;
	Button bFixPB;
	Button bSOSF;
	Button bLOG4J;
	Combo inputPreferedSOSVersion;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		composite = new Group(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		new Label(composite, SWT.NONE).setText("Fix parameter-block:");
		bFixPB = new Button(composite, SWT.CHECK);
		bFixPB.setEnabled(GeneralConfigurationRegistry.getInstance()
				.isWriteable());
		bFixPB.setSelection(GeneralConfigurationRegistry.getInstance()
				.isFixErrors());
		bFixPB.addSelectionListener(this);
		new Label(composite, SWT.NONE).setText("");

		new Label(composite, SWT.NONE).setText("Log4j Filename:");
		inputLOG4J = new Text(composite, SWT.SINGLE | SWT.BORDER);
		inputLOG4J.setText(GeneralConfigurationRegistry.getInstance()
				.getLog4jPropertiesFilename());
		inputLOG4J.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		inputLOG4J.setEditable(GeneralConfigurationRegistry.getInstance()
				.isWriteable());
		inputLOG4J.addModifyListener(this);

		bLOG4J = new Button(composite, SWT.PUSH);
		bLOG4J.setText("Browse");
		bLOG4J.setEnabled(GeneralConfigurationRegistry.getInstance()
				.isWriteable());

		new Label(composite, SWT.NONE).setText("SOSConfig Filename:");
		inputSOSFilename = new Text(composite, SWT.SINGLE | SWT.BORDER);
		inputSOSFilename.setText(GeneralConfigurationRegistry.getInstance()
				.getSOSconfigurationFilename());
		inputSOSFilename.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		inputSOSFilename.setEditable(GeneralConfigurationRegistry.getInstance()
				.isWriteable());
		inputSOSFilename.addModifyListener(this);

		bSOSF = new Button(composite, SWT.PUSH);
		bSOSF.setText("Browse");
		bSOSF.setEnabled(GeneralConfigurationRegistry.getInstance()
				.isWriteable());

		new Label(composite, SWT.NONE).setText("Cachetime Capabilities (ms):");
		inputTimeToCacheCapabilities = new Text(composite, SWT.SINGLE
				| SWT.BORDER);
		inputTimeToCacheCapabilities.setText(String
				.valueOf(GeneralConfigurationRegistry.getInstance()
						.getTimeToCacheCapabilities()));
		inputTimeToCacheCapabilities.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, false));
		inputTimeToCacheCapabilities.setEditable(GeneralConfigurationRegistry
				.getInstance().isWriteable());
		inputTimeToCacheCapabilities.addModifyListener(this);
		new Label(composite, SWT.NONE).setText("");

		new Label(composite, SWT.NONE).setText("Cachetime Datastore (ms):");
		inputTimeToCacheDatastore = new Text(composite, SWT.SINGLE | SWT.BORDER);
		inputTimeToCacheDatastore.setText(String
				.valueOf(GeneralConfigurationRegistry.getInstance()
						.getTimeToCacheCapabilities()));
		inputTimeToCacheDatastore.setLayoutData(new GridData(SWT.FILL,
				SWT.FILL, true, false));
		inputTimeToCacheDatastore.setEditable(GeneralConfigurationRegistry
				.getInstance().isWriteable());
		inputTimeToCacheDatastore.addModifyListener(this);
		new Label(composite, SWT.NONE).setText("");

		new Label(composite, SWT.NONE).setText("Prefered SOSVersion:");
		inputPreferedSOSVersion = new Combo(composite, SWT.SINGLE | SWT.BORDER);
		inputPreferedSOSVersion.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, false));
		for (final String s : SOSDataStoreFactory.supportedVersions) {
			inputPreferedSOSVersion.add(s);
		}
		inputPreferedSOSVersion.setText(GeneralConfigurationRegistry
				.getInstance().getPreferedSOSVersion());

		inputPreferedSOSVersion.setEnabled(GeneralConfigurationRegistry
				.getInstance().isWriteable());
		inputPreferedSOSVersion.addSelectionListener(this);
		new Label(composite, SWT.NONE).setText("");

		new Label(composite, SWT.NONE).setText("Hostname of proxy:");
		inputProxyHost = new Text(composite, SWT.SINGLE | SWT.BORDER);
		inputProxyHost.setText(String.valueOf(GeneralConfigurationRegistry
				.getInstance().getProxyHost()));
		inputProxyHost.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		inputProxyHost.setEditable(GeneralConfigurationRegistry.getInstance()
				.isWriteable());
		inputProxyHost.addModifyListener(this);
		new Label(composite, SWT.NONE).setText("");

		new Label(composite, SWT.NONE).setText("Port of proxy:");
		inputProxyPort = new Text(composite, SWT.SINGLE | SWT.BORDER);
		inputProxyPort.setText(String.valueOf(GeneralConfigurationRegistry
				.getInstance().getProxyPort()));
		inputProxyPort.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		inputProxyPort.setEditable(GeneralConfigurationRegistry.getInstance()
				.isWriteable());
		inputProxyPort.addModifyListener(this);
		new Label(composite, SWT.NONE).setText("");

		return composite;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(final IWorkbench workbench) {
		// TODO Auto-generated method stub

	}

	public void widgetDefaultSelected(final SelectionEvent e) {
		// TODO Auto-generated method stub

	}

	public void widgetSelected(final SelectionEvent e) {
		if (e.widget instanceof Button) {
			final Button b = (Button) e.widget;

			// open a new shell and display Capabilities
			// creates a new Datastore
			if (b.equals(bFixPB)) {
				if ((b.getSelection()) != GeneralConfigurationRegistry
						.getInstance().isFixErrors()) {
					GeneralConfigurationRegistry.getInstance().setFixErrors(
							b.getSelection());
				}

			} else if (b.equals(bLOG4J)) {
				final FileDialog fileDialog = new FileDialog(composite
						.getShell(), SWT.OPEN);
				fileDialog.setFilterExtensions(new String[] {
						"*.properties", "*.xml" }); //$NON-NLS-1$
				// fileDialog.setFilterNames(new
				// String[]{Messages.DependencyQueryPreferencePage_archive});
				final String result = fileDialog.open();
				if (result != null) {
					inputLOG4J.setText(result);
				}

			} else if (b.equals(bSOSF)) {
				final FileDialog fileDialog = new FileDialog(composite
						.getShell(), SWT.OPEN);
				fileDialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$
				// fileDialog.setFilterNames(new
				// String[]{Messages.DependencyQueryPreferencePage_archive});
				final String result = fileDialog.open();
				if (result != null) {
					inputSOSFilename.setText(result);
				}

			}

		} else if (e.widget instanceof Combo) {
			final Combo c = (Combo) e.widget;
			if (c.equals(inputPreferedSOSVersion)) {
				if (!c.getItem(c.getSelectionIndex()).equals(
						GeneralConfigurationRegistry.getInstance()
								.getPreferedSOSVersion())) {
					GeneralConfigurationRegistry.getInstance()
							.setPreferedSOSVersion(
									c.getItem(c.getSelectionIndex()));
				}
			}
		}
		GeneralConfigurationRegistry.getInstance().save();
	}

	public void modifyText(final ModifyEvent e) {
		if (e.widget instanceof Text) {
			final Text t = (Text) e.widget;

			// open a new shell and display Capabilities
			// creates a new Datastore
			if (t.equals(inputLOG4J)) {
				if (!t.getText().equals(
						GeneralConfigurationRegistry.getInstance()
								.getLog4jPropertiesFilename())) {
					GeneralConfigurationRegistry.getInstance()
							.setLog4jPropertiesFilename(t.getText());
				}
			} else if (t.equals(inputSOSFilename)) {
				if (!t.getText().equals(
						GeneralConfigurationRegistry.getInstance()
								.getSOSconfigurationFilename())) {
					GeneralConfigurationRegistry.getInstance()
							.setSOSconfigurationFilename(t.getText());
				}
			} else if (t.equals(inputProxyHost)) {
				if (!t.getText().equals(
						GeneralConfigurationRegistry.getInstance()
								.getProxyHost())) {
					GeneralConfigurationRegistry.getInstance().setProxyHost(
							t.getText());
				}
			} else if (t.equals(inputProxyPort)) {
				if (!t.getText().equals(
						GeneralConfigurationRegistry.getInstance()
								.getProxyPort())) {
					GeneralConfigurationRegistry.getInstance().setProxyPort(
							t.getText());
				}
			} else if (t.equals(inputTimeToCacheCapabilities)) {
				if (!t.getText().equals(
						GeneralConfigurationRegistry.getInstance()
								.getTimeToCacheCapabilities())) {
					GeneralConfigurationRegistry.getInstance()
							.setTimeToCacheCapabilities(
									Long.parseLong(t.getText()));
				}
			} else if (t.equals(inputTimeToCacheDatastore)) {
				if (!t.getText().equals(
						GeneralConfigurationRegistry.getInstance()
								.getTimeToCacheDatastore())) {
					GeneralConfigurationRegistry.getInstance()
							.setTimeToCacheDatastore(
									Long.parseLong(t.getText()));
				}
			}
			GeneralConfigurationRegistry.getInstance().save();
		}
	}
}
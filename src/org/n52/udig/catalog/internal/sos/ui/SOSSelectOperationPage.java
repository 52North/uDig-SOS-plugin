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
package org.n52.udig.catalog.internal.sos.ui;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.ResolveLabelProviderSimple;
import net.refractions.udig.catalog.ui.ResolveTitlesDecorator;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;

import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.DataStore;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.SOSOperations;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;

// TODO use net.refractions.udig.catalog.sos.SOSPreferencePage1 to configure
// this GUI
/**
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class SOSSelectOperationPage extends AbstractUDIGImportPage implements
		UDIGConnectionPage {

	private SOSDataStore dataStore;
	private final HashMap<SOSOperationType, Document> infos = new HashMap<SOSOperationType, Document>(
			5);
	private ListViewer operationIdViewer;

	private TextViewer textViewer;

	/** url from workbench selection * */
	private ResolveTitlesDecorator titleDecorator;

	public SOSSelectOperationPage() {
		this("Operation");
	}

	protected static boolean resetOperations;

	/**
	 * @param pageName
	 */
	public SOSSelectOperationPage(final String pageName) {
		super(pageName);
		super.setTitle("Sensor Observation Service");
		super.setDescription("Select an Operation");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(final Composite arg) {
		final Composite parent = new Composite(arg, SWT.NONE);
		parent.setLayout(new GridLayout(2, false));

		try {
			operationIdViewer = new ListViewer(parent, SWT.SINGLE);
			textViewer = new TextViewer(parent, SWT.MULTI | SWT.V_SCROLL
					| SWT.H_SCROLL);
			textViewer.getControl().setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));

			operationIdViewer.getControl().setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, false, true));
			// operationIdViewer.add(new String(" "));
			// init and cache caps

			operationIdViewer
					.addPostSelectionChangedListener(new ISelectionChangedListener() {

						public void selectionChanged(
								final SelectionChangedEvent event) {
							final IStructuredSelection selection = (IStructuredSelection) operationIdViewer
									.getSelection();
							if (selection == null) {
								return;
							}

							try {
								SOSOperationType sosopType = null;

								for (final Iterator<String> iterator = selection
										.iterator(); iterator.hasNext();) {
									final String s = iterator.next();
									sosopType = ((SOSOperationType) SOSDataStoreFactory
											.getInstance()
											.getCapabilities(
													SOSDataStoreFactory
															.workOnParams(((SOSWizardPage) getPreviousPage())
																	.getParameters()))
											.getOperations()
											.getOperationTypeByName(s));
								}

								textViewer.setInput(getInfo(sosopType));
								// workaround, force population of
								// ParameterViewer in SOSParameterConfiguration
								SOSParameterConfigurationPage.dirtyBit = true;

							} catch (final IOException e) {
								e.printStackTrace();
							}
						}

					});
			operationIdViewer.setContentProvider(new SOSContentProvider());

			titleDecorator = new ResolveTitlesDecorator(
					new ResolveLabelProviderSimple(), true);
			final LabelProvider labelProvider = new DecoratingLabelProvider(
					titleDecorator.getSource(), titleDecorator);

			operationIdViewer.setLabelProvider(labelProvider);

			final String allOperations[] = SOSDataStoreFactory
					.getInstance()
					.getCapabilities(
							SOSDataStoreFactory
									.workOnParams(((SOSWizardPage) getPreviousPage())
											.getParameters())).getOperations()
					.getAllOperationNames();
			final java.util.List<String> filteredOperations = new LinkedList<String>();

			for (final String op : allOperations) {
				if (SOSConfigurationRegistry.getInstance().getShowOperation(
						((SOSWizardPage) getPreviousPage()).getParameters()
								.get(SOSDataStoreFactory.URL_SERVICE.key)
								.toString(), op)) {
					filteredOperations.add(op);
				}
			}
			final String filteredOperationsArray[] = new String[filteredOperations
					.size()-1];
			int i = 0;
			for (final String s : filteredOperations) {
				// take care I reduced arraysize
				if (!s.equals(SOSOperations.opName_GetCapabilities)){
					filteredOperationsArray[i++] = s;
				}
			}
			operationIdViewer.setInput(filteredOperationsArray);

		} catch (final Exception e) {
			e.printStackTrace();
		}
		setControl(parent);
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	private Document getInfo(final SOSOperationType opType) {
		if (infos.containsKey(opType)) {
			return infos.get(opType);
		}

		final Document doc = new Document(opType.getInfo());
		infos.put(opType, doc);
		return doc;
	}

	public Map<String, Serializable> getParameters() {
		final Map<String, Serializable> p = ((SOSWizardPage) getPreviousPage())
				.getParameters();
		if (operationIdViewer != null) {
			final IStructuredSelection selection = (IStructuredSelection) operationIdViewer
					.getSelection();
			if (selection.isEmpty()) {
				return p;
			}
			final String selectedOperation = (String) selection
					.getFirstElement();

			p.put(SOSDataStoreFactory.OPERATION.key, selectedOperation);
		}
		return p;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.refractions.udig.catalog.ui.UDIGConnectionPage#getParams()
	 */
	public Map<String, Serializable> getParams() {
		return null;
	}

	@Override
	public boolean isPageComplete() {
		if (resetOperations) {

		}
		return super.isPageComplete();
	}
}
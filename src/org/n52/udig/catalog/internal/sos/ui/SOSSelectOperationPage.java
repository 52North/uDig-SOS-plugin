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
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.ResolveLabelProviderSimple;
import net.refractions.udig.catalog.ui.ResolveTitlesDecorator;
import net.refractions.udig.catalog.ui.UDIGConnectionPage;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.viewers.DecoratingLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.geotools.data.DataStore;
import org.n52.udig.catalog.internal.sos.SOSPlugin;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStore;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;
//TODO use net.refractions.udig.catalog.sos.SOSPreferencePage1 to configure this GUI
/**
 * @author 52n
 *
 */
public class SOSSelectOperationPage extends AbstractUDIGImportPage implements
		UDIGConnectionPage{

	private SOSDataStore dataStore;
	private final HashMap<SOSOperationType, Document> infos = new HashMap<SOSOperationType, Document>(
			5);
	private ListViewer operationIdViewer;
	private Map<String, Serializable> params;

	private TextViewer textViewer;

	/** url from workbench selection * */
	private ResolveTitlesDecorator titleDecorator;

	public SOSSelectOperationPage() {
		this("Operation");
	}

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

		final SOSWizardPage prevPage = (SOSWizardPage) getPreviousPage();
		params = prevPage.getParameters();
//		dataStore = (SOSDataStore) ((SOSWizardPage) getPreviousPage())
//				.getDataStore();

		try {
			operationIdViewer = new ListViewer(parent, SWT.MULTI);
			textViewer = new TextViewer(parent, SWT.MULTI | SWT.V_SCROLL
					| SWT.H_SCROLL);
			textViewer.getControl().setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));

			operationIdViewer.getControl().setLayoutData(
					new GridData(SWT.FILL, SWT.FILL, true, true));
			// init and cache caps
//			dataStore.getCapabilities();


			operationIdViewer
					.addPostSelectionChangedListener(new ISelectionChangedListener() {

						public void selectionChanged(final SelectionChangedEvent event) {
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
									sosopType = ((SOSOperationType) SOSDataStoreFactory.getInstance()
											.getCapabilities(SOSDataStoreFactory.workOnParams(params)).getOperations()
											.getOperationTypeByName(s));


									// this is a good time to configure the operation with SOSConfigurationRegistry
								SOSConfigurationRegistry.getInstance().updateParameterConfiguration(((URL)params.get(SOSDataStoreFactory.URL_SERVICE.key)).toExternalForm(),
										s,
										sosopType.getParameterConfiguration());
//									.setParameterConfiguration(
//											SOSConfigurationRegistry.getInstance().updateParameterConfiguration(((URL)params.get(SOSDataStoreFactory.URL_SERVICE.key)).toExternalForm(), params.get(SOSDataStoreFactory.OPERATION.key), );

									// TODO info erstellen
								}
								textViewer.setInput(getInfo(sosopType));

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

//			String allOperations[] = dataStore.getCapabilities().getOperations().getAllOperationNames();
			final String allOperations[] = SOSDataStoreFactory.getInstance()
				.getCapabilities(SOSDataStoreFactory.workOnParams(params)).getOperations().getAllOperationNames();
			final java.util.List<String> filteredOperations = new LinkedList<String>();

			for (final String op:allOperations){
				if (SOSConfigurationRegistry.getInstance().getShowOperation(params.get(SOSDataStoreFactory.URL_CAPS.key).toString(), op)){
					filteredOperations.add(op);
				}
			}
			final String filteredOperationsArray[] = new String[filteredOperations.size()];
			int i=0;
			for (final String s:filteredOperations){
				filteredOperationsArray[i++] = s;
			}
//			operationIdViewer.setInput(filteredOperations.toArray());
			operationIdViewer.setInput(filteredOperationsArray);

		} catch (final Exception e) {
			e.printStackTrace();
		}
		setControl(parent);
	}

	public DataStore getDataStore() {
		return dataStore;
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		return SOSPlugin.getDefault().getDialogSettings();
	}

	private Document getInfo(final SOSOperationType opType) {
		if (infos.containsKey(opType)) {
			return infos.get(opType);
		}
		final StringBuilder docSB = new StringBuilder();
		// String docS = new String("");
		docSB.append("ID:\n");
		docSB.append(opType.getId());
		docSB.append("\n\nGet:\n");
		docSB.append(opType.getGet());
		docSB.append("\n\nPost:\n");
		docSB.append(opType.getPost());
		docSB.append("\n\nSupported formats:\n");
		docSB.append(opType.getFormats());
		docSB.append("\n\nParameters:\n");
		docSB.append(opType.getParameterConfiguration().getParametersAsStrings());
		final Document doc = new Document(docSB.toString());
		infos.put(opType, doc);
		return doc;
	}

	public Map<String, Serializable> getParameters() {
		if (operationIdViewer != null) {
			final IStructuredSelection selection = (IStructuredSelection) operationIdViewer
					.getSelection();
			if (selection.isEmpty()) {
				return params;
			}
			final String selectedOperation = (String) selection.getFirstElement();
			params.put(SOSDataStoreFactory.OPERATION.key, selectedOperation);
		}
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

//	SOSOperationType sosoptye =	(SOSOperationType)SOSDataStoreFactory.getInstance()
//	.getCapabilities(SOSDataStoreFactory.workOnParams(params))
//	.getOperations().getOperationTypeByName((String)params.get(SOSDataStoreFactory.OPERATION.key));
//
//SOSConfigurationRegistry.getInstance().updateParameterConfiguration(((URL)params.get(SOSDataStoreFactory.URL_SERVICE.key)).toExternalForm(), operation, sosoptye.getParameterConfiguration())
////	.setParameterConfiguration(
////			SOSConfigurationRegistry.getInstance().updateParameterConfiguration(((URL)params.get(SOSDataStoreFactory.URL_SERVICE.key)).toExternalForm(), params.get(SOSDataStoreFactory.OPERATION.key), );

}
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;
import net.refractions.udig.catalog.ui.ResolveLabelProviderSimple;
import net.refractions.udig.catalog.ui.ResolveTitlesDecorator;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.eclipse.swt.widgets.Group;
import org.n52.oxf.owsCommon.capabilities.IDiscreteValueDomain;
import org.n52.oxf.util.LoggingHandler;
import org.n52.oxf.valueDomains.StringValueDomain;
import org.n52.oxf.valueDomains.time.TimePeriod;
import org.n52.oxf.valueDomains.time.TimePosition;
import org.n52.udig.catalog.internal.sos.SOSPlugin;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;
//TODO use net.refractions.udig.catalog.sos.SOSPreferencePage1 to configure this GUI
/**
 * @author 52n
 *
 */
public class SOSParameterConfigurationPage extends AbstractUDIGImportPage implements ISelectionChangedListener {

	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSParameterConfigurationPage.class);

	private ListViewer parameterValueViewer;

	private ListViewer parameterViewer;

	private Map<String, Serializable> params;

	private ResolveTitlesDecorator titleDecorator;

	private ResolveTitlesDecorator titleDecorator2;

	/**
	 * @param pageName
	 */
	public SOSParameterConfigurationPage() {
		this("ParameterConfiguration");
	}

	/**
	 * @param pageName
	 * @param title
	 * @param titleImage
	 */
	public SOSParameterConfigurationPage(final String pageName) {
		super(pageName);
		super.setTitle("Sensor Observation Service");
		super.setDescription("left: Select your parameters; right parametervalues; next button is disabled untli all parameters are configured");
	}

	private void getParameterViewer(final Composite composite){
		parameterViewer = new ListViewer(composite, SWT.MULTI);
		parameterViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));
		parameterViewer.addPostSelectionChangedListener(this);
		parameterViewer.setContentProvider(new SOSContentProvider());
		titleDecorator = new ResolveTitlesDecorator(
				new ResolveLabelProviderSimple(), true);
		final LabelProvider labelProvider = new DecoratingLabelProvider(
				titleDecorator.getSource(), titleDecorator);

		parameterViewer.setLabelProvider(labelProvider);


		SOSOperationType sosopType = null;
		try {
			sosopType = ((SOSOperationType) SOSDataStoreFactory.getInstance().getCapabilities(SOSDataStoreFactory.workOnParams(params))
					.getOperations().getOperationTypeByName(
							(String) params
									.get(SOSDataStoreFactory.OPERATION.key)));
		} catch (final IOException ioe) {
			LOGGER.error("No capabilities available", ioe);
		}
		final List<String> allParamsList = new LinkedList<String>();
		final List<String> allReqUncParams = sosopType.getParameterConfiguration().getUnconfiguredRequiredParametersAsStrings();
		final List<String> allOptionalUncParams = sosopType.getParameterConfiguration().getUnconfiguredOptionalParametersAsStrings();



		for (final String s : allReqUncParams) {
			if (!SOSConfigurationRegistry.getInstance().getOmitParameter(params.get(SOSDataStoreFactory.URL_CAPS.key).toString(), (String)params.get(SOSDataStoreFactory.OPERATION.key), s)){
				if (!sosopType.getHeartParameterName().equals(s)){
					allParamsList.add(s);
				}

			}
		}

		for (final String s : allOptionalUncParams) {
			if (!SOSConfigurationRegistry.getInstance().getOmitParameter(params.get(SOSDataStoreFactory.URL_CAPS.key).toString(), (String)params.get(SOSDataStoreFactory.OPERATION.key), s)){
				if (!sosopType.getHeartParameterName().equals(s)){
					allParamsList.add(s);
				}
			}
		}

		int i = 0;
		final String[] allParamsString = new String[allParamsList.size()];
		for (final String s : allParamsList) {
			allParamsString[i++] = s;
		}

		parameterViewer.setInput(allParamsString);

	}

	private void getParameterValueViewer(final Composite composite){
		parameterValueViewer = new ListViewer(composite, SWT.MULTI);

		parameterValueViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));


		parameterValueViewer
				.addPostSelectionChangedListener(this);
		setControl(composite);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(final Composite arg0) {
		init();
		final Composite composite = new Group(arg0, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
//		SOSSelectOperationPage prevPage = (SOSSelectOperationPage) getPreviousPage();
		getParameterViewer(composite);
		getParameterValueViewer(composite);
	}

	@Override
	protected IDialogSettings getDialogSettings() {
		return SOSPlugin.getDefault().getDialogSettings();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.refractions.udig.catalog.ui.UDIGConnectionPage#getParams()
	 */
	public Map<String, Serializable> getParams() {
		try {
			params.put(SOSDataStoreFactory.PARAMETERS.key, ((SOSOperationType) SOSDataStoreFactory.getInstance().getCapabilities(SOSDataStoreFactory.workOnParams(params))
					.getOperations().getOperationTypeByName((String) params.get(SOSDataStoreFactory.OPERATION.key))).getParameterConfiguration());
		} catch (final Exception e) {
			e.printStackTrace();
		}

		return SOSDataStoreFactory.workOnParams(params);
	}

	@Override
	public IWizardPage getPreviousPage() {
		this.dispose();
		setControl(null);
		return super.getPreviousPage();
	}

	public void init() {
		params = ((SOSSelectOperationPage) getPreviousPage()).getParameters();
	}

	@Override
	public boolean isPageComplete() {
		try {
			return ((SOSOperationType) SOSDataStoreFactory.getInstance().getCapabilities(SOSDataStoreFactory.workOnParams(params))
					.getOperations().getOperationTypeByName(
							(String) params
									.get(SOSDataStoreFactory.OPERATION.key)))
					.isOperationNearlyConfigured();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void selectionChanged(final SelectionChangedEvent event) {
		if (event.getSource().equals(parameterValueViewer)){
			try {
				final IStructuredSelection selection = (IStructuredSelection) parameterViewer
						.getSelection();
				final IStructuredSelection selection2 = (IStructuredSelection) parameterValueViewer
						.getSelection();

				if (selection == null || selection2 == null) {
					return;
				}

				final String selected = (String) selection
						.getFirstElement();
				// String selected2 =
				// (String)selection2.getFirstElement();
				final String[] selectedList2 = new String[selection2
						.toList().size()];
				int i = 0;
				for (final String s : (List<String>) selection2
						.toList()) {
					selectedList2[i++] = s;
				}

				String error = "";
				SOSOperationType sosopType = null;
				sosopType = (((SOSOperationType) SOSDataStoreFactory.getInstance().getCapabilities(SOSDataStoreFactory.workOnParams(params)).getOperations().getOperationTypeByName(
								(String) params
										.get(SOSDataStoreFactory.OPERATION.key))));

				if (selectedList2.length == 1) {
					error = sosopType.getParameterConfiguration().setParameter(selected,
							selectedList2[0]);
				} else {
					error = sosopType.getParameterConfiguration().setParameter(selected,
							selectedList2);
				}

				setErrorMessage(error);

			} catch (final Exception e) {
				e.printStackTrace();
			}
		}

		if (event.getSource().equals(parameterViewer)){
			try {
				final IStructuredSelection selection = (IStructuredSelection) parameterViewer
						.getSelection();
				if (selection == null) {
					return;
				}
				final String selected = (String) selection
						.getFirstElement();

				IDiscreteValueDomain<String> idvd = null;
				SOSOperationType sosopType = null;
				sosopType = ((SOSOperationType) SOSDataStoreFactory.getInstance().getCapabilities(SOSDataStoreFactory.workOnParams(params))
						.getOperations()
						.getOperationTypeByName(
								(String) params
										.get(SOSDataStoreFactory.OPERATION.key)));
				final org.n52.oxf.owsCommon.capabilities.Parameter pbID = sosopType.getParameterConfiguration()
						.getParameterByID(selected);

				if (pbID != null) {
					if (pbID.getValueDomain() instanceof TimePeriod){
						parameterValueViewer.setInput(null);
					}
					if (pbID.getValueDomain() instanceof TimePosition){
						parameterValueViewer.setInput(null);
					}

					if (pbID.getValueDomain() instanceof StringValueDomain) {
							idvd = (IDiscreteValueDomain<String>) pbID
									.getValueDomain();
						if (idvd != null) {
							final String[] possibleValues = new String[idvd
									.getPossibleValues().size()];
							int i = 0;

							for (final Object o : idvd
									.getPossibleValues()) {
								possibleValues[i++] = (String) o;
							}
							parameterValueViewer
									.setContentProvider(new SOSContentProvider());
							titleDecorator2 = new ResolveTitlesDecorator(
									new ResolveLabelProviderSimple(),
									true);
							final LabelProvider labelProvider2 = new DecoratingLabelProvider(
									titleDecorator.getSource(),
									titleDecorator);
							parameterValueViewer
									.setLabelProvider(labelProvider2);
							parameterValueViewer
									.setInput(possibleValues);
						}
					}
					}
			} catch (final IOException e) {
				e.printStackTrace();
			}
			}
		getWizard().getContainer().updateButtons();
		}
}
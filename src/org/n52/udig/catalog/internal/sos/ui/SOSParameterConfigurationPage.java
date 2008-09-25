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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.refractions.udig.catalog.ui.AbstractUDIGImportPage;

import org.apache.log4j.Logger;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.n52.oxf.OXFException;
import org.n52.oxf.owsCommon.capabilities.IDiscreteValueDomain;
import org.n52.oxf.owsCommon.capabilities.ITime;
import org.n52.oxf.serviceAdapters.ParameterShell;
import org.n52.oxf.util.LoggingHandler;
import org.n52.oxf.valueDomains.StringValueDomain;
import org.n52.oxf.valueDomains.time.ITimePeriod;
import org.n52.oxf.valueDomains.time.ITimePosition;
import org.n52.oxf.valueDomains.time.ITimeResolution;
import org.n52.oxf.valueDomains.time.TemporalValueDomain;
import org.n52.oxf.valueDomains.time.TimeFactory;
import org.n52.udig.catalog.internal.sos.dataStore.SOSDataStoreFactory;
import org.n52.udig.catalog.internal.sos.dataStore.SOSOperations;
import org.n52.udig.catalog.internal.sos.dataStore.config.ParameterConfiguration;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSConfigurationRegistry;
import org.n52.udig.catalog.internal.sos.dataStore.config.SOSOperationType;

public class SOSParameterConfigurationPage extends AbstractUDIGImportPage
		implements SelectionListener, ModifyListener {

	protected static boolean dirtyBit = false;

	private static final Logger LOGGER = LoggingHandler
			.getLogger(SOSParameterConfigurationPage.class);

	private Text description;

	private Composite featureIDcomposite;

	private Text featureIDInputText;

	private Label featureIDLabel;

	private Combo offeringComboBox = null;

	private TextViewer offeringTextViewer = null;
	private ParameterConfiguration paramConf;

	private Tree parameterValueViewer;

	private Tree parameterViewer;

	private SOSSelectOperationPage prevPage;

	private final Spinner[] spin1 = new Spinner[6];

	private final Spinner[] spin2 = new Spinner[6];

	private Composite timebox;

	public SOSParameterConfigurationPage() {
		this("ParameterConfiguration");
	}


	/**
	 * @param pageName
	 */
	public SOSParameterConfigurationPage(final String pageName) {
		super(pageName);
		super.setTitle("Sensor Observation Service");
		super
				.setDescription("left: Select your parameters; right parametervalues; next button is disabled untli all parameters are configured");
	}

	private String changedOfferingComboBox(){
		String error = null;
		try {
			error = paramConf.setParameterValue("offering", offeringComboBox.getItem(offeringComboBox.getSelectionIndex()));
			populateOperation();
		} catch (final Exception e) {
			LOGGER.error("An error occured while selecting an offering");
			error = e.getMessage();
		}

		return error;
	}

	private String changedParameterValueViewer() {
		String error = "";
		try {
			final String parameter = identifySelectedParameter();

			if (parameterValueViewer.getItemCount() > 0) {
				final List<String> l = new LinkedList<String>();

				for (final TreeItem titem : parameterValueViewer.getItems()) {
					if (titem.getChecked() == true) {
						l.add(titem.getText());
					}
				}

				final String[] values = new String[l.size()];
				int j = 0;
				// XXX where is the difference: checked and selection?
				for (final String s : l) {
					values[j++] = s;
				}

				if (values.length == 1) {
					error = paramConf.setParameterValue(parameter, values[0]);
				} else {
					error = paramConf.setParameterValue(parameter, values);
				}
			} else {
				return null;
			}
		} catch (final OXFException e) {
			LOGGER.error(e);
			error = e.getMessage();
		}

		return error;
	}

	private void changedParameterViewer() {
		parameterValueViewer.removeAll();

		// try {
		final String parameterS = identifySelectedParameter();
		if (parameterS == null || parameterS.trim().equals("")
				|| parameterS.equalsIgnoreCase(("optional"))
				|| parameterS.equalsIgnoreCase("required")) {
			parameterValueViewer.removeAll();
			return;
		}

		IDiscreteValueDomain<String> idvd = null;

		final org.n52.oxf.owsCommon.capabilities.Parameter pbID = paramConf.getParameterByID(parameterS);

		final List<String> selectedValues = new LinkedList<String>();

		int i = 0;

		if (pbID != null) {
			if (pbID.getValueDomain() instanceof TemporalValueDomain) {
				parameterValueViewer.removeAll();
				// parameterValueViewer.setEnabled(false);
				final TemporalValueDomain tvd = (TemporalValueDomain) pbID
						.getValueDomain();
				final List<ITime> possibleValues = tvd.getPossibleValues();
				String text = "";
				for (final ITime t : possibleValues) {
					text += (t.toString());
				}
				description.setText(text);
				if (possibleValues.get(0) instanceof ITimePeriod) {
					final ITimePeriod timePeriod = (ITimePeriod) possibleValues
							.get(0);
					for (final ITime t : possibleValues) {

						if ((!t.equals(timePeriod))
								&& (t instanceof ITimePosition || t instanceof ITimeResolution)) {
							LOGGER
									.warn("Setting different temporal valuedomains for a single parameter not supported by this GUI");
							return;
						}
						timebox.setEnabled(true);
						timebox.setVisible(true);
						enableTimeLine2(true);

						spin1[0].setSelection(timePeriod.getStart().getDay());
						spin1[1].setSelection(timePeriod.getStart().getMonth());
						spin1[2].setSelection(Integer.parseInt(""
								+ timePeriod.getStart().getYear()));
						spin1[3].setSelection(timePeriod.getStart().getHour());
						spin1[4]
								.setSelection(timePeriod.getStart().getMinute());

						// spin1[5].setSelection(Integer.parseInt(""+timePeriod.getStart().getSecond()));

						spin2[0].setSelection(timePeriod.getEnd().getDay());
						spin2[1].setSelection(timePeriod.getEnd().getMonth());
						spin2[2].setSelection(Integer.parseInt(""
								+ timePeriod.getEnd().getYear()));
						spin2[3].setSelection(timePeriod.getEnd().getHour());
						spin2[4].setSelection(timePeriod.getEnd().getMinute());

						// spin2[5].setSelection(Integer.parseInt(""+timePeriod.getEnd().getSecond()));
					}
				} else {
					LOGGER.warn("NOT SUPPORTED");
				}
			}

			// check if a Parameter needs to be checked in Viewer
			if (pbID.getValueDomain() instanceof StringValueDomain) {
				final ParameterShell ps = paramConf.getConfiguredParameterContainer()
				.getParameterShellWithServiceSidedName(
						pbID.getServiceSidedName());

				if (ps != null) {
					if (ps.hasMultipleSpecifiedValues()) {
						for (final Object o : ps.getSpecifiedValueArray()) {
							selectedValues.add((String) o);
						}
					} else if (ps.hasSingleSpecifiedValue()) {
						selectedValues.add((String) ps.getSpecifiedValue());
					}
				}
				parameterValueViewer.setEnabled(true);
				idvd = (IDiscreteValueDomain<String>) pbID.getValueDomain();
				if (idvd != null) {
					i = 0;
					parameterValueViewer.setItemCount(idvd.getPossibleValues().size());
					String s = null;

					if (identifyOperation().getId().equals(SOSOperations.opName_GetObservation)){
						final String offeringS = identifySelectedOffering();
						for (final Object o : idvd.getPossibleValues()) {
							s = (String) o;
							if (paramConf.isValueAllowedInOffering(s, offeringS)){
								if (selectedValues.contains(s)) {
									parameterValueViewer.getItem(i).setChecked(true);
								} else {
									parameterValueViewer.getItem(i).setChecked(false);
								}
								parameterValueViewer.getItem(i++).setText(s);
							} else {
								parameterValueViewer.setItemCount(parameterValueViewer.getItemCount()-1);
							}
						}
					} else{
						for (final Object o : idvd.getPossibleValues()) {
							s = (String) o;
							if (selectedValues.contains(s)) {
								parameterValueViewer.getItem(i)
										.setChecked(true);
							} else {
								parameterValueViewer.getItem(i).setChecked(
										false);
							}
							parameterValueViewer.getItem(i++).setText(s);

						}
					}
				}
			}
		} else {
			parameterValueViewer.removeAll();
		}
		// } catch (final IOException e) {
		// parameterValueViewer.removeAll();
		// LOGGER.error(e);
		// }
	}

	public void createBasicControl(final Composite arg0) {
		final Composite composite1 = new Group(arg0, SWT.NULL);
		composite1.setLayout(new GridLayout(1, false));
		composite1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite composite2 = new Composite(composite1, SWT.NULL);
		composite2.setLayout(new GridLayout(1, true));
		composite2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite composite = new Composite(composite1, SWT.NULL);
		composite.setLayout(new GridLayout(2, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		// SOSSelectOperationPage prevPage = (SOSSelectOperationPage)
		// getPreviousPage();
		getOfferingDropdown(composite2);
//		getOfferingDescription(composite2);
		getParameterViewer(composite);
		getParameterValueViewer(composite);
		getFeatureIDInput(composite);
		getTimeViewer(composite1);
		getDescriptionViewer(composite1);
		setControl(composite1);

//		if (!identifyOperation().equals("GetObservation")){
//			composite2.setVisible(false);
//		}

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(final Composite arg0) {
		init();
		createBasicControl(arg0);
		operationChanged();
	}

	private void enableTimeLine2(final boolean b) {
		for (final Spinner s : spin2) {
			s.setEnabled(b);
		}
	}

	private void getDescriptionViewer(final Composite composite) {
		description = new Text(composite, SWT.SINGLE);
		description
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}

	private void getFeatureIDInput(final Composite arg0) {
		featureIDcomposite = new Group(arg0, SWT.NULL);
		featureIDcomposite.setLayout(new GridLayout(2, false));
		featureIDLabel = new Label(featureIDcomposite, SWT.NONE);
		featureIDLabel.setText("FeatureID:");
		featureIDInputText = new Text(featureIDcomposite, SWT.SINGLE);
		featureIDInputText.setText("");
		featureIDInputText.setToolTipText("This text needs to be externalized");
		featureIDInputText.addModifyListener(this);
		if (getParams().get(SOSDataStoreFactory.OPERATION.key).equals(
				SOSOperations.opName_GetObservationById)) {
			featureIDLabel.setVisible(true);
			featureIDInputText.setVisible(true);
			featureIDcomposite.setVisible(true);
		} else {
			featureIDLabel.setVisible(false);
			featureIDInputText.setVisible(false);
			featureIDcomposite.setVisible(false);
		}
	}
	private void getOfferingDescription(final Composite composite){
		offeringTextViewer = new TextViewer(composite, SWT.MULTI | SWT.V_SCROLL
				| SWT.H_SCROLL);
		offeringTextViewer.getControl().setLayoutData(
				new GridData(SWT.FILL, SWT.FILL, true, true));

//		offeringTextViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
//				false));
//		offeringTextViewer.addSelectionListener(this);
	}

	private void getOfferingDropdown(final Composite composite){
		offeringComboBox = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		offeringComboBox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		offeringComboBox.addSelectionListener(this);
		offeringComboBox.setVisible(false);

	}

	private void getParameterValueViewer(final Composite composite) {
		parameterValueViewer = new Tree(composite, SWT.MULTI | SWT.CHECK);
		parameterValueViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
				true, true));
		parameterValueViewer.addSelectionListener(this);
	}

	private void getParameterViewer(final Composite composite) {
		parameterViewer = new Tree(composite, SWT.MULTI);
		parameterViewer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false));
		parameterViewer.addSelectionListener(this);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see net.refractions.udig.catalog.ui.UDIGConnectionPage#getParams()
	 */
	public Map<String, Serializable> getParams() {
		((SOSSelectOperationPage) getPreviousPage()).getParameters().put(
				SOSDataStoreFactory.PARAMETERS.key, paramConf);
		return SOSDataStoreFactory
				.workOnParams(((SOSSelectOperationPage) getPreviousPage())
						.getParameters());
	}

	private StringBuffer getTimeLine1(final StringBuffer timeS) {
		final int day1 = spin1[0].getSelection();
		final int month1 = spin1[1].getSelection();
		final int year1 = spin1[2].getSelection();
		final int hour1 = spin1[3].getSelection();
		final int minute1 = spin1[4].getSelection();
		final int second1 = spin1[5].getSelection();

		timeS.append(year1);
		timeS.append('-');
		timeS.append(month1);
		timeS.append('-');
		timeS.append(day1);
		timeS.append('T');
		timeS.append(hour1);
		timeS.append(':');
		timeS.append(minute1);
		timeS.append(':');
		timeS.append(second1);
		return timeS;
	}

	private StringBuffer getTimeLine2(final StringBuffer timeS) {
		final int day2 = spin2[0].getSelection();
		final int month2 = spin2[1].getSelection();
		final int year2 = spin2[2].getSelection();
		final int hour2 = spin2[3].getSelection();
		final int minute2 = spin2[4].getSelection();
		final int second2 = spin2[5].getSelection();

		// timeS.append('/');
		timeS.append(year2);
		timeS.append('-');
		timeS.append(month2);
		timeS.append('-');
		timeS.append(day2);
		timeS.append('T');
		timeS.append(hour2);
		timeS.append(':');
		timeS.append(minute2);
		timeS.append(':');
		timeS.append(second2);
		return timeS;
	}

	private void getTimeViewer(final Composite composite) {
		timebox = new Composite(composite, SWT.None);
		timebox.setLayout(new GridLayout(10, false));
		timebox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		timebox.setVisible(false);

		// headers
		new Label(timebox, SWT.NONE).setText("");
		new Label(timebox, SWT.NONE).setText("day");
		new Label(timebox, SWT.NONE).setText("month");
		new Label(timebox, SWT.NONE).setText("year");
		new Label(timebox, SWT.NONE).setText("");
		new Label(timebox, SWT.NONE).setText("hour");
		new Label(timebox, SWT.NONE).setText("");
		new Label(timebox, SWT.NONE).setText("minute");
		new Label(timebox, SWT.NONE).setText("");
		new Label(timebox, SWT.NONE).setText("second");

		// line 1
		new Label(timebox, SWT.NONE).setText("");
		spin1[0] = new Spinner(timebox, SWT.WRAP);
		spin1[1] = new Spinner(timebox, SWT.WRAP);
		spin1[2] = new Spinner(timebox, SWT.WRAP);
		new Label(timebox, SWT.NONE).setText("-");
		spin1[3] = new Spinner(timebox, SWT.WRAP);
		new Label(timebox, SWT.NONE).setText(":");
		spin1[4] = new Spinner(timebox, SWT.WRAP);
		new Label(timebox, SWT.NONE).setText(":");
		spin1[5] = new Spinner(timebox, SWT.WRAP);

		// line 2
		new Label(timebox, SWT.NONE).setText("");
		spin2[0] = new Spinner(timebox, SWT.WRAP);
		spin2[1] = new Spinner(timebox, SWT.WRAP);
		spin2[2] = new Spinner(timebox, SWT.WRAP);
		new Label(timebox, SWT.NONE).setText("-");
		spin2[3] = new Spinner(timebox, SWT.WRAP);
		new Label(timebox, SWT.NONE).setText(":");
		spin2[4] = new Spinner(timebox, SWT.WRAP);
		new Label(timebox, SWT.NONE).setText(":");
		spin2[5] = new Spinner(timebox, SWT.WRAP);

		spin1[0].setValues(1, 1, 31, 0, 1, 1);
		spin2[0].setValues(1, 1, 31, 0, 1, 1);

		spin1[1].setValues(1, 1, 12, 0, 1, 1);
		spin2[1].setValues(1, 1, 12, 0, 1, 1);

		spin1[2].setValues(2000, 0, 3000, 0, 1, 1);
		spin2[2].setValues(2000, 0, 3000, 0, 1, 1);

		spin1[3].setValues(0, 0, 23, 0, 1, 1);
		spin2[3].setValues(0, 0, 23, 0, 1, 1);

		spin1[4].setValues(0, 0, 60, 0, 1, 1);
		spin2[4].setValues(0, 0, 59, 0, 1, 1);

		spin1[5].setValues(0, 0, 59, 0, 1, 1);
		spin2[5].setValues(0, 0, 59, 0, 1, 1);

		for (final Spinner s : spin1) {
			s.addSelectionListener(this);
		}
		for (final Spinner s : spin2) {
			s.addSelectionListener(this);
		}
	}

	private SOSOperationType identifyOperation(){
		SOSOperationType sosopType = null;
		try {
			sosopType = ((SOSOperationType) SOSDataStoreFactory
					.getInstance()
					.getCapabilities(
							SOSDataStoreFactory
									.workOnParams(((SOSSelectOperationPage) getPreviousPage())
											.getParameters()))
					.getOperations()
					.getOperationTypeByName(
							(String) ((SOSSelectOperationPage) getPreviousPage())
									.getParameters().get(
											SOSDataStoreFactory.OPERATION.key)));
		} catch (final IOException ioe) {
			setErrorMessage("No capabilities available");
			LOGGER.error("No capabilities available", ioe);
		}
		return sosopType;
	}

	private String identifySelectedOffering() {
		return offeringComboBox.getItem(offeringComboBox.getSelectionIndex());
	}

	private String identifySelectedParameter() {
		if (parameterViewer.getSelection() != null && parameterViewer.getSelectionCount() > 0
				&& parameterViewer.getSelection()[0] != null) {
			if (parameterViewer.getSelection()[0].getItemCount() == 0) {
				return parameterViewer.getSelection()[0].getText();
			}
		}

		return null;
	}

	public void init() {
		prevPage = ((SOSSelectOperationPage) getPreviousPage());
		// ((SOSSelectOperationPage)getPreviousPage()).getParameters() =
		// ((SOSSelectOperationPage) getPreviousPage()).getParameters();
	}

	@Override
	public boolean isPageComplete() {
		if (dirtyBit) {
			operationChanged();
		}
		try {
			if (getParams().get(SOSDataStoreFactory.OPERATION.key).equals(
					SOSOperations.opName_GetObservationById)) {
				if (featureIDInputText.getText().trim().length() == 0) {
					return false;
				}
			}
			return ((SOSOperationType) SOSDataStoreFactory
					.getInstance()
					.getCapabilities(
							SOSDataStoreFactory
									.workOnParams(((SOSSelectOperationPage) getPreviousPage())
											.getParameters()))
					.getOperations()
					.getOperationTypeByName(
							(String) ((SOSSelectOperationPage) getPreviousPage())
									.getParameters().get(
											SOSDataStoreFactory.OPERATION.key)))
					.isOperationNearlyConfigured(paramConf);
		} catch (final IOException e) {
			setErrorMessage(e.getMessage());
			LOGGER.error(e);
		}
		return false;
	}

	public void modifyText(final ModifyEvent e) {
		if (e.widget instanceof Text) {
			final Text t = (Text) e.widget;
			if (t.equals(featureIDInputText)) {
				String error = "";
				if (t.getText().trim().length() > 0) {
					try {
						error = paramConf.setParameterValue("ObservationId", t
								.getText().trim());
						setErrorMessage(error);
						getWizard().getContainer().updateButtons();
					} catch (final OXFException oex) {
						setErrorMessage(oex.getMessage());
						LOGGER.error(oex);
					}
				}
			}
		}
	}

	private void operationChanged() {
		try {
			paramConf = identifyOperation().getNewPreconfiguredConfiguration();
			// this is a good time to configure the
			// operation with SOSConfigurationRegistry
			paramConf = SOSConfigurationRegistry
					.getInstance()
					.updateParameterConfiguration(
							((URL) ((SOSSelectOperationPage) getPreviousPage())
									.getParameters()
									.get(SOSDataStoreFactory.URL_SERVICE.key))
									.toExternalForm(),
							(String) ((SOSSelectOperationPage) getPreviousPage())
									.getParameters().get(
											SOSDataStoreFactory.OPERATION.key),
							paramConf);
		} catch (final Exception e) {
			setErrorMessage(e.getMessage());
			LOGGER.error(e);
			// e.printStackTrace();
		}

		if (getParams().get(SOSDataStoreFactory.OPERATION.key).equals(
				SOSOperations.opName_GetObservation)) {
			offeringComboBox.setVisible(true);
		}
		if (getParams().get(SOSDataStoreFactory.OPERATION.key).equals(
				SOSOperations.opName_GetObservationById)) {
			featureIDcomposite.setVisible(true);
			featureIDLabel.setVisible(true);
			featureIDInputText.setVisible(true);
		} else {
			featureIDcomposite.setVisible(false);
			featureIDLabel.setVisible(false);
			featureIDInputText.setVisible(false);
		}

//		populateOperation();
		if (identifyOperation().getId().equals("GetObservation")){
			populateOffering();
		} else{
			populateOperation();
		}


		dirtyBit = false;
	}

	protected void populateOffering(){
		offeringComboBox.removeAll();

		final SOSOperationType sosopType = identifyOperation();

		final List<String> offerings = sosopType.getCapabilitiesConfiguration().getOfferingsFromContents();
		for (final String s : offerings){
			offeringComboBox.add(s);
		}
//		populateOperation();
	}

	protected void populateOperation() {

		parameterViewer.removeAll();
		parameterValueViewer.removeAll();
		parameterViewer.setItemCount(2);

		final SOSOperationType sosopType = identifyOperation();

		final List<String> lReq = new LinkedList<String>();

		int i = 0;
		List<String> listReq = null;


		if (sosopType.getId().equals(SOSOperations.opName_GetObservation)){
			listReq = paramConf.getUnconfiguredRequiredParametersAsStringsForOffering(identifySelectedOffering());
		} else{
			listReq = paramConf.getUnconfiguredRequiredParametersAsStrings();
		}
		for (final String s : listReq) {

			if (!SOSConfigurationRegistry.getInstance().getOmitParameter(
					((SOSSelectOperationPage) getPreviousPage())
							.getParameters().get(
									SOSDataStoreFactory.URL_SERVICE.key)
							.toString(),
					(String) ((SOSSelectOperationPage) getPreviousPage())
							.getParameters().get(
									SOSDataStoreFactory.OPERATION.key), s)) {
				if (!sosopType.getNewPreconfiguredConfiguration()
						.getHeartParameterName().equals(s)) {
					lReq.add(s);
				}
			}
		}

		parameterViewer.getItem(0).setText("Required");
		parameterViewer.getItem(0).setItemCount(lReq.size());
		i = 0;
		for (final String s : lReq) {
			parameterViewer.getItem(0).getItem(i++).setText(s);
		}


		List<String> listOpt = null;
		final List<String> lOpt = new LinkedList<String>();

		if (sosopType.getId().equals(SOSOperations.opName_GetObservation)){
			listOpt = paramConf.getUnconfiguredOptionalParametersAsStringsForOffering(identifySelectedOffering());
		} else{
			listOpt = paramConf.getUnconfiguredOptionalParametersAsStrings();
		}

		for (final String s : listOpt) {
			if (!SOSConfigurationRegistry.getInstance().getOmitParameter(
					((SOSSelectOperationPage) getPreviousPage())
							.getParameters().get(
									SOSDataStoreFactory.URL_SERVICE.key)
							.toString(),
					(String) ((SOSSelectOperationPage) getPreviousPage())
							.getParameters().get(
									SOSDataStoreFactory.OPERATION.key), s)) {
				if (!sosopType.getNewPreconfiguredConfiguration()
						.getHeartParameterName().equals(s)) {
					lOpt.add(s);
				}
			}
		}
		parameterViewer.getItem(1).setText("Optional");
		parameterViewer.getItem(1).setItemCount(lOpt.size());
		i = 0;
		for (final String s : lOpt) {
			parameterViewer.getItem(1).getItem(i++).setText(s);
		}
	}

	public void widgetDefaultSelected(final SelectionEvent e) {
		// TODO Auto-generated method stub
	}

	public void widgetSelected(final SelectionEvent event) {
		String error = "";
		if (event.widget instanceof Combo) {
			final Combo eCombo = (Combo) event.widget;
			if (eCombo.equals(offeringComboBox)){
				error = changedOfferingComboBox();
				setErrorMessage(error);
			}
		} else if (event.widget instanceof Tree) {
			final Tree eTree = (Tree) event.widget;
			if (eTree.equals(parameterValueViewer)) {
				error = changedParameterValueViewer();
				setErrorMessage(error);
			} else if (event.getSource().equals(parameterViewer)) {
				changedParameterViewer();
			}
		} else if (event.widget instanceof Spinner) {
			// Take care, i make no difference between the spinners
			try {
				final org.n52.oxf.owsCommon.capabilities.Parameter parameter = paramConf
						.getParameterByID(identifySelectedParameter());
				final TemporalValueDomain tvd = (TemporalValueDomain) parameter
						.getValueDomain();
				final List<ITime> possibleValues = tvd.getPossibleValues();

				String text = "";

				for (final ITime t : possibleValues) {
					text += (t.toString());
				}
				description.setText(text);

				if (possibleValues.get(0) instanceof ITimePeriod) {
					final ITimePeriod timePeriod = (ITimePeriod) possibleValues
							.get(0);
					for (final ITime t : possibleValues) {

						if ((!t.equals(timePeriod))
								&& (t instanceof ITimePosition || t instanceof ITimeResolution)) {
							LOGGER
									.warn("Setting different temporal valuedomains for a single parameter not supported by this GUI");
							return;
						}
					}
					StringBuffer timeS = new StringBuffer(39);

					timeS = getTimeLine1(timeS);
					timeS.append("/");
					timeS = getTimeLine2(timeS);

					final ITime time = TimeFactory.createTime(timeS.toString());

					if (time instanceof ITimePeriod) {
						final ITimePeriod t2 = (ITimePeriod) time;
						if (t2.getStart().after(t2.getEnd())) {
							error = t2.getStart() + " is after " + t2.getEnd();
						} else {
							error = paramConf.setParameterValue(
									identifySelectedParameter(), time);
						}
					}
				}

			} catch (final Exception e) {
				LOGGER.error(e);
				e.printStackTrace();
			}

		}

		if (error == null) {
			setErrorMessage(null);
		} else if (!error.equals("")) {
			setErrorMessage(error);
		}
		getWizard().getContainer().updateButtons();

	}
}
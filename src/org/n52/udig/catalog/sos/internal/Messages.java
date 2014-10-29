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
package org.n52.udig.catalog.sos.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.n52.udig.catalog.sos.internal.messages"; //$NON-NLS-1$
	public static String SOSDataStoreFactory_desc;
	public static String SOSDataStoreFactory_name;
	
	public static String SOSServiceExtension_protocolfailure;
	public static String SOSServiceExtension_badService;
	public static String SOSServiceExtension_nullURL;

	public static String SOSServiceImpl_broken;
	public static String SOSServiceImpl_task_name;

	public static String SOSWizardPage_wizarddescription;
	public static String SOSWizardPage_main_title;
	public static String SOSWizardPage_label_url_text;
	public static String SOSWizardPage_serverConnectionError;
	public static String SOSWizardPage_connectionProblem;
	public static String SOSWizardPage_error_invalidURL;
	public static String SOSWizardPage_label_url_tooltip;
	public static String SOSWizardPage_version_text;
	public static String SOSWizardPage_version_text_tooltipp;
	
	public static String SOSServiceImpl_connecting_to;
	public static String SOSServiceImpl_could_not_connect;
	
	public static String SOSParameterConfigurationPage_label_reqParams;
	
	public static String SOSPreferencePage1_show_operation_tooltipp;
	public static String SOSPreferencePage1_select_parameter_tooltipp;
	public static String SOSPreferencePage1_omit_parameter_tooltipp;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}

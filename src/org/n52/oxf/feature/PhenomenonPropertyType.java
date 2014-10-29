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
package org.n52.oxf.feature;

import org.n52.oxf.feature.dataTypes.OXFPhenomenonPropertyType;

public class PhenomenonPropertyType extends OXFPhenomenonPropertyType implements
		Duplicatable {

	public PhenomenonPropertyType(final String urn) {
		super(urn);
	}

	public PhenomenonPropertyType(final String urn, final String uom) {
		super(urn, uom);
	}

	public PhenomenonPropertyType(final OXFPhenomenonPropertyType ppt) {
		this(ppt.getURN(), ppt.getUOM());
	}

	@Override
	public String toString() {
		String out = "";
		if (super.getURN() != null) {
			out += super.getURN();
		}
		// if (super.getUOM() != null) {
		// if (!out.equals("")) {
		// out += " ";
		// }
		// out += super.getUOM();
		// }
		return out;
	}

	public PhenomenonPropertyType duplicate() {
		if (super.getUOM() == null) {
			return new PhenomenonPropertyType(super.getURN());
		}
		return new PhenomenonPropertyType(super.getURN(), super.getUOM());
	}
}

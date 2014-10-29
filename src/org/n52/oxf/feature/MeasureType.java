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

import org.n52.oxf.feature.dataTypes.OXFMeasureType;

/**
 * An extended version of the OXFMeasureType delivered by the OXFramework.
 * Changed the .toString(), .equals(...) and added the Duplicatable interface.
 * 
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */

public class MeasureType extends OXFMeasureType implements Duplicatable {

	public int hashCode() {
		return 0;
	}

	/**
	 * Creates a new instance of {@link MeasureType}. Just calls super.
	 * 
	 * @param uomIdentifier
	 * @param value
	 */
	public MeasureType(final String uomIdentifier, final double value) {
		super(uomIdentifier, value);
	}

	@Override
	public String toString() {
		String out = "";
		if (super.getValue() != null) {
			out = out + super.getValue();
		}
		if (super.getUomIdentifier() != null
				&& !super.getUomIdentifier().equals("")) {
			if (!out.equals("")) {
				out = out + " : ";
			}
			out = out + super.getUomIdentifier();
		}
		return out;
	}

	/**
	 * Creates a new instance of {@link MeasureType} from an instance of
	 * {@link OXFMeasureType}
	 * 
	 * @param measuretype
	 */
	public MeasureType(final OXFMeasureType measuretype) {
		this(measuretype.getUomIdentifier(), measuretype.getValue());
	}

	public MeasureType duplicate() {
		return new MeasureType(getUomIdentifier(), getValue());
	}

	@Override
	public boolean equals(final Object obj) {
		// TODO quality!?
		if (obj instanceof MeasureType) {
			final MeasureType mtint = (MeasureType) obj;
			if (super.getUomIdentifier() == null) {
				if (mtint.getUomIdentifier() == null) {
					return true;
				} else {
					return false;
				}
			}
			if (super.getUomIdentifier().equals(mtint.getUomIdentifier())
					&& super.getValue().equals(mtint.getValue())) {
				return true;
			}
		}
		return false;
	}
}
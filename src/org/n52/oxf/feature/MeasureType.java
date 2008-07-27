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

Author: Arne Broering, Carsten Priess
Created: 02.04.2008
 *********************************************************************************/
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
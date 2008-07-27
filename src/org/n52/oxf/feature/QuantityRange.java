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
Created: 23.05.2008
 *********************************************************************************/
package org.n52.oxf.feature;

public class QuantityRange extends OXFQuantityRange implements IQuantity,
		Duplicatable {

	public QuantityRange(final OXFQuantityRange oqr) {
		super(oqr.getUomIdentifier(), oqr.getValue()[0], oqr.getValue()[1], oqr
				.getType());
	}

	public QuantityRange(final String uomIdentifier, final double min,
			final double max, final String type) {
		super(uomIdentifier, min, max, type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.oxf.feature.Duplicatable#duplicate()
	 */
	public Object duplicate() {
		// immuteable
		return this;
	}

	@Override
	public String toString() {
		return new StringBuffer().append("[QuantityRange [uom: ").append(
				super.getUomIdentifier()).append("] [min: ").append(
				super.getValue()[0]).append("] [max: ").append(
				super.getValue()[1]).append("]").toString();
	}

}

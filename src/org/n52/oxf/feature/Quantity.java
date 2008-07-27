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

/**
 * @author 52n
 * 
 */
public class Quantity extends OXFQuantity implements Duplicatable, IQuantity {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.n52.oxf.feature.Duplicatable#duplicate()
	 */
	public Object duplicate() {
		// immuteable
		return this;
	}

	public Quantity(final OXFQuantity oxfq) {
		super(oxfq.getUomIdentifier(), oxfq.getValue(), oxfq.getType());
	}

	public Quantity(final String uomIdentifier, final double value,
			final String type) {
		super(uomIdentifier, value, type);
	}

	@Override
	public String toString() {
		return new StringBuffer().append("[QuantityRange [urn: ").append(
				super.getUomIdentifier()).append("] [val: ").append(
				super.getValue()).append("]").toString();
	}
}

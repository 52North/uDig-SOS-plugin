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

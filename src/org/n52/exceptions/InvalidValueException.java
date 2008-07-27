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
Created: 20.03.2008
 *********************************************************************************/
package org.n52.exceptions;

/**
 * @author <a href="mailto:priess@52north.org">Carsten Priess</a>
 * 
 */
public class InvalidValueException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 71661129274072469L;

	/**
	 * 
	 */
	public InvalidValueException() {
		super();
	}

	/**
	 * @param message
	 */
	public InvalidValueException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public InvalidValueException(final Throwable cause) {
		super(cause);
	}

	/**
	 * @param message
	 * @param cause
	 */
	public InvalidValueException(final String message, final Throwable cause) {
		super(message, cause);
	}

}

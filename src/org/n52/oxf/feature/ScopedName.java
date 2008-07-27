/**
 * 
 */
package org.n52.oxf.feature;

import org.n52.oxf.feature.dataTypes.OXFScopedName;

/**
 * @author 52n
 * 
 */
public class ScopedName extends OXFScopedName implements Duplicatable {

	/**
	 * @param codeSpace
	 * @param value
	 */
	public ScopedName(final String codeSpace, final String value) {
		super(codeSpace, value);
	}
	
	

	public Object duplicate() {
		// immutable -> return current object
		return this;
	}

	public ScopedName(final OXFScopedName in) {
		this(in.getCodeSpace(), in.getValue());
	}

	@Override
	public String toString() {
		// StringBuffer sb = new StringBuffer("[Codespace: ");
		// sb.append(getCodeSpace());
		// sb.append(" [Value: ");
		// sb.append(getValue());
		// sb.append("]");
		// return sb.toString();
		return getValue();

	}

}

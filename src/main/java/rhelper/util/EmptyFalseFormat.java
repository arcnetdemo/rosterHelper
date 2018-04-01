/**
 * 
 */
package rhelper.util;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

/**
 * @author akira
 *
 */
public class EmptyFalseFormat extends Format {

	/** serialVersionUID */
	private static final long serialVersionUID = 1L;

	/*
	 * (非 Javadoc)
	 * 
	 * @see java.text.Format#format(java.lang.Object, java.lang.StringBuffer,
	 * java.text.FieldPosition)
	 */
	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo,
			FieldPosition pos) {
		if (!Boolean.FALSE.equals(obj)) {
			toAppendTo.append("出");
		}
		return toAppendTo;
	}

	/*
	 * (非 Javadoc)
	 * 
	 * @see java.text.Format#parseObject(java.lang.String,
	 * java.text.ParsePosition)
	 */
	@Override
	public Object parseObject(String source, ParsePosition pos) {
		if (source == null || source.length() <= 0) {
			return Boolean.FALSE;
		}
		pos.setIndex(pos.getIndex() + source.length());
		return Boolean.TRUE;
	}

}

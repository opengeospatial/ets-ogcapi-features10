package org.opengis.cite.ogcapifeatures10.exception;

import org.testng.SkipException;

/**
 * <p>
 * UnknownCrsException class.
 * </p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class UnknownCrsException extends SkipException {

	/**
	 * <p>
	 * Constructor for UnknownCrsException.
	 * </p>
	 * @param skipMessage a {@link java.lang.String} object
	 */
	public UnknownCrsException(String skipMessage) {
		super(skipMessage);
	}

}

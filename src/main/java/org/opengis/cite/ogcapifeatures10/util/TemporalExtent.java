package org.opengis.cite.ogcapifeatures10.util;

import java.time.ZonedDateTime;

/**
 * <p>TemporalExtent class.</p>
 *
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TemporalExtent {

	private ZonedDateTime begin;

	private ZonedDateTime end;

	/**
	 * <p>Constructor for TemporalExtent.</p>
	 *
	 * @param begin a {@link java.time.ZonedDateTime} object
	 * @param end a {@link java.time.ZonedDateTime} object
	 */
	public TemporalExtent(ZonedDateTime begin, ZonedDateTime end) {
		this.begin = begin;
		this.end = end;
	}

	/**
	 * <p>Getter for the field <code>begin</code>.</p>
	 *
	 * @return a {@link java.time.ZonedDateTime} object
	 */
	public ZonedDateTime getBegin() {
		return begin;
	}

	/**
	 * <p>Getter for the field <code>end</code>.</p>
	 *
	 * @return a {@link java.time.ZonedDateTime} object
	 */
	public ZonedDateTime getEnd() {
		return end;
	}

}

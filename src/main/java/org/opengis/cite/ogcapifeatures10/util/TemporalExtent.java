package org.opengis.cite.ogcapifeatures10.util;

import java.time.ZonedDateTime;

/**
 * @author <a href="mailto:goltz@lat-lon.de">Lyn Goltz </a>
 */
public class TemporalExtent {

    private ZonedDateTime begin;

    private ZonedDateTime end;

    public TemporalExtent( ZonedDateTime begin, ZonedDateTime end ) {
        this.begin = begin;
        this.end = end;
    }

    public ZonedDateTime getBegin() {
        return begin;
    }

    public ZonedDateTime getEnd() {
        return end;
    }

}
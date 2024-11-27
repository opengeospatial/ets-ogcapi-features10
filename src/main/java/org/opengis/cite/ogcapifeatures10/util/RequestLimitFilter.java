package org.opengis.cite.ogcapifeatures10.util;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

/**
 * <p>RequestLimitFilter class.</p>
 *
 * @author Benjamin Pross (b.pross @52north.org)
 *
 * This class sets a limit for certain request to collection items. If a limit parameter
 * is already set, nothing is done.
 */
public class RequestLimitFilter implements Filter {

	private static String ITEMS = "items";

	private static String LIMIT = "limit";

	/** {@inheritDoc} */
	@Override
	public Response filter(FilterableRequestSpecification requestSpec, FilterableResponseSpecification responseSpec,
			FilterContext ctx) {
		// make sure that the limit is set only for requests to collection items
		if (requestSpec.getURI().endsWith(ITEMS) || requestSpec.getURI().endsWith(ITEMS + "/")
				|| requestSpec.getURI().contains(ITEMS + "?")) {
			// do nothing if a limit was already specified
			if (!(requestSpec.getQueryParams().containsKey(LIMIT)
					|| requestSpec.getRequestParams().containsKey(LIMIT))) {
				requestSpec.queryParam(LIMIT, 10);
			}
		}
		return ctx.next(requestSpec, responseSpec);
	}

}

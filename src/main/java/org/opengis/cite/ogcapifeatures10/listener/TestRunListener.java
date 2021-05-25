package org.opengis.cite.ogcapifeatures10.listener;

import org.testng.IExecutionListener;

/**
 * A listener that is invoked before and after a test run. It is often used to
 * configure a shared fixture that endures for the duration of the entire test
 * run. A FixtureManager may be used to manage such a fixture.
 *
 * <p>A shared fixture should be used with caution in order to avoid undesirable
 * test interactions. In general, it should be populated with "read-only"
 * objects that are not modified during the test run.</p>
 *
 * @see com.occamlab.te.spi.executors.FixtureManager FixtureManager
 *
 */
public class TestRunListener implements IExecutionListener {

    @Override
    public void onExecutionStart() {
    }

    @Override
    public void onExecutionFinish() {
    }
}

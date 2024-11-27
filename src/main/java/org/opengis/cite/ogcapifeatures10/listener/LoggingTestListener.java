package org.opengis.cite.ogcapifeatures10.listener;

import java.util.logging.Logger;

import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

/**
 * <p>LoggingTestListener class.</p>
 *
 */
public class LoggingTestListener extends TestListenerAdapter {

	private static final Logger LOGR = Logger.getLogger(LoggingTestListener.class.getName());

	/** {@inheritDoc} */
	@Override
	public void onTestStart(ITestResult result) {
		super.onTestStart(result);
		ITestNGMethod testMethod = result.getMethod();
		LOGR.info("Invoking test " + testMethod.getMethodName() + ": " + testMethod.getDescription());
	}

	/** {@inheritDoc} */
	@Override
	public void onTestFailure(ITestResult tr) {
		super.onTestFailure(tr);
		LOGR.info(tr.getMethod().getMethodName() + " failed");
	}

	/** {@inheritDoc} */
	@Override
	public void onTestSkipped(ITestResult tr) {
		super.onTestSkipped(tr);
		LOGR.info(tr.getMethod().getMethodName() + " was skipped");
	}

	/** {@inheritDoc} */
	@Override
	public void onTestSuccess(ITestResult tr) {
		super.onTestSuccess(tr);
		LOGR.info(tr.getMethod().getMethodName() + " passed");
	}

}

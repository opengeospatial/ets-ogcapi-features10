package org.opengis.cite.ogcapifeatures10.listener;

import java.util.logging.Logger;

import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.TestListenerAdapter;

public class LoggingTestListener extends TestListenerAdapter {
  
  private static final Logger LOGR = Logger.getLogger(LoggingTestListener.class.getName());
  
  @Override
  public void onTestStart(ITestResult result) {
    super.onTestStart(result);
    ITestNGMethod testMethod = result.getMethod();
    LOGR.info("Invoking test " + testMethod.getMethodName() + ": " + testMethod.getDescription());
  }
  
  @Override
  public void onTestFailure(ITestResult tr) {
    super.onTestFailure(tr);
    LOGR.info(tr.getMethod().getMethodName() + " failed");
  }
  
  @Override
  public void onTestSkipped(ITestResult tr) {
    super.onTestSkipped(tr);
    LOGR.info(tr.getMethod().getMethodName() + " was skipped");
  }
  
  @Override
  public void onTestSuccess(ITestResult tr) {
    super.onTestSuccess(tr);
    LOGR.info(tr.getMethod().getMethodName() + " passed");
  }

}

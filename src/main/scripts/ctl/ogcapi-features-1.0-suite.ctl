<?xml version="1.0" encoding="UTF-8"?>
<ctl:package xmlns:ctl="http://www.occamlab.com/ctl"
             xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
             xmlns:tns="http://www.opengis.net/cite/ogcapi-features-1.0"
             xmlns:saxon="http://saxon.sf.net/"
             xmlns:tec="java:com.occamlab.te.TECore"
             xmlns:tng="java:org.opengis.cite.ogcapifeatures10.TestNGController">

  <ctl:function name="tns:run-ets-${ets-code}">
    <ctl:param name="testRunArgs">A Document node containing test run arguments (as XML properties).</ctl:param>
    <ctl:param name="outputDir">The directory in which the test results will be written.</ctl:param>
    <ctl:return>The test results as a Source object (root node).</ctl:return>
    <ctl:description>Runs the OGC API - Features (${version}) test suite.</ctl:description>
    <ctl:code>
      <xsl:variable name="controller" select="tng:new($outputDir)" />
      <xsl:copy-of select="tng:doTestRun($controller, $testRunArgs)" />
    </ctl:code>
  </ctl:function>

  <ctl:suite name="tns:ets-${ets-code}-${version}">
    <ctl:title>OGC API - Features Conformance Test Suite</ctl:title>
    <ctl:description>Checks OGC API - Features implementations for conformance.</ctl:description>
    <ctl:starting-test>tns:Main</ctl:starting-test>
  </ctl:suite>

  <ctl:test name="tns:Main">
    <ctl:assertion>The test subject satisfies all applicable constraints.</ctl:assertion>
    <ctl:code>
      <xsl:variable name="form-data">
        <ctl:form method="POST" width="800" height="600" xmlns="http://www.w3.org/1999/xhtml">
          <h2>OGC API - Features Conformance Test Suite</h2>
          <div style="background:#F0F8FF" bgcolor="#F0F8FF">
            <p>The implementation under test (IUT) is checked against the following specifications:</p>
            <ul>
              <li>
                <a href="http://docs.opengeospatial.org/is/17-069r3/17-069r3.html">OGC API - Features - Part 1: Core</a>
              </li>
              <li>
                <a href="http://docs.opengeospatial.org/is/18-058/18-058.html">OGC API - Features - Part 2: Coordinate Reference Systems by Reference</a>
              </li>
            </ul>
            <p>The following conformance levels are defined:</p>
            <ul>
              <li>Core</li>
              <li>Coordinate Reference Systems by Reference</li>
            </ul>
          </div>
          <fieldset style="background:#ccffff">
            <legend style="font-family: sans-serif; color: #000099;
			                 background-color:#F0F8FF; border-style: solid; 
                       border-width: medium; padding:4px">Implementation under test
            </legend>
            <p>
              <label for="ogc-api-features-uri">
                <h4 style="margin-bottom: 0.5em">Location of the landing page</h4>
              </label>
              <input id="ogc-api-features-uri" name="ogc-api-features-uri" size="128" type="text"
                     value="" />
            </p>
            <p>
              <h4 style="margin-bottom: 0.5em">Number of tested collections</h4>
              <div>
                <input type="radio" id="collectionsLimitLimited" name="collectionsLimit" value="limited" checked="checked"
                       onchange="document.getElementById('noOfCollections').disabled=document.getElementById('collectionsLimitAll').checked;"/>
                <label for="noOfCollectionsAll">Limited number of collections:</label>
                <input type="number" id="noOfCollections" name="noOfCollections" value="3" min="1" />
              </div>
              <div>
                <input type="radio" id="collectionsLimitAll" name="collectionsLimit" value="all"
                       onchange="document.getElementById('noOfCollections').disabled=document.getElementById('collectionsLimitAll').checked;" />

                <label for="noOfCollectionsAll">All collections</label>
              </div>
            </p>
          </fieldset>
          <p>
            <input class="form-button" type="submit" value="Start" />
            |
            <input class="form-button" type="reset" value="Clear" />
          </p>
        </ctl:form>
      </xsl:variable>
      <xsl:variable name="test-run-props">
        <properties version="1.0">
          <entry key="iut">
            <xsl:value-of select="normalize-space($form-data/values/value[@key='ogc-api-features-uri'])" />
          </entry>
          <entry key="noofcollections">
            <xsl:choose>
              <xsl:when test="$form-data/values/value[@key='collectionsLimit'] = 'all'">-1</xsl:when>
              <xsl:otherwise>
                <xsl:value-of select="$form-data/values/value[@key='noOfCollections']" />
              </xsl:otherwise>
            </xsl:choose>
          </entry>
        </properties>
      </xsl:variable>
      <xsl:variable name="testRunDir">
        <xsl:value-of select="tec:getTestRunDirectory($te:core)" />
      </xsl:variable>
      <xsl:variable name="test-results">
        <ctl:call-function name="tns:run-ets-${ets-code}">
          <ctl:with-param name="testRunArgs" select="$test-run-props" />
          <ctl:with-param name="outputDir" select="$testRunDir" />
        </ctl:call-function>
      </xsl:variable>
      <xsl:call-template name="tns:testng-report">
        <xsl:with-param name="results" select="$test-results" />
        <xsl:with-param name="outputDir" select="$testRunDir" />
      </xsl:call-template>
      <xsl:variable name="summary-xsl" select="tec:findXMLResource($te:core, '/testng-summary.xsl')" />
      <ctl:message>
        <xsl:value-of select="saxon:transform(saxon:compile-stylesheet($summary-xsl), $test-results)" />
        See detailed test report in the TE_BASE/users/
        <xsl:value-of
                select="concat(substring-after($testRunDir, 'users/'), '/html/')" />
        directory.
      </ctl:message>
      <xsl:if test="xs:integer($test-results/testng-results/@failed) gt 0">
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and not(@is-config='true')]">
          <ctl:message>
            Test method<xsl:value-of select="./@name" />:
            <xsl:value-of select=".//message" />
          </ctl:message>
        </xsl:for-each>
        <ctl:fail />
      </xsl:if>
      <xsl:if test="xs:integer($test-results/testng-results/@skipped) eq xs:integer($test-results/testng-results/@total)">
        <ctl:message>All tests were skipped. One or more preconditions were not satisfied.</ctl:message>
        <xsl:for-each select="$test-results//test-method[@status='FAIL' and @is-config='true']">
          <ctl:message>
            <xsl:value-of select="./@name" />:
            <xsl:value-of select=".//message" />
          </ctl:message>
        </xsl:for-each>
        <ctl:skipped />
      </xsl:if>
    </ctl:code>
  </ctl:test>

  <xsl:template name="tns:testng-report">
    <xsl:param name="results" />
    <xsl:param name="outputDir" />
    <xsl:variable name="stylesheet" select="tec:findXMLResource($te:core, '/testng-report.xsl')" />
    <xsl:variable name="reporter" select="saxon:compile-stylesheet($stylesheet)" />
    <xsl:variable name="report-params" as="node()*">
      <xsl:element name="testNgXslt.outputDir">
        <xsl:value-of select="concat($outputDir, '/html')" />
      </xsl:element>
    </xsl:variable>
    <xsl:copy-of select="saxon:transform($reporter, $results, $report-params)" />
  </xsl:template>
</ctl:package>

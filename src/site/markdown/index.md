
# Test suite: ets-wfs30

## Scope

This executable test suite (ETS) verifies the conformance of the implementation 
under test (IUT) with respect to the set of relevant specifications depicted in 
Figure 1. Conformance testing is a kind of "black box" testing that examines the 
externally visible characteristics or behaviors of the IUT while disregarding 
any implementation details.

**Figure 1: Relevant specifications**

![Set of relevant specifications](img/specifications.png)

Several conformance classes are defined in the principal specifications; the ones 
listed below are covered by this test suite:

* Class A 
    - List capabilities of conformance class A
* Class B 
    - List capabilities of conformance class B


## Test requirements

The documents listed below stipulate requirements that must be satisfied by a 
conforming implementation.

1. [Web Content Accessibility Guidelines (WCAG) 2.0](http://www.w3.org/TR/WCAG20/)
2. [Extensible Markup Language (XML) 1.0, Fifth Edition](http://www.w3.org/TR/xml/)
3. [RFC 7231](https://tools.ietf.org/html/rfc7231) - Hypertext Transfer Protocol 
(HTTP/1.1): Semantics and Content 

If any of the following preconditions are not satisfied then all tests in the 
suite will be marked as skipped.

1. Precondition 1;
2. Precondition 2.


## Test suite structure

The test suite definition file (testng.xml) is located in the root package, 
`org.opengis.cite.wfs30`. A conformance class corresponds to a &lt;test&gt; element, each 
of which includes a set of test classes that contain the actual test methods. 
The general structure of the test suite is shown in Table 1.

<table>
  <caption>Table 1 - Test suite structure</caption>
  <thead>
    <tr style="text-align: left; background-color: LightCyan">
      <th>Conformance class</th>
      <th>Test classes</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>Conformance Level 1</td>
      <td>org.opengis.cite.wfs30.level1.*</td>
    </tr>
    <tr>
      <td>Conformance Level 2</td>
      <td>org.opengis.cite.wfs30.level2.*</td>
    </tr>
  </tbody>
</table>

The Javadoc documentation provides more detailed information about the test 
methods that constitute the suite.


## Test run arguments

The test run arguments are summarized in Table 2. The _Obligation_ descriptor can 
have the following values: M (mandatory), O (optional), or C (conditional).

<table>
	<caption>Table 2 - Test run arguments</caption>
	<thead>
    <tr>
      <th>Name</th>
      <th>Value domain</th>
	    <th>Obligation</th>
	    <th>Description</th>
    </tr>
  </thead>
	<tbody>
    <tr>
      <td>iut</td>
      <td>URI</td>
      <td>M</td>
      <td>A URI that refers to the implementation under test or metadata about it.
    Ampersand ('&amp;') characters must be percent-encoded as '%26'.</td>
    </tr>
	  <tr>
      <td>ics</td>
      <td>A comma-separated list of string values.</td>
      <td>O</td>
      <td>An implementation conformance statement that indicates which conformance 
      classes or options are supported.</td>
    </tr>
	</tbody>
</table>

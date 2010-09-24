package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * These are the tests that register themselves as variation-long. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.variation.AlleleFrequencies </li> 
 * </ul>
 *
 * @author Autogenerated
 *
 */
public class VariationLong extends GroupOfTests {
	
	public VariationLong() {

		addTest(
			org.ensembl.healthcheck.testcase.variation.AlleleFrequencies.class
		);
	}
}
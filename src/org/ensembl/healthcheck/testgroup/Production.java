package org.ensembl.healthcheck.testgroup;

import org.ensembl.healthcheck.GroupOfTests;

/**
 * These are the tests that register themselves as production. The tests are:
 * 
 * <ul>
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionBiotypes </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMasterTables </li> 
 *   <li> org.ensembl.healthcheck.testcase.generic.ProductionMeta </li> 
 * </ul>
 *
 * @author Autogenerated
 *
 */
public class Production extends GroupOfTests {
	
	public Production() {

		addTest(
			org.ensembl.healthcheck.testcase.generic.ProductionBiotypes.class,
			org.ensembl.healthcheck.testcase.generic.ProductionMasterTables.class,
			org.ensembl.healthcheck.testcase.generic.ProductionMeta.class
		);
	}
}
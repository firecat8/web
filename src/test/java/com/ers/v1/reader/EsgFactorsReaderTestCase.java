/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.reader;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

import com.ers.v1.calc.esg.EsgObject;

/**
 *
 * @author snyanakieva
 */
public class EsgFactorsReaderTestCase {

	@Test
	public void readEsgFileTest() {
		EsgFactorsReader reader = new EsgFactorsReader();
		reader.readFromStream(getClass().getResourceAsStream("/ESG.xls"));
		List<EsgObject> factors = reader.getEsgFactors();
		Assert.assertFalse("Empty list", factors.isEmpty());
	}

	@Test
	public void readWrongEsgFileTest() {
		EsgFactorsReader reader = new EsgFactorsReader();
		try {
			reader.readFromStream(getClass().getResourceAsStream("/WrongESG.xls"));
			Assert.fail("Exception not cought");
		} catch (Exception e) {
			Logger.getLogger("Asert").log(Level.INFO, e.getMessage());
		}
	}

}

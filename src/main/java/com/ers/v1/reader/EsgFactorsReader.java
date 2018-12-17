/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.reader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.ers.v1.calc.esg.EsgObject;
import com.ers.v1.parser.EsgObjectParser;
import com.ers.v1.reader.exceptions.InvalidSheetFormatException;
import com.ers.v1.reader.exceptions.UnableToParseDateException;

/**
 *
 * @author snyanakieva
 */
public class EsgFactorsReader extends ExcelReader {

	private final List<EsgObject> esgFactors = new ArrayList<>();
	private final EsgObjectParser parser = new EsgObjectParser();

	@Override
	public void readFromStream(InputStream inputStream) throws InvalidSheetFormatException, UnableToParseDateException {
		esgFactors.clear();
		super.readFromStream(inputStream);
	}

	@Override
	protected void parseAndSave(Row row) {
		if (row.getRowNum() == 0) {
			return;
		}

		EsgObject factor = parser.parse(row);
		esgFactors.add(factor);
	}

	@Override
	protected void checkSheetValidity(final Sheet sheet) {
		sheet.forEach((Row row) -> {
			if (row.getPhysicalNumberOfCells() != 4) {
				throw new InvalidSheetFormatException();
			}
		});
	}

	public List<EsgObject> getEsgFactors() {
		return esgFactors;
	}
}

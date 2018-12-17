package com.ers.v1.parser;

import org.apache.poi.ss.usermodel.Row;

import com.ers.v1.calc.esg.EsgObject;

/**
 *
 * @author snayanakieva
 */
public class EsgObjectParser extends Parser<EsgObject> {

	@Override
	public EsgObject parse(Row row) {
		String mfId = dataFormatter.formatCellValue(row.getCell(0));
		int e = toInt(row.getCell(1));
		int s = toInt(row.getCell(2));
		int g = toInt(row.getCell(3));

		return new EsgObject(mfId, e, s, g);
	}

}

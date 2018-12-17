/*
 * EuroRisk Systems (c) Ltd. All rights reserved.
 */
package com.ers.v1.reader;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.ers.v1.reader.exceptions.InvalidSheetFormatException;
import com.ers.v1.reader.exceptions.UnableToParseDateException;

/**
 *
 * @author snyanakieva
 */
public abstract class ExcelReader {

	protected final static Logger LOGGER = Logger.getLogger(ExcelReader.class.getCanonicalName());
	protected int positionOnFirstSheet = 0; //default 0

	public void readFromStream(InputStream inputStream) throws InvalidSheetFormatException, UnableToParseDateException {
		try {
			Sheet sheet = createSheet(inputStream);
			checkSheetValidity(sheet);
			sheet.forEach(row -> parseAndSave(row));
		} catch (IOException | InvalidFormatException ex) {
			LOGGER.log(Level.SEVERE, null, ex);
		}
	}

	private Sheet createSheet(InputStream inputStream) throws IOException, InvalidFormatException {
		Workbook workbook = WorkbookFactory.create(inputStream);
		return workbook.getSheetAt(positionOnFirstSheet);
	}

	protected abstract void checkSheetValidity(final Sheet sheet);

	protected abstract void parseAndSave(Row row);

}

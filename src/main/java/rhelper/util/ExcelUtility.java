/**
 * 
 */
package rhelper.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * @author akira
 *
 */
public class ExcelUtility {
	
	// パターン
	private static final String CELL_STR_PATTERN = "[A-Z]+[0-9]+";

	public int[] convCellFromRc(String string) {

		String cellStr = string.toUpperCase();
		if (!cellStr.matches(CELL_STR_PATTERN)) {
			throw new NumberFormatException("セル指定に誤りがあります。");
		}

		String rowStr = cellStr.replaceAll("[^0-9]", "");
		int row = Integer.parseInt(rowStr) - 1;
		if (row < 0) {
			throw new NumberFormatException("行指定が異常です。");
		}

		String columnStr = cellStr.replaceAll("[^A-Z]", "");
		int column = columnStr.codePointAt(0) - 'A';
		if (columnStr.length() > 1) {
			column = (column + 1) * 26 + (columnStr.codePointAt(1) - 'A');

		}
		int[] retVal = { row, column };
		return retVal;
	}

	public Sheet acquireSheet(String filepath) throws IOException,
			InvalidFormatException {
		InputStream is = null;
		try {
			is = new FileInputStream(filepath);
			return acquireSheet(is);
		} finally {
			if (is != null) {
				is.close();
			}
		}
	}

	public Sheet acquireSheet(InputStream is) throws IOException,
			InvalidFormatException {
		Workbook wb = WorkbookFactory.create(is);
		return wb.getSheetAt(0);
	}

	public int acquireIntValue(Sheet sheet, int rowIndex, int columnIndex) {
		return (int) acquireCell(sheet, rowIndex, columnIndex)
				.getNumericCellValue();
	}

	public String acquireStringValue(Sheet sheet, int rowIndex, int columnIndex) {
		return acquireCell(sheet, rowIndex, columnIndex).getStringCellValue();
	}

	private Cell acquireCell(Sheet sheet, int rowIndex, int columnIndex) {
		return sheet.getRow(rowIndex).getCell(columnIndex);
	}

	public int acquireIntValue(Sheet sheet, String cellName) {
		return (int) acquireCell(sheet, cellName).getNumericCellValue();
	}

	public String acquireStringValue(Sheet sheet, String cellName) {
		return acquireCell(sheet, cellName).getStringCellValue();
	}

	private Cell acquireCell(Sheet sheet, String cellName) {
		int point[] = convCellFromRc(cellName);
		return sheet.getRow(point[0]).getCell(point[1]);
	}
}

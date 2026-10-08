// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.PaneInformation;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public class ExcelHelper {
	
	public static Workbook read(File file) throws Exception {
		Workbook wb = null;
		InputStream is = new FileInputStream(file);
		try {
			wb = WorkbookFactory.create(is);
		} finally {
			is.close();
		}
		return wb;
	}

	public static ExcelData getData(Workbook wb) {
		ExcelData.Sheet[] sheets = new ExcelData.Sheet[wb.getNumberOfSheets()];
		for (int i = 0; i < wb.getNumberOfSheets(); i++) {
			sheets[i] = getData(wb.getSheetAt(i));
		}
		return new ExcelData(sheets);
	}

	private static ExcelData.Sheet getData(Sheet sheet) {
		String name = sheet.getSheetName();
		Object[][] values = new Object[getRowCount(sheet)][];
		for (int i = 0; i < values.length; i++) {
			Row row = sheet.getRow(i);
			values[i] = new Object[getRowLength(row)];
			for (int j = 0; j < values[i].length; j++) {
				Object value = getCellValue(row.getCell(j));
				values[i][j] = value;
			}
		}
		PaneInformation pi = sheet.getPaneInformation();
		int fixedRowCount = pi != null ? pi.getHorizontalSplitPosition() : 0;
		return new ExcelData.Sheet(name, values, fixedRowCount);
	}
	
	private static int getRowCount(Sheet sheet) {
		return sheet.getLastRowNum() + 1;
	}
	
	private static int getRowLength(Row row) {
		return row != null ? Math.max(row.getLastCellNum(), 0) : 0;
	}
	
	private static Object getCellValue(Cell cell) {
		if (cell != null) {
			switch (cell.getCellType()) {
				case BLANK: return "";
				case STRING: return getStringCellValue(cell);
				case NUMERIC: return cell.getNumericCellValue();
				case BOOLEAN: return cell.getBooleanCellValue();
				case FORMULA:
					switch (cell.getCachedFormulaResultType()) {
						case BLANK: return "";
						case STRING: return getStringCellValue(cell);
						case NUMERIC: return cell.getNumericCellValue();
						case BOOLEAN: return cell.getBooleanCellValue();
						default: return null;
					}
				default: return null;
			}
		} else {
			return null;
		}
	}

	private static String getStringCellValue(Cell cell) {
		String s = cell.getStringCellValue();
		XSSFRichTextString t = (XSSFRichTextString)cell.getRichStringCellValue();
		if (t.hasFormatting()) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < s.length(); i++) {
				sb.append(s.charAt(i));
				if (s.charAt(i) != ' ' && t.getFontAtIndex(i).getUnderline() == Font.U_SINGLE) {
					sb.append('_');
				}
			}
			s = sb.toString();
		}
		return s;
	}
}

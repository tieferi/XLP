// SPDX-License-Identifier: GPL-3.0-or-later

package de.t11r.cards;

public class ExcelData {

	public static class Sheet {

		private String name;
		private Object[][] values;
		private int fixedRowCount;

		public Sheet(String name, Object[][] values, int fixedRowCount) {
			this.name = name;
			this.values = values;
			this.fixedRowCount = fixedRowCount;
		}

		public String getName() {
			return name;
		}
		
		public Object getValue(int i, int j) {
			return i < values.length && j < values[i].length ? values[i][j] : null;
		}
		
		public String getString(int i, int j) {
			Object x = getValue(i, j);
			return x != null && x.getClass().equals(String.class) ? (String)x : null;
		}
		
		public int getRowCount() {
			return values.length;
		}

		public int getFixedRowCount() {
			return fixedRowCount;
		}
	}
	
	private Sheet[] sheets;

	public ExcelData(Sheet[] sheets) {
		this.sheets = sheets;
	}

	public Sheet[] getSheets() {
		return sheets;
	}

	public Sheet getSheet(String name) {
		for (Sheet sheet : sheets) {
			if (sheet.getName().equals(name)) {
				return sheet;
			}
		}
		return null;
	}
}
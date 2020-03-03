package com.wma.framework.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * The ExcelUtilities class is meant to provide a simple way to read/write
 * excel. 
 * <br>
 * Below is a sample code to demonstrate the usage :-
 * 
 * <pre>
 * ExcelUtilities excel= new ExcelUtilities("excelFile.xlsx");
 * int rowCount= excel.getRowCount("Sheet1");
 * Map&lt;String, String&gt; dataRow = excel.getRowData(1, "Sheet1");
 * System.out.println("Column Value" + dataRow.get("ColumnName"));
 *
 * Map&lt;String, String&gt; anotherDataRow = excel.getRowDataWhere("Sheet1", "ColumnName", "ColumnValue");
 * System.out.println("Another Column Value" + anotherDataRow.get("AnotherColumnName"));
 *
 * excel.updateValue("Sheet1","Col_1","setValue", "Col_2='col2Value'");
 * </pre>
 * 
 */
public class ExcelUtilities {

	private static Logger log = Logger.getLogger(ExcelUtilities.class);

	private String filePath;

	public ExcelUtilities(String filepath) {
		this.filePath = filepath;
	}

	/**
	 * To get the total number of rows in the Excel Sheet
	 * 
	 * @param sheetName
	 *            The name of the sheet, for which row count is expected
	 * @return Number of Rows present in the sheet
	 */
	public int getRowCount(String sheetName) {
		Workbook workbook = null;
		try {
			FileInputStream excelFile = new FileInputStream(new File(filePath));
			workbook = new XSSFWorkbook(excelFile);
			Sheet sheet = workbook.getSheet(sheetName);
			return sheet.getPhysicalNumberOfRows();

		} catch (Exception e) {

		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return 0;
	}

	/**
	 * To get a row data in the form of key-Value pair where key is the field/column
	 * name and value is the data present in the corresponding field
	 * 
	 * @param rowNum
	 *            The row number of which the data is expected
	 * @param sheetName
	 *            The name of the sheet from which the data is expected
	 * @return The row in the form of key-Value which is an object of HashMap
	 */
	public Map<String, String> getRowData(int rowNum, String sheetName) {
		Map<String, String> record = new HashMap<>();

		Workbook workbook = null;
		try {
			List<String> cols = getFieldNames(sheetName);

			FileInputStream excelFile = new FileInputStream(new File(filePath));
			workbook = new XSSFWorkbook(excelFile);
			Sheet sheet = workbook.getSheet(sheetName);

			Row currentRow = sheet.getRow(rowNum);
			if (currentRow == null)
				throw new Exception("Invalid row number :" + rowNum + " in sheet " + sheetName);

			Iterator<Cell> cellIterator = currentRow.iterator();
			int columnCounter = 0;
			while (cellIterator.hasNext()) {
				Cell currentCell = cellIterator.next();

				if (currentCell.getCellType() == Cell.CELL_TYPE_STRING)
					record.put(cols.get(columnCounter++), currentCell.getStringCellValue());

				else if (currentCell.getCellType() == Cell.CELL_TYPE_NUMERIC)
					record.put(cols.get(columnCounter++), ((int) currentCell.getNumericCellValue()) + "");

				else if (currentCell.getCellType() == Cell.CELL_TYPE_FORMULA)
					record.put(cols.get(columnCounter++), currentCell.getRichStringCellValue().toString());

				else if (currentCell.getCellType() == Cell.CELL_TYPE_BLANK)
					record.put(cols.get(columnCounter++), "");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			log.info("Exception occured while reading Row Number :" + rowNum + " in sheet " + sheetName);
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return record;
	}
	/**
	 * To get a row data in the form of key-Value pair where key is the field/column name and 
	 * value is the data present in the corresponding field 
	 * It works like "Get data from sheetName where colName = 'value'"
	 * Ensure the mentioned value is unique in the given colName
	 * 
	 * @param sheetName The name of the sheet
	 * @param colName   The name of the field for filter 
	 * @param value     The value present in the field 
	 * @return It returns the row for which the given field (specified by colName param) has value (specified by value param)
	 */
	public Map<String, String> getRowWhere(String sheetName, String colName, String value) {
		Map<String, String> record = new HashMap<>();

		for(int row = 1; row < getRowCount(sheetName); row++) {
			record = getRowData(row, sheetName);
			if(record.get(colName).equalsIgnoreCase(value))
				break;
		}

		if(record.isEmpty())
			log.error("No Record found for SheetName :" + sheetName + "Column : " + colName + "Value : " + value);

		return record;
	}

	/**
	 * Get the list of rows which has column 'colName' with value provided in the method 
	 * 
	 *  @param sheetName 
	 *  @param colName
	 *  @param value
	 *  @return
	 */
	public List<Map<String, String>> getRowsWhere(String sheetName, String colName, String value) {
		List<Map<String, String>> records = new ArrayList<>();

		for(int row = 1; row < getRowCount(sheetName); row++) {
			Map<String, String> record = getRowData(row,sheetName);
			if(record.get(colName).equalsIgnoreCase(value))
				records.add(record);
		}

		if(records.isEmpty())
			log.error("No Record found for SheetName : " + sheetName + " where " + colName + " = " + value);

		return records;
	}

	public String getFilePath() {
		return filePath;
	}

	/**
	 * Create a new sheet in the excel with given columns 
	 * @param sheetName 
	 * @param colNames
	 */
	public void addNewSheet(String sheetName, String... colNames) {
		Workbook workbook = null;
		try {
			FileInputStream excelFile = new FileInputStream(new File(filePath));
			workbook = new XSSFWorkbook(excelFile);

			Sheet sheet = workbook.createSheet(sheetName);
			if(colNames.length > 0) {
				Row row = sheet.createRow(0);
				for(int i=0; i<colNames.length; i++) {
					Cell cell = row.createCell(i, Cell.CELL_TYPE_STRING);
					cell.setCellValue(colNames[i]);
				}
			}

			FileOutputStream outputStream = new FileOutputStream(filePath);
			workbook.write(outputStream);
			outputStream.close();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				}  catch (IOException e) {
					e.printStackTrace();
				}
			}	
		}
	}

	/**
	 * To get a all rows data in the form of string 
	 * @param sheetName The name of the sheet from which the data is expected
	 * @return contents of the excel sheet in the string 
	 */
	public String toString(String sheetName) {
		StringBuilder contents = new StringBuilder();

		for(int i=1; i<getRowCount(sheetName); i++)
			contents.append(getRowData(i, sheetName).toString());
		return contents.toString();
	}	

	/**
	 * Read values from the specific column of the given sheet
	 * 
	 * @param sheetName
	 * @param colName
	 * @return
	 */
	public List<String> getValuesForColumn(String sheetName, String colName) {
		List<String> records = new ArrayList<>();

		int rows = getRowCount(sheetName);

		//Check whether there are rows are not, if there is only one row, it is assumed to be header row having column names
		if(rows <=1)
			return records;

		for(int r=1; r<rows; r++)
			records.add(r, getRowData(r, sheetName).get(colName));

		return records;
	}

	/**
	 * <p>Get the column names of from the given sheet.
	 * <pre>It is assumed that the first row of the excel contains the column names</pre></p>
	 * @param sheetName
	 * @return
	 */
	public List<String> getFieldNames(String sheetName) {
		List<String> fields = new ArrayList<>();
		Workbook workbook = null;
		try {
			FileInputStream excelFile = new FileInputStream(new File(filePath));
			workbook = new XSSFWorkbook(excelFile);
			Sheet sheet = workbook.getSheet(sheetName);
			Row row = sheet.getRow(0);

			Iterator<Cell> cellIterator = row.cellIterator();
			while(cellIterator.hasNext())
				fields.add(cellIterator.next().getStringCellValue());

		} catch(Exception e) {

		} finally {
			if (workbook != null) {
				try {
					workbook.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		return fields;
	}

	/**
	 * To check whether there is a sheet with name 'sheetName' in the excel file 
	 * 
	 * @param sheetName
	 * @return
	 */
	public boolean isSheetPresent(String sheetName) {
		Workbook workbook = null;
		boolean flag = false;
		try {
			FileInputStream excelFile = new FileInputStream(new File(filePath));
			workbook = new XSSFWorkbook(excelFile);
			int sheetCount = workbook.getNumberOfSheets();
			for(int i=0; i<sheetCount; i++) {
				flag = true;
				break;
			}
		}
		catch(Exception e ) {
			log.error(e.getLocalizedMessage());
		} finally {
			if(workbook != null) {
				try {
					workbook.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return flag;
	}

}

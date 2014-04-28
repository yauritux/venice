package com.gdn.venice.hssf;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

public class ExcelToPojo {
	
	private  MappingReader mappingReader=new MappingReader();
	private ArrayList<PojoInterface>pojoResult=new ArrayList<PojoInterface>();
	
	@SuppressWarnings("rawtypes")
	private Class pojo;
	private String fileName;
	private String excelFileName;
	private int startRow;
	private int startColumn;
	
	/*
	 * The row number should any errors occur
	 */
	private Integer errorRowNumber = 1;
	
	private String errorMessage = "";
	
	/**
	 * Constructor refactored as a copy constructor.
	 */
	@SuppressWarnings("rawtypes")
	public ExcelToPojo(Class pojo, String fileName, String excelFileName, int startRow, int startColumn) throws Exception{
		this.pojo = pojo;
		this.fileName = fileName;
		this.excelFileName = excelFileName;
		this.startRow = startRow;
		this.startColumn = startColumn;
	}
	
	/**
	 * Refactored to do this work in a separate method to the constructor
	 * @return the pojo with the mapping done.
	 * @throws Exception 
	 */
	public ExcelToPojo getPojo() throws Exception{
		ArrayList<String> Mapper;
		
		mappingReader.readMapOrder(fileName);
		Mapper=mappingReader.getMapResult();
		try {
				//load excel
				Import i = new Import();
				DataArgs data = i.ToObject(excelFileName);
				if (data != null) {
					//Get Rows
					List<DataRow> rows = data.getRows();
					System.out.println("rows.size(): "+rows.size());
					errorRowNumber=1;
					for (int r = startRow; r < rows.size(); ++r) {
						PojoInterface objPojo=(PojoInterface) pojo.newInstance();
						List<GCell> gCells = rows.get(r).getCells();
						
						System.out.println("GCell Size = " + gCells.size());
						for (int e = startColumn; e < gCells.size(); ++e) {
							Method[] methods=pojo.getMethods();
//							System.out.println("Index = " + e + ", Value = "+ gCells.get(e).getValue());							
							
							for(Method method:methods){
//								System.out.println("Method = " + method.getName() + ", Mapper = "+ Mapper.get(e));								
								if(method.getName().equalsIgnoreCase(Mapper.get(e)))
								{
									String value = (String)gCells.get(e).getValue();
									
									try{
										BigDecimal decimalValue = new BigDecimal(value);
										value = decimalValue.toPlainString();
									}catch (Exception ex) {
									}
									
									if(!"".equals(value)){
										method.invoke(objPojo,value);										
									}else{
										method.invoke(objPojo,"");
									}
									System.out.println("Nomor Element = " + e + ", Element = " + method.getName() + ", Value = "+ value);
									break;
								}
							}
						}
						pojoResult.add(objPojo);
						errorRowNumber++;
					}
				}
		} catch (Exception e) {
			mappingReader.getResultColumn();
			e.printStackTrace();
			throw new Exception ("Exception mapping spreadsheet to the required format at row:" + errorRowNumber + " Column:" + mappingReader.getResultColumn() + ". Original Exception was:" + e.getClass().getName() + ":" + e.getMessage());
		}
		return this;
	}
	
	/**
	 * Refactored to do this work in a separate method to the constructor
	 * @return the pojo with the mapping done.
	 * @throws Exception 
	 */
	public ExcelToPojo getPojoToExcel(int endColumn, String startRows,String endRow) throws Exception{
		ArrayList<String> Mapper;
		
		mappingReader.readMapOrder(fileName);
		Mapper=mappingReader.getMapResult();
		boolean flag=true;
		try {
				//load excel
				Import i = new Import();
				DataArgs data = i.ToObject(excelFileName);
				if (data != null) {
					//Get Rows
					List<DataRow> rows = data.getRows();
					System.out.println("rows.size(): "+rows.size());
					errorRowNumber=1;
					if(!startRows.equals("")){
						for(int j=0;j<rows.size();++j){
							List<GCell> gCells = rows.get(j).getCells();
							if(gCells.get(startColumn).getValue().toString().toLowerCase().equals(startRows.toLowerCase())){
								startRow=j+1;
								break;					
							}
						}
					}
					
					for (int r = startRow; r < rows.size() && flag; ++r) {
						PojoInterface objPojo=(PojoInterface) pojo.newInstance();
						List<GCell> gCells = rows.get(r).getCells();
						
						if(gCells.get(startColumn).getValue().toString().toLowerCase().equals(endRow.toLowerCase())){
							flag=false;
							System.out.println("END row = " + r + ", Value = "+ gCells.get(startColumn).getValue().toString().toLowerCase());	
						}else{						
								System.out.println("endColumn = " + endColumn);
								for (int e = startColumn; e < endColumn; ++e) {
									Method[] methods=pojo.getMethods();					
									
									for(Method method:methods){							
										if(method.getName().equalsIgnoreCase(Mapper.get(e)))
										{
											String value = (String)gCells.get(e).getValue();
											
											try{
												BigDecimal decimalValue = new BigDecimal(value);
												value = decimalValue.toPlainString();
											}catch (Exception ex) {
											}
											
											if(!"".equals(value)){
												method.invoke(objPojo,value);										
											}else{
												method.invoke(objPojo,"");
											}
//											System.out.println("Nomor Element = " + e + ", Element = " + method.getName() + ", Value = "+ value);
											break;
										}
									}
								}
								pojoResult.add(objPojo);
						}
						errorRowNumber++;
					}
				}
		} catch (Exception e) {
			mappingReader.getResultColumn();
			e.printStackTrace();
			throw new Exception ("Exception mapping spreadsheet to the required format at row:" + errorRowNumber + " Column:" + mappingReader.getResultColumn() + ". Original Exception was:" + e.getClass().getName() + ":" + e.getMessage());
		}
		return this;
	}
	
	public ArrayList<PojoInterface> getPojoResult() {
		return pojoResult;
	}


	public void setPojoResult(ArrayList<PojoInterface> pojoResult) {
		this.pojoResult = pojoResult;
	}

	/**
	 * @return the errorRowNumber
	 */
	public Integer getErrorRowNumber() {
		return errorRowNumber;
	}

	public ExcelToPojo(String mappingFileName, String excelFileName,
			int startRow, int startColumn, int endColumn, Class pojoTemplate) {
		
		//Read mapping from XML File
		MappingReader mappingReader = new MappingReader();
		mappingReader.readMappingOrder(mappingFileName);
		ArrayList<HashMap<String, Class>> mapping = mappingReader.getMappingResult();
		
		try {
			if (excelFileName != "") {
				InputStream xls = null;
				try {
					xls = new FileInputStream(excelFileName);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					errorMessage = "Error while opening excel file";
					return;
				}

				HSSFWorkbook wb = null;
				HSSFSheet sheet = null;
				try {
					wb = new HSSFWorkbook(xls);
					sheet = wb.getSheetAt(0);
				} catch (IOException e) {
					e.printStackTrace();
					errorMessage = "Error while opening file, make sure file type is excel";
					return;
				}

				if (sheet != null) {
					Iterator<Row> rows = sheet.rowIterator();
					boolean importRow = false;
					int currentRow = -1;
					ArrayList<String> errorList = new ArrayList<String>();
					
					while (rows.hasNext()) {
						HSSFRow row = (HSSFRow) rows.next();
						currentRow = !importRow && row.equals(sheet.getRow(startRow - 1)) ? startRow : currentRow;
						importRow = importRow || row.equals(sheet.getRow(startRow - 1));
						
						if (importRow) {
							PojoInterface objPojo = (PojoInterface) pojoTemplate.newInstance();
							Method[] methods = pojoTemplate.getMethods();
							
							for(int columnIndex = startColumn - 1; columnIndex < endColumn; columnIndex++) {
								HSSFCell cell = row.getCell(columnIndex, Row.CREATE_NULL_AS_BLANK);
								
								String dataName = "";
								for (String key : mapping.get(columnIndex).keySet()) {
									dataName =  key;
								}
								
								for (Method method : methods) {
									if (method.getName().equalsIgnoreCase("set" + dataName)) {
										try {
											if (mapping.get(columnIndex).get(dataName).getName().toLowerCase().contains("bigdecimal")) {
												switch (cell.getCellType()) {
													case Cell.CELL_TYPE_STRING:
														method.invoke(objPojo, new BigDecimal(cell.getRichStringCellValue().getString().trim()));
									                    break;
									                case Cell.CELL_TYPE_NUMERIC:
									                    if (!DateUtil.isCellDateFormatted(cell)) {
									                    	method.invoke(objPojo, new BigDecimal(cell.getNumericCellValue()));
									                    }
									                    break;
									                case Cell.CELL_TYPE_FORMULA:
														method.invoke(objPojo, new BigDecimal(cell.getRichStringCellValue().getString().trim()));
									                    break;
									                default:
									                    break;
												}
											} else if (mapping.get(columnIndex).get(dataName).getName().toLowerCase().contains("date")) {
												switch (cell.getCellType()) {
													case Cell.CELL_TYPE_STRING:
														method.invoke(objPojo, new Date(new Long(cell.getRichStringCellValue().getString()).longValue()));
									                    break;
									                case Cell.CELL_TYPE_NUMERIC:
									                    if (DateUtil.isCellDateFormatted(cell)) {
									                    	method.invoke(objPojo, cell.getDateCellValue());
									                    } else {
									                    	method.invoke(objPojo, new Date((long) cell.getNumericCellValue()));
									                    }
									                    break;
									                case Cell.CELL_TYPE_FORMULA:
														method.invoke(objPojo, new Date(new Long(cell.getRichStringCellValue().getString()).longValue()));
									                    break;
									                default:
									                    break;
												}
											} else {
												switch (cell.getCellType()) {
													case Cell.CELL_TYPE_STRING:
														method.invoke(objPojo, cell.getRichStringCellValue().getString().trim());
									                    break;
									                case Cell.CELL_TYPE_NUMERIC:
									                    if (DateUtil.isCellDateFormatted(cell)) {
									                    	method.invoke(objPojo, cell.getDateCellValue().toString().trim());
									                    } else {
									                    	method.invoke(objPojo, new Long((long) cell.getNumericCellValue()).toString().trim());
									                    }
									                    break;
									                case Cell.CELL_TYPE_BOOLEAN:
														method.invoke(objPojo, new Boolean(cell.getBooleanCellValue()).toString().trim());
								                    	break;
									                case Cell.CELL_TYPE_FORMULA:
														method.invoke(objPojo, cell.getRichStringCellValue().getString().trim());
									                    break;
									                default:
									                    break;
												}
											}
										} catch (Exception importException) {
											importException.printStackTrace();
											errorList.add("   Row: " + currentRow + ", Column " + (columnIndex + 1) + " is expected using " + mapping.get(columnIndex).get(dataName).getSimpleName() + " type");
											importException.printStackTrace();
										}

										break;
									}
								}
							}
							
							this.pojoResult.add(objPojo);
						}
						
						currentRow++;
					}
					
					if (errorList.size() > 0) {
						errorMessage = "Error while importing data from excel\n" + StringUtils.join(errorList.toArray(), "\n");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage += "Error while importing data from excel";
		}
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}

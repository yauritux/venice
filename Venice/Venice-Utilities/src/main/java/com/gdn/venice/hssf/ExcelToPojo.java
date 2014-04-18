package com.gdn.venice.hssf;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
		System.out.println("ExcelToPojo:getPojo");
		ArrayList<String> Mapper;		
		mappingReader.readMapOrder(fileName);
		Mapper=mappingReader.getMapResult();
		System.out.println("mapper size(): "+Mapper.size());
		
		try {
				Import i = new Import();
				DataArgs data = i.ToObject(excelFileName);
				
				if (data != null) {
					List<DataRow> rows = data.getRows();
					System.out.println("rows size(): "+rows.size());
					errorRowNumber=1;
					for (int r = startRow; r < rows.size()+startRow; ++r) {
						PojoInterface objPojo=(PojoInterface) pojo.newInstance();
						List<GCell> gCells = rows.get(r).getCells();
						for (int e = startColumn; e < gCells.size(); ++e) {
							Method[] methods=pojo.getMethods();
							System.out.println("Index = " + e + ", Value = "+ gCells.get(e).getValue());							
							
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

	public ExcelToPojo getPojoToExcel(int endColumn, String startRows,String endRow) throws Exception{
		System.out.println("ExcelToPojo:getPojoToExcel");
		ArrayList<String> Mapper;		
		mappingReader.readMapOrder(fileName);
		Mapper=mappingReader.getMapResult();
		boolean flag=true;
		try {
				Import i = new Import();
				DataArgs data = i.ToObject(excelFileName);
				if (data != null) {
					List<DataRow> rows = data.getRows();
					System.out.println("rows size(): "+rows.size());
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
						}else{						
							for (int e = startColumn; e < endColumn; ++e) {
								Method[] methods=pojo.getMethods();					
								System.out.println("Index = " + e + ", Value = "+ gCells.get(e).getValue());
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

	public Integer getErrorRowNumber() {
		return errorRowNumber;
	}

//	public static void main(String[] args) {
//		ExcelToPojo x=new ExcelToPojo("com.venice.logistic.excel.FormatInvoiceRPX","FormatInvoiceRPXMap.xml","C:\\test.xls", 3);
//		
//		ArrayList<PojoInterface>result=x.getPojoResult();
//		
//		for(PojoInterface element:result){
//			FormatInvoiceRPX FormatInvoiceRPX=(FormatInvoiceRPX)element;			
//		}
//	}
}

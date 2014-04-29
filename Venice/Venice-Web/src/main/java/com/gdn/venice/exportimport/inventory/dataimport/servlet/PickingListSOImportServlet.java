package com.gdn.venice.exportimport.inventory.dataimport.servlet;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.request.PickPackageRequest;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.exportimport.finance.dataimport.servlet.FinanceImportServletConstants;
import com.gdn.venice.exportimport.inventory.dataimport.PickingListSO;
import com.gdn.venice.exportimport.logistics.dataimport.LogisticsServletConstants;
import com.gdn.venice.hssf.ExcelToPojo;
import com.gdn.venice.hssf.PojoInterface;
import com.gdn.venice.server.app.inventory.service.PickingListManagementService;
import com.gdn.venice.server.util.Util;

/**
 * 
 * @author Roland
 */
public class PickingListSOImportServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static Logger _log = null;

	private String notificationText = "";

	public PickingListSOImportServlet() {
		super();
		Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
		_log = loggerFactory.getLog4JLogger("com.gdn.venice.exportimport.inventory.dataimport.servlet.PickingListSOImportServlet");
	}

	   protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	    	boolean isMultipart = ServletFileUpload.isMultipartContent(request);
	    	System.out.println("PickingListSOImportServlet");
			
			notificationText = LogisticsServletConstants.JAVASCRIPT_ALERT_NOTIFICATION_TEXT_DEFAULT;
			
			if (isMultipart) {				
				process(request, response);				
			}
	    }
	    
	    @SuppressWarnings("unchecked")
		private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
	    	String username = Util.getUserName(request);
	    	String message = "";
			SimpleDateFormat sdf = new SimpleDateFormat(LogisticsServletConstants.DATE_TIME_FORMAT_STRING);
	    	String filePath = System.getenv("VENICE_HOME") + "/files/import/inventory/pickingList/";
			String fileName = "PickingListSOReport-"+sdf.format(new Date())+".xls";			
			System.out.println("Opening file for writing:" + filePath + fileName);

			File uploadedFile = new File(filePath + fileName);
			ServletFileUpload servletFileUpload = new ServletFileUpload(new DiskFileItemFactory());
			List<FileItem> fileItemsList = null;
			
			try {
				fileItemsList = servletFileUpload.parseRequest(request);
			} catch (FileUploadException e) {	
				e.printStackTrace();
				message = LogisticsServletConstants.EXCEPTION_TEXT_FILE_PARSE + e.getMessage();
				notificationText = notificationText.replaceFirst("REPLACE", message);			
			}
			
			Iterator<FileItem> it = fileItemsList.iterator();
			while (it.hasNext()) {
				FileItem fileItem = (FileItem) it.next();
				if (!fileItem.isFormField()) {
					try {
						fileItem.write(uploadedFile);
					} catch (Exception e) {
						e.printStackTrace();
						message = LogisticsServletConstants.EXCEPTION_TEXT_UPLOAD_FILE_WRITE + fileName + " :" + e.getMessage();						
						notificationText = notificationText.replaceFirst("REPLACE", message);
						response.getOutputStream().println(notificationText);
					}
				}
			}
			
			ExcelToPojo x = null;
			try {
				x = new ExcelToPojo(PickingListSO.class, System.getenv("VENICE_HOME") + "/files/template/PickingListSOReport.xml", filePath + fileName, 1, 0);
				x = x.getPojoToExcel(14, "1", "0");
			} catch (Exception e1) {
				e1.printStackTrace();
				message = LogisticsServletConstants.EXCEPTION_TEXT_UPLOAD_FILE_WRITE + fileName + " :" + e1.getMessage();				
				notificationText = notificationText.replaceFirst("REPLACE", message);
				response.getOutputStream().println(notificationText);
			}
									
			ArrayList<PojoInterface> result = x.getPojoResult();
						
			if(result.isEmpty()){
				try {
					System.out.println("pojo result empty");
					message = FinanceImportServletConstants.EXCEPTION_TEXT_ZERO_ROWS_TO_PROCESS;
					notificationText = notificationText.replaceFirst("REPLACE", message);
					response.getOutputStream().println(notificationText);
					throw new Exception(message);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			PickingListManagementService pickingListService = new PickingListManagementService();
	    	
			List<PickPackageRequest> pickPackageRequestList = new ArrayList<PickPackageRequest>();
	    	for (PojoInterface element : result) {
	    		PickingListSO pickingListSO = (PickingListSO) element;
				System.out.println("packageID: "+pickingListSO.getPackageId());
				System.out.println("merchantCode: "+pickingListSO.getMerchantCode());
				System.out.println("pickerName: "+pickingListSO.getPickerName());
				System.out.println("type: "+pickingListSO.getKeterangan());
				System.out.println("warehouseSkuId: "+pickingListSO.getWarehouseSkuId());
				System.out.println("shelfCode: "+pickingListSO.getShelfCode());
				System.out.println("storageCode: "+pickingListSO.getStorageCode());
				System.out.println("qtyPicked: "+pickingListSO.getQtyPicked());	
				System.out.println("containerID: "+pickingListSO.getContainerId());	
				
				PickPackageRequest pickPackageRequest = new PickPackageRequest();
				pickPackageRequest.setPackageId(pickingListSO.getPackageId());
				pickPackageRequest.setMerchantCode(pickingListSO.getMerchantCode());
				pickPackageRequest.setPickerName(pickingListSO.getPickerName());
				pickPackageRequest.setKeterangan(pickingListSO.getKeterangan());
				pickPackageRequest.setSalesOrderId(pickingListSO.getSalesOrderId());
				pickPackageRequest.setWarehouseSkuId(pickingListSO.getWarehouseSkuId());
				pickPackageRequest.setShelfCode(pickingListSO.getShelfCode());
				pickPackageRequest.setStorageCode(pickingListSO.getStorageCode());
				pickPackageRequest.setQtyPicked(pickingListSO.getQtyPicked());	
				pickPackageRequest.setContainerId(pickingListSO.getContainerId());			
				
				pickPackageRequestList.add(pickPackageRequest);
			}
	    	
			ResultWrapper<String> uploadResult = pickingListService.uploadPickingListSO(username, pickPackageRequestList);
			if(uploadResult!=null && uploadResult.isSuccess()){
				message = "Report uploaded successfully";
				notificationText = notificationText.replaceFirst("REPLACE", message);
			}else{
				message = "Report upload failed";
				notificationText = notificationText.replaceFirst("REPLACE", message);
			}
						
			response.getOutputStream().println(notificationText);			
	    }
}

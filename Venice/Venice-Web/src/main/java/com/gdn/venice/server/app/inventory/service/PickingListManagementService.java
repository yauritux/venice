package com.gdn.venice.server.app.inventory.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.Picker;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.module.outbound.InventoryRequestItem;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackage;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackageSalesOrder;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.exchange.entity.request.PickPackagePrintRequest;
import com.gdn.inventory.exchange.entity.request.PickPackageRequest;
import com.gdn.inventory.exchange.type.InventoryRequestStatus;
import com.gdn.inventory.exchange.type.SOStatus;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultListWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;

/**
*
* @author Roland
*/
public class PickingListManagementService{
	HttpClient httpClient;
	ObjectMapper mapper;
	protected static Logger _log = null;
	
	public PickingListManagementService() {
		httpClient = new HttpClient();
		mapper = new ObjectMapper();
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.service.PickingListManagementService");
	}
	
	public InventoryPagingWrapper<WarehouseItem> getPickingList(RafDsRequest request) throws HttpException, IOException{
		System.out.println("getPickingList");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickingList/getPickingList?warehouseId=" + request.getParams().get("warehouseId")
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<WarehouseItem>>() {});
        } else {
        	return null;
        }
	}	
	
	public ResultWrapper<PickingListDetail> getPickingListDetail(RafDsRequest request) throws HttpException, IOException{
		System.out.println("getPickingListDetail");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickingList/getDetail?username="+request.getParams().get("username")
                + "&warehouseItemId="+ request.getParams().get("warehouseItemId");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<PickingListDetail>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<WarehouseItem> getWarehouseItem(RafDsRequest request) throws HttpException, IOException{
		System.out.println("getWarehouseItem");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouseItem/getWarehouseItemById?&warehouseItemId="+ request.getParams().get("warehouseItemId");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<WarehouseItem>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<PickingListDetail> releasePickingLock(String username, String warehouseId) throws HttpException, IOException{
		System.out.println("releasePickingLock");
		System.out.println("username: "+username);
		System.out.println("warehouse id: "+warehouseId);
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickingList/releasePickingLock?username="+ username
                + "&warehouseId="+ warehouseId;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<PickingListDetail>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<PickingListDetail> submitPickingList(String username, PickingListDetail pickingListDetail) throws HttpException, IOException{
		System.out.println("submitPickingList");
		System.out.println("username: "+username);
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickingList/submitPickingList?username="+ username;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
		String json = mapper.writeValueAsString(pickingListDetail);
		System.out.println("json: "+json);
		httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
		httpPost.setRequestHeader("Content-Type", "application/json");
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<PickingListDetail>>() {});
        } else {
        	return null;
        }
	}
	
	public InventoryPagingWrapper<PickPackage> getPickingListIR(RafDsRequest request) throws HttpException, IOException{
		System.out.println("getPickingListIR");		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/getAllCreatedIR?username="+request.getParams().get("username")
				+ "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        Map<String, Object> searchMap = new HashMap<String, Object>();
        searchMap.put("status", InventoryRequestStatus.PICK_PACKAGE_CREATED.toString());
        searchMap.put("warehouseId", request.getParams().get("warehouseId"));
                
        String json = mapper.writeValueAsString(searchMap);
        System.out.println("json: "+json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
        httpPost.setRequestHeader("Content-Type", "application/json");
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<PickPackage>>() {});
        } else {
        	return null;
        }
	}	
	
	public ResultWrapper<PickPackage> getSinglePickingListIR(String username, String packageId) throws HttpException, IOException{
		System.out.println("getSinglePickingListIR");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/getDetail?id=" + packageId
                + "&username="+username;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<PickPackage>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultListWrapper<InventoryRequestItem> getIRItemByIRId(String iRId) throws HttpException, IOException{
		System.out.println("getPickingListItemIR");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "inventoryRequest/findInventoryRequestItemByInventoryRequestId?username=roland&id=" + iRId;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultListWrapper<InventoryRequestItem>>() {});
        } else {
        	return null;
        }
	}
	
    public InventoryPagingWrapper<Picker> getPickerDataByWarehouse(RafDsRequest request) throws HttpException, IOException {
    	System.out.println("getPickerDataByWarehouse");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "picker/findByFilter?page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        Map<String, Object> searchMap = new HashMap<String, Object>();
        searchMap.put("pickerStatus", "TRUE");
        searchMap.put("pickerWarehouseName", request.getParams().get("warehouseName"));
        
        String json = mapper.writeValueAsString(searchMap);
        System.out.println("json: "+json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<Picker>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<PickPackage> submitPicker(List<String> packageIdList, String pickerId) throws HttpException, IOException{
		System.out.println("submitPicker");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/submitPicker?pickerId="+ pickerId;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
		String json = mapper.writeValueAsString(packageIdList);
		System.out.println("json: "+json);
		httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
		httpPost.setRequestHeader("Content-Type", "application/json");
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<PickPackage>>() {});
        } else {
        	return null;
        }
	}
    
    public List<PickPackagePrintRequest> getPickPackageIRPrint(String pickerId, String warehouseId) throws HttpException, IOException{
		System.out.println("getPickPackageIRPrint");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/getPickPackageIRPrint?warehouseId=" + warehouseId
                + "&pickerId=" + pickerId;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<List<PickPackagePrintRequest>>() {});
        } else {
        	return null;
        }
	}
	
    public ResultWrapper<String> uploadPickingListIR(String username, List<PickPackageRequest> pickPackageRequest) throws HttpException, IOException{
		System.out.println("uploadPickingListIR");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/uploadPickPackageIR?username="+ username;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
		String json = mapper.writeValueAsString(pickPackageRequest);
		System.out.println("json: "+json);
		httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
		httpPost.setRequestHeader("Content-Type", "application/json");
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<String>>() {});
        } else {
        	return null;
        }
	}
    
    public InventoryPagingWrapper<PickPackage> getPickingListSO(RafDsRequest request) throws HttpException, IOException{
		System.out.println("getPickingListSO");		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/getAllCreatedSO?username="+request.getParams().get("username")
				+ "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        Map<String, Object> searchMap = new HashMap<String, Object>();
        searchMap.put("status", SOStatus.PICK_PACKAGE_CREATED.toString());
        searchMap.put("warehouseId", request.getParams().get("warehouseId"));
                
        String json = mapper.writeValueAsString(searchMap);
        System.out.println("json: "+json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
        httpPost.setRequestHeader("Content-Type", "application/json");
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<PickPackage>>() {});
        } else {
        	return null;
        }
	}
    
	public ResultListWrapper<PickPackageSalesOrder> getPickingListSODetail(String username, String packageId) throws HttpException, IOException{
		System.out.println("getPickingListSODetail");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/getListPickPackageSalesOrder?id=" + packageId
                + "&username="+username;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultListWrapper<PickPackageSalesOrder>>() {});
        } else {
        	return null;
        }
	}	
	
   public List<PickPackagePrintRequest> getPickPackageSOPrint(String pickerId, String warehouseId) throws HttpException, IOException{
		System.out.println("getPickPackageSOPrint");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/getPickPackageSOPrint?warehouseId=" + warehouseId
                + "&pickerId=" + pickerId;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<List<PickPackagePrintRequest>>() {});
        } else {
        	return null;
        }
	}
	
    public ResultWrapper<String> uploadPickingListSO(String username, List<PickPackageRequest> pickPackageRequest) throws HttpException, IOException{
		System.out.println("uploadPickingListSO");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickPackaging/uploadPickPackageSO?username="+ username;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
		String json = mapper.writeValueAsString(pickPackageRequest);
		System.out.println("json: "+json);
		httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
		httpPost.setRequestHeader("Content-Type", "application/json");
        
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<String>>() {});
        } else {
        	return null;
        }
	}
}


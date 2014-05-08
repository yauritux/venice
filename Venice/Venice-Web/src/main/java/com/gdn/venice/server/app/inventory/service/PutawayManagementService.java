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
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.Item;
import com.gdn.inventory.exchange.entity.PutawayRequest;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNoteItem;
import com.gdn.inventory.exchange.entity.module.inbound.Putaway;
import com.gdn.inventory.exchange.entity.module.inbound.PutawayDetail;
import com.gdn.inventory.exchange.entity.module.inbound.PutawayItem;
import com.gdn.inventory.exchange.type.StockType;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;

/**
*
* @author Roland
*/
public class PutawayManagementService{
	HttpClient httpClient;
	ObjectMapper mapper;
	protected static Logger _log = null;
	
	public PutawayManagementService() {
		httpClient = new HttpClient();
		mapper = new ObjectMapper();
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.service.GRNManagementService");
	}
	
	public WarehouseItem getWarehouseItemData(Long itemId, Long warehouseId, Long supplierId, StockType stockType) throws HttpException, IOException{
		System.out.println("getWarehouseItemData");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouseItem/getWarehouseItemByItemId?itemId=" +itemId+"&warehouseId="+warehouseId+"&supplierId="+supplierId;
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
		
		String json = mapper.writeValueAsString(stockType);
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
            return mapper.readValue(sb.toString(), new TypeReference<WarehouseItem>() {});
        } else {
        	return null;
        }
	}	
	
	public InventoryPagingWrapper<GoodReceivedNoteItem> getGRNItemDataListByWarehouseId(RafDsRequest request, String warehouseId) throws HttpException, IOException{
		System.out.println("getGRNItemDataListByWarehouseId");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/findItemByWarehouseId?warehouseId=" +warehouseId
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        if (request.getCriteria() != null) {
            Map<String, Object> searchMap = new HashMap<String, Object>();
            for (JPQLSimpleQueryCriteria criteria : request.getCriteria().getSimpleCriteria()) {
                System.out.println("adding criteria:" + criteria.getFieldName() + ", " + criteria.getValue());
                searchMap.put(criteria.getFieldName(), criteria.getValue());
            }
            String json = mapper.writeValueAsString(searchMap);
            System.out.println("json: " + json);
            httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
            httpPost.setRequestHeader("Content-Type", "application/json");
        } else {
            System.out.println("No criteria");
        }
    	        
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<GoodReceivedNoteItem>>() {});
        } else {
        	return null;
        }
	}
	
	public List<WarehouseItemStorageStock> getWarehouseItemStorageList(Long warehouseItemId) throws HttpException, IOException{
		System.out.println("getWarehouseItemStorageList");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/getWarehouseItemStorageList?warehouseItemId=" +warehouseItemId;
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
            return mapper.readValue(sb.toString(), new TypeReference<List<WarehouseItemStorageStock>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<Putaway> savePutaway(String username, List<PutawayItem> putawayItemList) 
			throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("savePutaway");
		String url=InventoryUtil.getStockholmProperties().getProperty("address")
				+ "putaway/addPutaway?username=" + username;
		System.out.println("url: "+url);
		PostMethod httpPost = new PostMethod(url);
		
		PutawayItem[] item = putawayItemList.toArray(new PutawayItem[0]);
		
		PutawayRequest putawayRequest = new PutawayRequest();
		putawayRequest.setPutawayItem(item);
		
		String json = mapper.writeValueAsString(putawayRequest);
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
			return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<Putaway>>() {});
		} else {
			return null;
		}
	}	
	
	public Item findItemById(String username, Long id) throws HttpException, IOException{
		System.out.println("findItemById");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "item/findById?username="+username+"&id=" +id;
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
            return mapper.readValue(sb.toString(), new TypeReference<Item>() {});
        } else {
        	return null;
        }
	}
	
	public InventoryPagingWrapper<Putaway> getPutawayListByWarehouseId(RafDsRequest request, String warehouseId) throws HttpException, IOException{
		System.out.println("getPutawayListByWarehouseId");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "putaway/getPutawayListByWarehouse?warehouseId=" +warehouseId
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
        
        if (request.getCriteria() != null) {
            Map<String, Object> searchMap = new HashMap<String, Object>();
            for (JPQLSimpleQueryCriteria criteria : request.getCriteria().getSimpleCriteria()) {
                System.out.println("adding criteria:" + criteria.getFieldName() + ", " + criteria.getValue());
                searchMap.put(criteria.getFieldName(), criteria.getValue());
            }
            String json = mapper.writeValueAsString(searchMap);
            System.out.println("json: " + json);
            httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
            httpPost.setRequestHeader("Content-Type", "application/json");
        } else {
            System.out.println("No criteria");
        }
    	        
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<Putaway>>() {});
        } else {
        	return null;
        }
	}
	
	public InventoryPagingWrapper<GoodReceivedNoteItem> getGRNItemDataListByGrnId(String grnId) throws HttpException, IOException{
		System.out.println("getGRNItemDataListByGrnId");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/findItemByGRNId?grnId=" +grnId+"&page=1&limit=20";
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<GoodReceivedNoteItem>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<PutawayDetail> savePutawayInputLocation(String username, List<PutawayDetail> putawayDetailList) 
			throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("savePutawayInputLocation");
		String url=InventoryUtil.getStockholmProperties().getProperty("address")
				+ "putaway/addPutawayInputLocation?username=" + username;
		System.out.println("url: "+url);
		PostMethod httpPost = new PostMethod(url);
		
		PutawayDetail[] putawayDetail = putawayDetailList.toArray(new PutawayDetail[0]);
		
		PutawayRequest putawayRequest = new PutawayRequest();
		putawayRequest.setPutawayDetail(putawayDetail);
		
		String json = mapper.writeValueAsString(putawayRequest);
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
			return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<PutawayDetail>>() {});
		} else {
			return null;
		}
	}
	
	public ResultWrapper<GoodReceivedNoteItem> getGRNItemDataListById(String grnItemId) throws HttpException, IOException{
		System.out.println("getGRNItemDataListById");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/findItemByGRNItemId?grnItemId=" +grnItemId;
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<GoodReceivedNoteItem>>() {});
        } else {
        	return null;
        }
	}
	
	public List<Putaway> getPutawayByGrnId(String grnId) throws HttpException, IOException{
		System.out.println("getPutawayByGrnId");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "putaway/getPutawayByGrnId?grnId=" +grnId;
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
            return mapper.readValue(sb.toString(), new TypeReference<List<Putaway>>() {});
        } else {
        	return null;
        }
	}
}


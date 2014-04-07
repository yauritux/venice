package com.gdn.venice.server.app.inventory.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.Attribute;
import com.gdn.inventory.exchange.entity.GrnRequest;
import com.gdn.inventory.exchange.entity.Item;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNote;
import com.gdn.inventory.exchange.entity.module.inbound.GoodReceivedNoteItem;
import com.gdn.inventory.exchange.type.StockType;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;

/**
*
* @author Roland
*/
public class GRNManagementService{
	HttpClient httpClient;
	ObjectMapper mapper;
	protected static Logger _log = null;
	
	public GRNManagementService() {
		httpClient = new HttpClient();
		mapper = new ObjectMapper();
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.service.GRNManagementService");
	}
	
	public InventoryPagingWrapper<GoodReceivedNote> getGRNDataList(RafDsRequest request) throws HttpException, IOException{
		_log.info("getGRNDataList");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/getCreatedList?"
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	
        if (request.getCriteria() != null) {
            Map<String, Object> searchMap = new HashMap<String, Object>();
            for (JPQLSimpleQueryCriteria criteria:request.getCriteria().getSimpleCriteria()) {
            	_log.debug("adding criteria:"+criteria.getFieldName()+", "+criteria.getValue());
                searchMap.put(criteria.getFieldName(), criteria.getValue());
            }
            String json = mapper.writeValueAsString(searchMap);
            _log.info("json: "+json);
            httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
            httpPost.setRequestHeader("Content-Type", "application/json");
        } else{
        	_log.debug("No criteria");
        }
        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<GoodReceivedNote>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<GoodReceivedNote> getGRNData(RafDsRequest request, String grnId) throws HttpException, IOException{
		_log.info("getGRNData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/getDetail?username=" + request.getParams().get("username")
                + "&grnId=" + grnId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
                 
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug("return value: "+sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<GoodReceivedNote>>() {});
        } else {
        	return null;
        }
	}		
	
	public InventoryPagingWrapper<GoodReceivedNoteItem> getGRNItemDataList(RafDsRequest request, String grnId) throws HttpException, IOException{
		_log.info("getGRNItemDataList");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/findItemByGRNId?grnId=" +grnId
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<GoodReceivedNoteItem>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<GoodReceivedNote> saveGrn(String username, GoodReceivedNote grn, List<GoodReceivedNoteItem> itemList) 
			throws JsonGenerationException, JsonMappingException, IOException {
		_log.info("saveGrn");
		String url=InventoryUtil.getStockholmProperties().getProperty("address")
				+ "goodReceivedNote/addGRN?username=" + username;
		_log.info("url: "+url);
		PostMethod httpPost = new PostMethod(url);
		
		GoodReceivedNoteItem[] item = itemList.toArray(new GoodReceivedNoteItem[0]);
		
		GrnRequest grnRequest = new GrnRequest();
		grnRequest.setGrn(grn);
		grnRequest.setGrnItem(item);
		
		String json = mapper.writeValueAsString(grnRequest);
		_log.info("json: "+json);
		httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

		httpPost.setRequestHeader("Content-Type", "application/json");

		int httpCode = httpClient.executeMethod(httpPost);
		_log.info("response code: "+httpCode);
		if (httpCode == HttpStatus.SC_OK) {
			InputStream is = httpPost.getResponseBodyAsStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			StringBuilder sb = new StringBuilder();
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				sb.append(line);
			}
			is.close();
			_log.debug(sb.toString());
			return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<GoodReceivedNote>>() {});
		} else {
			return null;
		}
	}	
	
	public ResultWrapper<AdvanceShipNoticeItem> findItemByASNItemId(String asnItemId) throws HttpException, IOException{
		_log.info("findItemByASNItemId");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "advanceShipNotice/findItemByASNItemId?asnItemId=" +asnItemId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<AdvanceShipNoticeItem>>() {});
        } else {
        	return null;
        }
	}
	
	public List<WarehouseItem> getWarehouseItemDataList(String itemId) throws HttpException, IOException{
		_log.info("getWarehouseItemDataList");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/getWarehouseItemByItem?itemId=" +itemId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<List<WarehouseItem>>() {});
        } else {
        	return null;
        }
	}
	
	public List<Attribute> getAttributeDataListByWarehouseItem(String warehouseItemId) throws HttpException, IOException{
		_log.info("getAttributeDataListByWarehouseItem");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "attribute/getAttributeByItem?itemId=" +warehouseItemId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<List<Attribute>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<GoodReceivedNoteItem> findItemByGRNItemId(String grnItemId) throws HttpException, IOException{
		_log.info("findItemByGRNItemId");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/findItemByGRNItemId?grnItemId=" +grnItemId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<GoodReceivedNoteItem>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<List<Attribute>> saveAttributesToCache(String username, String asnItemId, Set<String> attribute) throws JsonGenerationException, JsonMappingException, IOException {
		_log.info("saveAttributesToCache");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "goodReceivedNote/saveAttributteToCache?username=" + username + "&asnItemId=" + asnItemId;
        _log.info("url: "+url);
        PostMethod httpPost = new PostMethod(url);
        String json = mapper.writeValueAsString(attribute);
        _log.info("json: "+json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            _log.debug(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<Attribute>>>() {
            });
        } else {
            return null;
        }
    }
	
	public Item findItemByItemId(String itemId) throws HttpException, IOException{
		_log.info("findItemByItemId");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "item/findById?itemId=" +itemId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<Item>() {});
        } else {
        	return null;
        }
	}
	
	public WarehouseItem findWarehouseItem(String itemId, String warehouseId, String supplierId, StockType stockType) throws HttpException, IOException{
		_log.info("findWarehouseItem");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouseItem/getWarehouseItemByItemId?itemId=" +itemId+"&warehouseId="+warehouseId+"&supplierId="+supplierId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
        String json = mapper.writeValueAsString(stockType);
        _log.info("json: "+json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
        httpPost.setRequestHeader("Content-Type", "application/json");
    	        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<WarehouseItem>() {});
        } else {
        	return null;
        }
	}
}


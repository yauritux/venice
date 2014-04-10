package com.gdn.venice.server.app.inventory.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
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
import com.gdn.inventory.exchange.entity.AdvanceShipNotice;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalForm;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentFinalItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrder;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrderItem;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;

/**
*
* @author Roland
*/
public class ASNManagementService{
	HttpClient httpClient;
	ObjectMapper mapper;
	protected static Logger _log = null;
	
	public ASNManagementService() {
		httpClient = new HttpClient();
		mapper = new ObjectMapper();
		
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.server.app.inventory.service.ASNManagementService");
	}
	
	public InventoryPagingWrapper<AdvanceShipNotice> getASNDataList(RafDsRequest request) throws HttpException, IOException{
		_log.info("getASNDataList");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "advanceShipNotice/getActiveList?"
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	
        if (request.getCriteria() != null) {
        	_log.debug("criteria not null");
            Map<String, Object> searchMap = new HashMap<String, Object>();
            for (JPQLSimpleQueryCriteria criteria:request.getCriteria().getSimpleCriteria()) {
            	_log.debug("adding criteria:"+criteria.getFieldName()+", "+criteria.getValue());
                searchMap.put(criteria.getFieldName(), criteria.getValue());
            }
            String json = mapper.writeValueAsString(searchMap);
            _log.debug("json: "+json);
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<AdvanceShipNotice>>() {});
        } else {
        	return null;
        }
	}
	
	public InventoryPagingWrapper<PurchaseOrder> getPOData(RafDsRequest request, String reffNumber) throws HttpException, IOException{
		System.out.println("getPOData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "purchaseOrder/getAllCreated?username=" + request.getParams().get("username")
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: "+url);
    	
        Map<String, Object> searchMap = new HashMap<String, Object>();
        
        JPQLSimpleQueryCriteria numberCriteria = new JPQLSimpleQueryCriteria();
        numberCriteria.setFieldName("keyword");
        numberCriteria.setOperator("equals");
        numberCriteria.setValue(reffNumber);
        numberCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.INV_PO_NUMBER));
				
        System.out.println("adding criteria:"+numberCriteria.getFieldName()+", "+numberCriteria.getValue());
        searchMap.put(numberCriteria.getFieldName(), numberCriteria.getValue());
        
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
            System.out.println("return value: "+sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<PurchaseOrder>>() {});
        } else {
        	return null;
        }
	}
	
	public InventoryPagingWrapper<ConsignmentFinalForm> getCFFData(RafDsRequest request, String reffNumber) throws HttpException, IOException{
		_log.info("getCFFData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "consignmentFinal/findByFilter?username=" + request.getParams().get("username")
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
    	
        Map<String, Object> searchMap = new HashMap<String, Object>();
        
        JPQLSimpleQueryCriteria criteria = new JPQLSimpleQueryCriteria();
        criteria.setFieldName("cffNumber");
        criteria.setOperator("equals");
        criteria.setValue(reffNumber);
        criteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.INV_CFF_NUMBER));
        
        _log.debug("adding criteria:"+criteria.getFieldName()+", "+criteria.getValue());
        searchMap.put(criteria.getFieldName(), criteria.getValue());
        
        String json = mapper.writeValueAsString(searchMap);
        _log.debug(json);
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
            _log.debug("return value: "+sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<ConsignmentFinalForm>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<PurchaseOrderItem> getPOItemData(RafDsRequest request, String id) throws HttpException, IOException{
		_log.info("getPOItemData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "purchaseOrder/getDetailItem?username=" + request.getParams().get("username")
                + "&id=" + id;
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<PurchaseOrderItem>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<ConsignmentFinalItem> getCFFItemData(RafDsRequest request, Long id) throws HttpException, IOException{
		_log.info("getCFFItemData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "consignmentFinal/getDetailItem?username=" + request.getParams().get("username")
                + "&cffItemId=" + id;
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ConsignmentFinalItem>>() {});
        } else {
        	return null;
        }
	}
		
	public InventoryPagingWrapper<AdvanceShipNoticeItem> getASNItemData(RafDsRequest request, String id) throws JsonGenerationException, JsonMappingException, IOException {
		_log.info("getASNItemData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "advanceShipNotice/findItemByASNId?&asnId=" + id
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
		_log.info("url: "+url);
        PostMethod httpPost = new PostMethod(url);
                
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
            _log.debug("return value: "+sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<AdvanceShipNoticeItem>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<AdvanceShipNoticeItem> getSingleASNItemData(String asnItemId) throws HttpException, IOException{
		_log.info("getSingleASNItemData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "advanceShipNotice/findItemByASNItemId?asnItemId=" + asnItemId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
                 
        int httpCode = httpClient.executeMethod(httpPost);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug("return value: "+sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<AdvanceShipNoticeItem>>() {});
        } else {
        	return null;
        }
	}	
}


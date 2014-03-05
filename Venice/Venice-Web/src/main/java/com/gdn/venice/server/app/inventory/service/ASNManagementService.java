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
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.gdn.inventory.exchange.entity.AdvanceShipNotice;
import com.gdn.inventory.exchange.entity.AdvanceShipNoticeItem;
import com.gdn.inventory.exchange.entity.ConsignmentFinalForm;
import com.gdn.inventory.exchange.entity.module.inbound.ConsignmentApprovalItem;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseOrder;
import com.gdn.inventory.exchange.entity.module.inbound.PurchaseRequisitionItem;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultListWrapper;
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
	
	public ASNManagementService() {
		httpClient = new HttpClient();
		mapper = new ObjectMapper();
	}
	
	public InventoryPagingWrapper<AdvanceShipNotice> getASNDataList(RafDsRequest request) throws HttpException, IOException{
		System.out.println("getASNDataList");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "advanceShipNotice/getActiveList?"
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
    	System.out.println("url: "+url);
    	
        if (request.getCriteria() != null) {
        	System.out.println("criteria not null");
            Map<String, Object> searchMap = new HashMap<String, Object>();
            for (JPQLSimpleQueryCriteria criteria:request.getCriteria().getSimpleCriteria()) {
            	System.out.println("adding criteria:"+criteria.getFieldName()+", "+criteria.getValue());
                searchMap.put(criteria.getFieldName(), criteria.getValue());
            }
            String json = mapper.writeValueAsString(searchMap);
            System.out.println("json: "+json);
            httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
            httpPost.setRequestHeader("Content-Type", "application/json");
        } else{
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<AdvanceShipNotice>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<AdvanceShipNotice> getASNData(RafDsRequest request, String asnId) throws HttpException, IOException{
		System.out.println("getASNData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "advanceShipNotice/getDetail?username=" + request.getParams().get("username")
                + "&asnId=" + asnId;
        PostMethod httpPost = new PostMethod(url);
    	System.out.println(url);
                 
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<AdvanceShipNotice>>() {});
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
    	System.out.println(url);
    	
        Map<String, Object> searchMap = new HashMap<String, Object>();
        
        JPQLSimpleQueryCriteria numberCriteria = new JPQLSimpleQueryCriteria();
        numberCriteria.setFieldName("keyword");
        numberCriteria.setOperator("equals");
        numberCriteria.setValue(reffNumber);
        numberCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.INV_PO_NUMBER));
				
        System.out.println("adding criteria:"+numberCriteria.getFieldName()+", "+numberCriteria.getValue());
        searchMap.put(numberCriteria.getFieldName(), numberCriteria.getValue());
        
        String json = mapper.writeValueAsString(searchMap);
        System.out.println(json);
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
		System.out.println("getCFFData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "consignmentFinal/findByFilter?username=" + request.getParams().get("username")
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
    	System.out.println(url);
    	
        Map<String, Object> searchMap = new HashMap<String, Object>();
        
        JPQLSimpleQueryCriteria criteria = new JPQLSimpleQueryCriteria();
        criteria.setFieldName("cffNumber");
        criteria.setOperator("equals");
        criteria.setValue(reffNumber);
        criteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.INV_CFF_NUMBER));
        
        System.out.println("adding criteria:"+criteria.getFieldName()+", "+criteria.getValue());
        searchMap.put(criteria.getFieldName(), criteria.getValue());
        
        String json = mapper.writeValueAsString(searchMap);
        System.out.println(json);
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<ConsignmentFinalForm>>() {});
        } else {
        	return null;
        }
	}
		
	public InventoryPagingWrapper<AdvanceShipNoticeItem> getASNItemData(RafDsRequest request, String id) throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("getASNItemData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "advanceShipNotice/findItemByASNId?&asnId=" + id
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
		System.out.println("url: "+url);
        PostMethod httpPost = new PostMethod(url);
                
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
            System.out.println("return value: "+sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<AdvanceShipNoticeItem>>() {});
        } else {
        	return null;
        }
	}

	public ResultListWrapper<PurchaseRequisitionItem> getPRItemData(RafDsRequest request, long id) throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("getPRItemData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "purchaseRequisition/findPrItemByPrId?username=" + request.getParams().get("username")
                + "&id=" + id
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
		System.out.println("url: "+url);
        PostMethod httpPost = new PostMethod(url);
                
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
            System.out.println("return value: "+sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultListWrapper<PurchaseRequisitionItem>>() {});
        } else {
        	return null;
        }
	}

	public ResultListWrapper<ConsignmentApprovalItem> getCAFItemData(RafDsRequest request, long id) throws JsonGenerationException, JsonMappingException, IOException {
		System.out.println("getCAFItemData");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "consignmentApprovalForm/findItemCafByCafId?username=" + request.getParams().get("username")
                + "&id=" + id
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
		System.out.println("url: "+url);
        PostMethod httpPost = new PostMethod(url);
                
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
            System.out.println("return value: "+sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultListWrapper<ConsignmentApprovalItem>>() {});
        } else {
        	return null;
        }
	}
}


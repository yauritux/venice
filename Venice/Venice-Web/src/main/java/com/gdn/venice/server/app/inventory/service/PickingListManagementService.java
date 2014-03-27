package com.gdn.venice.server.app.inventory.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.paging.InventoryPagingWrapper;
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
		_log.info("getPickingListDetail");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickingList/getDetail?username="+request.getParams().get("username")
                + "&warehouseItemId="+ request.getParams().get("warehouseItemId");
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<PickingListDetail>>() {});
        } else {
        	return null;
        }
	}
	
	public ResultWrapper<WarehouseItem> getWarehouseItem(RafDsRequest request) throws HttpException, IOException{
		_log.info("getWarehouseItem");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouseItem/getWarehouseItemById?&warehouseItemId="+ request.getParams().get("warehouseItemId");
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
}


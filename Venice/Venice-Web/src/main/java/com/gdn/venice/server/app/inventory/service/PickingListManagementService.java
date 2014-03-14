package com.gdn.venice.server.app.inventory.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.WarehouseItem;
import com.gdn.inventory.exchange.entity.module.outbound.PickingListDetail;
import com.gdn.inventory.paging.InventoryPagingWrapper;
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
		_log.info("getPickingList");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickingList/getPickingList?warehouseId=" + request.getParams().get("warehouseId")
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
        System.out.println("url: "+url);
        
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<WarehouseItem>>() {});
        } else {
        	return null;
        }
	}	
	
	public PickingListDetail getPickingListDetail(RafDsRequest request, String warehouseItemId) throws HttpException, IOException{
		_log.info("getPickingListDetail");
		System.out.println("getPickingListDetail");
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "pickingList/getDetail?username="+request.getParams().get("username")
                + "&warehouseId="+warehouseItemId;
        PostMethod httpPost = new PostMethod(url);
        _log.info("url: "+url);
        
        int httpCode = httpClient.executeMethod(httpPost);
        _log.info("response code: "+httpCode);
        System.out.println("response code: "+httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            _log.debug(sb.toString());
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<PickingListDetail>() {});
        } else {
        	return null;
        }
	}	
}


package com.gdn.venice.server.app.inventory.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.gdn.inventory.exchange.entity.Department;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;

/**
*
* @author Roland
*/
public class DepartmentManagementService{
	HttpClient httpClient;
	ObjectMapper mapper;
	
	public DepartmentManagementService() {
		httpClient = new HttpClient();
		mapper = new ObjectMapper();
	}
	
	public InventoryPagingWrapper<Department> getAllDepartmentData(long page, long limit) throws HttpException, IOException{
		System.out.println("getAllDepartmentData");
		
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "department/getList?"
                + "page=" + page
                + "&limit=" + limit;
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<Department>>() {});
        } else {
        	return null;
        }
	}
}


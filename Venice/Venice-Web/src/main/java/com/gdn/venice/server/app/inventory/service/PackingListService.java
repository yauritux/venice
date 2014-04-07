/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.service;

import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.gdn.inventory.exchange.entity.AttributeName;
import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.exchange.entity.module.outbound.SalesOrderAWBInfo;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.HeaderAndDetailWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.ByteArrayRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 *
 * @author Maria Olivia
 */
public class PackingListService {

    HttpClient httpClient;
    ObjectMapper mapper;

    public PackingListService() {
        httpClient = new HttpClient();
        mapper = new ObjectMapper();
    }

    public InventoryPagingWrapper<AWBInfo> getReadyPackingData(RafDsRequest request) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/getPackingList?warehouseId=" + request.getParams().get("warehouseId")
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println(url);

        if (request.getCriteria() != null) {
            System.out.println("criteria not null");
            Map<String, Object> searchMap = new HashMap<String, Object>();
            for (JPQLSimpleQueryCriteria criteria : request.getCriteria().getSimpleCriteria()) {
                System.out.println("adding criteria:" + criteria.getFieldName() + ", " + criteria.getValue());
                searchMap.put(criteria.getFieldName(), criteria.getValue());
            }
            String json = mapper.writeValueAsString(searchMap);
            System.out.println(json);
            httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
            httpPost.setRequestHeader("Content-Type", "application/json");
        } else {
            System.out.println("No criteria");
        }

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println(httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<AWBInfo>>() {
            });
        } else {
            return null;
        }
    }

    public HeaderAndDetailWrapper<String, SalesOrderAWBInfo> getDetailPackingData(String awbInfoId, String username) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/getDetail?awbInfoId=" + awbInfoId
                + "&username=" + username;
        PostMethod httpPost = new PostMethod(url);
        System.out.println(url);

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println(httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<HeaderAndDetailWrapper<String, SalesOrderAWBInfo>>() {
            });
        } else {
            return null;
        }
    }

    public List<AttributeName> getAttributeNameListByItemId(String itemId, String username) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "attributeName/findByItemId?id=" + itemId
                + "&username=" + username;
        PostMethod httpPost = new PostMethod(url);
        System.out.println(url);

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println(httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<List<AttributeName>>() {
            });
        } else {
            return new ArrayList<AttributeName>();
        }
    }

    public ResultWrapper<Map<String, String>> saveAttributes(String username, String salesOrderId, Set<String> attribute) throws JsonGenerationException, JsonMappingException, IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/checkAttribute?username=" + username + "&salesOrderId=" + salesOrderId;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);
        String json = mapper.writeValueAsString(attribute);
        System.out.println(json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println(httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<Map<String, String>>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<AWBInfo> savePacking(String username, String awbInfoId) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/submitPacking?username=" + username + "&awbInfoId=" + awbInfoId;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println(httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<AWBInfo>>() {
            });
        } else {
            return null;
        }
    }
    
    

    public ResultWrapper<AWBInfo> rejectPacking(String username, String salesOrderId) throws JsonGenerationException, JsonMappingException, IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/rejectPacking?username=" + username + "&salesOrderId=" + salesOrderId;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println(httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<AWBInfo>>() {
            });
        } else {
            return null;
        }
    }
}

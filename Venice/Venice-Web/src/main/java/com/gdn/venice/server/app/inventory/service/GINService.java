/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.service;

import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.exchange.entity.module.outbound.GoodIssuedNote;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
public class GINService {

    HttpClient httpClient;
    ObjectMapper mapper;

    public GINService() {
        httpClient = new HttpClient();
        mapper = new ObjectMapper();
    }

    public InventoryPagingWrapper<GoodIssuedNote> getGinData(RafDsRequest request) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "gin/getGinList?warehouseCode=" + request.getParams().get("warehouseCode")
                + "&pageNumber=" + request.getParams().get("page")
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<GoodIssuedNote>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<List<AWBInfo>> getGinDetailData(String ginId) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/getGinDetail?ginId=" + ginId;
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<AWBInfo>>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<AWBInfo> getAwbDetail(String awbNumber) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "gin/getAwb?awbNumber=" + awbNumber;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);

        int httpCode = httpClient.executeMethod(httpPost);
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
    
    public ResultWrapper<GoodIssuedNote> saveGIN(String username, GoodIssuedNote newGin, String awbNumberArray) throws JsonGenerationException, JsonMappingException, IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "gin/createGin?username=" + username+"&awbNumberArray="+awbNumberArray;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);
        String json = mapper.writeValueAsString(newGin);
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<GoodIssuedNote>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<List<AWBInfo>> getAwbList(String ginId) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "gin/getAwbList?ginId=" + ginId;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);

        int httpCode = httpClient.executeMethod(httpPost);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<AWBInfo>>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<com.gdn.inventory.exchange.beans.GoodIssuedNote> getGinForPrint(String ginId) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "gin/printGin?ginId=" + ginId;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);

        int httpCode = httpClient.executeMethod(httpPost);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<com.gdn.inventory.exchange.beans.GoodIssuedNote>>() {
            });
        } else {
            return null;
        }
    }
}

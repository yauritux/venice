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
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.gdn.inventory.exchange.entity.Warehouse;
import com.gdn.inventory.exchange.entity.WarehouseWIP;
import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;

/**
 *
 * @author Maria Olivia
 */
public class WarehouseManagementService {

    HttpClient httpClient;
    ObjectMapper mapper;

    public WarehouseManagementService() {
        httpClient = new HttpClient();
        mapper = new ObjectMapper();
    }

    public InventoryPagingWrapper<Warehouse> getWarehouseData(RafDsRequest request) throws HttpException, IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouse/findByFilter?username=" + request.getParams().get("username")
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<Warehouse>>() {
            });
        } else {
            return null;
        }
    }

    public InventoryPagingWrapper<WarehouseWIP> getWarehouseInProcessData(RafDsRequest request) throws JsonGenerationException, JsonMappingException, IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouse/findInProcessByFilter?username=" + request.getParams().get("username")//);
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);

        Map<String, Object> searchMap = new HashMap<String, Object>();
        searchMap.put(DataNameTokens.INV_WAREHOUSE_APPROVALTYPE, request.getParams().get(DataNameTokens.INV_WAREHOUSE_APPROVALTYPE));
        if (request.getCriteria() != null) {
            for (JPQLSimpleQueryCriteria criteria : request.getCriteria().getSimpleCriteria()) {
                System.out.println(criteria.getValue());
                if (criteria.getFieldName().equals(DataNameTokens.INV_WAREHOUSE_APPROVALSTATUS)) {
                    searchMap.put(criteria.getFieldName(), ApprovalStatus.valueOf(criteria.getValue()));
                } else {
                    searchMap.put(criteria.getFieldName(), criteria.getValue());
                }
            }
        }

        String json = mapper.writeValueAsString(searchMap);
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<WarehouseWIP>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<WarehouseWIP> findInProcessById(String username, String id) throws HttpException, IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouse/findInProcessById?username=" + username + "&id=" + id;
        System.out.println("get warehouse wip: " + url);
        GetMethod httpGet = new GetMethod(url);
        int httpCode = httpClient.executeMethod(httpGet);

        if (httpCode == HttpStatus.SC_OK) {
            System.out.println("status OK");
            InputStream is = httpGet.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<WarehouseWIP>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<WarehouseWIP> saveOrUpdateWarehouseInProcess(String username, WarehouseWIP warehouse) throws JsonGenerationException, JsonMappingException, IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouse/saveOrUpdateInProcess?username=" + username;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);
        String json = mapper.writeValueAsString(warehouse);
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<WarehouseWIP>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<Warehouse> findByCode(String warehouseCode) throws HttpException, IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "warehouse/findByCode?warehouseCode=" + warehouseCode;
        GetMethod httpGet = new GetMethod(url);
        int httpCode = httpClient.executeMethod(httpGet);

        if (httpCode == HttpStatus.SC_OK) {
            System.out.println("status OK");
            InputStream is = httpGet.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<Warehouse>>() {
            });
        } else {
            return null;
        }
    }
}

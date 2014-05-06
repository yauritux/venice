/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.service;

import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.gdn.inventory.exchange.entity.Supplier;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.entity.module.outbound.Opname;
import com.gdn.inventory.exchange.entity.module.outbound.OpnameDetail;
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
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

/**
 *
 * @author Maria Olivia
 */
public class OpnameService {

    HttpClient httpClient;
    ObjectMapper mapper;

    public OpnameService() {
        httpClient = new HttpClient();
        mapper = new ObjectMapper();
    }

    public ResultWrapper<List<WarehouseItemStorageStock>> getStorageItemData(RafDsRequest request) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "opname/getItemForOpname?warehouseCode=" + request.getParams().get("warehouseCode")
                + "&stockType=" + request.getParams().get("stockType")
                + "&supplierCode=" + request.getParams().get("supplierCode");
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<WarehouseItemStorageStock>>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<List<WarehouseItemStorageStock>> getStorageItemData(List<Long> idList) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "opname/getItemForOpnameById";
        PostMethod httpPost = new PostMethod(url);
        System.out.println(url);

        String json = mapper.writeValueAsString(idList);
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
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<WarehouseItemStorageStock>>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<List<Supplier>> getSupplierData(String warehouseCode, String stockType) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "supplier/getByWarehouse?warehouseCode=" + warehouseCode
                + "&stockType=" + stockType;
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<Supplier>>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<List<OpnameDetail>> saveOpnameList(String username, List<Long> itemStorageId,
            String warehouseCode, String stockType, String supplierCode) throws Exception {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "opname/createOpnameList?username=" + username + "&warehouseCode=" + warehouseCode
                + "&stockType=" + stockType + "&supplierCode=" + supplierCode;
        System.out.println(url);
        PostMethod httpPost = new PostMethod(url);
        String json = mapper.writeValueAsString(itemStorageId);
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<OpnameDetail>>>() {
            });
        } else {
            return null;
        }
    }

    public InventoryPagingWrapper<Opname> getOpnameData(RafDsRequest request) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "opname/getOpnameList?page=" + request.getParams().get("page")
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
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<Opname>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<List<OpnameDetail>> getOpnameDetailData(String opnameId) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "opname/getDetailByOpnameId?opnameId=" + opnameId;
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<OpnameDetail>>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<String> saveOrUpdateOpnameDetail(String opnameId, OpnameDetail newOpnameDetail, String username) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "opname/saveOrUpdateOpnameDetail?username=" + username;
        if (opnameId != null && !opnameId.trim().isEmpty()) {
            url += "&opnameId=" + opnameId;
        }

        PostMethod httpPost = new PostMethod(url);
        System.out.println(url);

        String json = mapper.writeValueAsString(newOpnameDetail);
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
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<String>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<String> submitOpnameAdjustment(String username, String opnameId) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "opname/submitOpname?username=" + username + "&opnameId=" + opnameId;

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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<String>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<List<String>> getCategory() throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "item/getExistingCategory";
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<String>>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<List<String>> getUoM() throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "item/getExistingUoM";
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<String>>>() {
            });
        } else {
            return null;
        }
    }
}

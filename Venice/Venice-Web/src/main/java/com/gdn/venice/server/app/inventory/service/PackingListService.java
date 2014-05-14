/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.server.app.inventory.service;

import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.gdn.inventory.exchange.dto.PackingList;
import com.gdn.inventory.exchange.entity.AttributeName;
import com.gdn.inventory.exchange.entity.module.outbound.AWBInfo;
import com.gdn.inventory.exchange.entity.module.outbound.PickPackageSalesOrder;
import com.gdn.inventory.exchange.type.PickPackageStatus;
import com.gdn.inventory.wrapper.HeaderAndDetailWrapper;
import com.gdn.inventory.wrapper.ResultListWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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

    public ResultListWrapper<PackingList> getReadyPackingData(RafDsRequest request) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/getPackingList?warehouseId=" + request.getParams().get("warehouseId");

        if (request.getCriteria() != null) {
            System.out.println("criteria not null");
            for (JPQLSimpleQueryCriteria criteria : request.getCriteria().getSimpleCriteria()) {
                if (DataNameTokens.INV_PACKING_PICKPACKAGE_CONTAINERID.equalsIgnoreCase(criteria.getFieldName())) {
                    url += "&containerId=" + criteria.getValue();
                } else if (DataNameTokens.INV_PACKING_PICKPACKAGE_STATUS.equalsIgnoreCase(criteria.getFieldName())) {
                    url += "&ppStatus=" + PickPackageStatus.valueOf(criteria.getValue());
                }
            }
        } else {
            System.out.println("No criteria");
        }
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultListWrapper<PackingList>>() {
            });
        } else {
            return null;
        }
    }

    public HeaderAndDetailWrapper<String, PickPackageSalesOrder> getDetailPackingData(String pickPackageId, String username) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/getDetail?pickPackageId=" + pickPackageId
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
            return mapper.readValue(sb.toString(), new TypeReference<HeaderAndDetailWrapper<String, PickPackageSalesOrder>>() {
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

    public ResultWrapper<AWBInfo> savePacking(String username, String pickPackageId, String awbNumber) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "packingList/submitPacking?username=" + username
                + "&pickPackageId=" + pickPackageId
                + "&awbNumber=" + awbNumber;
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

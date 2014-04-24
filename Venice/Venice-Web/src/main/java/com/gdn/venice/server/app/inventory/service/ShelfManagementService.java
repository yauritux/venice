package com.gdn.venice.server.app.inventory.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
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
import com.gdn.inventory.exchange.entity.Shelf;
import com.gdn.inventory.exchange.entity.ShelfRequest;
import com.gdn.inventory.exchange.entity.ShelfWIP;
import com.gdn.inventory.exchange.entity.Storage;
import com.gdn.inventory.exchange.entity.StorageWIP;
import com.gdn.inventory.exchange.entity.WarehouseItemStorageStock;
import com.gdn.inventory.exchange.type.ApprovalStatus;
import com.gdn.inventory.paging.InventoryPagingWrapper;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.util.InventoryUtil;

/**
 *
 * @author Roland
 */
public class ShelfManagementService {

    HttpClient httpClient;
    ObjectMapper mapper;

    public ShelfManagementService() {
        httpClient = new HttpClient();
        mapper = new ObjectMapper();
    }

    public InventoryPagingWrapper<Shelf> getShelfData(RafDsRequest request) throws HttpException, IOException {
        System.out.println("getShelfData");

        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/findByFilter?username=" + request.getParams().get("username")
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: " + url);

        if (request.getCriteria() != null) {
            System.out.println("criteria not null");
            Map<String, Object> searchMap = new HashMap<String, Object>();
            for (JPQLSimpleQueryCriteria criteria : request.getCriteria().getSimpleCriteria()) {
                System.out.println("adding criteria:" + criteria.getFieldName() + ", " + criteria.getValue());
                searchMap.put(criteria.getFieldName(), criteria.getValue());
            }
            String json = mapper.writeValueAsString(searchMap);
            System.out.println("json: " + json);
            httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
            httpPost.setRequestHeader("Content-Type", "application/json");
        } else {
            System.out.println("No criteria");
        }

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<Shelf>>() {
            });
        } else {
            return null;
        }
    }

    public InventoryPagingWrapper<ShelfWIP> getShelfInProcessData(RafDsRequest request) throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("getShelfInProcessData");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/findWIPByFilter?username=" + request.getParams().get("username")
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        Map<String, Object> searchMap = new HashMap<String, Object>();
        searchMap.put(DataNameTokens.INV_SHELF_APPROVALTYPE, request.getParams().get(DataNameTokens.INV_SHELF_APPROVALTYPE));
        if (request.getCriteria() != null) {
            for (JPQLSimpleQueryCriteria criteria : request.getCriteria().getSimpleCriteria()) {
                System.out.println(criteria.getValue());
                if (criteria.getFieldName().equals(DataNameTokens.INV_SHELF_APPROVALSTATUS)) {
                    searchMap.put(criteria.getFieldName(), ApprovalStatus.valueOf(criteria.getValue()));
                } else {
                    searchMap.put(criteria.getFieldName(), criteria.getValue());
                }
            }
        }

        String json = mapper.writeValueAsString(searchMap);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));
        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println("return value: " + sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<ShelfWIP> findById(String username, String id) throws HttpException, IOException {
        System.out.println("findInProcessById");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/findById?username=" + username + "&id=" + id;
        System.out.println("url: " + url);
        GetMethod httpGet = new GetMethod(url);
        int httpCode = httpClient.executeMethod(httpGet);

        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpGet.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }

    public ResultWrapper<ShelfWIP> findShelfWIPById(String username, String id) throws HttpException, IOException {
        System.out.println("findShelfWIPById");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/findWIPById?username=" + username + "&id=" + id;
        System.out.println("url: " + url);
        GetMethod httpGet = new GetMethod(url);
        int httpCode = httpClient.executeMethod(httpGet);

        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpGet.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> saveShelfWIP(String username, ShelfWIP shelf, List<Storage> storageList)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("saveShelfWIP");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/saveShelfWIP?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        Storage[] storage = storageList.toArray(new Storage[0]);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);
        shelfRequest.setStorage(storage);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> approveCreateShelfWIP(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("approveCreateShelfWIP");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/approveCreateShelfWIP?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> rejectCreateShelfWIP(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("rejectCreateShelfWIP");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/rejectCreateShelfWIP?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> needCorrectionCreateShelfWIP(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("needCorrectionCreateShelfWIP");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/needCorrectionCreateShelfWIP?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> nonActiveCreateShelfWIP(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("nonActiveCreateShelfWIP");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/nonActiveCreateShelfWIP?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> approveNonActiveShelf(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("approveNonActiveShelf");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/approveNonActiveShelf?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> rejectNonActiveShelf(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("rejectNonActiveShelf");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/rejectNonActiveShelf?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> editShelf(String username, ShelfWIP shelf, List<Storage> storageList)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("editShelf");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/editShelf?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        Storage[] storage = storageList.toArray(new Storage[0]);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);
        shelfRequest.setStorage(storage);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }

    public InventoryPagingWrapper<StorageWIP> getStorageData(RafDsRequest request, Long shelfId) throws HttpException, IOException {
        System.out.println("getStorageData");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/findStorageByShelf?username=" + request.getParams().get("username")
                + "&shelfId=" + shelfId
                + "&page=" + request.getParams().get("page")
                + "&limit=" + request.getParams().get("limit");
        PostMethod httpPost = new PostMethod(url);
        System.out.println("url: " + url);

        httpClient = new HttpClient();
        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            System.out.println(sb.toString());
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<InventoryPagingWrapper<StorageWIP>>() {
            });
        } else {
            return null;
        }
    }

    /*
     * Added by Maria Olivia - 20140414
     */
    public ResultWrapper<List<WarehouseItemStorageStock>> getStorageByItemData(String warehouseCode,
            String stockType, String supplierCode, String itemCode) throws IOException {
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/getItemStorageByWarehouseItemDetail?warehouseCode=" + warehouseCode
                + "&stockType=" + stockType + "&itemCode=" + itemCode;
        if (supplierCode != null && !supplierCode.trim().isEmpty()) {
            url += "&supplierCode=" + supplierCode;
        }
        
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
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<WarehouseItemStorageStock>>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> rejectEditShelf(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("rejectEditShelf");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/rejectEditShelf?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> approveEditShelf(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("approveEditShelf");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/approveEditShelf?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> needCorrectionEditShelf(String username, ShelfWIP shelf)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("needCorrectionEditShelf");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/needCorrectionEditShelf?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> editShelfAddWIP(String username, ShelfWIP shelf, List<Storage> storageList)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("editShelfAddWIP");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/editShelfAddWIP?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        Storage[] storage = storageList.toArray(new Storage[0]);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);
        shelfRequest.setStorage(storage);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
    
    public ResultWrapper<ShelfWIP> editShelfEditWIP(String username, ShelfWIP shelf, List<Storage> storageList)
            throws JsonGenerationException, JsonMappingException, IOException {
        System.out.println("editShelfEditWIP");
        String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "shelf/editShelfEditWIP?username=" + username;
        System.out.println("url: " + url);
        PostMethod httpPost = new PostMethod(url);

        Storage[] storage = storageList.toArray(new Storage[0]);

        ShelfRequest shelfRequest = new ShelfRequest();
        shelfRequest.setShelfWIP(shelf);
        shelfRequest.setStorage(storage);

        String json = mapper.writeValueAsString(shelfRequest);
        System.out.println("json: " + json);
        httpPost.setRequestEntity(new ByteArrayRequestEntity(json.getBytes(), "application/json"));

        httpPost.setRequestHeader("Content-Type", "application/json");

        int httpCode = httpClient.executeMethod(httpPost);
        System.out.println("response code: " + httpCode);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            System.out.println(sb.toString());
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<ShelfWIP>>() {
            });
        } else {
            return null;
        }
    }
}

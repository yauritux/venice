package com.gdn.venice.server.app.inventory.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.gdn.inventory.exchange.entity.WarehouseUser;
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.InventoryUtil;

/**
 * Fetch Command for warehouse combo box
 * 
 * @author Roland
 */

public class FetchWarehouseComboBoxDataCommand implements RafRpcCommand{	
	String username;
	
	public FetchWarehouseComboBoxDataCommand(String username) {
		this.username = username;
	}

	public String execute() {
		HashMap<String, String> map = new HashMap<String, String>();
		try{		
			username = "roland";
			ResultWrapper<List<WarehouseUser>> whuWrapper = getWarehouseUserData(username);
			if(whuWrapper!=null){
				for(WarehouseUser wu : whuWrapper.getContent()){
    				map.put("data"+wu.getWarehouse().getId().toString(), wu.getWarehouse().getName());
                }
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return Util.formXMLfromHashMap(map);
	}	
	
	public ResultWrapper<List<WarehouseUser>> getWarehouseUserData(String username) throws HttpException, IOException{
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
                + "user/getWarehouseList?username=" + username;
        PostMethod httpPost = new PostMethod(url);
    	
    	HttpClient httpClient= new HttpClient();
    	ObjectMapper mapper = new ObjectMapper();
        int httpCode = httpClient.executeMethod(httpPost);
        if (httpCode == HttpStatus.SC_OK) {
            InputStream is = httpPost.getResponseBodyAsStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
            }
            is.close();
            return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<WarehouseUser>>>() {});
        } else {
        	return null;
        }
	}
}

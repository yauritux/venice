package com.gdn.venice.server.app.inventory.command;

import com.gdn.inventory.exchange.entity.WarehouseUser;
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
import com.gdn.inventory.wrapper.ResultWrapper;
import com.gdn.venice.server.command.RafRpcCommand;
import com.gdn.venice.server.util.Util;
import com.gdn.venice.util.InventoryUtil;

/**
 * Fetch Command for warehouse combo box
 *
 * @author Roland
 */
public class FetchWarehouseComboBoxDataCommand implements RafRpcCommand {

	String username;
	boolean isCode;

	public FetchWarehouseComboBoxDataCommand(String username, boolean isCode) {
		this.username = username;
		this.isCode = isCode;
	}

	/*
	 * Edited by Maria Olivia 20140320
	 */
	public String execute() {
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			ResultWrapper<List<WarehouseUser>> whuWrapper = getWarehouseUserData(username);
			if(whuWrapper != null){
				if (whuWrapper.isSuccess()) {
					for (WarehouseUser wu : whuWrapper.getContent()) {
						if (isCode) {
							map.put("data" + wu.getWarehouse().getCode(), wu.getWarehouse().getName());
						} else {
							map.put("data" + wu.getWarehouse().getId().toString(), wu.getWarehouse().getName());
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Util.formXMLfromHashMap(map);
	}

	public ResultWrapper<List<WarehouseUser>> getWarehouseUserData(String username) throws HttpException, IOException {
		String url = InventoryUtil.getStockholmProperties().getProperty("address")
				+ "user/getWarehouseList?username=" + username;
		PostMethod httpPost = new PostMethod(url);
		HttpClient httpClient = new HttpClient();
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
			System.out.println(sb.toString());
			return mapper.readValue(sb.toString(), new TypeReference<ResultWrapper<List<WarehouseUser>>>() {
			});
		} else {
			return null;
		}
	}
}

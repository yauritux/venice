/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gdn.venice.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

/**
 *
 * @author Maria Olivia
 */
public class InventoryUtil {    
    protected static final String STOCKHOLM_PROPERTIES_FILE = System.getenv("VENICE_HOME") + "/conf/stockholm.properties";
    
    public static Properties getStockholmProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(STOCKHOLM_PROPERTIES_FILE));
        } catch (Exception e) {
//            _log.error("Error getting airwaybill-engine.properties", e);
            e.printStackTrace();
            return null;
        }
        return properties;
    }

    private static String getUserRole(String username) {
        BufferedReader in = null;
        String role = null;
        try {
            String url = getStockholmProperties().getProperty("address") + "role/?serviceType=MarginTransactionFeeRequest&orderItemId=" + username;
            URL obj = new URL(url);

            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");

            if (con.getResponseCode() == HttpStatus.SC_OK) {
                in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                if (response != null) {
                    JSONObject jsonObject = new JSONObject(response.toString());
                    if (jsonObject.getBoolean("success")) {
                        role = jsonObject.getString("role");
                    }
                }
            }
        } catch (Exception e) {
            
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ex) {
                    
                }
            }
        }
        return role;
    }
    
    public static boolean isApprover(String username) {
        if (InventoryUtil.getUserRole(username).toLowerCase().contains("warehouse_approver")) {
            return true;
        } else {
            return false;
        }
    }
    
    public static HashMap<String, String> convertToHashMap(String s) {
	    String[] arr = s.split(", ");
	    String str = null;
	    HashMap<String, String> map = new HashMap<String, String>();
	    for (int i=0;i<arr.length;i++) {
	    	str = arr[i].replace("{", "").replace("}", "");
	        String[] splited = str.split("=");

	        map.put(splited[0], splited[1]);
	    }
	    
	    return map;
	}
}

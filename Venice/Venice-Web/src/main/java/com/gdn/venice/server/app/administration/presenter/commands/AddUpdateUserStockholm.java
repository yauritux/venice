package com.gdn.venice.server.app.administration.presenter.commands;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.inventory.exchange.entity.User;

public class AddUpdateUserStockholm {
	protected static Logger _log = null;
	protected static final String STOCKHOLM_PROPERTIES_FILE = System.getenv("VENICE_HOME") + "/conf/stockholm.properties";
	
	public AddUpdateUserStockholm(){
        Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
        _log = loggerFactory.getLog4JLogger("com.gdn.venice.administration.AddUpdateUserStockholm");
	}
	
    private Properties getStockholmProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(STOCKHOLM_PROPERTIES_FILE));
        } catch (Exception e) {
            _log.error("Error getting stockholm.properties", e);
            e.printStackTrace();
            return null;
        }
        return properties;
    }
    
    public Boolean addUser(String username, User stockholmUser) throws JsonGenerationException, JsonMappingException, IOException{
		String url = getStockholmProperties().getProperty("address")+ "user/saveOrUpdate?username="+username;
		_log.info("Service address: " + url);
		
		HttpClient client = new HttpClient();
		PostMethod request = new PostMethod(url);
		int responseCode = 0;
		
		ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(stockholmUser);
        request.setRequestHeader("Content-type", "application/json");
        request.setRequestEntity(new StringRequestEntity(json, "application/json", "UTF-8"));

        BufferedReader br = null;
        
		try {			
			responseCode = client.executeMethod(request);
			_log.info("response status: " + responseCode);
			
			 if (responseCode != 0) {
				if (responseCode == HttpStatus.SC_OK) {
					_log.info("service call to Stockholm success");
			
	                br = new BufferedReader(new InputStreamReader(request.getResponseBodyAsStream()));
	                
	                String inputLine;
	                StringBuffer sb = new StringBuffer();
	                
	                _log.debug("reading response");
	                while ((inputLine = br.readLine()) != null) {
	                    sb.append(inputLine);
	                }
	                
	                _log.debug("response value: "+sb.toString());
	
				}else {
					_log.error("service call to Stockholm service failed, response code: " + responseCode);
					return false;
				}
			 }
		}catch (Exception e) {
			_log.error("exception when call Stockholm service", e);
			return false;
		}finally{
			request.releaseConnection();
			try {
				if (br != null) br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

		return true;
	}
}


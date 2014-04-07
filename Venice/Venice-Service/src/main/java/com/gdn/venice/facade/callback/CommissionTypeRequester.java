package com.gdn.venice.facade.callback;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.ejb.EJBException;

import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import com.djarum.raf.utilities.Log4jLoggerFactory;
import com.gdn.venice.dto.CommissionTypeTemp;
import com.gdn.venice.persistence.VenOrder;
import com.gdn.venice.persistence.VenOrderItem;
import com.gdn.venice.persistence.VenSettlementRecord;



public class CommissionTypeRequester {
	protected static Logger _log = null;
	 protected static final String AIRWAYBILL_ENGINE_PROPERTIES_FILE = System.getenv("VENICE_HOME") + "/conf/airwaybill-engine.properties";

	CommissionTypeRequester() {
	    Log4jLoggerFactory loggerFactory = new Log4jLoggerFactory();
	    _log = loggerFactory.getLog4JLogger("com.gdn.venice.facade.callback.CommissionTypeRequester");
	}
	
    private Properties getAirwayBillEngineProperties() {
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(AIRWAYBILL_ENGINE_PROPERTIES_FILE));
        } catch (Exception e) {
            _log.error("Error getting airwaybill-engine.properties", e);
            e.printStackTrace();
            return null;
        }
        return properties;
    }
	
	public List<VenSettlementRecord> getCommissionType(VenOrder order) {
        _log.debug("start getCommissionType from MTA");

		List<VenOrderItem> itemList = order.getVenOrderItems();
		List<VenSettlementRecord> settlementList = new ArrayList<VenSettlementRecord>();
		String merchantIds = new String();
        ObjectMapper mapper = new ObjectMapper();

		StringBuilder result = new StringBuilder();
		for(VenOrderItem oi : itemList){
		        result.append(oi.getVenMerchantProduct().getVenMerchant().getWcsMerchantId());
		        result.append(",");
		}
		
	    merchantIds = result.length() > 0 ? result.substring(0, result.length() - 1): "";        

        try {
            String url = getAirwayBillEngineProperties().getProperty("mtaAddress") + "merchantWS?merchantCodes=" + merchantIds;
            URL obj = new URL(url);
            _log.info("Service: " + url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", "GdnWS/1.0");

            if (con.getResponseCode() == HttpStatus.SC_OK) {
                _log.info("service call to MTA service success, response status: " + con.getResponseCode());
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                if (response != null) {
                    _log.info("result from MTA: " + response.toString());
                    List<CommissionTypeTemp> commTypeTemp = mapper.readValue(response.toString(), new TypeReference<List<CommissionTypeTemp>>() {});
        				
                	for(VenOrderItem oi : itemList){
                		for(CommissionTypeTemp temp : commTypeTemp){
                			if(temp.getMerchantCode().equalsIgnoreCase(oi.getVenMerchantProduct().getVenMerchant().getWcsMerchantId())){
                        		VenSettlementRecord settlement = new VenSettlementRecord();
                        		settlement.setVenOrderItem(oi);
                				settlement.setSettlementRecordTimestamp(new Timestamp(System.currentTimeMillis()));
                				settlement.setCommissionType(temp.getCommissionType());
                				settlementList.add(settlement);
                			}
                		}                    		
                	}				                    
                }
            } else {
                _log.error("service call to MTA service failed, response status: " + con.getResponseCode());
                throw new EJBException("service call to MTA service failed, response status: " + con.getResponseCode());
            }
        } catch (Exception e) {
            _log.error("service call to MTA service failed", e);
            throw new EJBException(e.getMessage());
        }

        return settlementList;
    }
}

package com.gdn.venice.server.app.fraud.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.FrdCustomerWhitelistSessionEJBRemote;
import com.gdn.venice.persistence.FrdCustomerWhitelist;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Delete Command for Customer Whitelist Maintenance
 * 
 * @author Arifin
 */

public class DeleteCustomerWhiteListDataCommand implements RafDsCommand {
	RafDsRequest request;
	
	public DeleteCustomerWhiteListDataCommand(RafDsRequest request) {
		this.request = request;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<FrdCustomerWhitelist> whiteistList = new ArrayList<FrdCustomerWhitelist>();		
		List<HashMap<String,String >> dataList = request.getData();		
		FrdCustomerWhitelist entityWhitelist = new FrdCustomerWhitelist();
		
		for (int i=0;i<dataList.size();i++) {
			Map<String, String> data = dataList.get(i);
			Iterator<String> iter = data.keySet().iterator();

			while (iter.hasNext()) {
				String key = iter.next();
				if (key.equals(DataNameTokens.FRDCUSTOMERWHITELIST_CUSTOMERWHITELISTID)) {
					entityWhitelist.setId(new Long(data.get(DataNameTokens.FRDCUSTOMERWHITELIST_CUSTOMERWHITELISTID)));
				} 
			}						
			whiteistList.add(entityWhitelist);			
		}
				
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			FrdCustomerWhitelistSessionEJBRemote sessionHome = (FrdCustomerWhitelistSessionEJBRemote) locator.lookup(FrdCustomerWhitelistSessionEJBRemote.class, "FrdCustomerWhitelistSessionEJBBean");
						
			JPQLAdvancedQueryCriteria criteria = new JPQLAdvancedQueryCriteria();
			criteria.setBooleanOperator("or");
			for (int i=0;i<whiteistList.size();i++) {
				JPQLSimpleQueryCriteria simpleCriteria = new JPQLSimpleQueryCriteria();
				simpleCriteria.setFieldName(DataNameTokens.FRDCUSTOMERWHITELIST_CUSTOMERWHITELISTID);
				simpleCriteria.setOperator("equals");
				simpleCriteria.setValue(whiteistList.get(i).getId().toString());
				simpleCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.FRDCUSTOMERWHITELIST_CUSTOMERWHITELISTID));
				criteria.add(simpleCriteria);
			}
			
			whiteistList = sessionHome.findByFrdCustomerWhitelistLike(entityWhitelist, criteria, request.getStartRow(), request.getEndRow());
			sessionHome.removeFrdCustomerWhitelistList((ArrayList<FrdCustomerWhitelist>)whiteistList);			
									
			rafDsResponse.setStatus(0);
		} catch (Exception ex) {
			ex.printStackTrace();
			rafDsResponse.setStatus(-1);
		} finally {
			try {
				if(locator!=null){
					locator.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}		
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}

package com.gdn.venice.server.app.seattle.presenter.commands;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote;
import com.gdn.venice.persistence.SeatFulfillmentInPercentage;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Update Command for UpdateSLAFulfillmenDataCommand
 * 
 * @author Roland
 */

public class UpdateSLAFulfillmenDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	public UpdateSLAFulfillmenDataCommand(RafDsRequest request, String username) {
		this.request = request;
		this.username = username;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<SeatFulfillmentInPercentage> entityFulfillList = new ArrayList<SeatFulfillmentInPercentage>();		
		List<HashMap<String,String >> dataList = request.getData();		
		SeatFulfillmentInPercentage entityFulfill = new SeatFulfillmentInPercentage();
		
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			SeatFulfillmentInPercentageSessionEJBRemote sessionHome = (SeatFulfillmentInPercentageSessionEJBRemote) locator.lookup(SeatFulfillmentInPercentageSessionEJBRemote.class, "SeatFulfillmentInPercentageSessionEJBBean");
			
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);				
				try{
						entityFulfill = sessionHome.queryByRange("select o from SeatFulfillmentInPercentage o where o.fulfillmentInPercentageId = "+ new Long(data.get(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_ID)), 0, 1).get(0);
				}catch(NumberFormatException e){
						entityFulfill.setFulfillmentInPercentageId(new Long(data.get(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_ID)));
				}				
				if(data.get(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MIN)!=null){
					entityFulfill.setMin(new BigDecimal(data.get(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MIN)));
				}
				if(data.get(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MAX)!=null){
					entityFulfill.setMax(new BigDecimal(data.get(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MAX)));		
				}							
				entityFulfill.setByUser(username);
				entityFulfill.setUpdateDate(new Timestamp(System.currentTimeMillis()));
				entityFulfillList.add(entityFulfill);			
			}
			
			sessionHome.mergeSeatFulfillmentInPercentageList((ArrayList<SeatFulfillmentInPercentage>)entityFulfillList);
			
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

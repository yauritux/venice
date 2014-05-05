package com.gdn.venice.server.app.seattle.presenter.commands;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.SeatStatusUomSessionEJBRemote;
import com.gdn.venice.persistence.SeatStatusUom;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Update Command for UpdateUoMDataCommand
 * 
 * @author Roland
 */

public class UpdateUoMDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	public UpdateUoMDataCommand(RafDsRequest request, String username) {
		this.request = request;
		this.username = username;
	}

	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<SeatStatusUom> entityUoMList = new ArrayList<SeatStatusUom>();		
		List<HashMap<String,String >> dataList = request.getData();		
		SeatStatusUom entityUoM = new SeatStatusUom();
		
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			SeatStatusUomSessionEJBRemote sessionHome = (SeatStatusUomSessionEJBRemote) locator.lookup(SeatStatusUomSessionEJBRemote.class, "SeatStatusUomSessionEJBBean");
			
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);				
				try{
						entityUoM = sessionHome.queryByRange("select o from SeatStatusUom o where o.statusUomId = "+ new Long(data.get(DataNameTokens.SEATSTATUSUOM_ID)), 0, 1).get(0);
				}catch(NumberFormatException e){
						entityUoM.setStatusUomId(new Long(data.get(DataNameTokens.SEATSTATUSUOM_ID)));
				}							
				if(data.get(DataNameTokens.SEATSTATUSUOM_STATUSUOMFROM)!=null){
					entityUoM.setStatusUomFrom(new BigDecimal(data.get(DataNameTokens.SEATSTATUSUOM_STATUSUOMFROM)));
				}
				if(data.get(DataNameTokens.SEATSTATUSUOM_STATUSUOMEND)!=null){
					entityUoM.setStatusUomEnd(new BigDecimal(data.get(DataNameTokens.SEATSTATUSUOM_STATUSUOMEND)));		
				}					
				entityUoM.setByUser(username);
				entityUoM.setUpdateDate(new Timestamp(System.currentTimeMillis()));
				entityUoMList.add(entityUoM);			
			}
			
			sessionHome.mergeSeatStatusUomList((ArrayList<SeatStatusUom>)entityUoMList);
			
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

package com.gdn.venice.server.app.seattle.presenter.commands;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote;
import com.gdn.venice.facade.SeatSlaStatusSessionEJBRemote;
import com.gdn.venice.persistence.SeatSlaStatus;
import com.gdn.venice.persistence.SeatSlaStatusPercentage;
import com.gdn.venice.persistence.SeatStatusUom;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Update Command for UpdateSLAStatusDataCommand
 * 
 * @author Roland
 */

public class UpdateSLAStatusDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	public UpdateSLAStatusDataCommand(RafDsRequest request, String username) {
		this.request = request;
		this.username = username;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<SeatSlaStatusPercentage> entityStatusList = new ArrayList<SeatSlaStatusPercentage>();		
		List<HashMap<String,String >> dataList = request.getData();		
		SeatSlaStatusPercentage entityStatus = new SeatSlaStatusPercentage();
		
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			SeatSlaStatusPercentageSessionEJBRemote sessionHome = (SeatSlaStatusPercentageSessionEJBRemote) locator.lookup(SeatSlaStatusPercentageSessionEJBRemote.class, "SeatSlaStatusPercentageSessionEJBBean");
			SeatSlaStatusSessionEJBRemote sessionSlaHome = (SeatSlaStatusSessionEJBRemote) locator.lookup(SeatSlaStatusSessionEJBRemote.class, "SeatSlaStatusSessionEJBBean");
			
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);				
				try{
						entityStatus = sessionHome.queryByRange("select o from SeatSlaStatusPercentage o where o.seatSlaStatusPercentageId = "+ new Long(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_ID)), 0, 1).get(0);
				}catch(NumberFormatException e){
						entityStatus.setSeatSlaStatusPercentageId(new Long(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_ID)));
				}				
				if(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_MAX)!=null){
					entityStatus.setMax(new BigDecimal(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_MAX)));		
				}
				if(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_MIN)!=null){
					entityStatus.setMin(new BigDecimal(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_MIN)));
				}		
				if(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SLA)!=null){
					entityStatus.getSeatSlaStatus().setSla(new BigDecimal(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SLA)));
				}						
				
				entityStatus.getSeatSlaStatus().setByUser(username);
				
				SeatSlaStatus seatSlaStatus= entityStatus.getSeatSlaStatus(); 
				seatSlaStatus.setByUser(username);
				seatSlaStatus.setUpdateDate(new Timestamp(System.currentTimeMillis()));
				if(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_ID)!=null){					
					SeatStatusUom statusUoM = new SeatStatusUom();
					statusUoM.setStatusUomId(new Long(data.get(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_ID)));
					seatSlaStatus.setSeatStatusUom(statusUoM);
				}	
				seatSlaStatus = sessionSlaHome.mergeSeatSlaStatus(seatSlaStatus);
				entityStatus.setSeatSlaStatus(seatSlaStatus);
				
				entityStatusList.add(entityStatus);			
			}
			
			sessionHome.mergeSeatSlaStatusPercentageList((ArrayList<SeatSlaStatusPercentage>)entityStatusList);
			
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

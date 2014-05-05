package com.gdn.venice.server.app.seattle.presenter.commands;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.VenHolidaySessionEJBRemote;
import com.gdn.venice.persistence.VenHoliday;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Update Command for UpdateHolidayDataCommand
 * 
 * @author Roland
 */

public class UpdateHolidayDataCommand implements RafDsCommand {
	RafDsRequest request;
	String username;
	
	public UpdateHolidayDataCommand(RafDsRequest request, String username) {
		this.request = request;
		this.username = username;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		
		List<VenHoliday> entityHolidayList = new ArrayList<VenHoliday>();		
		List<HashMap<String,String >> dataList = request.getData();		
		VenHoliday entityHoliday = new VenHoliday();
		
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			VenHolidaySessionEJBRemote sessionHome = (VenHolidaySessionEJBRemote) locator.lookup(VenHolidaySessionEJBRemote.class, "VenHolidaySessionEJBBean");
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
			
			for (int i=0;i<dataList.size();i++) {
				Map<String, String> data = dataList.get(i);				
				try{
						entityHoliday = sessionHome.queryByRange("select o from VenHoliday o where o.holidayId = "+ new Long(data.get(DataNameTokens.HOLIDAY_ID)), 0, 1).get(0);
				}catch(NumberFormatException e){
						entityHoliday.setHolidayId(new Long(data.get(DataNameTokens.HOLIDAY_ID)));
				}				
				if(data.get(DataNameTokens.HOLIDAY_DATE)!=null){
					entityHoliday.setHolidayDate(formatter.parse(data.get(DataNameTokens.HOLIDAY_DATE)));		
				}
				if(data.get(DataNameTokens.HOLIDAY_DESKRIPSI)!=null){
					entityHoliday.setDeskripsi(data.get(DataNameTokens.HOLIDAY_DESKRIPSI));
				}										
				entityHolidayList.add(entityHoliday);			
			}			
			sessionHome.mergeVenHolidayList((ArrayList<VenHoliday>)entityHolidayList);
			
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

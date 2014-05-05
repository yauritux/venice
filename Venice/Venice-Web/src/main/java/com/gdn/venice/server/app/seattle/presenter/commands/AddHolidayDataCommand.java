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

public class AddHolidayDataCommand implements RafDsCommand {

	RafDsRequest request;
	
	public AddHolidayDataCommand(RafDsRequest request){
		this.request=request;
		
	}
	//VenHoliday.java
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		Locator<VenHoliday> locator=null;
		List<HashMap<String, String>> dataList=new ArrayList<HashMap<String,String>>();
		try{
			locator = new Locator<VenHoliday>();
			VenHolidaySessionEJBRemote sessionHome = (VenHolidaySessionEJBRemote) locator.lookup(VenHolidaySessionEJBRemote.class, "VenHolidaySessionEJBBean");
			dataList=request.getData();
			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
			 for(int i=0;i< dataList.size();i++){
				Map<String, String> data = dataList.get(i);	
				VenHoliday binContact = new VenHoliday();
					if(data.get(DataNameTokens.HOLIDAY_DATE)!=null){
						binContact.setHolidayDate(formatter.parse(data.get(DataNameTokens.HOLIDAY_DATE)));				
					}
					if(data.get(DataNameTokens.HOLIDAY_DESKRIPSI)!=null){
						binContact.setDeskripsi(data.get(DataNameTokens.HOLIDAY_DESKRIPSI));						
					} 
					binContact=sessionHome.persistVenHoliday(binContact);
			}			
			rafDsResponse.setStatus(0);					
		}catch(Exception e){
			e.printStackTrace();
			rafDsResponse.setStatus(-1);
		}finally{
			try{
				if(locator!=null){
					locator.close();
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		rafDsResponse.setData(dataList);
		return rafDsResponse;
	}
}

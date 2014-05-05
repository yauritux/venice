package com.gdn.venice.server.app.seattle.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.VenHolidaySessionEJBRemote;
import com.gdn.venice.persistence.VenHoliday;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

public class FetchHolidayDataCommand implements RafDsCommand {

	RafDsRequest request;
	String userName;	
	
	public FetchHolidayDataCommand(RafDsRequest request, String userName){
		this.request=request;
		this.userName = userName;
		
	}
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();			
		Locator<Object> locator = null;
		try{		
			List<VenHoliday> VenHolidayList = null;				
			locator = new Locator<Object>();
			VenHolidaySessionEJBRemote sessionHome = (VenHolidaySessionEJBRemote) locator.lookup(VenHolidaySessionEJBRemote.class, "VenHolidaySessionEJBBean");
			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			
			
			if (criteria == null) {				
				String sql ="select o from VenHoliday o ";
				VenHolidayList = sessionHome.queryByRange(sql, 0, 0);					
			} else {				
				VenHoliday bl = new VenHoliday();			
				VenHolidayList = sessionHome.findByVenHolidayLike(bl, criteria, 0, 0);				
			}
			for(int i=0; i<VenHolidayList.size();i++){
				HashMap<String, String> map = new HashMap<String, String>();
				VenHoliday list = VenHolidayList.get(i);
				map.put(DataNameTokens.HOLIDAY_ID, list.getHolidayId().toString());
				map.put(DataNameTokens.HOLIDAY_DATE, Util.isNull(list.getHolidayDate(),"").toString());	
				map.put(DataNameTokens.HOLIDAY_DESKRIPSI, Util.isNull(list.getDeskripsi(),"").toString());				
				dataList.add(map);				
			}
			rafDsResponse.setStatus(0);
			rafDsResponse.setStartRow(request.getStartRow());
			rafDsResponse.setTotalRows(dataList.size());
			rafDsResponse.setEndRow(request.getStartRow()+dataList.size());
		}catch(Exception e){
			e.printStackTrace();
			rafDsResponse.setStatus(-1);
		}finally{			
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
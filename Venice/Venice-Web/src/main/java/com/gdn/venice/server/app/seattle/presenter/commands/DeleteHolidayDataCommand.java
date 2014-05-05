package com.gdn.venice.server.app.seattle.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.VenHolidaySessionEJBRemote;
import com.gdn.venice.persistence.VenHoliday;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;

/**
 * Delete DeleteHolidayDataCommand
 * 
 * @author Arifin
 */

public class DeleteHolidayDataCommand implements RafDsCommand {
	RafDsRequest request;
	
	public DeleteHolidayDataCommand(RafDsRequest request) {
		this.request = request;
	}
	
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		//VenHoliday
		List<VenHoliday> holidayKpiList = new ArrayList<VenHoliday>();		
		List<HashMap<String,String >> dataList = request.getData();		
		VenHoliday entityHoliday = new VenHoliday();
		
		for (int i=0;i<dataList.size();i++) {
			Map<String, String> data = dataList.get(i);
			Iterator<String> iter = data.keySet().iterator();

			while (iter.hasNext()) {
				String key = iter.next();
				if (key.equals(DataNameTokens.HOLIDAY_ID)) {
					entityHoliday.setHolidayId(new Long(data.get(DataNameTokens.HOLIDAY_ID)));
				} 
			}						
			holidayKpiList.add(entityHoliday);			
		}
				
		Locator<Object> locator = null;
		try {
			locator = new Locator<Object>();
			VenHolidaySessionEJBRemote sessionHome = (VenHolidaySessionEJBRemote) locator.lookup(VenHolidaySessionEJBRemote.class, "VenHolidaySessionEJBBean");
						
			JPQLAdvancedQueryCriteria criteria = new JPQLAdvancedQueryCriteria();
			criteria.setBooleanOperator("or");
			for (int i=0;i<holidayKpiList.size();i++) {
				JPQLSimpleQueryCriteria simpleCriteria = new JPQLSimpleQueryCriteria();
				simpleCriteria.setFieldName(DataNameTokens.HOLIDAY_ID);
				simpleCriteria.setOperator("equals");
				simpleCriteria.setValue(holidayKpiList.get(i).getHolidayId().toString());
				simpleCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.HOLIDAY_ID));
				criteria.add(simpleCriteria);
			}
			holidayKpiList = sessionHome.findByVenHolidayLike(entityHoliday, criteria, 0, 0);
			sessionHome.removeVenHolidayList((ArrayList<VenHoliday>)holidayKpiList);			
									
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

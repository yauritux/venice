package com.gdn.venice.server.app.seattle.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.SeatFulfillmentInPercentageSessionEJBRemote;
import com.gdn.venice.persistence.SeatFulfillmentInPercentage;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;
public class FetchSLAFulfillmenDataCommand implements RafDsCommand {

	RafDsRequest request;
	String userName;	
	
	public FetchSLAFulfillmenDataCommand(RafDsRequest request, String userName){
		this.request=request;
		this.userName = userName;
		
	}
	@Override
	public RafDsResponse execute() {
		String userRole = request.getParams().get("userRole");
		userRole=userRole.contains("#")?userRole.replace("#", ""):userRole;
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();			
		Locator<Object> locator = null;
		try{		
			List<SeatFulfillmentInPercentage> seatFulfillmentInPercentageList = null;				
			locator = new Locator<Object>();
			SeatFulfillmentInPercentageSessionEJBRemote sessionHome = (SeatFulfillmentInPercentageSessionEJBRemote) locator.lookup(SeatFulfillmentInPercentageSessionEJBRemote.class, "SeatFulfillmentInPercentageSessionEJBBean");
			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			
			
			if (criteria == null) {				
				String sql ="select o from SeatFulfillmentInPercentage o where o.seatOrderStatus.pic = '"+userRole+"'";
				System.out.println("get data : "+sql);
				seatFulfillmentInPercentageList = sessionHome.queryByRange(sql, 0, 0);					
			} else {				
				SeatFulfillmentInPercentage bl = new SeatFulfillmentInPercentage();
				
				criteria.setBooleanOperator("and");
				JPQLSimpleQueryCriteria caseIdCriteria = new JPQLSimpleQueryCriteria();
				caseIdCriteria.setFieldName(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_PIC);
				caseIdCriteria.setOperator("equals");
				caseIdCriteria.setValue(userRole);
				caseIdCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_PIC));
				criteria.add(caseIdCriteria);				
				seatFulfillmentInPercentageList = sessionHome.findBySeatFulfillmentInPercentageLike(bl, criteria, 0, 0);				
			}
			for(int i=0; i<seatFulfillmentInPercentageList.size();i++){
				HashMap<String, String> map = new HashMap<String, String>();
				SeatFulfillmentInPercentage list = seatFulfillmentInPercentageList.get(i);
				map.put(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_ID, list.getFulfillmentInPercentageId().toString());
				map.put(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_VENORDERSTATUS_CODE, Util.isNull(list.getSeatOrderStatus().getVenOrderStatus().getOrderStatusCode(),"").toString());	
				map.put(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATRESULTSTATUSTRACKING_DESC, Util.isNull(list.getSeatResultStatusTracking().getResultStatusTrackingDesc(),"").toString());	
				map.put(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MIN, Util.isNull(list.getMin(),"").toString());	
				map.put(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_MAX, Util.isNull(list.getMax(),"").toString());	
				map.put(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_BYUSER, Util.isNull(list.getByUser(),"").toString());	
				map.put(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_UPDATEDATE, Util.isNull(list.getUpdateDate(),"").toString());	
				map.put(DataNameTokens.SEATFULFILLMENTINPERCENTAGE_SEATORDERSTATUS_PIC, Util.isNull(list.getSeatOrderStatus().getPic(),"").toString());	
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
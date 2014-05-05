package com.gdn.venice.server.app.seattle.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.SeatSlaStatusPercentageSessionEJBRemote;
import com.gdn.venice.persistence.SeatSlaStatusPercentage;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;
public class FetchSLAStatusDataCommand implements RafDsCommand {

	RafDsRequest request;
	String userName;	
	
	public FetchSLAStatusDataCommand(RafDsRequest request, String userName){
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
			List<SeatSlaStatusPercentage> SeatSlaStatusPercentageList = null;				
			locator = new Locator<Object>();
			SeatSlaStatusPercentageSessionEJBRemote sessionHome = (SeatSlaStatusPercentageSessionEJBRemote) locator.lookup(SeatSlaStatusPercentageSessionEJBRemote.class, "SeatSlaStatusPercentageSessionEJBBean");
			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			
			
			if (criteria == null) {				
				String sql ="select o from SeatSlaStatusPercentage o where o.seatSlaStatus.seatOrderStatus.pic = '"+userRole+"'";
				System.out.println("get data : "+sql);
				SeatSlaStatusPercentageList = sessionHome.queryByRange(sql, 0, 0);					
			} else {				
				SeatSlaStatusPercentage bl = new SeatSlaStatusPercentage();
				
				criteria.setBooleanOperator("and");
				JPQLSimpleQueryCriteria caseIdCriteria = new JPQLSimpleQueryCriteria();
				caseIdCriteria.setFieldName(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_PIC);
				caseIdCriteria.setOperator("equals");
				caseIdCriteria.setValue(userRole);
				caseIdCriteria.setFieldClass(DataNameTokens.getDataNameToken().getFieldClass(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_PIC));
				criteria.add(caseIdCriteria);				
				SeatSlaStatusPercentageList = sessionHome.findBySeatSlaStatusPercentageLike(bl, criteria, 0, 0);				
			}
			for(int i=0; i<SeatSlaStatusPercentageList.size();i++){
				HashMap<String, String> map = new HashMap<String, String>();
				SeatSlaStatusPercentage list = SeatSlaStatusPercentageList.get(i);
				
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_ID, list.getSeatSlaStatusPercentageId().toString());
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_VENORDERSTATUS_CODE, Util.isNull(list.getSeatSlaStatus().getSeatOrderStatus().getVenOrderStatus().getOrderStatusCode(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATRESULTSTATUSTRACKING_DESC, Util.isNull(list.getSeatResultStatusTracking().getResultStatusTrackingDesc(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_MIN, Util.isNull(list.getMin(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_MAX, Util.isNull(list.getMax(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_BYUSER, Util.isNull(list.getSeatSlaStatus().getByUser(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_UPDATEDATE, Util.isNull(list.getSeatSlaStatus().getUpdateDate(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATORDERSTATUS_PIC, Util.isNull(list.getSeatSlaStatus().getSeatOrderStatus().getPic(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SLA, Util.isNull(list.getSeatSlaStatus().getSla(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_DESC, Util.isNull(list.getSeatSlaStatus().getSeatStatusUom().getStatusUomDesc(),"").toString());	
				map.put(DataNameTokens.SEATSLASTATUSPERCENTAGE_SEATSLASTATUS_SEATSTATUSUOM_ID, Util.isNull(list.getSeatSlaStatus().getSeatStatusUom().getStatusUomId(),"").toString());	
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
package com.gdn.venice.server.app.seattle.presenter.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.djarum.raf.utilities.JPQLAdvancedQueryCriteria;
import com.djarum.raf.utilities.JPQLSimpleQueryCriteria;
import com.djarum.raf.utilities.Locator;
import com.gdn.venice.client.app.DataNameTokens;
import com.gdn.venice.facade.SeatStatusUomSessionEJBRemote;
import com.gdn.venice.persistence.SeatStatusUom;
import com.gdn.venice.server.command.RafDsCommand;
import com.gdn.venice.server.data.RafDsRequest;
import com.gdn.venice.server.data.RafDsResponse;
import com.gdn.venice.server.util.Util;

public class FetchUoMDataCommand implements RafDsCommand {

	RafDsRequest request;
	String userName;	
	
	public FetchUoMDataCommand(RafDsRequest request, String userName){
		this.request=request;
		this.userName = userName;
		
	}
	@Override
	public RafDsResponse execute() {
		RafDsResponse rafDsResponse = new RafDsResponse();
		List<HashMap<String, String>> dataList= new ArrayList<HashMap<String, String>>();			
		Locator<Object> locator = null;
		try{		
			List<SeatStatusUom> seatStatusUomList = null;				
			locator = new Locator<Object>();
			SeatStatusUomSessionEJBRemote sessionHome = (SeatStatusUomSessionEJBRemote) locator.lookup(SeatStatusUomSessionEJBRemote.class, "SeatStatusUomSessionEJBBean");
			JPQLAdvancedQueryCriteria criteria = request.getCriteria();			
			
			if (criteria == null) {				
				String sql ="select o from SeatStatusUom o ";
				seatStatusUomList = sessionHome.queryByRange(sql, 0, 0);					
			} else {				
				SeatStatusUom bl = new SeatStatusUom();			
				seatStatusUomList = sessionHome.findBySeatStatusUomLike(bl, criteria, 0, 0);				
			}
			for(int i=0; i<seatStatusUomList.size();i++){
				HashMap<String, String> map = new HashMap<String, String>();
				SeatStatusUom list = seatStatusUomList.get(i);
								
				map.put(DataNameTokens.SEATSTATUSUOM_ID, list.getStatusUomId().toString());
				map.put(DataNameTokens.SEATSTATUSUOM_STATUSUOMDESC, Util.isNull(list.getStatusUomDesc(),"").toString());	
				map.put(DataNameTokens.SEATSTATUSUOM_STATUSUOMFROM, Util.isNull(list.getStatusUomFrom(),"").toString());	
				map.put(DataNameTokens.SEATSTATUSUOM_STATUSUOMEND, Util.isNull(list.getStatusUomEnd(),"").toString());	
				map.put(DataNameTokens.SEATSTATUSUOM_BYUSER, Util.isNull(list.getByUser(),"").toString());	
				map.put(DataNameTokens.SEATSTATUSUOM_UPDATEDATE, Util.isNull(list.getUpdateDate(),"").toString());	
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